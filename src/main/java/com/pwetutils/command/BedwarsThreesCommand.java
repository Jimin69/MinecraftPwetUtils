package com.pwetutils.command;

import net.weavemc.loader.api.command.Command;
import net.minecraft.client.Minecraft;

public class BedwarsThreesCommand extends Command {
    public BedwarsThreesCommand() {
        super("bw3s", "b3s");
    }

    @Override
    public void handle(String[] args) {
        Minecraft.getMinecraft().thePlayer.sendChatMessage("/play bedwars_four_three");
    }
}