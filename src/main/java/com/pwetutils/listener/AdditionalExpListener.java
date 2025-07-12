package com.pwetutils.listener;

import com.pwetutils.settings.ModuleSettings;
import net.weavemc.loader.api.event.RenderGameOverlayEvent;
import net.weavemc.loader.api.event.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;

import java.util.ArrayList;
import java.util.List;

public class AdditionalExpListener {
    private static int totalXP = 0;
    private static int timePlayedXP = 0;
    private static int resourcesXP = 0;
    private static int killsXP = 0;
    private static int bedsXP = 0;
    private static int bonusXP = 0;
    private static boolean hasPlayedGame = false;

    public static void addXP(int amount, String source) {
        totalXP += amount;

        if (source.contains("Time Played")) {
            timePlayedXP += amount;
        } else if (source.contains("Diamonds") || source.contains("Emeralds")) {
            resourcesXP += amount;
        } else if (source.contains("First Kill") || source.contains("Final Kill")) {
            killsXP += amount;
        } else if (source.contains("Bed Break")) {
            bedsXP += amount;
        } else if (source.contains("Position Bonus")) {
            bonusXP += amount;
        }
    }

    public static void startGame() {
        hasPlayedGame = true;
        totalXP = 0;
        timePlayedXP = 0;
        resourcesXP = 0;
        killsXP = 0;
        bedsXP = 0;
        bonusXP = 0;
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Pre event) {
        if (!ModuleSettings.isExperienceCounterEnabled() || !GameStateTracker.shouldShowOverlays()) return;
        Minecraft mc = Minecraft.getMinecraft();
        boolean chatOpen = mc.currentScreen instanceof GuiChat;
        List<String> lines = new ArrayList<>();

        String xpValue = hasPlayedGame ? String.valueOf(totalXP) : "?";
        lines.add("§7Total §eBedWars §7XP: §b" + xpValue);

        if (!hasPlayedGame || timePlayedXP > 0 || chatOpen) {
            String timeValue = hasPlayedGame ? String.valueOf(timePlayedXP) : "?";
            String timeColor = (hasPlayedGame && timePlayedXP == 0) ? "§8" : "§b";
            lines.add("§7From time played: " + timeColor + timeValue);
        }

        if (!hasPlayedGame || resourcesXP > 0 || chatOpen) {
            String resourceValue = hasPlayedGame ? String.valueOf(resourcesXP) : "?";
            String resourceColor = (hasPlayedGame && resourcesXP == 0) ? "§8" : "§b";
            lines.add("§7From resources: " + resourceColor + resourceValue);
        }

        if (!hasPlayedGame || killsXP > 0 || chatOpen) {
            String killsValue = hasPlayedGame ? String.valueOf(killsXP) : "?";
            String killsColor = (hasPlayedGame && killsXP == 0) ? "§8" : "§b";
            lines.add("§7From kill(s): " + killsColor + killsValue);
        }

        if (!hasPlayedGame || bedsXP > 0 || chatOpen) {
            String bedsValue = hasPlayedGame ? String.valueOf(bedsXP) : "?";
            String bedsColor = (hasPlayedGame && bedsXP == 0) ? "§8" : "§b";
            lines.add("§7From bed(s): " + bedsColor + bedsValue);
        }

        if (!hasPlayedGame || bonusXP > 0 || chatOpen) {
            String bonusValue = hasPlayedGame ? String.valueOf(bonusXP) : "?";
            String bonusColor = (hasPlayedGame && bonusXP == 0) ? "§8" : "§b";
            lines.add("§7Bonus: " + bonusColor + bonusValue);
        }

        int padding = 2;
        int x = 4;
        int y = 30;
        int width = 0;
        for (String text : lines) {
            width = Math.max(width, mc.fontRendererObj.getStringWidth(text));
        }
        int height = mc.fontRendererObj.FONT_HEIGHT;
        int totalHeight = height * lines.size() + (lines.size() - 1) * 2;

        Gui.drawRect(x - padding, y - padding, x + width + padding, y + totalHeight + padding, 0x80000000);
        for (int i = 0; i < lines.size(); i++) {
            mc.fontRendererObj.drawStringWithShadow(lines.get(i), x, y + i * (height + 2), 0xFFFFFF);
        }
    }
}