package com.pwetutils.listener;

import com.pwetutils.settings.ModuleSettings;
import net.weavemc.loader.api.event.RenderGameOverlayEvent;
import net.weavemc.loader.api.event.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScorePlayerTeam;

import java.util.Collection;

public class ResourceOverlayListener {
    private static boolean gameStarted = false;
    private static int lastScoreboardTime = -1;
    private static int totalGameTime = 0;

    public static void startGame() {
        gameStarted = true;
        lastScoreboardTime = -1;
        totalGameTime = 0;
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Pre event) {
        if (!ModuleSettings.isResourceTimerEnabled() || !GameStateTracker.shouldShowOverlays()) return;
        Minecraft mc = Minecraft.getMinecraft();

        String diamondTier = "§8III";
        String emeraldTier = "§8III";
        String diamondTime = "?";
        String emeraldTime = "?";

        if (gameStarted && mc.theWorld != null) {
            Scoreboard scoreboard = mc.theWorld.getScoreboard();
            ScoreObjective sidebar = scoreboard.getObjectiveInDisplaySlot(1);

            if (sidebar != null) {
                Collection<Score> scores = scoreboard.getSortedScores(sidebar);

                for (Score score : scores) {
                    if (score.getScorePoints() == 12) {
                        String playerName = score.getPlayerName();
                        ScorePlayerTeam team = scoreboard.getPlayersTeam(playerName);

                        if (team != null) {
                            String prefix = team.getColorPrefix();
                            String suffix = team.getColorSuffix();
                            String fullLine = prefix + playerName + suffix;

                            if (fullLine.matches(".*\\d+:\\d+.*")) {
                                String timeStr = fullLine.replaceAll("[^0-9:]", "");
                                int currentScoreboardTime = parseTime(timeStr);

                                // Update total game time
                                if (lastScoreboardTime != -1) {
                                    int timeDiff = lastScoreboardTime - currentScoreboardTime;
                                    if (timeDiff < 0) timeDiff += 360; // Handle timer reset
                                    totalGameTime += timeDiff;
                                }

                                lastScoreboardTime = currentScoreboardTime;

                                diamondTime = String.valueOf(calculateDiamondTime(totalGameTime));
                                emeraldTime = String.valueOf(calculateEmeraldTime(totalGameTime));
                                diamondTier = getDiamondTier(totalGameTime);
                                emeraldTier = getEmeraldTier(totalGameTime);
                            }
                        }
                        break;
                    }
                }
            }
        }

        String text1 = diamondTier + " §bDiamonds §7spawn in §e" + diamondTime + "§7s";
        String text2 = emeraldTier + " §2Emeralds §7spawn in §e" + emeraldTime + "§7s";
        int padding = 2;
        int x = 4;
        int y = 4;
        int width = Math.max(mc.fontRendererObj.getStringWidth(text1), mc.fontRendererObj.getStringWidth(text2));
        int height = mc.fontRendererObj.FONT_HEIGHT;
        int totalHeight = height * 2 + 2;

        Gui.drawRect(x - padding, y - padding, x + width + padding, y + totalHeight + padding, 0x80000000);
        mc.fontRendererObj.drawStringWithShadow(text1, x, y, 0xFFFFFF);
        mc.fontRendererObj.drawStringWithShadow(text2, x, y + height + 2, 0xFFFFFF);
    }

    private String getDiamondTier(int totalSeconds) {
        if (totalSeconds < 360) {
            return "§fI§8II";
        } else if (totalSeconds < 1080) {
            return "§eII§8I";
        } else {
            return "§6III";
        }
    }

    private String getEmeraldTier(int totalSeconds) {
        if (totalSeconds < 720) {
            return "§fI§8II";
        } else if (totalSeconds < 1440) {
            return "§eII§8I";
        } else {
            return "§6III";
        }
    }

