package com.pwetutils.listener;

import net.weavemc.loader.api.event.RenderGameOverlayEvent;
import net.weavemc.loader.api.event.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class TextOverlayListener {
    public static boolean isKorean = false;
    private boolean wasCtrlPressed = false;
    private boolean languageClicked = false;
    private boolean wasMouseDown = false;

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!(mc.currentScreen instanceof GuiChat)) return;

        boolean ctrlPressed = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);
        if (ctrlPressed && !wasCtrlPressed) {
            isKorean = !isKorean;
        }
        wasCtrlPressed = ctrlPressed;

        int padding = 2;
        int baseY = new ScaledResolution(mc).getScaledHeight() - 27;

        String languageText = isKorean ? "한글" : "English";
        int languageX = 4;
        int languageWidth = mc.fontRendererObj.getStringWidth(languageText);
        int textHeight = mc.fontRendererObj.FONT_HEIGHT;

        ScaledResolution sr = new ScaledResolution(mc);
        int mouseX = Mouse.getX() * sr.getScaledWidth() / mc.displayWidth;
        int mouseY = sr.getScaledHeight() - Mouse.getY() * sr.getScaledHeight() / mc.displayHeight - 1;

        boolean languageHovering = mouseX >= languageX - padding &&
                mouseX <= languageX + languageWidth + padding &&
                mouseY >= baseY - padding &&
                mouseY <= baseY + textHeight + padding;

        boolean mouseDown = Mouse.isButtonDown(0);
        if (languageHovering && mouseDown && !wasMouseDown) {
            isKorean = !isKorean;
        }
        wasMouseDown = mouseDown;
        languageClicked = languageHovering && mouseDown;

        Gui.drawRect(languageX - padding, baseY - padding,
                languageX + languageWidth + padding, baseY + textHeight + padding,
                languageHovering ? 0x40FFFFFF : 0x80000000);
        mc.fontRendererObj.drawStringWithShadow(languageText, languageX, baseY,
                languageClicked ? 0xFFFF00 : 0xFFAA00);

        String settingsText = "§l⚙";
        int settingsX = isKorean ? 26 : 45;
        int settingsWidth = 11;
        boolean settingsHovering = mouseX >= settingsX - padding &&
                mouseX <= settingsX + settingsWidth + padding &&
                mouseY >= baseY - padding &&
                mouseY <= baseY + textHeight + padding;
        Gui.drawRect(settingsX - padding, baseY - padding,
                settingsX + settingsWidth + padding, baseY + textHeight + padding,
                settingsHovering ? 0x40FFFFFF : 0x80000000);
        int textWidth = mc.fontRendererObj.getStringWidth(settingsText);
        int centeredX = settingsX + (settingsWidth - textWidth) / 2 + 1;
        mc.fontRendererObj.drawStringWithShadow(settingsText, centeredX, baseY, 0xAAAAAA);
    }
}