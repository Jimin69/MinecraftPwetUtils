package com.pwetutils.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.ChatComponentText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class UsernameMentionMixin {
    @Inject(method = "handleChat", at = @At("HEAD"))
    private void onHandleChat(S02PacketChat packet, CallbackInfo ci) {
        if (packet.getChatComponent() != null && Minecraft.getMinecraft().thePlayer != null) {
            String playerName = Minecraft.getMinecraft().thePlayer.getName();
            String text = packet.getChatComponent().getUnformattedText();
            String formatted = packet.getChatComponent().getFormattedText();

            int colonIndex = text.indexOf(": ");
            String searchArea = colonIndex != -1 ? text.substring(colonIndex + 2) : text;

            if (searchArea.toLowerCase().contains(playerName.toLowerCase())) {
                String highlighted = formatted;

                if (colonIndex != -1) {
                    int colonIndexFormatted = formatted.indexOf(": ");
                    String beforeColon = formatted.substring(0, colonIndexFormatted + 2);
                    String afterColon = formatted.substring(colonIndexFormatted + 2);

                    String afterColonHighlighted = afterColon.replaceAll("(?i)" + playerName, "§b§n$0§r");
                    highlighted = beforeColon + afterColonHighlighted;
                } else {
                    highlighted = formatted.replaceAll("(?i)" + playerName, "§b§n$0§r");
                }

                packet.chatComponent = new ChatComponentText(highlighted);
            }
        }
    }
}