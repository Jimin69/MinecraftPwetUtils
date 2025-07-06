package com.pwetutils.command;

import net.weavemc.loader.api.command.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
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
                EmoteHandler.setEmotesEnabled(true);
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Emote conversion has been §aenabled")
                );
            } else if (args[1].equalsIgnoreCase("disable")) {
                EmoteHandler.setEmotesEnabled(false);
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
                ResourceOverlayListener.isEnabled = true;
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Bedwars resource timer has been §aenabled")
                );
            } else if (args[1].equalsIgnoreCase("disable")) {
                ResourceOverlayListener.isEnabled = false;
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
                AdditionalExpListener.isEnabled = true;
                mc.thePlayer.addChatMessage(
                        new ChatComponentText("§7[§6PwetUtils§7] §7Bedwars experience counter has been §aenabled")
                );
            } else if (args[1].equalsIgnoreCase("disable")) {
                AdditionalExpListener.isEnabled = false;
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

        mc.thePlayer.addChatMessage(
                new ChatComponentText("§7[§6PwetUtils§7] §7Unknown argument.")
        );
    }
}