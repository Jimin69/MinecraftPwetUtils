package com.pwetutils.command;

import net.weavemc.loader.api.command.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
import com.pwetutils.listener.HarvesterOverlayListener;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ThreadLocalRandom;

public class HarvesterCommand extends Command {
    private static boolean harvesterActive = false;
    private static boolean interrupted = false;
    private static float anvilSoundVolume = 0.3F;
    private static Thread harvesterThread;
    private static Thread soundThread;
    private static Thread interruptedCheckThread;
    private static final AtomicBoolean isPaused = new AtomicBoolean(false);
    private static final AtomicInteger phase = new AtomicInteger(0);
    private static final AtomicInteger tickCounter = new AtomicInteger(0);
    private static final AtomicInteger waitTicks = new AtomicInteger(520);
    private static final AtomicBoolean goLeftFirst = new AtomicBoolean(true);
    private static final AtomicBoolean shouldRepeat = new AtomicBoolean(false);
    private static float lastHealth = -1;

    private static int leftMoveTicks = 5;
    private static int pauseTicks = 2;
    private static int rightMoveTicks = 5;

    public HarvesterCommand() {
        super("harvester", "h", "harvest");
    }

    public static boolean isActive() {
        return harvesterActive;
    }

    public static boolean isInterrupted() {
        return interrupted;
    }

    public static boolean isPaused() {
        return isPaused.get();
    }

    public static int getRemainingSeconds() {
        if (!harvesterActive) return 0;
        if (phase.get() != 3 && phase.get() != 4) return 0;
        int remainingTicks = (phase.get() == 4 ? 15 : waitTicks.get()) - tickCounter.get();
        return Math.max(0, (remainingTicks + 19) / 20);
    }

    public static void stopByTrigger() {
        if (!harvesterActive && !interrupted) return;
        if (harvesterActive) {
            interruptByGameChange();
        }
    }

