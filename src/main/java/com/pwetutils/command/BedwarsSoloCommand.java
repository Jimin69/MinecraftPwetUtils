package com.pwetutils.command;

import net.weavemc.loader.api.command.Command;
import net.minecraft.client.Minecraft;

public class BedwarsSoloCommand extends Command {
    public BedwarsSoloCommand() {
        super("bw1s", "bw1", "b1", "b1s");
    }

    @Override
    public void handle(String[] args) {
        Minecraft.getMinecraft().thePlayer.sendChatMessage("/play bedwars_eight_one");
    }
}