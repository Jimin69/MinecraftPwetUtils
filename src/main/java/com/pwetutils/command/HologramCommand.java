package com.pwetutils.command;

import com.pwetutils.listener.HologramImageListener;
import net.weavemc.loader.api.command.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

public class HologramCommand extends Command {
    private static HologramImageListener hologramListener;

    public HologramCommand() {
        super("hologram", "holo", "hg");
    }

    public static void setHologramListener(HologramImageListener listener) {
        hologramListener = listener;
    }

    @Override
    public void handle(String[] args) {
        Minecraft mc = Minecraft.getMinecraft();

        if (args.length == 0) {
            mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Usage: /hologram <video|image|clear|vp|vr|vsf|vsb>"));
            return;
        }

        if (args[0].equalsIgnoreCase("vsf")) {
            if (hologramListener != null && hologramListener.hasVideoHologram()) {
                int skipSeconds = 5;
                if (args.length >= 2) {
                    try {
                        skipSeconds = Integer.parseInt(args[1]);
                        if (skipSeconds != 5 && skipSeconds != 10 && skipSeconds != 15 &&
                                skipSeconds != 20 && skipSeconds != 30 && skipSeconds != 60) {
                            mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Invalid skip duration. Use: 5, 10, 15, 20, 30, or 60"));
                            return;
                        }
                    } catch (NumberFormatException e) {
                        mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Invalid number format"));
                        return;
                    }
                }
                hologramListener.skipForward(skipSeconds);
                mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Skipped forward " + skipSeconds + " seconds"));
            } else {
                mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] No video hologram to skip"));
            }
            return;
        }

