package com.pwetutils.command;

import net.weavemc.loader.api.command.Command;
import net.minecraft.client.Minecraft;

public class BedwarsDoublesCommand extends Command {
    public BedwarsDoublesCommand() {
        super("bw2s", "b2s");
    }

    @Override
    public void handle(String[] args) {
        Minecraft.getMinecraft().thePlayer.sendChatMessage("/play bedwars_eight_two");
    }
}