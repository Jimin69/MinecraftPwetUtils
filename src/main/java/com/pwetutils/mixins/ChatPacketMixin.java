package com.pwetutils.mixins;

import com.pwetutils.emotes.EmoteHandler;
import com.pwetutils.settings.ModuleSettings;
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

    @Inject(method = "<init>(Ljava/lang/String;)V", at = @At("RETURN"))
    private void processEmotes(String messageIn, CallbackInfo ci) {
        if (ModuleSettings.isEmoteConverterEnabled() && !ModuleSettings.isIncreaseChatLengthEnabled()) {
            this.message = EmoteHandler.processEmotes(this.message);
        }
    }
}