package com.pwetutils.mixins;

import com.pwetutils.listener.ChatOverlayListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S02PacketChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class ShoutDetectorMixin {
    @Inject(method = "handleChat", at = @At("HEAD"))
    private void onHandleChat(S02PacketChat packet, CallbackInfo ci) {
        if (packet.getChatComponent() != null && Minecraft.getMinecraft().thePlayer != null) {
            String text = packet.getChatComponent().getUnformattedText();
            String playerName = Minecraft.getMinecraft().thePlayer.getName();

            if (text.startsWith("[SHOUT]")) {
                int colonIndex = text.indexOf(": ");
                if (colonIndex != -1) {
                    String beforeColon = text.substring(0, colonIndex);
                    if (beforeColon.contains(playerName)) {
                        ChatOverlayListener.onPlayerShouted();
                    }
                }
            }
        }
    }
}