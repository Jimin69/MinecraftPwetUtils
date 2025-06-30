package com.pwetutils.mixins;

import com.pwetutils.emotes.EmoteHandler;
import net.minecraft.network.play.client.C01PacketChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(C01PacketChatMessage.class)
public class ChatPacketMixin {
    @Shadow
    @Mutable
    private String message;

    // increase char limit & handle chat emotes
    @Inject(method = "<init>(Ljava/lang/String;)V", at = @At("RETURN"))
    private void processEmotes(String message, CallbackInfo ci) {
        this.message = EmoteHandler.processEmotes(this.message.substring(0, Math.min(256, this.message.length())));
    }
}