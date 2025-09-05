package com.pwetutils.command;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.weavemc.loader.api.command.Command;

public class DebugGuiCommand extends Command {
    public DebugGuiCommand() {
        super("debuggui", "dbg", "ds");
    }

    @Override
    public void handle(String[] args) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Debug started - leave the game now!"));

        new Thread(() -> {
            try {
                // Wait 5 seconds
                Thread.sleep(5000);

                // Get world and player info and immediately log to console
                String worldInfo = mc.theWorld == null ? "null" : "exists";
                String playerInfo = mc.thePlayer == null ? "null" : "exists";

                System.out.println("[PwetUtils] World: " + worldInfo + ", Player: " + playerInfo);
                System.out.println("[PwetUtils] Hello world");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}