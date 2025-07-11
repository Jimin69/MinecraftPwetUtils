package com.pwetutils.mixins;

import com.pwetutils.listener.AutoFriendListener;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S02PacketChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class AutoFriendMixin {
    @Inject(method = "handleChat", at = @At("HEAD"))
    private void onHandleChat(S02PacketChat packet, CallbackInfo ci) {
        if (packet.getChatComponent() != null) {
            String text = packet.getChatComponent().getUnformattedText();

            // Check if message contains the friend request pattern
            if (text.contains("-----------------------------------------------------") &&
                    text.contains("다음 플레이어로부터 친구 요청을 받았습니다.") &&
                    text.contains("[수락] - [거절] - [BLOCK]")) {

                // Extract the line with the player name
                String[] lines = text.split("\n");
                for (String line : lines) {
                    if (line.contains("다음 플레이어로부터 친구 요청을 받았습니다.")) {
                        AutoFriendListener.handleFriendRequest(line);
                        break;
                    }
                }
            }
        }
    }
}