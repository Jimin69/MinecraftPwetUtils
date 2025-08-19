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
    private static int sessionTotalXP = 0;
    private static long sessionStartTime = 0;
    private static boolean sessionStarted = false;
    private static boolean hasPlayedGame = false;

    public static void addXP(int amount, String source) {
        totalXP += amount;
        sessionTotalXP += amount;

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
        if (!sessionStarted) {
            sessionStarted = true;
            sessionStartTime = System.currentTimeMillis();
            sessionTotalXP = 0;
        }
        hasPlayedGame = true;
        totalXP = 0;
        timePlayedXP = 0;
        resourcesXP = 0;
        killsXP = 0;
        bedsXP = 0;
        bonusXP = 0;
    }

    public static int getSessionTotalXP() {
        return sessionTotalXP;
    }

    public static boolean isSessionStarted() {
        return sessionStarted;
    }

    public static String getSessionDuration() {
        if (!sessionStarted) return "?§7m";
        long elapsed = System.currentTimeMillis() - sessionStartTime;
        int minutes = (int) (elapsed / 60000);
        int hours = minutes / 60;
        minutes = minutes % 60;
        if (hours > 0) {
            return hours + "§7h §e" + minutes + "§7m";
        }
        return minutes + "§7m";
    }

    public static String getStarsGained() {
        if (!sessionStarted) return "§7✫?";
        double stars = sessionTotalXP / 5000.0;
        boolean capped = false;

        if (stars > 20.99) {
            stars = 20.99;
            capped = true;
        }

        if (stars >= 20.00) {
            String formatted = String.format("%.2f", stars);
            String base = "§7✪§8" + formatted.charAt(0) + "§7" + formatted.charAt(1) + "§f" + formatted.charAt(2) + "§7" + formatted.charAt(3) + "§8" + formatted.charAt(4);
            return capped ? base + "§b+" : base;
        } else if (stars >= 19.00) {
            return String.format("§8✪§5%.2f", stars);
        } else if (stars >= 18.00) {
            return String.format("§1✪§9%.2f", stars);
        } else if (stars >= 17.00) {
            return String.format("§5✪§d%.2f", stars);
        } else if (stars >= 16.00) {
            return String.format("§4✪§c%.2f", stars);
        } else if (stars >= 15.00) {
            return String.format("§9✪§3%.2f", stars);
        } else if (stars >= 14.00) {
            return String.format("§2✪§a%.2f", stars);
        } else if (stars >= 13.00) {
            return String.format("§3✪§b%.2f", stars);
        } else if (stars >= 12.00) {
            return String.format("§6✪§e%.2f", stars);
        } else if (stars >= 11.00) {
            return String.format("§7✪§f%.2f", stars);
        } else if (stars >= 10.00) {
            String formatted = String.format("%.2f", stars);
            return "§d✫§c" + formatted.charAt(0) + "§6" + formatted.charAt(1) + "§e" + formatted.charAt(2) + "§a" + formatted.charAt(3) + "§b" + formatted.charAt(4);
        } else if (stars >= 9.00) {
            return String.format("§5✫%.2f", stars);
        } else if (stars >= 8.00) {
            return String.format("§9✫%.2f", stars);
        } else if (stars >= 7.00) {
            return String.format("§d✫%.2f", stars);
        } else if (stars >= 6.00) {
            return String.format("§4✫%.2f", stars);
        } else if (stars >= 5.00) {
            return String.format("§3✫%.2f", stars);
        } else if (stars >= 4.00) {
            return String.format("§2✫%.2f", stars);
        } else if (stars >= 3.00) {
            return String.format("§b✫%.2f", stars);
        } else if (stars >= 2.00) {
            return String.format("§2✫%.2f", stars);
        } else if (stars >= 1.00) {
            return String.format("§f✫%.2f", stars);
        } else {
            return String.format("§7✫%.2f", stars);
        }
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