    private static void interruptByGameChange() {
        if (interrupted) return;
        harvesterActive = false;
        interrupted = true;
        stopHarvester();
        HarvesterOverlayListener.setInterrupted(true);
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Harvester §cdisabled §7(game server changed)"));
        }
        startSoundLoop();
        startInterruptedCheck();
    }

    public static void interruptByDamage() {
        if (!harvesterActive || interrupted) return;
        interrupted = true;
        harvesterActive = false;
        stopHarvester();
        HarvesterOverlayListener.setInterrupted(true);
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Harvester §cdisabled §7(interrupted by foreign activity)"));
        }
        startSoundLoop();
        startInterruptedCheck();
    }

    private static void startInterruptedCheck() {
        if (interruptedCheckThread != null) {
            interruptedCheckThread.interrupt();
        }
        interruptedCheckThread = new Thread(() -> {
            while (interrupted) {
                Minecraft mc = Minecraft.getMinecraft();
                if (mc.theWorld == null || mc.thePlayer == null) {
                    interrupted = false;
                    harvesterActive = false;
                    HarvesterOverlayListener.setInterrupted(false);
                    HarvesterOverlayListener.triggerShutdown();
                    stopSoundLoop();
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        interruptedCheckThread.start();
    }

    public static void stopOnDisconnect() {
        interrupted = false;
        harvesterActive = false;
        HarvesterOverlayListener.setInterrupted(false);
        HarvesterOverlayListener.triggerShutdown();
        stopSoundLoop();
        stopHarvester();
        if (interruptedCheckThread != null) {
            interruptedCheckThread.interrupt();
        }
    }

    private static void playPreviewSound() {
        new Thread(() -> {
            for (int i = 0; i < 2; i++) {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    Minecraft mc = Minecraft.getMinecraft();
                    if (mc.thePlayer != null) {
                        mc.thePlayer.playSound("random.anvil_land", anvilSoundVolume, 2.0F);
                    }
                });
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }).start();
    }

    private static void startSoundLoop() {
        stopSoundLoop();
        soundThread = new Thread(() -> {
            while (interrupted) {
                for (int i = 0; i < 2; i++) {
                    if (!interrupted) break;
                    Minecraft.getMinecraft().addScheduledTask(() -> {
                        Minecraft mc = Minecraft.getMinecraft();
                        if (mc.thePlayer != null && interrupted) {
                            mc.thePlayer.playSound("random.anvil_land", anvilSoundVolume, 2.0F);
                        }
                    });
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                try {
                    Thread.sleep(3600);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        soundThread.start();
    }

    private static void stopSoundLoop() {
        if (soundThread != null) {
            soundThread.interrupt();
            soundThread = null;
        }
    }

    @Override
    public void handle(String[] args) {
        Minecraft mc = Minecraft.getMinecraft();

        if (args.length == 0) {
            mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Usage: /harvester <on|off|sound>"));
            return;
        }

        if (args[0].equalsIgnoreCase("sound")) {
            if (args.length == 1) {
                mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Current anvil sound volume: §e" + anvilSoundVolume));
                playPreviewSound();
            } else if (args.length == 2) {
                try {
                    float newVolume = Float.parseFloat(args[1]);
                    if (newVolume < 0 || newVolume > 1) {
                        mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] §cVolume must be between 0.0 and 1.0"));
                        return;
                    }
                    anvilSoundVolume = newVolume;
                    mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Anvil sound volume set to: §e" + anvilSoundVolume));
                    playPreviewSound();
                } catch (NumberFormatException e) {
                    mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] §cInvalid number format"));
                }
            } else {
                mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] §7Unknown argument. Use §e/harvester <on|off|sound>"));
            }
            return;
        }

        if (args[0].equalsIgnoreCase("on")) {
            if (args.length > 1) {
                mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] §7Unknown argument. Use §e/harvester <on|off|sound>"));
                return;
            }
            if (!harvesterActive || interrupted) {
                harvesterActive = true;
                interrupted = false;
                HarvesterOverlayListener.setInterrupted(false);
                stopSoundLoop();
                if (interruptedCheckThread != null) {
                    interruptedCheckThread.interrupt();
                }
                startHarvester();
                mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Harvester §aenabled"));
            }
        } else if (args[0].equalsIgnoreCase("off")) {
            if (args.length > 1) {
                mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] §7Unknown argument. Use §e/harvester <on|off|sound>"));
                return;
            }
            if (harvesterActive || interrupted) {
                harvesterActive = false;
                interrupted = false;
                HarvesterOverlayListener.setInterrupted(false);
                stopSoundLoop();
                if (interruptedCheckThread != null) {
                    interruptedCheckThread.interrupt();
                }
                stopHarvester();
                HarvesterOverlayListener.triggerShutdown();
                mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Harvester §cdisabled"));
            }
        } else {
            mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] §7Unknown argument. Use §e/harvester <on|off|sound>"));
        }
    }

    private boolean shouldPause(Minecraft mc) {
        if (mc.currentScreen == null) return false;
        String className = mc.currentScreen.getClass().getName();
        if (className.contains("com.moonsworth.lunar")) {
            return false;
        }
        return true;
    }

    private void startHarvester() {
        phase.set(0);
        tickCounter.set(0);
        isPaused.set(false);
        waitTicks.set(ThreadLocalRandom.current().nextInt(520, 581));
        goLeftFirst.set(ThreadLocalRandom.current().nextBoolean());
        shouldRepeat.set(false);
        lastHealth = -1;

        harvesterThread = new Thread(() -> {
            long lastTickTime = System.nanoTime();
            final long NANOS_PER_TICK = 50_000_000L;

            while (harvesterActive) {
                long currentTime = System.nanoTime();

                if (currentTime - lastTickTime >= NANOS_PER_TICK) {
                    lastTickTime = currentTime;

                    Minecraft.getMinecraft().addScheduledTask(() -> {
                        if (!harvesterActive) return;
                        Minecraft mc = Minecraft.getMinecraft();

                        if (mc.theWorld == null || mc.thePlayer == null) {
                            stopOnDisconnect();
                            return;
                        }

                        float currentHealth = mc.thePlayer.getHealth();
                        if (lastHealth > 0 && currentHealth < lastHealth) {
                            interruptByDamage();
                            return;
                        }
                        lastHealth = currentHealth;

                        boolean shouldWork = !shouldPause(mc);

                        if (shouldWork && isPaused.get()) {
                            isPaused.set(false);

                            if ((phase.get() == 3 || phase.get() == 4) && tickCounter.get() >= (phase.get() == 4 ? 15 : waitTicks.get())) {
                                phase.set(0);
                                tickCounter.set(0);
                                waitTicks.set(ThreadLocalRandom.current().nextInt(520, 581));
                                goLeftFirst.set(ThreadLocalRandom.current().nextBoolean());
                                shouldRepeat.set(ThreadLocalRandom.current().nextBoolean());
                            }
                        } else if (!shouldWork && !isPaused.get()) {
                            isPaused.set(true);
                            KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
                            KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
                        }

                        if (isPaused.get() && (phase.get() == 3 || phase.get() == 4)) {
                            int maxTicks = phase.get() == 4 ? 15 : waitTicks.get();
                            if (tickCounter.get() < maxTicks) {
                                tickCounter.incrementAndGet();
                            }
                            return;
                        }

                        if (!shouldWork) return;

                        int leftKey = mc.gameSettings.keyBindLeft.getKeyCode();
                        int rightKey = mc.gameSettings.keyBindRight.getKeyCode();
                        boolean leftFirst = goLeftFirst.get();

                        tickCounter.incrementAndGet();
                        int currentPhase = phase.get();
                        int currentTick = tickCounter.get();

                        if (currentPhase == 0) {
                            KeyBinding.setKeyBindState(leftKey, leftFirst);
                            KeyBinding.setKeyBindState(rightKey, !leftFirst);
                            if (currentTick >= (leftFirst ? leftMoveTicks : rightMoveTicks)) {
                                phase.set(1);
                                tickCounter.set(0);
                                KeyBinding.setKeyBindState(leftKey, true);
                                KeyBinding.setKeyBindState(rightKey, true);
                            }
                        } else if (currentPhase == 1) {
                            KeyBinding.setKeyBindState(leftKey, true);
                            KeyBinding.setKeyBindState(rightKey, true);
                            if (currentTick >= pauseTicks) {
                                phase.set(2);
                                tickCounter.set(0);
                            }
                        } else if (currentPhase == 2) {
                            KeyBinding.setKeyBindState(leftKey, !leftFirst);
                            KeyBinding.setKeyBindState(rightKey, leftFirst);
                            if (currentTick >= (leftFirst ? rightMoveTicks : leftMoveTicks)) {
                                if (shouldRepeat.get()) {
                                    phase.set(4);
                                    shouldRepeat.set(false);
                                } else {
                                    phase.set(3);
                                }
                                tickCounter.set(0);
                                KeyBinding.setKeyBindState(leftKey, true);
                                KeyBinding.setKeyBindState(rightKey, true);
                            }
                        } else if (currentPhase == 3) {
                            KeyBinding.setKeyBindState(leftKey, true);
                            KeyBinding.setKeyBindState(rightKey, true);
                            if (currentTick >= waitTicks.get()) {
                                phase.set(0);
                                tickCounter.set(0);
                                waitTicks.set(ThreadLocalRandom.current().nextInt(520, 581));
                                goLeftFirst.set(ThreadLocalRandom.current().nextBoolean());
                                shouldRepeat.set(ThreadLocalRandom.current().nextBoolean());
                            }
                        } else if (currentPhase == 4) {
                            KeyBinding.setKeyBindState(leftKey, true);
                            KeyBinding.setKeyBindState(rightKey, true);
                            if (currentTick >= 15) {
                                phase.set(0);
                                tickCounter.set(0);
                            }
                        }
                    });
                }

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    break;
                }
            }

            Minecraft.getMinecraft().addScheduledTask(() -> {
                Minecraft mc = Minecraft.getMinecraft();
                if (mc.gameSettings != null) {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
                }
            });
        });
        harvesterThread.start();
    }

    private static void stopHarvester() {
        harvesterActive = false;
        isPaused.set(false);
        if (harvesterThread != null) {
            harvesterThread.interrupt();
        }
    }
}