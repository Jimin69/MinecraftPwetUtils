package com.pwetutils.mixins;

import com.pwetutils.emotes.EmoteHandler;
import com.pwetutils.settings.ModuleSettings;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.C01PacketChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityPlayerSP.class)
public class EntityPlayerSPMixin {
    @Redirect(
            method = "sendChatMessage",
            at = @At(value = "NEW", target = "(Ljava/lang/String;)Lnet/minecraft/network/play/client/C01PacketChatMessage;")
    )
    private C01PacketChatMessage redirectChatPacket(String message) {
        if (ModuleSettings.isIncreaseChatLengthEnabled() && message.length() > 100) {
            try {
                C01PacketChatMessage packet = new C01PacketChatMessage("");
                java.lang.reflect.Field messageField = C01PacketChatMessage.class.getDeclaredField("message");
                messageField.setAccessible(true);
                String finalMessage = message.substring(0, Math.min(256, message.length()));
                if (ModuleSettings.isEmoteConverterEnabled()) {
                    finalMessage = EmoteHandler.processEmotes(finalMessage);
                }
                messageField.set(packet, finalMessage);
                return packet;
            } catch (Exception e) {
                return new C01PacketChatMessage(message);
            }
        }

        return new C01PacketChatMessage(message);
    }
}