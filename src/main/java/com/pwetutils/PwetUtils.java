package com.pwetutils;

import com.pwetutils.command.*;
import com.pwetutils.listener.TextOverlayListener;
import net.weavemc.loader.api.ModInitializer;
import net.weavemc.loader.api.command.CommandBus;
import net.weavemc.loader.api.event.EventBus;

public class PwetUtils implements ModInitializer {
    public static final String VERSION = "1.1.12";

    @Override
    public void preInit() {
        System.out.println("[Pwet] Initializing PwetUtils v" + VERSION);
        CommandBus.register(new InfoCommand());
        CommandBus.register(new BedwarsSoloCommand());
        CommandBus.register(new BedwarsDoublesCommand());
        CommandBus.register(new BedwarsThreesCommand());
        CommandBus.register(new BedwarsFoursCommand());
        CommandBus.register(new Bedwars4Command());
        CommandBus.register(new RequeueCommand());
        EventBus.subscribe(new TextOverlayListener());
    }
}