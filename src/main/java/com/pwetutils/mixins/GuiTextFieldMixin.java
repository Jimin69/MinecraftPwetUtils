package com.pwetutils.mixins;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiTextField.class)
public class GuiTextFieldMixin {
    @Shadow private String text;
    @Shadow private int lineScrollOffset;

    @Redirect(method = "drawTextBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I"))
    private int drawColoredText(FontRenderer fontRenderer, String text, float x, float y, int color) {
        return drawStringWithEmoteColors(fontRenderer, text, (int)x, (int)y, color);
    }

    private int drawStringWithEmoteColors(FontRenderer font, String text, int x, int y, int defaultColor) {
        int currentX = x;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            current.append(c);
            String currentStr = current.toString();

            boolean foundEmote = false;

            if (currentStr.endsWith("<3")) {
                boolean valid = (i + 1 >= text.length()) || !Character.isLetterOrDigit(text.charAt(i + 1));
                if (valid) {
                    String before = currentStr.substring(0, currentStr.length() - 2);
                    if (before.length() > 0) {
                        font.drawStringWithShadow(before, currentX, y, defaultColor);
                        currentX += font.getStringWidth(before);
                    }
                    font.drawStringWithShadow("<3", currentX, y, 0x55FFFF);
                    currentX += font.getStringWidth("<3");
                    current.setLength(0);
                    foundEmote = true;
                }
            } else if (currentStr.endsWith("o/") || currentStr.endsWith("h/")) {
                String emote = currentStr.substring(currentStr.length() - 2);
                int startIdx = i - 1;
                boolean validStart = (startIdx < 0) || !Character.isLetterOrDigit(text.charAt(startIdx - 1));
                boolean validEnd = (i + 1 >= text.length()) || !Character.isLetterOrDigit(text.charAt(i + 1));

                if (validStart && validEnd) {
                    String before = currentStr.substring(0, currentStr.length() - 2);
                    if (before.length() > 0) {
                        font.drawStringWithShadow(before, currentX, y, defaultColor);
                        currentX += font.getStringWidth(before);
                    }
                    font.drawStringWithShadow(emote, currentX, y, 0x55FFFF);
                    currentX += font.getStringWidth(emote);
                    current.setLength(0);
                    foundEmote = true;
                }
            } else if (currentStr.endsWith("^-^") || currentStr.endsWith("^_^")) {
                String emote = currentStr.substring(currentStr.length() - 3);
                String before = currentStr.substring(0, currentStr.length() - 3);
                if (before.length() > 0) {
                    font.drawStringWithShadow(before, currentX, y, defaultColor);
                    currentX += font.getStringWidth(before);
                }
                font.drawStringWithShadow(emote, currentX, y, 0x55FFFF);
                currentX += font.getStringWidth(emote);
                current.setLength(0);
                foundEmote = true;
            }

            if (!foundEmote && c == ':') {
                for (int j = i + 1; j <= text.length(); j++) {
                    if (j < text.length() && text.charAt(j) == ':') {
                        String possibleEmote = text.substring(i, j + 1);
                        if (isEmote(possibleEmote)) {
                            String before = current.substring(0, current.length() - 1);
                            if (before.length() > 0) {
                                font.drawStringWithShadow(before, currentX, y, defaultColor);
                                currentX += font.getStringWidth(before);
                            }
                            font.drawStringWithShadow(possibleEmote, currentX, y, 0x55FFFF);
                            currentX += font.getStringWidth(possibleEmote);
                            current.setLength(0);
                            i = j;
                            foundEmote = true;
                            break;
                        }
                    }
                }
            }
        }

        if (current.length() > 0) {
            font.drawStringWithShadow(current.toString(), currentX, y, defaultColor);
            currentX += font.getStringWidth(current.toString());
        }

        return currentX;
    }

    private boolean isEmote(String text) {
        return text.equals(":star:") || text.equals(":yes:") || text.equals(":no:") ||
                text.equals(":java:") || text.equals(":arrow:") || text.equals(":shrug:") ||
                text.equals(":tableflip:") || text.equals(":123:") || text.equals(":totem:") ||
                text.equals(":typing:") || text.equals(":maths:") || text.equals(":snail:") ||
                text.equals(":thinking:") || text.equals(":gimme:") || text.equals(":wizard:") ||
                text.equals(":pvp:") || text.equals(":peace:") || text.equals(":oof:") ||
                text.equals(":puffer:") || text.equals(":cute:") || text.equals(":yey:") ||
                text.equals(":cat:") || text.equals(":dab:") || text.equals(":sloth:") ||
                text.equals(":dj:") || text.equals(":snow:") || text.equals(":dog:");
    }
}