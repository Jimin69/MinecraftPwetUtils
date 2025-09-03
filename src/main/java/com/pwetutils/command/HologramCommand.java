package com.pwetutils.command;

import com.pwetutils.listener.HologramImageListener;
import net.weavemc.loader.api.command.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

public class HologramCommand extends Command {
    private static HologramImageListener hologramListener;

    public HologramCommand() {
        super("hologram", "holo");
    }

    public static void setHologramListener(HologramImageListener listener) {
        hologramListener = listener;
    }

    @Override
    public void handle(String[] args) {
        Minecraft mc = Minecraft.getMinecraft();

        if (args.length == 0) {
            mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Usage: /hologram <video|clear|url>"));
            return;
        }

        if (args[0].equalsIgnoreCase("video")) {
            if (hologramListener != null && mc.thePlayer != null) {
                double x = mc.thePlayer.posX;
                double y = mc.thePlayer.posY + 2.0;
                double z = mc.thePlayer.posZ;
                hologramListener.loadVideo(x, y, z);
                mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Creating video hologram..."));
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

        String url = args[0];
        if (hologramListener != null && mc.thePlayer != null) {
            double x = mc.thePlayer.posX;
            double y = mc.thePlayer.posY + 1.5;
            double z = mc.thePlayer.posZ;
            hologramListener.loadImage(url, x, y, z);
            mc.thePlayer.addChatMessage(new ChatComponentText("§7[§6PwetUtils§7] Creating image hologram..."));
        }
    }
}