package com.pwetutils.command;

import com.pwetutils.listener.HologramImageListener;
import com.pwetutils.listener.VideoHologram;
import net.weavemc.loader.api.command.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;

public class HologramCommand extends Command {
    private static HologramImageListener hologramListener;

    public HologramCommand() {
        super("hologram", "holoram", "holo", "hg");
    }

    public static void setHologramListener(HologramImageListener listener) {
        hologramListener = listener;
    }

    @Override
    public void handle(String[] args) {
        Minecraft mc = Minecraft.getMinecraft();
        boolean silent = args.length > 0 && args[args.length - 1].equalsIgnoreCase("silent");
        if (silent) {
            String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 0, newArgs, 0, args.length - 1);
            args = newArgs;
        }

        if (args.length == 0) {
            if (!silent) mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Usage: /hologram <video|image|clear|vp|vr|vpr|vsf|vsb>"));
            return;
        }

        // add this with the other shorthand commands at the beginning of handle()
        if (args[0].equalsIgnoreCase("vmc")) {  // "video move closer"
            if (hologramListener != null && hologramListener.hasVideoHologram()) {
                VideoHologram video = hologramListener.getCurrentVideoHologram();

                // get player's look direction (horizontal only)
                float yaw = mc.thePlayer.rotationYaw;
                double radYaw = Math.toRadians(yaw);

                // calculate direction vector (what player is looking at)
                double lookX = -Math.sin(radYaw);
                double lookZ = Math.cos(radYaw);

                // move hologram 0.5 blocks OPPOSITE to look direction (towards player)
                double newX = video.getX() - (lookX * 0.5);
                double newZ = video.getZ() - (lookZ * 0.5);

                video.moveTo(newX, video.getY(), newZ);

                if (!silent) {
                    mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Moved hologram closer"));
                }
            } else if (!silent) {
                mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] No video hologram to move"));
            }
            return;
        }

        if (args[0].equalsIgnoreCase("vd")) {
            if (hologramListener != null) {
                hologramListener.clearVideoHologram();
                if (!silent) mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Deleted video hologram"));
            }
            return;
        }

        if (args[0].equalsIgnoreCase("vmh") || (args[0].equalsIgnoreCase("video") && args.length >= 2 && args[1].equalsIgnoreCase("movehere"))) {
            if (hologramListener != null && hologramListener.hasVideoHologram()) {
                hologramListener.moveVideoHologramToPlayer();
                if (!silent) mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Video hologram moved to your location"));
            } else if (!silent) {
                mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] No video hologram to move"));
            }
            return;
        }

        if (args[0].equalsIgnoreCase("vcyclesize")) {
            if (hologramListener != null && hologramListener.hasVideoHologram()) {
                boolean forward = args.length < 2 || !args[1].equalsIgnoreCase("back");
                hologramListener.cycleVideoSize(forward);
                if (!silent) {
                    VideoHologram video = hologramListener.getCurrentVideoHologram();
                    mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Video size changed to " + video.getSizeLevel()));
                }
            }
            return;
        }

        if (args[0].equalsIgnoreCase("vcycletrans") || args[0].equalsIgnoreCase("vct")) {
            if (hologramListener != null && hologramListener.hasVideoHologram()) {
                boolean forward = args.length < 2 || !args[1].equalsIgnoreCase("back");
                hologramListener.cycleVideoTransparency(forward);
                if (!silent) {
                    VideoHologram video = hologramListener.getCurrentVideoHologram();
                    String modeName = video.getTransparencyMode().name().toLowerCase();
                    mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Video transparency changed to " + modeName));
                }
            }
            return;
        }

        if (args[0].equalsIgnoreCase("vsf")) {
            if (hologramListener != null && hologramListener.hasVideoHologram()) {
                int skipSeconds = 5;
                if (args.length >= 2) {
                    try {
                        skipSeconds = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {}
                }
                hologramListener.skipForward(skipSeconds);
                if (!silent) mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Skipped forward " + skipSeconds + " seconds"));
            }
            return;
        }

        if (args[0].equalsIgnoreCase("vsb")) {
            if (hologramListener != null && hologramListener.hasVideoHologram()) {
                int skipSeconds = 5;
                if (args.length >= 2) {
                    try {
                        skipSeconds = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {}
                }
                hologramListener.skipBackward(skipSeconds);
                if (!silent) mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Skipped backward " + skipSeconds + " seconds"));
            }
            return;
        }

        if (args[0].equalsIgnoreCase("vpr")) {
            if (hologramListener != null && hologramListener.hasVideoHologram()) {
                float progress = hologramListener.getVideoProgress();
                float duration = hologramListener.getVideoDuration();
                float currentTime = duration * progress;

                ChatComponentText message = new ChatComponentText("§7[§6PwetUtils§7] §7[");

                for (int i = 0; i < 70; i++) {
                    float barProgress = i / 69.0f;
                    float videoTime = duration * barProgress;
                    String timeStr = formatTime(videoTime, duration);

                    ChatComponentText bar = new ChatComponentText(i <= (int)(progress * 69) ? "§c|" : "§f|");
                    bar.setChatStyle(new ChatStyle()
                            .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§f" + timeStr)))
                            .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hologram vseek " + videoTime)));
                    message.appendSibling(bar);
                }

                message.appendSibling(new ChatComponentText("§7] §7" + formatTime(currentTime, duration) + "§f/§7" + formatTime(duration, duration)));
                mc.thePlayer.addChatMessage(message);
            } else {
                mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] No video hologram playing"));
            }
            return;
        }

        if (args[0].equalsIgnoreCase("vseek")) {
            if (args.length < 2) return;
            if (hologramListener != null && hologramListener.hasVideoHologram()) {
                try {
                    float seconds = Float.parseFloat(args[1]);
                    hologramListener.seekVideo(seconds);

                    float progress = hologramListener.getVideoProgress();
                    float duration = hologramListener.getVideoDuration();
                    float currentTime = duration * progress;

                    ChatComponentText message = new ChatComponentText("§7[§6PwetUtils§7] §7[");

                    for (int i = 0; i < 70; i++) {
                        float barProgress = i / 69.0f;
                        float videoTime = duration * barProgress;
                        String timeStr = formatTime(videoTime, duration);

                        ChatComponentText bar = new ChatComponentText(i <= (int)(progress * 69) ? "§c|" : "§f|");
                        bar.setChatStyle(new ChatStyle()
                                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§f" + timeStr)))
                                .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hologram vseek " + videoTime)));
                        message.appendSibling(bar);
                    }

                    message.appendSibling(new ChatComponentText("§7] §7" + formatTime(currentTime, duration) + "§f/§7" + formatTime(duration, duration)));
                    mc.thePlayer.addChatMessage(message);
                } catch (NumberFormatException e) {
                    // Silent fail
                }
            }
            return;
        }

        if (args[0].equalsIgnoreCase("vp") || args[0].equalsIgnoreCase("video") && args.length >= 2 && args[1].equalsIgnoreCase("pause")) {
            if (hologramListener != null && hologramListener.hasVideoHologram()) {
                boolean paused = hologramListener.togglePauseVideo();
                if (!silent) mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Video " + (paused ? "paused" : "resumed")));
            } else if (!silent) {
                mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] No video hologram to pause"));
            }
            return;
        }

        if (args[0].equalsIgnoreCase("vr") || args[0].equalsIgnoreCase("video") && args.length >= 2 && args[1].equalsIgnoreCase("restart")) {
            if (hologramListener != null && hologramListener.hasVideoHologram()) {
                hologramListener.restartVideo();
                if (!silent) mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Video restarted"));
            } else if (!silent) {
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

                if (args[1].equalsIgnoreCase("progress")) {
                    if (hologramListener != null && hologramListener.hasVideoHologram()) {
                        float progress = hologramListener.getVideoProgress();
                        float duration = hologramListener.getVideoDuration();
                        float currentTime = duration * progress;

                        ChatComponentText message = new ChatComponentText("§7[§6PwetUtils§7] §7[");

                        for (int i = 0; i < 70; i++) {
                            float barProgress = i / 69.0f;
                            float videoTime = duration * barProgress;
                            String timeStr = formatTime(videoTime, duration);

                            ChatComponentText bar = new ChatComponentText(i <= (int)(progress * 69) ? "§c|" : "§f|");
                            bar.setChatStyle(new ChatStyle()
                                    .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§f" + timeStr)))
                                    .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hologram vseek " + videoTime)));
                            message.appendSibling(bar);
                        }

                        message.appendSibling(new ChatComponentText("§7] §7" + formatTime(currentTime, duration) + "§f/§7" + formatTime(duration, duration)));
                        mc.thePlayer.addChatMessage(message);
                    } else {
                        mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] No video hologram playing"));
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
            VideoHologram.TransparencyMode transparencyMode = VideoHologram.TransparencyMode.SOLID;

            if (args.length >= 2) {
                try {
                    size = Integer.parseInt(args[1]);
                    if (size < 2 || size > 6) {
                        if (!silent) mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Size must be between 2 and 6"));
                        return;
                    }
                } catch (NumberFormatException e) {}
            }

            if (args.length >= 3) {
                String mode = args[2].toLowerCase();
                if (mode.equals("transparent")) {
                    transparencyMode = VideoHologram.TransparencyMode.TRANSPARENT;
                } else if (mode.equals("idle")) {
                    transparencyMode = VideoHologram.TransparencyMode.IDLE;
                }
            }

            if (hologramListener != null && mc.thePlayer != null) {
                double x = mc.thePlayer.posX;
                double yOffset = size >= 6 ? 1.5 : size >= 5 ? 1.0 : size >= 4 ? 0.5 : 0;
                double y = mc.thePlayer.posY + 2.0 + yOffset;
                double z = mc.thePlayer.posZ;
                hologramListener.loadVideo(x, y, z, size, transparencyMode);
                if (!silent) {
                    String transparencyText = transparencyMode.name().toLowerCase();
                    mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Creating " + transparencyText + " video hologram (size " + size + ")..."));
                }
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

    private String formatTime(float seconds, float totalSeconds) {
        int totalSec = (int)seconds;
        int hours = totalSec / 3600;
        int minutes = (totalSec % 3600) / 60;
        int secs = totalSec % 60;

        if (totalSeconds >= 3600) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        } else if (totalSeconds >= 600) {
            return String.format("%02d:%02d", minutes, secs);
        } else {
            return String.format("%d:%02d", minutes, secs);
        }
    }
}