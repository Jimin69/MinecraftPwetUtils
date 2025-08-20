package com.pwetutils.listener;

import com.pwetutils.command.HarvesterCommand;
import com.pwetutils.settings.ModuleSettings;
import net.weavemc.loader.api.event.RenderGameOverlayEvent;
import net.weavemc.loader.api.event.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

public class HarvesterOverlayListener {
    private static final String[] COGWHEEL_COLORS = {"§4§l⚙", "§c§l⚙", "§f§l⚙", "§c§l⚙"};
    private static final String[] WARNING_COLORS = {"§6 ⚠", "§e ⚠", "§f ⚠", "§e ⚠"};
    private static int colorIndex = 0;
    private static int warningColorIndex = 0;
    private static long lastColorChange = 0;
    private static long lastWarningChange = 0;
    private static long lastFlash = 0;
    private static boolean flashWhite = true;
    private static long shutdownTime = 0;
    private static boolean interrupted = false;

    public static void setInterrupted(boolean value) {
        interrupted = value;
        if (!value) {
            warningColorIndex = 0;
        }
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Pre event) {
        if (!GameStateTracker.shouldShowOverlays()) return;

        // Check if we should show shutdown state
        boolean showShutdown = false;
        if (shutdownTime > 0) {
            if (System.currentTimeMillis() - shutdownTime < 3000) {
                showShutdown = true;
            } else {
                shutdownTime = 0;
                return;
            }
        }

        if (!HarvesterCommand.isActive() && !showShutdown && !interrupted) return;

        Minecraft mc = Minecraft.getMinecraft();

        // Calculate resource timer box width to position harvester box
        String text1 = "§8III §bDiamonds §7spawn in §e?§7s";
        String text2 = "§8III §2Emeralds §7spawn in §e?§7s";
        int resourceWidth = Math.max(mc.fontRendererObj.getStringWidth(text1), mc.fontRendererObj.getStringWidth(text2));

        int padding = 2;
        int baseX = 4 + resourceWidth + padding + 2 + 2;
        int baseY = 2 + 2;

        // Cogwheel indicator box
        int boxSize = mc.fontRendererObj.FONT_HEIGHT * 2 + 2;

        String cogwheelText;
        String timerText;
        String warningText;

        long currentTime = System.currentTimeMillis();

        if (showShutdown) {
            // Shutdown state - all dark gray
            cogwheelText = "§8§l⚙";
            timerText = "§8 30s";
            warningText = "§8 ⚠";
        } else if (interrupted) {
            // Interrupted state - solid red cogwheel
            cogwheelText = "§c§l⚙";
            timerText = "§8 0s";

            // Flash warning colors
            if (currentTime - lastWarningChange >= 250) {
                warningColorIndex = (warningColorIndex + 1) % WARNING_COLORS.length;
                lastWarningChange = currentTime;
            }
            warningText = WARNING_COLORS[warningColorIndex];
        } else {
            // Normal operation
            if (currentTime - lastColorChange >= 250) {
                colorIndex = (colorIndex + 1) % COGWHEEL_COLORS.length;
                lastColorChange = currentTime;
            }
            cogwheelText = COGWHEEL_COLORS[colorIndex];

            int remainingSeconds = HarvesterCommand.getRemainingSeconds();
            boolean isPaused = HarvesterCommand.isPaused();

            String timerColor = "§7";
            if (remainingSeconds == 0) {
                if (isPaused) {
                    if (currentTime - lastFlash >= 250) {
                        flashWhite = !flashWhite;
                        lastFlash = currentTime;
                    }
                    timerColor = flashWhite ? "§f" : "§7";
                } else {
                    timerColor = "§f";
                }
            }

            timerText = timerColor + " " + remainingSeconds + "s";
            warningText = isPaused ? "§9§l ┃┃" : "§a ✔";
        }

        // Draw cogwheel box
        Gui.drawRect(baseX - padding, baseY - padding,
                baseX + boxSize + padding, baseY + boxSize + padding,
                0x80000000);

        // Draw scaled cogwheel
        GlStateManager.pushMatrix();
        GlStateManager.translate(baseX + boxSize/2, baseY + boxSize/2, 0);
        GlStateManager.scale(2.0F, 2.0F, 1.0F);
        mc.fontRendererObj.drawStringWithShadow(cogwheelText,
                -mc.fontRendererObj.getStringWidth(cogwheelText)/2,
                -mc.fontRendererObj.FONT_HEIGHT/2,
                0xFFFFFF);
        GlStateManager.popMatrix();

        // Combined box for timer and warning
        int textX = baseX + boxSize + padding;
        int textY = baseY;
        int textHeight = mc.fontRendererObj.FONT_HEIGHT;

        int boxWidth = Math.max(mc.fontRendererObj.getStringWidth(timerText),
                mc.fontRendererObj.getStringWidth(warningText));
        int totalHeight = textHeight * 2 + 2;

        Gui.drawRect(textX - padding, textY - padding,
                textX + boxWidth + padding, textY + totalHeight + padding,
                0x80000000);

        mc.fontRendererObj.drawStringWithShadow(timerText, textX, textY, 0xFFFFFF);
        mc.fontRendererObj.drawStringWithShadow(warningText, textX, textY + textHeight + 2, 0xFFFFFF);
    }

    public static void triggerShutdown() {
        shutdownTime = System.currentTimeMillis();
    }
}