package com.pwetutils.listener;

import com.pwetutils.emotes.EmoteHandler;
import com.pwetutils.settings.ModuleSettings;
import net.weavemc.loader.api.event.RenderGameOverlayEvent;
import net.weavemc.loader.api.event.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

public class SettingsOverlayListener {
    private boolean wasMouseDown = false;

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!(mc.currentScreen instanceof GuiChat)) return;
        if (!ChatOverlayListener.settingsOpen) return;

        int iconX = 4;
        int iconWidth = 11;
        int moduleWidth = 139;
        int statusWidth = 30;
        int height = mc.fontRendererObj.FONT_HEIGHT;
        int padding = 2;

        int moduleX = iconX + iconWidth + padding + 3;
        int statusX = moduleX + moduleWidth + padding * 2 + 1;

        ScaledResolution sr = new ScaledResolution(mc);
        int baseY = sr.getScaledHeight() - 42;

        int mouseX = Mouse.getX() * sr.getScaledWidth() / mc.displayWidth;
        int mouseY = sr.getScaledHeight() - Mouse.getY() * sr.getScaledHeight() / mc.displayHeight - 1;
        boolean mouseDown = Mouse.isButtonDown(0);

        String module1 = "bedwarsResourceTimer";
        String module2 = "bedwarsChatWarnings";
        String module3 = "bedwarsExperienceCounter";
        String module4 = "hypixelEmoteConverter";
        String module5 = "nameMentionIndicator";
        String module6 = "increaseChatLength";
        String module7 = "languageInputSwitch";

        String icon1 = "§2§l✦";
        String icon2 = "§b§l!";
        String icon3 = "§e✫";
        String icon4 = "§c§l❤";
        String icon5 = "§b@";
        String icon6 = "§f#";
        String icon7 = "§6A";

        String status1 = ModuleSettings.isResourceTimerEnabled() ? "TRUE" : "FALSE";
        int status1Color = ModuleSettings.isResourceTimerEnabled() ? 0x55FF55 : 0xFF5555;
        String status2 = ModuleSettings.isChatWarningsEnabled() ? "TRUE" : "FALSE";
        int status2Color = ModuleSettings.isChatWarningsEnabled() ? 0x55FF55 : 0xFF5555;
        String status3 = ModuleSettings.isExperienceCounterEnabled() ? "TRUE" : "FALSE";
        int status3Color = ModuleSettings.isExperienceCounterEnabled() ? 0x55FF55 : 0xFF5555;
        String status4 = ModuleSettings.isEmoteConverterEnabled() ? "TRUE" : "FALSE";
        int status4Color = ModuleSettings.isEmoteConverterEnabled() ? 0x55FF55 : 0xFF5555;
        String status5 = ModuleSettings.isNameMentionEnabled() ? "TRUE" : "FALSE";
        int status5Color = ModuleSettings.isNameMentionEnabled() ? 0x55FF55 : 0xFF5555;
        String status6 = ModuleSettings.isIncreaseChatLengthEnabled() ? "TRUE" : "FALSE";
        int status6Color = ModuleSettings.isIncreaseChatLengthEnabled() ? 0x55FF55 : 0xFF5555;
        String status7 = ModuleSettings.isLanguageInputEnabled() ? "TRUE" : "FALSE";
        int status7Color = ModuleSettings.isLanguageInputEnabled() ? 0x55FF55 : 0xFF5555;

        int y1 = baseY;
        boolean hover1 = mouseX >= iconX - padding && mouseX <= statusX + statusWidth + padding &&
                mouseY >= y1 - padding && mouseY <= y1 + height + padding;
        int bg1 = hover1 ? 0x40FFFFFF : 0x80000000;
        Gui.drawRect(iconX - padding, y1 - padding, iconX + iconWidth + padding, y1 + height + padding, bg1);
        int iconTextWidth = mc.fontRendererObj.getStringWidth(icon7);
        int centeredIconX = iconX + (iconWidth - iconTextWidth) / 2 + 1;
        mc.fontRendererObj.drawStringWithShadow(icon7, centeredIconX, y1, 0xFFFFFF);
        Gui.drawRect(moduleX - padding, y1 - padding, moduleX + moduleWidth + padding, y1 + height + padding, bg1);
        mc.fontRendererObj.drawStringWithShadow(module7, moduleX, y1, 0xAAAAAA);
        Gui.drawRect(statusX - padding, y1 - padding, statusX + statusWidth + padding, y1 + height + padding, bg1);
        int statusTextWidth = mc.fontRendererObj.getStringWidth(status7);
        int centeredStatusX = statusX + (statusWidth - statusTextWidth) / 2;
        mc.fontRendererObj.drawStringWithShadow(status7, centeredStatusX, y1, status7Color);
        if (hover1 && mouseDown && !wasMouseDown) {
            ModuleSettings.setLanguageInputEnabled(!ModuleSettings.isLanguageInputEnabled());
        }

        int y2 = y1 - height - padding * 2 - 1;
        boolean hover2 = mouseX >= iconX - padding && mouseX <= statusX + statusWidth + padding &&
                mouseY >= y2 - padding && mouseY <= y2 + height + padding;
        int bg2 = hover2 ? 0x40FFFFFF : 0x80000000;
        Gui.drawRect(iconX - padding, y2 - padding, iconX + iconWidth + padding, y2 + height + padding, bg2);
        iconTextWidth = mc.fontRendererObj.getStringWidth(icon6);
        centeredIconX = iconX + (iconWidth - iconTextWidth) / 2 + 1;
        mc.fontRendererObj.drawStringWithShadow(icon6, centeredIconX, y2, 0xFFFFFF);
        Gui.drawRect(moduleX - padding, y2 - padding, moduleX + moduleWidth + padding, y2 + height + padding, bg2);
        mc.fontRendererObj.drawStringWithShadow(module6, moduleX, y2, 0xAAAAAA);
        Gui.drawRect(statusX - padding, y2 - padding, statusX + statusWidth + padding, y2 + height + padding, bg2);
        statusTextWidth = mc.fontRendererObj.getStringWidth(status6);
        centeredStatusX = statusX + (statusWidth - statusTextWidth) / 2;
        mc.fontRendererObj.drawStringWithShadow(status6, centeredStatusX, y2, status6Color);
        if (hover2 && mouseDown && !wasMouseDown) {
            ModuleSettings.setIncreaseChatLengthEnabled(!ModuleSettings.isIncreaseChatLengthEnabled());
        }

        int y3 = y2 - height - padding * 2 - 1;
        boolean hover3 = mouseX >= iconX - padding && mouseX <= statusX + statusWidth + padding &&
                mouseY >= y3 - padding && mouseY <= y3 + height + padding;
        int bg3 = hover3 ? 0x40FFFFFF : 0x80000000;
        Gui.drawRect(iconX - padding, y3 - padding, iconX + iconWidth + padding, y3 + height + padding, bg3);
        iconTextWidth = mc.fontRendererObj.getStringWidth(icon5);
        centeredIconX = iconX + (iconWidth - iconTextWidth) / 2 + 1;
        mc.fontRendererObj.drawStringWithShadow(icon5, centeredIconX, y3, 0xFFFFFF);
        Gui.drawRect(moduleX - padding, y3 - padding, moduleX + moduleWidth + padding, y3 + height + padding, bg3);
        mc.fontRendererObj.drawStringWithShadow(module5, moduleX, y3, 0xAAAAAA);
        Gui.drawRect(statusX - padding, y3 - padding, statusX + statusWidth + padding, y3 + height + padding, bg3);
        statusTextWidth = mc.fontRendererObj.getStringWidth(status5);
        centeredStatusX = statusX + (statusWidth - statusTextWidth) / 2;
        mc.fontRendererObj.drawStringWithShadow(status5, centeredStatusX, y3, status5Color);
        if (hover3 && mouseDown && !wasMouseDown) {
            ModuleSettings.setNameMentionEnabled(!ModuleSettings.isNameMentionEnabled());
        }

        int y4 = y3 - height - padding * 2 - 1;
        boolean hover4 = mouseX >= iconX - padding && mouseX <= statusX + statusWidth + padding &&
                mouseY >= y4 - padding && mouseY <= y4 + height + padding;
        int bg4 = hover4 ? 0x40FFFFFF : 0x80000000;
        Gui.drawRect(iconX - padding, y4 - padding, iconX + iconWidth + padding, y4 + height + padding, bg4);
        iconTextWidth = mc.fontRendererObj.getStringWidth(icon4);
        centeredIconX = iconX + (iconWidth - iconTextWidth) / 2 + 1;
        mc.fontRendererObj.drawStringWithShadow(icon4, centeredIconX, y4, 0xFFFFFF);
        Gui.drawRect(moduleX - padding, y4 - padding, moduleX + moduleWidth + padding, y4 + height + padding, bg4);
        mc.fontRendererObj.drawStringWithShadow(module4, moduleX, y4, 0xAAAAAA);
        Gui.drawRect(statusX - padding, y4 - padding, statusX + statusWidth + padding, y4 + height + padding, bg4);
        statusTextWidth = mc.fontRendererObj.getStringWidth(status4);
        centeredStatusX = statusX + (statusWidth - statusTextWidth) / 2;
        mc.fontRendererObj.drawStringWithShadow(status4, centeredStatusX, y4, status4Color);
        if (hover4 && mouseDown && !wasMouseDown) {
            ModuleSettings.setEmoteConverterEnabled(!ModuleSettings.isEmoteConverterEnabled());
        }

        int y5 = y4 - height - padding * 2 - 1;
        boolean hover5 = mouseX >= iconX - padding && mouseX <= statusX + statusWidth + padding &&
                mouseY >= y5 - padding && mouseY <= y5 + height + padding;
        int bg5 = hover5 ? 0x40FFFFFF : 0x80000000;
        Gui.drawRect(iconX - padding, y5 - padding, iconX + iconWidth + padding, y5 + height + padding, bg5);
        iconTextWidth = mc.fontRendererObj.getStringWidth(icon2);
        centeredIconX = iconX + (iconWidth - iconTextWidth) / 2 + 1;
        mc.fontRendererObj.drawStringWithShadow(icon2, centeredIconX, y5, 0xFFFFFF);
        Gui.drawRect(moduleX - padding, y5 - padding, moduleX + moduleWidth + padding, y5 + height + padding, bg5);
        mc.fontRendererObj.drawStringWithShadow(module2, moduleX, y5, 0xAAAAAA);
        Gui.drawRect(statusX - padding, y5 - padding, statusX + statusWidth + padding, y5 + height + padding, bg5);
        statusTextWidth = mc.fontRendererObj.getStringWidth(status2);
        centeredStatusX = statusX + (statusWidth - statusTextWidth) / 2;
        mc.fontRendererObj.drawStringWithShadow(status2, centeredStatusX, y5, status2Color);
        if (hover5 && mouseDown && !wasMouseDown) {
            ModuleSettings.setChatWarningsEnabled(!ModuleSettings.isChatWarningsEnabled());
        }

        int y6 = y5 - height - padding * 2 - 1;
        boolean hover6 = mouseX >= iconX - padding && mouseX <= statusX + statusWidth + padding &&
                mouseY >= y6 - padding && mouseY <= y6 + height + padding;
        int bg6 = hover6 ? 0x40FFFFFF : 0x80000000;
        Gui.drawRect(iconX - padding, y6 - padding, iconX + iconWidth + padding, y6 + height + padding, bg6);
        iconTextWidth = mc.fontRendererObj.getStringWidth(icon3);
        centeredIconX = iconX + (iconWidth - iconTextWidth) / 2 + 1;
        mc.fontRendererObj.drawStringWithShadow(icon3, centeredIconX, y6, 0xFFFFFF);
        Gui.drawRect(moduleX - padding, y6 - padding, moduleX + moduleWidth + padding, y6 + height + padding, bg6);
        mc.fontRendererObj.drawStringWithShadow(module3, moduleX, y6, 0xAAAAAA);
        Gui.drawRect(statusX - padding, y6 - padding, statusX + statusWidth + padding, y6 + height + padding, bg6);
        statusTextWidth = mc.fontRendererObj.getStringWidth(status3);
        centeredStatusX = statusX + (statusWidth - statusTextWidth) / 2;
        mc.fontRendererObj.drawStringWithShadow(status3, centeredStatusX, y6, status3Color);
        if (hover6 && mouseDown && !wasMouseDown) {
            ModuleSettings.setExperienceCounterEnabled(!ModuleSettings.isExperienceCounterEnabled());
        }

        int y7 = y6 - height - padding * 2 - 1;
        boolean hover7 = mouseX >= iconX - padding && mouseX <= statusX + statusWidth + padding &&
                mouseY >= y7 - padding && mouseY <= y7 + height + padding;
        int bg7 = hover7 ? 0x40FFFFFF : 0x80000000;
        Gui.drawRect(iconX - padding, y7 - padding, iconX + iconWidth + padding, y7 + height + padding, bg7);
        iconTextWidth = mc.fontRendererObj.getStringWidth(icon1);
        centeredIconX = iconX + (iconWidth - iconTextWidth) / 2 + 1;
        mc.fontRendererObj.drawStringWithShadow(icon1, centeredIconX, y7, 0xFFFFFF);
        Gui.drawRect(moduleX - padding, y7 - padding, moduleX + moduleWidth + padding, y7 + height + padding, bg7);
        mc.fontRendererObj.drawStringWithShadow(module1, moduleX, y7, 0xAAAAAA);
        Gui.drawRect(statusX - padding, y7 - padding, statusX + statusWidth + padding, y7 + height + padding, bg7);
        statusTextWidth = mc.fontRendererObj.getStringWidth(status1);
        centeredStatusX = statusX + (statusWidth - statusTextWidth) / 2;
        mc.fontRendererObj.drawStringWithShadow(status1, centeredStatusX, y7, status1Color);
        if (hover7 && mouseDown && !wasMouseDown) {
            ModuleSettings.setResourceTimerEnabled(!ModuleSettings.isResourceTimerEnabled());
        }

        wasMouseDown = mouseDown;
    }
}