        if (args[0].equalsIgnoreCase("vsb")) {
            if (hologramListener != null && hologramListener.hasVideoHologram()) {
                int skipSeconds = 5;
                if (args.length >= 2) {
                    try {
                        skipSeconds = Integer.parseInt(args[1]);
                        if (skipSeconds != 5 && skipSeconds != 10 && skipSeconds != 15 &&
                                skipSeconds != 20 && skipSeconds != 30 && skipSeconds != 60) {
                            mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Invalid skip duration. Use: 5, 10, 15, 20, 30, or 60"));
                            return;
                        }
                    } catch (NumberFormatException e) {
                        mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Invalid number format"));
                        return;
                    }
                }
                hologramListener.skipBackward(skipSeconds);
                mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Skipped backward " + skipSeconds + " seconds"));
            } else {
                mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] No video hologram to skip"));
            }
            return;
        }

        if (args[0].equalsIgnoreCase("vp")) {
            if (hologramListener != null && hologramListener.hasVideoHologram()) {
                boolean paused = hologramListener.togglePauseVideo();
                mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Video " + (paused ? "paused" : "resumed")));
            } else {
                mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] No video hologram to pause"));
            }
            return;
        }

        if (args[0].equalsIgnoreCase("vr")) {
            if (hologramListener != null && hologramListener.hasVideoHologram()) {
                hologramListener.restartVideo();
                mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Video restarted"));
            } else {
                mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] No video hologram to restart"));
            }
            return;
        }

        if (args[0].equalsIgnoreCase("image")) {
            if (args.length < 2) {
                mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Usage: /hologram image <url>"));
                return;
            }

            String url = args[1];
            if (hologramListener != null && mc.thePlayer != null) {
                double x = mc.thePlayer.posX;
                double y = mc.thePlayer.posY + 1.5;
                double z = mc.thePlayer.posZ;
                hologramListener.loadImage(url, x, y, z);
                mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Creating image hologram..."));
            }
            return;
        }

        if (args[0].equalsIgnoreCase("video")) {
            if (args.length >= 2) {
                if (args[1].equalsIgnoreCase("help")) {
                    mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Usage: §e/hologram video §7<size> <transparency:true/false>"));
                    mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Or: §e/hologram video §7<pause|restart|delete>"));
                    mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Or: §e/hologram video skipf/skipb §7[5|10|15|20|30|60]"));
                    return;
                }

                if (args[1].equalsIgnoreCase("delete")) {
                    if (hologramListener != null) {
                        hologramListener.clearVideoHologram();
                        mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Deleted video hologram"));
                    }
                    return;
                }

                if (args[1].equalsIgnoreCase("pause")) {
                    if (hologramListener != null && hologramListener.hasVideoHologram()) {
                        boolean paused = hologramListener.togglePauseVideo();
                        mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Video " + (paused ? "paused" : "resumed")));
                    } else {
                        mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] No video hologram to pause"));
                    }
                    return;
                }

                if (args[1].equalsIgnoreCase("restart")) {
                    if (hologramListener != null && hologramListener.hasVideoHologram()) {
                        hologramListener.restartVideo();
                        mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Video restarted"));
                    } else {
                        mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] No video hologram to restart"));
                    }
                    return;
                }

                if (args[1].equalsIgnoreCase("skipf")) {
                    if (hologramListener != null && hologramListener.hasVideoHologram()) {
                        int skipSeconds = 5;
                        if (args.length >= 3) {
                            try {
                                skipSeconds = Integer.parseInt(args[2]);
                                if (skipSeconds != 5 && skipSeconds != 10 && skipSeconds != 15 &&
                                        skipSeconds != 20 && skipSeconds != 30 && skipSeconds != 60) {
                                    mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Invalid skip duration. Use: 5, 10, 15, 20, 30, or 60"));
                                    return;
                                }
                            } catch (NumberFormatException e) {
                                mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Invalid number format"));
                                return;
                            }
                        }
                        hologramListener.skipForward(skipSeconds);
                        mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Skipped forward " + skipSeconds + " seconds"));
                    } else {
                        mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] No video hologram to skip"));
                    }
                    return;
                }

                if (args[1].equalsIgnoreCase("skipb")) {
                    if (hologramListener != null && hologramListener.hasVideoHologram()) {
                        int skipSeconds = 5;
                        if (args.length >= 3) {
                            try {
                                skipSeconds = Integer.parseInt(args[2]);
                                if (skipSeconds != 5 && skipSeconds != 10 && skipSeconds != 15 &&
                                        skipSeconds != 20 && skipSeconds != 30 && skipSeconds != 60) {
                                    mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Invalid skip duration. Use: 5, 10, 15, 20, 30, or 60"));
                                    return;
                                }
                            } catch (NumberFormatException e) {
                                mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Invalid number format"));
                                return;
                            }
                        }
                        hologramListener.skipBackward(skipSeconds);
                        mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Skipped backward " + skipSeconds + " seconds"));
                    } else {
                        mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] No video hologram to skip"));
                    }
                    return;
                }
            }

            int size = 4;
            boolean transparent = false;

            if (args.length >= 2) {
                try {
                    size = Integer.parseInt(args[1]);
                    if (size < 2 || size > 6) {
                        mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Size must be between 2 and 6"));
                        return;
                    }
                } catch (NumberFormatException e) {
                    mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Invalid size. Usage: /hologram video [2-6] [true/false]"));
                    return;
                }
            }

            if (args.length >= 3) {
                if (args[2].equalsIgnoreCase("true")) {
                    transparent = true;
                } else if (!args[2].equalsIgnoreCase("false")) {
                    mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Transparency must be true or false"));
                    return;
                }
            }

            if (hologramListener != null && mc.thePlayer != null) {
                double x = mc.thePlayer.posX;
                double yOffset = size >= 6 ? 1.5 : size >= 5 ? 1.0 : size >= 4 ? 0.5 : 0;
                double y = mc.thePlayer.posY + 2.0 + yOffset;
                double z = mc.thePlayer.posZ;
                hologramListener.loadVideo(x, y, z, size, transparent);
                String transparencyText = transparent ? " transparent" : " solid";
                mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Creating" + transparencyText + " video hologram (size " + size + ")..."));
            }
            return;
        }

        if (args[0].equalsIgnoreCase("clear")) {
            if (hologramListener != null) {
                hologramListener.clearHolograms();
                mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Cleared all holograms"));
            }
            return;
        }

        mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Unknown argument. Use /hologram <video|image|clear|vp|vr|vsf|vsb>"));
    }
}