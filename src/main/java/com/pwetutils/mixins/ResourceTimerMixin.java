package com.pwetutils.mixins;

import com.pwetutils.listener.ChatOverlayListener;
import com.pwetutils.listener.ResourceOverlayListener;
import com.pwetutils.listener.GameStateTracker;
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
                ResourceOverlayListener.startGame(false);
                ChatOverlayListener.startGame();
                GameStateTracker.reset();
            } else if (text.contains("All generators are maxed! Your bed has three") ||
                    text.contains("모든 생성기가 최대치로 강화됩니다! 침대에 삼중 보호막이")) {
                ResourceOverlayListener.startGame(true);
                ChatOverlayListener.startGame();
                GameStateTracker.reset();
            }
        }
    }
}