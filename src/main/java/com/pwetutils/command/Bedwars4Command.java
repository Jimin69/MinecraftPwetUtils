package com.pwetutils.command;

import net.weavemc.loader.api.command.Command;
import net.minecraft.client.Minecraft;

public class Bedwars4Command extends Command {
    public Bedwars4Command() {
        super("bw4", "b4");
    }

    @Override
    public void handle(String[] args) {
        Minecraft.getMinecraft().thePlayer.sendChatMessage("/play bedwars_two_four");
    }
}