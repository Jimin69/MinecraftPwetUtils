package com.pwetutils.mixins;

import net.minecraft.network.play.client.C01PacketChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(C01PacketChatMessage.class)
public class ChatPacketMixin {
    @Redirect(
            method = "<init>(Ljava/lang/String;)V",
            at = @At(value = "INVOKE", target = "Ljava/lang/String;substring(II)Ljava/lang/String;")
    )
    private String increaseChatLimit(String message, int start, int end) {
        return message.substring(0, Math.min(256, message.length()));
    }
}