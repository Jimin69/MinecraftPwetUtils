package com.pwetutils.command;

import net.minecraft.util.ChatComponentText;
import net.weavemc.loader.api.command.Command;
import net.minecraft.client.Minecraft;

public class BedwarsFoursCommand extends Command {
    public BedwarsFoursCommand() {
        super("bw4s", "b4s");
    }

    @Override
    public void handle(String[] args) {
        Minecraft.getMinecraft().thePlayer.sendChatMessage("/play bedwars_four_four");
    }
}