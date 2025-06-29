package com.pwetutils;

import com.pwetutils.command.InfoCommand;
import com.pwetutils.listener.TextOverlayListener;
import net.weavemc.loader.api.ModInitializer;
import net.weavemc.loader.api.command.CommandBus;
import net.weavemc.loader.api.event.EventBus;

public class PwetUtils implements ModInitializer {
    public static final String VERSION = "1.0.24";

    @Override
    public void preInit() {
        System.out.println("[Pwet] Initializing PwetUtils v" + VERSION);
        CommandBus.register(new InfoCommand());
        EventBus.subscribe(new TextOverlayListener());
    }
}