    private int calculateDiamondTime(int totalSeconds) {
        // Sync points
        if (totalSeconds == 0) return 31;
        if (totalSeconds == 360) return 23;   // 6:00
        if (totalSeconds == 720) return 1;    // 12:00
        if (totalSeconds == 1080) return 12;  // 18:00
        if (totalSeconds == 1260) return 2;   // 21:00
        if (totalSeconds == 1440) return 2;   // 24:00
        if (totalSeconds == 1620) return 3;   // 27:00
        if (totalSeconds == 1800) return 4;   // 30:00
        if (totalSeconds == 1980) return 5;   // 33:00
        if (totalSeconds == 2160) return 5;   // 36:00

        // Calculate based on cycles
        if (totalSeconds < 360) {
            // Tier I: 30s cycles
            if (totalSeconds < 31) return 31 - totalSeconds;
            int elapsed = totalSeconds - 31;
            return 30 - (elapsed % 30);
        } else if (totalSeconds < 1080) {
            // Tier II: 23s cycles
            int baseTime = findLastSyncTime(totalSeconds, new int[]{360, 720});
            int syncValue = (baseTime == 360) ? 23 : 1;
            int elapsed = totalSeconds - baseTime - syncValue;
            if (elapsed < 0) return -elapsed;
            return 23 - (elapsed % 23);
        } else {
            // Tier III: 12s cycles
            int baseTime = findLastSyncTime(totalSeconds, new int[]{1080, 1260, 1440, 1620, 1800, 1980, 2160});
            int syncValue = getSyncValue(baseTime, new int[]{1080, 1260, 1440, 1620, 1800, 1980, 2160},
                    new int[]{12, 2, 2, 3, 4, 5, 5});
            int elapsed = totalSeconds - baseTime - syncValue;
            if (elapsed < 0) return -elapsed;
            return 12 - (elapsed % 12);
        }
    }

    private int calculateEmeraldTime(int totalSeconds) {
        // sync
        if (totalSeconds == 0) return 86;
        if (totalSeconds == 720) return 40;   // 12:00
        if (totalSeconds == 1080) return 40;  // 18:00
        if (totalSeconds == 1440) return 27;  // 24:00
        if (totalSeconds == 1620) return 9;   // 27:00
        if (totalSeconds == 1800) return 19;  // 30:00
        if (totalSeconds == 1980) return 1;   // 33:00
        if (totalSeconds == 2160) return 10;  // 36:00

        // calc based on cycles (calc stands for calculator im just using slang for anyone new to the stream)
        if (totalSeconds < 720) {
            // Tier I: 55s cycles
            if (totalSeconds < 86) return 86 - totalSeconds;
            int elapsed = totalSeconds - 86;
            return 55 - (elapsed % 55);
        } else if (totalSeconds < 1440) {
            // Tier II: 40s cycles
            int baseTime = findLastSyncTime(totalSeconds, new int[]{720, 1080});
            int syncValue = 40;
            int elapsed = totalSeconds - baseTime - syncValue;
            if (elapsed < 0) return -elapsed;
            return 40 - (elapsed % 40);
        } else {
            // Tier III: 27s cycles
            int baseTime = findLastSyncTime(totalSeconds, new int[]{1440, 1620, 1800, 1980, 2160});
            int syncValue = getSyncValue(baseTime, new int[]{1440, 1620, 1800, 1980, 2160},
                    new int[]{27, 9, 19, 1, 10});
            int elapsed = totalSeconds - baseTime - syncValue;
            if (elapsed < 0) return -elapsed;
            return 27 - (elapsed % 27);
        }
    }

    private int findLastSyncTime(int totalSeconds, int[] syncTimes) {
        int lastSync = 0;
        for (int sync : syncTimes) {
            if (sync <= totalSeconds) lastSync = sync;
            else break;
        }
        return lastSync;
    }

    private int getSyncValue(int time, int[] syncTimes, int[] syncValues) {
        for (int i = 0; i < syncTimes.length; i++) {
            if (syncTimes[i] == time) return syncValues[i];
        }
        return 0;
    }

    private int parseTime(String time) {
        String[] parts = time.split(":");
        if (parts.length == 2) {
            try {
                return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}