package com.pwetutils.mixins;

import com.pwetutils.command.RequeueCommand;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S02PacketChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class IncomingChatMixin {
    @Inject(method = "handleChat", at = @At("HEAD"))
    private void onHandleChat(S02PacketChat packet, CallbackInfo ci) {
        if (packet.getChatComponent() != null) {
            String text = packet.getChatComponent().getUnformattedText();
            if (text.startsWith("{") && text.contains("\"gametype\":\"BEDWARS\"") && text.contains("\"mode\":")) {
                int modeStart = text.indexOf("\"mode\":\"") + 8;
                int modeEnd = text.indexOf("\"", modeStart);
                if (modeStart > 7 && modeEnd > modeStart) {
                    String mode = text.substring(modeStart, modeEnd);
                    RequeueCommand.setLastGameMode(mode);
                }
            }
        }
    }
}