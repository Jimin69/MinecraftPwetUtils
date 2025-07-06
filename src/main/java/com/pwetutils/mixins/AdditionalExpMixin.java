package com.pwetutils.mixins;

import com.pwetutils.listener.AdditionalExpListener;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S02PacketChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(NetHandlerPlayClient.class)
public class AdditionalExpMixin {
    private static final Pattern XP_PATTERN = Pattern.compile("\\+(\\d+) Bed Wars XP \\((.+)\\)");
    private static String lastMessage = "";
    private static long lastMessageTime = 0;

    @Inject(method = "handleChat", at = @At("HEAD"))
    private void onHandleChat(S02PacketChat packet, CallbackInfo ci) {
        if (packet.getChatComponent() != null) {
            String text = packet.getChatComponent().getUnformattedText();

            if (text.contains("Protect your bed and destroy the enemy beds.") ||
                    text.contains("자신의 침대를 보호하고 적들의 침대를 파괴하세요.")) {
                AdditionalExpListener.startGame();
                return;
            }

            Matcher matcher = XP_PATTERN.matcher(text);
            if (matcher.find()) {
                long currentTime = System.currentTimeMillis();
                if (text.equals(lastMessage) && currentTime - lastMessageTime < 100) {
                    return;
                }

                lastMessage = text;
                lastMessageTime = currentTime;

                int xp = Integer.parseInt(matcher.group(1));
                String source = matcher.group(2);
                AdditionalExpListener.addXP(xp, source);
            }
        }
    }
}