package com.pwetutils.listener;

import com.pwetutils.command.HologramCommand;
import com.pwetutils.settings.ModuleSettings;
import net.weavemc.loader.api.event.RenderGameOverlayEvent;
import net.weavemc.loader.api.event.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoControlPanelListener {
    private final List<ControlButton> staticButtons = new ArrayList<>();
    private static final int BASE_OFFSET = 34;
    private boolean panelExpanded = false;
    private boolean wasMouseDown = false;
    private boolean wasRightMouseDown = false;
    private final Map<String, Long> clickAnimations = new HashMap<>();
    private static final long CLICK_ANIMATION_DURATION = 150;
    private HologramImageListener hologramListener;
    private final Map<String, Long> firstClickTime = new HashMap<>();
    private static final long DOUBLE_CLICK_TIME = 500;
    private long noVideoErrorTime = 0;
    private static final long ERROR_DISPLAY_DURATION = 2000;
    private boolean toggleModeActive = false;
    private static final long TOGGLE_ANIMATION_INTERVAL = 500;
    private float lastPlayerHealth = -1;
    private long greenFlashStartTime = 0;
    private long redFlashStartTime = 0;
    private static final long GREEN_FLASH_DURATION = 3000;
    private static final long RED_FLASH_DURATION = 3000;

    public VideoControlPanelListener() {
        staticButtons.add(new ControlButton("§7< H", 2, 0, 0, ButtonType.COLLAPSE, null));
        staticButtons.add(new ControlButton("§7/ ]", 2, 0, 0, ButtonType.BORDER, null));
        staticButtons.add(new ControlButton("§7[", 214, 0, 0, ButtonType.BORDER, null));
        staticButtons.add(new ControlButton("§7\\ ]", 2, 0, 1, ButtonType.BORDER, null));
        staticButtons.add(new ControlButton("§7[", 214, 0, 1, ButtonType.BORDER, null));
    }

    private HologramImageListener getHologramListener() {
        try {
            java.lang.reflect.Field field = HologramCommand.class.getDeclaredField("hologramListener");
            field.setAccessible(true);
            return (HologramImageListener) field.get(null);
        } catch (Exception e) {
            return null;
        }
    }

    private List<ControlButton> getDynamicButtons() {
        List<ControlButton> buttons = new ArrayList<>();

        // Add collapse button with dynamic text
        long currentTime = System.currentTimeMillis();
        String collapseText = "§7< H";
        if (currentTime - noVideoErrorTime < ERROR_DISPLAY_DURATION) {
            collapseText = "§c✖ H";
        }
        buttons.add(new ControlButton(collapseText, 2, 0, 0, ButtonType.COLLAPSE, null));

        // Add rest of static buttons (except A button)
        for (int i = 1; i < staticButtons.size(); i++) {
            buttons.add(staticButtons.get(i));
        }

        HologramImageListener hologramListener = getHologramListener();

        // Dynamic progress bar
        String progressBarText = buildProgressBar(hologramListener);
        buttons.add(new ControlButton(progressBarText, 24, 185, 0, ButtonType.PROGRESS, "/hologram vpr silent"));

        // Dynamic A toggle button with animation
        String toggleSymbol = "§7A";
        boolean inGreenFlash = (currentTime - greenFlashStartTime < GREEN_FLASH_DURATION);

        if (inGreenFlash) {
            // Green flash animation
            boolean useLight = (currentTime / TOGGLE_ANIMATION_INTERVAL) % 2 == 0;
            toggleSymbol = useLight ? "§aA" : "§2A";
        } else if (toggleModeActive) {
            // Normal yellow/orange animation when active
            boolean useYellow = (currentTime / TOGGLE_ANIMATION_INTERVAL) % 2 == 0;
            toggleSymbol = useYellow ? "§eA" : "§6A";
        }
        buttons.add(new ControlButton(toggleSymbol, 24, 10, 1, ButtonType.TOGGLE, "toggle_mode"));

        // Size button - dynamic symbol
        String sizeSymbol = "§7➍";
        if (hologramListener != null && hologramListener.hasVideoHologram()) {
            VideoHologram video = hologramListener.getCurrentVideoHologram();
            switch (video.getSizeLevel()) {
                case 2: sizeSymbol = "§7➋"; break;
                case 3: sizeSymbol = "§7➌"; break;
                case 4: sizeSymbol = "§7➍"; break;
                case 5: sizeSymbol = "§7➎"; break;
                case 6: sizeSymbol = "§7➏"; break;
            }
        }
        buttons.add(new ControlButton(sizeSymbol, 39, 10, 1, ButtonType.SIZE, "/hologram vcyclesize silent"));

        // Skip forward button with special type
        buttons.add(new ControlButton("§f⫸", 54, 35, 1, ButtonType.SKIP_FORWARD, "/hologram vsf"));

        // Pause button - dynamic symbol with flash animations
        String pauseSymbol = "§f┃┃";
        boolean inRedFlash = (currentTime - redFlashStartTime < RED_FLASH_DURATION);

        if (inGreenFlash) {
            // Green flash for pause button (when resuming)
            boolean useLight = (currentTime / TOGGLE_ANIMATION_INTERVAL) % 2 == 0;
            String greenColor = useLight ? "§a" : "§2";
            if (hologramListener != null && hologramListener.hasVideoHologram()) {
                VideoHologram video = hologramListener.getCurrentVideoHologram();
                pauseSymbol = video.isPaused() ? greenColor + "§l⫸" : greenColor + "┃┃";
            } else {
                pauseSymbol = greenColor + "┃┃";
            }
        } else if (inRedFlash) {
            // Red flash for pause button (when damaged)
            boolean useLight = (currentTime / TOGGLE_ANIMATION_INTERVAL) % 2 == 0;
            String redColor = useLight ? "§c" : "§4";
            if (hologramListener != null && hologramListener.hasVideoHologram()) {
                VideoHologram video = hologramListener.getCurrentVideoHologram();
                pauseSymbol = video.isPaused() ? redColor + "§l⫸" : redColor + "┃┃";
            } else {
                pauseSymbol = redColor + "┃┃";
            }
        } else if (hologramListener != null && hologramListener.hasVideoHologram()) {
            VideoHologram video = hologramListener.getCurrentVideoHologram();
            if (video.isPaused()) {
                pauseSymbol = "§f§l⫸";
            }
        }
        buttons.add(new ControlButton(pauseSymbol, 94, 15, 1, ButtonType.PAUSE, "/hologram vp silent"));

        buttons.add(new ControlButton("§f⏎", 114, 15, 1, ButtonType.DOUBLE_CLICK, "/hologram vr silent"));

        // Skip backward button with special type
        buttons.add(new ControlButton("§f⫷", 134, 35, 1, ButtonType.SKIP_BACKWARD, "/hologram vsb"));

        // Transparency button - dynamic symbol with flash animations
        String transSymbol = "§7①";
        if (inGreenFlash) {
            // Green flash for transparency button (when resuming)
            boolean useLight = (currentTime / TOGGLE_ANIMATION_INTERVAL) % 2 == 0;
            String greenColor = useLight ? "§a" : "§2";
            if (hologramListener != null && hologramListener.hasVideoHologram()) {
                VideoHologram video = hologramListener.getCurrentVideoHologram();
                switch (video.getTransparencyMode()) {
                    case SOLID: transSymbol = greenColor + "①"; break;
                    case TRANSPARENT: transSymbol = greenColor + "②"; break;
                    case IDLE: transSymbol = greenColor + "③"; break;
                }
            } else {
                transSymbol = greenColor + "①";
            }
        } else if (inRedFlash) {
            // Red flash for transparency button (when damaged)
            boolean useLight = (currentTime / TOGGLE_ANIMATION_INTERVAL) % 2 == 0;
            String redColor = useLight ? "§c" : "§4";
            if (hologramListener != null && hologramListener.hasVideoHologram()) {
                VideoHologram video = hologramListener.getCurrentVideoHologram();
                switch (video.getTransparencyMode()) {
                    case SOLID: transSymbol = redColor + "①"; break;
                    case TRANSPARENT: transSymbol = redColor + "②"; break;
                    case IDLE: transSymbol = redColor + "③"; break;
                }
            } else {
                transSymbol = redColor + "①";
            }
        } else if (hologramListener != null && hologramListener.hasVideoHologram()) {
            VideoHologram video = hologramListener.getCurrentVideoHologram();
            switch (video.getTransparencyMode()) {
                case SOLID: transSymbol = "§7①"; break;
                case TRANSPARENT: transSymbol = "§7②"; break;
                case IDLE: transSymbol = "§7③"; break;
            }
        }
        buttons.add(new ControlButton(transSymbol, 174, 15, 1, ButtonType.TRANSPARENCY, "/hologram vct silent"));

        buttons.add(new ControlButton("§7§l⬇", 194, 15, 1, ButtonType.DOUBLE_CLICK, "/hologram vmh silent"));

        return buttons;
    }

    private String buildProgressBar(HologramImageListener hologramListener) {
        if (hologramListener == null || !hologramListener.hasVideoHologram()) {
            // Default when no video
            StringBuilder bar = new StringBuilder();
            for (int i = 0; i < 60; i++) {
                bar.append("§7|");
            }
            return "§f0:00 " + bar.toString() + " §f0:00";
        }

        VideoHologram video = hologramListener.getCurrentVideoHologram();
        float progress = video.getProgress();
        float duration = video.getDuration();
        float currentTime = duration * progress;

        // Build the bar with 60 segments
        StringBuilder bar = new StringBuilder();
        int progressedBars = (int)(progress * 60);

        for (int i = 0; i < 60; i++) {
            if (i < progressedBars) {
                bar.append("§c|");
            } else {
                bar.append("§7|");
            }
        }

        // Format times
        String currentTimeStr = formatTime(currentTime);
        String durationStr = formatTime(duration);

        return "§f" + currentTimeStr + " " + bar.toString() + " §f" + durationStr;
    }

    private String formatTime(float seconds) {
        int totalSec = (int)seconds;
        int minutes = totalSec / 60;
        int secs = totalSec % 60;
        return String.format("%d:%02d", minutes, secs);
    }

    public boolean isToggleModeActive() {
        return toggleModeActive;
    }

    private void checkPlayerDamage() {
        if (!toggleModeActive) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        float currentHealth = mc.thePlayer.getHealth();

        // Initialize last health if needed
        if (lastPlayerHealth < 0) {
            lastPlayerHealth = currentHealth;
            return;
        }

        // Check if health decreased
        if (currentHealth < lastPlayerHealth) {
            HologramImageListener listener = getHologramListener();
            if (listener != null && listener.hasVideoHologram()) {
                VideoHologram video = listener.getCurrentVideoHologram();
                if (video != null && !video.isPaused()) {
                    // Pause the video
                    video.pause();
                    // Set transparency to IDLE
                    video.setTransparencyMode(VideoHologram.TransparencyMode.IDLE);
                    // Start red flash animation
                    redFlashStartTime = System.currentTimeMillis();
                }
            }
        }

        lastPlayerHealth = currentHealth;
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        // Always check for player damage regardless of chat GUI
        checkPlayerDamage();

        Minecraft mc = Minecraft.getMinecraft();
        if (!(mc.currentScreen instanceof GuiChat)) return;
        if (!ModuleSettings.isIngameHologramsEnabled()) return;

        ScaledResolution sr = new ScaledResolution(mc);
        int baseY = sr.getScaledHeight() - 27;
        int rightEdge = sr.getScaledWidth();

        int mouseX = Mouse.getX() * sr.getScaledWidth() / mc.displayWidth;
        int mouseY = sr.getScaledHeight() - Mouse.getY() * sr.getScaledHeight() / mc.displayHeight - 1;
        boolean mouseDown = Mouse.isButtonDown(0);
        boolean rightMouseDown = Mouse.isButtonDown(1);
        boolean shiftHeld = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);

        List<ControlButton> buttons = getDynamicButtons();

        // Clean up old animations
        clickAnimations.entrySet().removeIf(entry ->
                System.currentTimeMillis() - entry.getValue() > CLICK_ANIMATION_DURATION);
        firstClickTime.entrySet().removeIf(entry ->
                System.currentTimeMillis() - entry.getValue() > DOUBLE_CLICK_TIME);

        // Check if any border button is hovered
        boolean anyBorderHovered = false;
        if (panelExpanded) {
            for (ControlButton button : buttons) {
                if (button.type == ButtonType.BORDER) {
                    if (isHovering(button, rightEdge, baseY, mouseX, mouseY, mc)) {
                        anyBorderHovered = true;
                        break;
                    }
                }
            }
        }

        // Handle clicks
        if (mouseDown && !wasMouseDown) {
            if (!panelExpanded) {
                for (ControlButton button : buttons) {
                    if (button.type == ButtonType.COLLAPSE) {
                        if (isHovering(button, rightEdge, baseY, mouseX, mouseY, mc)) {
                            HologramImageListener listener = getHologramListener();
                            if (listener != null && listener.hasVideoHologram()) {
                                panelExpanded = true;
                            } else {
                                // Show error - no video hologram
                                noVideoErrorTime = System.currentTimeMillis();
                            }
                            break;
                        }
                    }
                }
            } else {
                for (ControlButton button : buttons) {
                    if (button.type != ButtonType.COLLAPSE && isHovering(button, rightEdge, baseY, mouseX, mouseY, mc)) {
                        if (button.type == ButtonType.BORDER) {
                            panelExpanded = false;
                            break;
                        } else if (button.type == ButtonType.TOGGLE) {
                            if (shiftHeld) {
                                // Shift+click on A button - resume from damage pause
                                HologramImageListener listener = getHologramListener();
                                if (listener != null && listener.hasVideoHologram()) {
                                    VideoHologram video = listener.getCurrentVideoHologram();
                                    if (video != null && video.isPaused() &&
                                            video.getTransparencyMode() == VideoHologram.TransparencyMode.IDLE) {
                                        // Resume video and set to solid
                                        video.resume();
                                        video.setTransparencyMode(VideoHologram.TransparencyMode.SOLID);
                                        // Start green flash animation
                                        greenFlashStartTime = System.currentTimeMillis();
                                        clickAnimations.put(button.command, System.currentTimeMillis());
                                    }
                                }
                            } else {
                                // Normal click - toggle the mode
                                toggleModeActive = !toggleModeActive;
                                clickAnimations.put(button.command, System.currentTimeMillis());
                                // Reset health tracking when toggling
                                lastPlayerHealth = -1;
                            }
                            break;
                        } else if (button.type == ButtonType.SKIP_FORWARD) {
                            // Handle skip forward with shift modifier
                            clickAnimations.put(button.command, System.currentTimeMillis());
                            int skipAmount = shiftHeld ? 10 : 5;
                            mc.thePlayer.sendChatMessage(button.command + " " + skipAmount + " silent");
                            break;
                        } else if (button.type == ButtonType.SKIP_BACKWARD) {
                            // Handle skip backward with shift modifier
                            clickAnimations.put(button.command, System.currentTimeMillis());
                            int skipAmount = shiftHeld ? 10 : 5;
                            mc.thePlayer.sendChatMessage(button.command + " " + skipAmount + " silent");
                            break;
                        } else if (button.type == ButtonType.DOUBLE_CLICK && button.command != null) {
                            long currentTime = System.currentTimeMillis();
                            Long lastClick = firstClickTime.get(button.command);

                            if (lastClick != null && currentTime - lastClick < DOUBLE_CLICK_TIME) {
                                clickAnimations.put(button.command, currentTime);
                                mc.thePlayer.sendChatMessage(button.command);
                                firstClickTime.remove(button.command);
                            } else {
                                firstClickTime.put(button.command, currentTime);
                                clickAnimations.put(button.command + "_pending", currentTime);
                            }
                            break;
                        } else if (button.command != null) {
                            if (button.type != ButtonType.PROGRESS && button.row == 1) {
                                clickAnimations.put(button.command, System.currentTimeMillis());
                            }
                            mc.thePlayer.sendChatMessage(button.command);
                            break;
                        }
                    }
                }
            }
        }

        // handle right clicks for size and transparency
        if (rightMouseDown && !wasRightMouseDown && panelExpanded) {
            for (ControlButton button : buttons) {
                if (isHovering(button, rightEdge, baseY, mouseX, mouseY, mc)) {
                    if (button.type == ButtonType.SIZE) {
                        clickAnimations.put(button.command + "_right", System.currentTimeMillis());
                        mc.thePlayer.sendChatMessage("/hologram vcyclesize back silent");
                        break;
                    } else if (button.type == ButtonType.TRANSPARENCY) {
                        clickAnimations.put(button.command + "_right", System.currentTimeMillis());
                        mc.thePlayer.sendChatMessage("/hologram vct back silent");
                        break;
                    }
                }
            }
        }

        // render buttons
        for (ControlButton button : buttons) {
            if (shouldRenderButton(button)) {
                renderButton(mc, button, rightEdge, baseY, mouseX, mouseY, anyBorderHovered, shiftHeld);
            }
        }

        wasMouseDown = mouseDown;
        wasRightMouseDown = rightMouseDown;
    }

    private boolean shouldRenderButton(ControlButton button) {
        if (button.type == ButtonType.COLLAPSE) {
            return !panelExpanded;
        } else {
            return panelExpanded;
        }
    }

    private boolean isHovering(ControlButton button, int rightEdge, int baseY, int mouseX, int mouseY, Minecraft mc) {
        int padding = 2;
        int textWidth = mc.fontRendererObj.getStringWidth(button.text);
        int boxWidth = button.width == 0 ? textWidth + padding * 2 : button.width;
        int height = mc.fontRendererObj.FONT_HEIGHT;

        int actualXFromRight = button.xFromRight + BASE_OFFSET;
        int x = rightEdge - actualXFromRight - boxWidth;
        int rowHeight = height + padding * 2 + 1;
        int y = baseY - (button.row * rowHeight);

        return mouseX >= x - padding &&
                mouseX <= x + boxWidth + padding &&
                mouseY >= y - padding &&
                mouseY <= y + height + padding;
    }

    private void renderButton(Minecraft mc, ControlButton button, int rightEdge, int baseY, int mouseX, int mouseY, boolean anyBorderHovered, boolean shiftHeld) {
        int padding = 2;
        String displayText = button.text;

        // modify display text for skip buttons when shift is held - keep white color
        if (shiftHeld && button.type == ButtonType.SKIP_FORWARD) {
            displayText = "§f⫸⫸";  // Double arrow in white
        } else if (shiftHeld && button.type == ButtonType.SKIP_BACKWARD) {
            displayText = "§f⫷⫷";  // Double arrow in white
        }

        int textWidth = mc.fontRendererObj.getStringWidth(displayText);
        int boxWidth = button.width == 0 ? textWidth + padding * 2 : button.width;
        int height = mc.fontRendererObj.FONT_HEIGHT;

        int actualXFromRight = button.xFromRight + BASE_OFFSET;
        int x = rightEdge - actualXFromRight - boxWidth;

        int rowHeight = height + padding * 2 + 1;
        int y = baseY - (button.row * rowHeight);

        boolean hovering = isHovering(button, rightEdge, baseY, mouseX, mouseY, mc);

        if (button.type == ButtonType.BORDER) {
            hovering = anyBorderHovered;
        }

        // check if this button has a pending double-click
        boolean isPending = button.type == ButtonType.DOUBLE_CLICK &&
                firstClickTime.containsKey(button.command);

        Gui.drawRect(x - padding, y - padding,
                x + boxWidth + padding, y + height + padding,
                isPending ? 0x60FFFF00 : (hovering ? 0x40FFFFFF : 0x80000000));

        // check for click animation
        boolean isClicked = clickAnimations.containsKey(button.command) ||
                clickAnimations.containsKey(button.command + "_right") ||
                clickAnimations.containsKey(button.command + "_pending");

        if (button.type == ButtonType.COLLAPSE && hovering) {
            // con't change color if it's showing error
            if (!displayText.startsWith("§c")) {
                displayText = displayText.replace("§7<", "§f<");
            }
        } else if (button.type == ButtonType.BORDER && hovering) {
            displayText = displayText.replaceFirst("§.", "§f");
        } else if (isPending) {
            displayText = displayText.replaceFirst("§.", "§6");
        } else if (isClicked && button.row == 1 && button.type != ButtonType.BORDER && button.type != ButtonType.TOGGLE) {
            displayText = displayText.replaceFirst("§.", "§e");
        }
        // toggle button doesn't change color on click since it has its own animation

        int textX = x + (boxWidth - mc.fontRendererObj.getStringWidth(displayText)) / 2;
        mc.fontRendererObj.drawStringWithShadow(displayText, textX, y, 0xFFFFFF);
    }

    private enum ButtonType {
        NORMAL,
        BORDER,
        COLLAPSE,
        SIZE,
        TRANSPARENCY,
        PAUSE,
        PROGRESS,
        DOUBLE_CLICK,
        SKIP_FORWARD,
        SKIP_BACKWARD,
        TOGGLE
    }

    private static class ControlButton {
        String text;
        int xFromRight;
        int width;
        int row;
        ButtonType type;
        String command;

        ControlButton(String text, int xFromRight, int width, int row, ButtonType type, String command) {
            this.text = text;
            this.xFromRight = xFromRight;
            this.width = width;
            this.row = row;
            this.type = type;
            this.command = command;
        }
    }
}