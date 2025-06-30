package com.pwetutils.command;

import net.weavemc.loader.api.command.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

public class RequeueCommand extends Command {
    private static String lastGameMode = null;

    public RequeueCommand() {
        super("reque", "rq");
    }

    @Override
    public void handle(String[] args) {
        Minecraft mc = Minecraft.getMinecraft();
        if (lastGameMode == null) {
            mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] You didn't play a game of bedwars yet this session!"));
            return;
        }
        mc.thePlayer.sendChatMessage("/play " + lastGameMode.toLowerCase());
    }

    public static void setLastGameMode(String mode) {
        lastGameMode = mode;
    }
}