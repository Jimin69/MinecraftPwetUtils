package com.pwetutils.listener;

import com.pwetutils.settings.ModuleSettings;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoFriendListener {
    private static final Pattern FRIEND_REQUEST_PATTERN = Pattern.compile(
            "다음 플레이어로부터 친구 요청을 받았습니다\\. (?:\\[[^\\]]+\\] )?(.+)"
    );

    private static final Map<String, Long> recentRequests = new HashMap<>();
    private static final long COOLDOWN_MS = 5000;

    public static void handleFriendRequest(String message) {
        if (!ModuleSettings.isAutoFriendAcceptEnabled()) return;

        Matcher matcher = FRIEND_REQUEST_PATTERN.matcher(message);
        if (matcher.find()) {
            String playerName = matcher.group(1).trim();

            long currentTime = System.currentTimeMillis();
            Long lastRequestTime = recentRequests.get(playerName);

            if (lastRequestTime != null && currentTime - lastRequestTime < COOLDOWN_MS) {
                return;
            }

            recentRequests.put(playerName, currentTime);

            Minecraft.getMinecraft().addScheduledTask(() -> {
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/f accept " + playerName);
            });

            recentRequests.entrySet().removeIf(entry ->
                    currentTime - entry.getValue() > COOLDOWN_MS * 2
            );
        }
    }
}