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
public class BwIncomingDetectorMixin {
    @Inject(method = "handleChat", at = @At("HEAD"))
    private void onHandleChat(S02PacketChat packet, CallbackInfo ci) {
        if (packet.getChatComponent() != null) {
            String text = packet.getChatComponent().getUnformattedText();

            if (text.startsWith("§9Party §8> ") || (!text.contains("[SHOUT]") && isTeamChat(text))) {
                int colonIndex = text.indexOf(": ");
                if (colonIndex != -1) {
                    String message = text.substring(colonIndex + 2);
                    String messageLower = message.toLowerCase();
                    String[] words = messageLower.split("\\s+");
                    String[] originalWords = message.split("\\s+");

                    for (int i = 0; i < words.length; i++) {
                        String word = words[i];
                        if (word.equals("inc") ||
                                (word.startsWith("inc") && word.substring(3).matches("\\\\+")) ||
                                (word.startsWith("inc") && word.substring(3).matches("!+")) ||
                                (word.startsWith("inc") && word.substring(3).matches("1+"))) {

                            Minecraft.getMinecraft().addScheduledTask(() ->
                                    Minecraft.getMinecraft().thePlayer.playSound("random.successful_hit", 1.0F, 0.0F)
                            );

                            String incWord = originalWords[i];
                            String formatted = packet.getChatComponent().getFormattedText();
                            String highlighted = formatted.replace(incWord, "§b§n" + incWord + "§r");
                            packet.chatComponent = new ChatComponentText(highlighted);
                            break;
                        }
                    }
                }
            }
        }
    }

    private boolean isTeamChat(String text) {
        return text.matches(".*\\[\\d+✫\\].*\\[(RED|BLUE|YELLOW|GREEN|AQUA|WHITE|PINK|GRAY|빨강|파랑|노랑|초록|하늘색|하양|분홍|회색)\\].*:.*");
    }
}