package com.pwetutils.mixins;

import com.pwetutils.listener.ChatOverlayListener;
import com.pwetutils.listener.ResourceOverlayListener;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S02PacketChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class ResourceTimerMixin {
    @Inject(method = "handleChat", at = @At("HEAD"))
    private void onHandleChat(S02PacketChat packet, CallbackInfo ci) {
        if (packet.getChatComponent() != null) {
            String text = packet.getChatComponent().getUnformattedText();
            if (text.contains("Protect your bed and destroy the enemy beds.") ||
                    text.contains("자신의 침대를 보호하고 적들의 침대를 파괴하세요.")) {
                ResourceOverlayListener.startGame();
                ChatOverlayListener.startGame();
            }
        }
    }
}