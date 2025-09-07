package com.pwetutils;

import com.pwetutils.command.*;
import com.pwetutils.listener.*;
import net.weavemc.loader.api.ModInitializer;
import net.weavemc.loader.api.command.CommandBus;
import net.weavemc.loader.api.event.EventBus;

public class PwetUtils implements ModInitializer {
    public static final String VERSION = "1.8.37";

    @Override
    public void preInit() {
        System.out.println("[Pwet] Initializing PwetUtils v" + VERSION);
        CommandBus.register(new PwetUtilsCommand());
        CommandBus.register(new BedwarsSoloCommand());
        CommandBus.register(new BedwarsDoublesCommand());
        CommandBus.register(new BedwarsThreesCommand());
        CommandBus.register(new BedwarsFoursCommand());
        CommandBus.register(new Bedwars4Command());
        CommandBus.register(new RequeueCommand());
        CommandBus.register(new HarvesterCommand());
        CommandBus.register(new DebugGuiCommand());

        HologramImageListener hologramListener = new HologramImageListener();
        CommandBus.register(new HologramCommand());
        HologramCommand.setHologramListener(hologramListener);

        EventBus.subscribe(new ChatOverlayListener());
        EventBus.subscribe(new ResourceOverlayListener());
        EventBus.subscribe(new AdditionalExpListener());
        EventBus.subscribe(new SettingsOverlayListener());
        EventBus.subscribe(new HarvesterOverlayListener());
        EventBus.subscribe(new VideoControlPanelListener());
    }
}