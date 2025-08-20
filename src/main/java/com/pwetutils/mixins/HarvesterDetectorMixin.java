package com.pwetutils.mixins;

import com.pwetutils.command.HarvesterCommand;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S02PacketChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class HarvesterDetectorMixin {
    @Inject(method = "handleChat", at = @At("HEAD"))
    private void onHandleChat(S02PacketChat packet, CallbackInfo ci) {
        if (packet.getChatComponent() != null) {
            String text = packet.getChatComponent().getUnformattedText();
            // Detect server change via JSON message
            if (text.startsWith("{") && text.contains("\"server\":") && text.contains("\"gametype\":")) {
                HarvesterCommand.stopByTrigger();
            }
        }
    }
}