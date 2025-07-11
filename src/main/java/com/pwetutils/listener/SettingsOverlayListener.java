package com.pwetutils.listener;

import com.pwetutils.settings.ModuleSettings;
import net.weavemc.loader.api.event.RenderGameOverlayEvent;
import net.weavemc.loader.api.event.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SettingsOverlayListener {
    private boolean wasMouseDown = false;
    private final List<ModuleItem> modules = new ArrayList<>();

    public SettingsOverlayListener() {
        modules.add(new ModuleItem("§2§l✦", "bedwarsResourceTimer",
                ModuleSettings::isResourceTimerEnabled,
                ModuleSettings::setResourceTimerEnabled));

        modules.add(new ModuleItem("§e✫", "bedwarsExperienceCounter",
                ModuleSettings::isExperienceCounterEnabled,
                ModuleSettings::setExperienceCounterEnabled));

        modules.add(new ModuleItem("§b§l!", "bedwarsChatWarnings",
                ModuleSettings::isChatWarningsEnabled,
                ModuleSettings::setChatWarningsEnabled));

        modules.add(new ModuleItem("§c§l❤", "hypixelAutoFriendKR",
                ModuleSettings::isAutoFriendAcceptEnabled,
                ModuleSettings::setAutoFriendAcceptEnabled));

        modules.add(new ModuleItem("§do/", "hypixelEmoteConverter",
                ModuleSettings::isEmoteConverterEnabled,
                ModuleSettings::setEmoteConverterEnabled));

        modules.add(new ModuleItem("§b@", "nameMentionIndicator",
                ModuleSettings::isNameMentionEnabled,
                ModuleSettings::setNameMentionEnabled));

        modules.add(new ModuleItem("§f#", "increaseChatLength",
                ModuleSettings::isIncreaseChatLengthEnabled,
                ModuleSettings::setIncreaseChatLengthEnabled));

        modules.add(new ModuleItem("§6A", "languageInputSwitch",
                ModuleSettings::isLanguageInputEnabled,
                ModuleSettings::setLanguageInputEnabled));

        // modules.add(new ModuleItem("§5★", "test", null, null));
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!(mc.currentScreen instanceof GuiChat) || !ChatOverlayListener.settingsOpen) return;

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

        // Title bar
        int titleWidth = 190;
        int titleY = baseY - (height + padding * 2 + 1) * modules.size();
        String titleText = "§6PwetUtils Modules §7(/pwetutils help)";
        boolean titleHover = mouseX >= iconX - padding && mouseX <= iconX + titleWidth + padding &&
                mouseY >= titleY - padding && mouseY <= titleY + height + padding;

        Gui.drawRect(iconX - padding, titleY - padding, iconX + titleWidth + padding, titleY + height + padding,
                titleHover ? 0x40FFFFFF : 0x80000000);
        int titleTextWidth = mc.fontRendererObj.getStringWidth(titleText);
        int titleCenteredX = iconX + (titleWidth - titleTextWidth) / 2;
        mc.fontRendererObj.drawStringWithShadow(titleText, titleCenteredX, titleY, 0xFFFFFF);

        if (titleHover && mouseDown && !wasMouseDown) {
            ChatOverlayListener.settingsOpen = false;
            mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] §6§m----------------------------------------------"));
            mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] §7/pwetutils §eDisplays mod info and version"));
            mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] §7/pwetutils help §eDisplays this list of commands"));

            for (ModuleItem module : modules) {
                if (module.getStatus != null) {
                    sendModuleHelpMessage(mc, module.name);
                }
            }

            mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] §7/rq§8|§7/requeue §eJoin the last BedWars mode you played."));
            mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] §7/b4s§8|§7/b4§8|§7/b3s§8|§7/b2s§8|§7/b1s §eJoin BedWars mode"));
            mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] §6§m----------------------------------------------"));
        }

        // Render modules
        int currentY = baseY;
        for (int i = modules.size() - 1; i >= 0; i--) {
            ModuleItem module = modules.get(i);
            boolean hover = mouseX >= iconX - padding && mouseX <= statusX + statusWidth + padding &&
                    mouseY >= currentY - padding && mouseY <= currentY + height + padding;
            int bg = hover ? 0x40FFFFFF : 0x80000000;

            // Icon
            Gui.drawRect(iconX - padding, currentY - padding, iconX + iconWidth + padding, currentY + height + padding, bg);
            int iconTextWidth = mc.fontRendererObj.getStringWidth(module.icon);
            int centeredIconX = iconX + (iconWidth - iconTextWidth) / 2 + 1;
            mc.fontRendererObj.drawStringWithShadow(module.icon, centeredIconX, currentY, 0xFFFFFF);

            // Module name
            Gui.drawRect(moduleX - padding, currentY - padding, moduleX + moduleWidth + padding, currentY + height + padding, bg);
            mc.fontRendererObj.drawStringWithShadow(module.name, moduleX, currentY, 0xAAAAAA);

            // Status
            Gui.drawRect(statusX - padding, currentY - padding, statusX + statusWidth + padding, currentY + height + padding, bg);
            String status = module.getStatus != null && module.getStatus.get() ? "TRUE" : "FALSE";
            int statusColor = module.getStatus != null && module.getStatus.get() ? 0x55FF55 : 0xFF5555;

            if (module.getStatus == null) {
                status = "N/A";
                statusColor = 0x555555;
            }

            int statusTextWidth = mc.fontRendererObj.getStringWidth(status);
            int centeredStatusX = statusX + (statusWidth - statusTextWidth) / 2;
            mc.fontRendererObj.drawStringWithShadow(status, centeredStatusX, currentY, statusColor);

            // Click handling
            if (hover && mouseDown && !wasMouseDown && module.setStatus != null) {
                module.setStatus.accept(!module.getStatus.get());
            }

            currentY -= height + padding * 2 + 1;
        }

        wasMouseDown = mouseDown;
    }

    private void sendModuleHelpMessage(Minecraft mc, String moduleName) {
        ChatComponentText prefix = new ChatComponentText("§7[§6PwetUtils§7] §7/pwetutils " + moduleName + " §8<");

        ChatComponentText enable = new ChatComponentText("§aenable");
        enable.setChatStyle(new ChatStyle()
                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§fClick to paste command")))
                .setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/pwetutils " + moduleName + " enable")));

        ChatComponentText separator = new ChatComponentText("§8|");

        ChatComponentText disable = new ChatComponentText("§cdisable");
        disable.setChatStyle(new ChatStyle()
                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§fClick to paste command")))
                .setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/pwetutils " + moduleName + " disable")));

        ChatComponentText suffix = new ChatComponentText("§8>");

        prefix.appendSibling(enable);
        prefix.appendSibling(separator);
        prefix.appendSibling(disable);
        prefix.appendSibling(suffix);

        mc.thePlayer.addChatMessage(prefix);
    }

    private static class ModuleItem {
        final String icon;
        final String name;
        final Supplier<Boolean> getStatus;
        final Consumer<Boolean> setStatus;

        ModuleItem(String icon, String name, Supplier<Boolean> getStatus, Consumer<Boolean> setStatus) {
            this.icon = icon;
            this.name = name;
            this.getStatus = getStatus;
            this.setStatus = setStatus;
        }
    }
}