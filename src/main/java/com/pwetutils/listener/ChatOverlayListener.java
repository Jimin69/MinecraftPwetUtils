package com.pwetutils.listener;

import com.pwetutils.settings.ModuleSettings;
import net.weavemc.loader.api.event.RenderGameOverlayEvent;
import net.weavemc.loader.api.event.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.scoreboard.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.Collection;

public class ChatOverlayListener {
    public static boolean isKorean = false;
    public static boolean systemKoreanDetected = false;
    public static boolean settingsOpen = false;
    private boolean wasCtrlPressed = false;
    private boolean languageClicked = false;
    private boolean wasMouseDown = false;

    private static boolean gameStarted = false;
    private static int lastShoutScoreboardTime = -1;
    private static boolean shoutJustHappened = false;
    private static final int SHOUT_COOLDOWN = 60;

    public static void startGame() {
        gameStarted = true;
        lastShoutScoreboardTime = -1;
        shoutJustHappened = false;
    }

    public static void onPlayerShouted() {
        if (gameStarted) {
            shoutJustHappened = true;
        }
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

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();

        if (!(mc.currentScreen instanceof GuiChat)) {
            settingsOpen = false;
            return;
        }

        if (ModuleSettings.isLanguageInputEnabled()) {
            boolean ctrlPressed = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);
            if (ctrlPressed && !wasCtrlPressed) {
                isKorean = !isKorean;
                systemKoreanDetected = false;
            }
            wasCtrlPressed = ctrlPressed;
        }

        int padding = 2;
        int baseY = new ScaledResolution(mc).getScaledHeight() - 27;
        int textHeight = mc.fontRendererObj.FONT_HEIGHT;

        ScaledResolution sr = new ScaledResolution(mc);
        int mouseX = Mouse.getX() * sr.getScaledWidth() / mc.displayWidth;
        int mouseY = sr.getScaledHeight() - Mouse.getY() * sr.getScaledHeight() / mc.displayHeight - 1;
        boolean mouseDown = Mouse.isButtonDown(0);

        if (ModuleSettings.isLanguageInputEnabled()) {
            String languageText = isKorean ? "한글" : "English";
            int languageX = 4;
            int languageWidth = mc.fontRendererObj.getStringWidth(languageText);

            boolean languageHovering = mouseX >= languageX - padding &&
                    mouseX <= languageX + languageWidth + padding &&
                    mouseY >= baseY - padding &&
                    mouseY <= baseY + textHeight + padding;

            if (languageHovering && mouseDown && !wasMouseDown) {
                isKorean = !isKorean;
                systemKoreanDetected = false;
            }
            languageClicked = languageHovering && mouseDown;

            int textColor;
            if (systemKoreanDetected) {
                textColor = 0xFF5555;
            } else {
                textColor = languageClicked ? 0xFFFF00 : 0xFFAA00;
            }

            Gui.drawRect(languageX - padding, baseY - padding,
                    languageX + languageWidth + padding, baseY + textHeight + padding,
                    languageHovering ? 0x40FFFFFF : 0x80000000);
            mc.fontRendererObj.drawStringWithShadow(languageText, languageX, baseY, textColor);
        }

        String settingsText = "§l⚙";
        int settingsX = ModuleSettings.isLanguageInputEnabled() ? (isKorean ? 26 : 45) : 4;
        int settingsWidth = 11;
        boolean settingsHovering = mouseX >= settingsX - padding &&
                mouseX <= settingsX + settingsWidth + padding &&
                mouseY >= baseY - padding &&
                mouseY <= baseY + textHeight + padding;

        if (settingsHovering && mouseDown && !wasMouseDown) {
            settingsOpen = !settingsOpen;
        }

        wasMouseDown = mouseDown;

        Gui.drawRect(settingsX - padding, baseY - padding,
                settingsX + settingsWidth + padding, baseY + textHeight + padding,
                settingsHovering ? 0x40FFFFFF : 0x80000000);
        int textWidth = mc.fontRendererObj.getStringWidth(settingsText);
        int centeredX = settingsX + (settingsWidth - textWidth) / 2 + 1;
        mc.fontRendererObj.drawStringWithShadow(settingsText, centeredX, baseY, settingsOpen ? 0xFFFFFF : 0xAAAAAA);

        int currentScoreboardTime = -1;
        boolean timerFound = false;

        if (gameStarted && mc.theWorld != null) {
            Scoreboard scoreboard = mc.theWorld.getScoreboard();
            ScoreObjective sidebar = scoreboard.getObjectiveInDisplaySlot(1);

            if (sidebar != null) {
                Collection<Score> scores = scoreboard.getSortedScores(sidebar);
                for (Score score : scores) {
                    if (score.getScorePoints() == 12) {
                        ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
                        if (team != null) {
                            String suffix = team.getColorSuffix();
                            if (suffix.matches(".*\\d+:\\d+.*")) {
                                String timeStr = suffix.replaceAll("[^0-9:]", "");
                                currentScoreboardTime = parseTime(timeStr);
                                timerFound = true;
                            }
                        }
                        break;
                    }
                }
            }
        }

        if (!timerFound) {
            lastShoutScoreboardTime = -1;
        }

        if (shoutJustHappened && timerFound) {
            lastShoutScoreboardTime = currentScoreboardTime;
            shoutJustHappened = false;
        }

        if (lastShoutScoreboardTime != -1 && timerFound) {
            int timeElapsed = lastShoutScoreboardTime - currentScoreboardTime;
            int remainingSeconds = SHOUT_COOLDOWN - timeElapsed;

            if (remainingSeconds > 0) {
                String shoutText = "§6[SHOUT] §7in §c" + remainingSeconds + "§7s";
                int shoutX = ModuleSettings.isLanguageInputEnabled() ? (isKorean ? 43 : 62) : 21;
                int shoutWidth = mc.fontRendererObj.getStringWidth(shoutText);

                Gui.drawRect(shoutX - padding, baseY - padding,
                        shoutX + shoutWidth + padding, baseY + textHeight + padding,
                        0x80000000);
                mc.fontRendererObj.drawStringWithShadow(shoutText, shoutX, baseY, 0xFFFFFF);
            } else {
                lastShoutScoreboardTime = -1;
            }
        }
    }
}