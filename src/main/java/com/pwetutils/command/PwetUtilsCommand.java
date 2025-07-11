package com.pwetutils.command;

import com.pwetutils.settings.ModuleSettings;
import net.weavemc.loader.api.command.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.IChatComponent;
import com.pwetutils.PwetUtils;
import com.pwetutils.emotes.EmoteHandler;
import com.pwetutils.listener.ResourceOverlayListener;
import com.pwetutils.listener.AdditionalExpListener;

public class PwetUtilsCommand extends Command {
    public PwetUtilsCommand() {
        super("pwetutils", "jimin", "pu");
    }

    @Override
    public void handle(String[] args) {
        Minecraft mc = Minecraft.getMinecraft();

        if (args.length == 0) {
            mc.thePlayer.addChatMessage(
                    new ChatComponentText("§7[§6PwetUtils§7] §7Small personal utilities by §9@ji.min")
            );
            mc.thePlayer.addChatMessage(
                    new ChatComponentText("§7[§6PwetUtils§7] §7Mod version: §fv" + PwetUtils.VERSION)
            );
            mc.thePlayer.addChatMessage(
                    new ChatComponentText("§7[§6PwetUtils§7] §7Help: §e/pwetutils help §7 or §e/pu <args>§7 for short.")
            );
            return;
        }

        if (args[0].equalsIgnoreCase("help")) {
            mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] §6§m----------------------------------------------"));
            mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] §7/pwetutils §eDisplays mod info and version"));
            mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] §7/pwetutils help §eDisplays this list of commands"));

            sendModuleHelpMessage(mc, "bedwarsResourceTimer");
            sendModuleHelpMessage(mc, "bedwarsExperienceCounter");
            sendModuleHelpMessage(mc, "bedwarsChatWarnings");
            sendModuleHelpMessage(mc, "hypixelAutoFriendKR");
            sendModuleHelpMessage(mc, "emotes");
            sendModuleHelpMessage(mc, "nameMentionIndicator");
            sendModuleHelpMessage(mc, "increaseChatLength");
            sendModuleHelpMessage(mc, "languageInputSwitch");

            mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] §7/rq§8|§7/requeue §eJoin the last BedWars mode you played."));
            mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] §7/b4s§8|§7/b4§8|§7/b3s§8|§7/b2s§8|§7/b1s §eJoin BedWars mode"));
            mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] §6§m----------------------------------------------"));
            return;
        }

        if (args[0].equalsIgnoreCase("emotes")) {
            if (args.length < 2) {
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Usage: /pwetutils emotes <enable|disable>")
                );
                return;
            }

            if (args[1].equalsIgnoreCase("enable")) {
                ModuleSettings.setEmoteConverterEnabled(true);
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Emote conversion has been §aenabled")
                );
            } else if (args[1].equalsIgnoreCase("disable")) {
                ModuleSettings.setEmoteConverterEnabled(false);
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Emote conversion has been §cdisabled")
                );
            } else {
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Usage: /pwetutils emotes <enable|disable>")
                );
            }
            return;
        }

        if (args[0].equalsIgnoreCase("bedwarsResourceTimer")) {
            if (args.length < 2) {
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Usage: /pwetutils bedwarsResourceTimer <enable|disable>")
                );
                return;
            }

            if (args[1].equalsIgnoreCase("enable")) {
                ModuleSettings.setResourceTimerEnabled(true);
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Bedwars resource timer has been §aenabled")
                );
            } else if (args[1].equalsIgnoreCase("disable")) {
                ModuleSettings.setResourceTimerEnabled(false);
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Bedwars resource timer has been §cdisabled")
                );
            } else {
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Usage: /pwetutils bedwarsResourceTimer <enable|disable>")
                );
            }
            return;
        }

        if (args[0].equalsIgnoreCase("bedwarsExperienceCounter")) {
            if (args.length < 2) {
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Usage: /pwetutils bedwarsExperienceCounter <enable|disable>")
                );
                return;
            }

            if (args[1].equalsIgnoreCase("enable")) {
                ModuleSettings.setExperienceCounterEnabled(true);
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Bedwars experience counter has been §aenabled")
                );
            } else if (args[1].equalsIgnoreCase("disable")) {
                ModuleSettings.setExperienceCounterEnabled(false);
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Bedwars experience counter has been §cdisabled")
                );
            } else {
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Usage: /pwetutils bedwarsExperienceCounter <enable|disable>")
                );
            }
            return;
        }

        if (args[0].equalsIgnoreCase("nameMentionIndicator")) {
            if (args.length < 2) {
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Usage: /pwetutils nameMentionIndicator <enable|disable>")
                );
                return;
            }

            if (args[1].equalsIgnoreCase("enable")) {
                ModuleSettings.setNameMentionEnabled(true);
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Name mention indicator has been §aenabled")
                );
            } else if (args[1].equalsIgnoreCase("disable")) {
                ModuleSettings.setNameMentionEnabled(false);
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Name mention indicator has been §cdisabled")
                );
            } else {
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Usage: /pwetutils nameMentionIndicator <enable|disable>")
                );
            }
            return;
        }

        if (args[0].equalsIgnoreCase("bedwarsChatWarnings")) {
            if (args.length < 2) {
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Usage: /pwetutils bedwarsChatWarnings <enable|disable>")
                );
                return;
            }

            if (args[1].equalsIgnoreCase("enable")) {
                ModuleSettings.setChatWarningsEnabled(true);
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Bedwars chat warnings have been §aenabled")
                );
            } else if (args[1].equalsIgnoreCase("disable")) {
                ModuleSettings.setChatWarningsEnabled(false);
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Bedwars chat warnings have been §cdisabled")
                );
            } else {
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Usage: /pwetutils bedwarsChatWarnings <enable|disable>")
                );
            }
            return;
        }

        if (args[0].equalsIgnoreCase("languageInputSwitch")) {
            if (args.length < 2) {
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Usage: /pwetutils languageInputSwitch <enable|disable>")
                );
                return;
            }

            if (args[1].equalsIgnoreCase("enable")) {
                ModuleSettings.setLanguageInputEnabled(true);
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Language input switch has been §aenabled")
                );
            } else if (args[1].equalsIgnoreCase("disable")) {
                ModuleSettings.setLanguageInputEnabled(false);
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Language input switch has been §cdisabled")
                );
            } else {
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Usage: /pwetutils languageInputSwitch <enable|disable>")
                );
            }
            return;
        }

        if (args[0].equalsIgnoreCase("increaseChatLength")) {
            if (args.length < 2) {
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Usage: /pwetutils increaseChatLength <enable|disable>")
                );
                return;
            }

            if (args[1].equalsIgnoreCase("enable")) {
                ModuleSettings.setIncreaseChatLengthEnabled(true);
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Increased chat length has been §aenabled")
                );
            } else if (args[1].equalsIgnoreCase("disable")) {
                ModuleSettings.setIncreaseChatLengthEnabled(false);
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Increased chat length has been §cdisabled")
                );
            } else {
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Usage: /pwetutils increaseChatLength <enable|disable>")
                );
            }
            return;
        }

        if (args[0].equalsIgnoreCase("hypixelAutoFriendKR")) {
            if (args.length < 2) {
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Usage: /pwetutils hypixelAutoFriendKR <enable|disable>")
                );
                return;
            }

            if (args[1].equalsIgnoreCase("enable")) {
                ModuleSettings.setAutoFriendAcceptEnabled(true);
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Hypixel auto friend accept (KR) has been §aenabled")
                );
            } else if (args[1].equalsIgnoreCase("disable")) {
                ModuleSettings.setAutoFriendAcceptEnabled(false);
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Hypixel auto friend accept (KR) has been §cdisabled")
                );
            } else {
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Usage: /pwetutils hypixelAutoFriendKR <enable|disable>")
                );
            }
            return;
        }

        mc.thePlayer.addChatMessage(
                new ChatComponentText("§7[§6PwetUtils§7] §7Unknown argument. Use §e/pwetutils help §7for command list.")
        );
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
}