package com.pwetutils.listener;

import com.pwetutils.command.HologramCommand;
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

    public VideoControlPanelListener() {
        // Static buttons only - progress bar is now dynamic
        staticButtons.add(new ControlButton("§7< H", 2, 0, 0, ButtonType.COLLAPSE, null));
        staticButtons.add(new ControlButton("§7/ ]", 2, 0, 0, ButtonType.BORDER, null));
        staticButtons.add(new ControlButton("§7[", 214, 0, 0, ButtonType.BORDER, null));
        staticButtons.add(new ControlButton("§7\\ ]", 2, 0, 1, ButtonType.BORDER, null));
        staticButtons.add(new ControlButton("§7A", 24, 10, 1, ButtonType.NORMAL, null));
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
        List<ControlButton> buttons = new ArrayList<>(staticButtons);
        HologramImageListener hologramListener = getHologramListener();

        // Dynamic progress bar
        String progressBarText = buildProgressBar(hologramListener);
        buttons.add(new ControlButton(progressBarText, 24, 185, 0, ButtonType.PROGRESS, "/hologram vpr silent"));

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

        // Pause button - dynamic symbol
        String pauseSymbol = "§f┃┃";
        if (hologramListener != null && hologramListener.hasVideoHologram()) {
            VideoHologram video = hologramListener.getCurrentVideoHologram();
            if (video.isPaused()) {
                pauseSymbol = "§f§l⫸";
            }
        }
        buttons.add(new ControlButton(pauseSymbol, 94, 15, 1, ButtonType.PAUSE, "/hologram vp silent"));

        buttons.add(new ControlButton("§f⏎", 114, 15, 1, ButtonType.DOUBLE_CLICK, "/hologram vr silent"));

        // Skip backward button with special type
        buttons.add(new ControlButton("§f⫷", 134, 35, 1, ButtonType.SKIP_BACKWARD, "/hologram vsb"));

        // Transparency button - dynamic symbol
        String transSymbol = "§7①";
        if (hologramListener != null && hologramListener.hasVideoHologram()) {
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

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!(mc.currentScreen instanceof GuiChat)) return;

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
                            panelExpanded = true;
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

        // Handle right clicks for size and transparency
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

        // Render buttons
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

        // Modify display text for skip buttons when shift is held
        if (shiftHeld && button.type == ButtonType.SKIP_FORWARD) {
            displayText = "§e⫸⫸";  // Double arrow or different color to indicate 10s
        } else if (shiftHeld && button.type == ButtonType.SKIP_BACKWARD) {
            displayText = "§e⫷⫷";  // Double arrow or different color to indicate 10s
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

        // Check if this button has a pending double-click
        boolean isPending = button.type == ButtonType.DOUBLE_CLICK &&
                firstClickTime.containsKey(button.command);

        Gui.drawRect(x - padding, y - padding,
                x + boxWidth + padding, y + height + padding,
                isPending ? 0x60FFFF00 : (hovering ? 0x40FFFFFF : 0x80000000));

        // Check for click animation
        boolean isClicked = clickAnimations.containsKey(button.command) ||
                clickAnimations.containsKey(button.command + "_right") ||
                clickAnimations.containsKey(button.command + "_pending");

        if (button.type == ButtonType.COLLAPSE && hovering) {
            displayText = displayText.replace("§7<", "§f<");
        } else if (button.type == ButtonType.BORDER && hovering) {
            displayText = displayText.replaceFirst("§.", "§f");
        } else if (isPending) {
            displayText = displayText.replaceFirst("§.", "§6");
        } else if (isClicked && button.row == 1 && button.type != ButtonType.BORDER) {
            // Keep the yellow color for shift-held skip buttons when clicked
            if (!shiftHeld || (button.type != ButtonType.SKIP_FORWARD && button.type != ButtonType.SKIP_BACKWARD)) {
                displayText = displayText.replaceFirst("§.", "§e");
            }
        }

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
        SKIP_BACKWARD
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