package com.pwetutils.mixins;

import com.pwetutils.korean.HangulAssembler;
import com.pwetutils.listener.ChatOverlayListener;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiChat.class)
public class KoreanInputMixin {
    @Shadow
    protected GuiTextField inputField;

    private static final String CHO = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ";
    private HangulAssembler.Hangul currentHangul = new HangulAssembler.Hangul();
    private boolean composing = false;

    @Inject(method = "initGui", at = @At("TAIL"))
    public void onInitGui(CallbackInfo ci) {
        ChatOverlayListener.systemKoreanDetected = false;
        ChatOverlayListener.settingsOpen = false;
    }

    @Inject(method = "keyTyped", at = @At("HEAD"), cancellable = true)
    public void onKeyTyped(char typedChar, int keyCode, CallbackInfo ci) {
        boolean isSystemKorean = (typedChar >= 0xAC00 && typedChar <= 0xD7A3) || // complete korean syllables
                (typedChar >= 0x1100 && typedChar <= 0x11FF) || // korean jamo
                (typedChar >= 0x3130 && typedChar <= 0x318F);   // compatibility jamo

        if (isSystemKorean) {
            ChatOverlayListener.systemKoreanDetected = true;
            if (ChatOverlayListener.isKorean) {
                ci.cancel();
                return;
            }
        } else if (Character.isLetter(typedChar)) {
            ChatOverlayListener.systemKoreanDetected = false;
        }

        if (!ChatOverlayListener.isKorean) return;

        if (GuiScreen.isCtrlKeyDown() || GuiScreen.isKeyComboCtrlA(keyCode) ||
                GuiScreen.isKeyComboCtrlC(keyCode) || GuiScreen.isKeyComboCtrlV(keyCode) ||
                GuiScreen.isKeyComboCtrlX(keyCode)) {
            currentHangul.clear();
            composing = false;
            return;
        }

        if (!Character.isLetter(typedChar) && keyCode != 14) {
            currentHangul.clear();
            composing = false;
            return;
        }

        if (keyCode == 14) { // backspace
            if (composing && inputField.getCursorPosition() > 0) {
                String text = inputField.getText();
                int cursor = inputField.getCursorPosition();
                char prevChar = text.charAt(cursor - 1);

                HangulAssembler.Hangul h = HangulAssembler.decompose(prevChar);
                if (h.cho != -1) {
                    // if korean syllable, decompose it
                    text = text.substring(0, cursor - 1) + text.substring(cursor);
                    inputField.setText(text);
                    inputField.setCursorPosition(cursor - 1);

                    if (h.jong != -1) {
                        // if complex jong
                        int simplifiedJong = HangulAssembler.decomposeComplexJong(h.jong);
                        if (simplifiedJong != -1) {
                            // complex jong: remove the last part
                            h.jong = simplifiedJong;
                            char newChar = h.toChar();
                            inputField.writeText(String.valueOf(newChar));
                            currentHangul = h;
                        } else {
                            // simple jong: remove it entirely
                            h.jong = -1;
                            char newChar = h.toChar();
                            inputField.writeText(String.valueOf(newChar));
                            currentHangul = h;
                        }
                    } else if (h.jung != -1) {
                        // remove jung, leave cho
                        char choChar = CHO.charAt(h.cho);
                        inputField.writeText(String.valueOf(choChar));
                        currentHangul.clear();
                        currentHangul.cho = h.cho;
                    } else {
                        // only cho left, already removed
                        currentHangul.clear();
                    }
                    composing = true;
                    ci.cancel();
                    return;
                }
            }

            currentHangul.clear();
            composing = false;
            return;
        }

        if (!Character.isLetter(typedChar)) {
            currentHangul.clear();
            composing = false;
            return;
        }

        if (Character.isLetter(typedChar)) {
            String text = inputField.getText();
            int cursor = inputField.getCursorPosition();

            if (composing && cursor > 0 && shouldReplaceLastChar(typedChar)) {
                // only remove if modifying the same syllable
                text = text.substring(0, cursor - 1) + text.substring(cursor);
                inputField.setText(text);
                inputField.setCursorPosition(cursor - 1);
            }

            boolean handled = processKoreanInput(typedChar);
            if (handled) {
                ci.cancel();
            }
        }
    }

    private boolean shouldReplaceLastChar(char key) {
        // only replace if adding to current syllable
        if (currentHangul.cho != -1 && currentHangul.jung == -1) {
            // have consonant: check if this is a vowel
            return HangulAssembler.getJungIndex(key) != -1;
        } else if (currentHangul.cho != -1 && currentHangul.jung != -1 && currentHangul.jong == -1) {
            // have consonant+vowel: check if this is a final consonant OR combinable vowel
            int jungIdx = HangulAssembler.getJungIndex(key);
            if (jungIdx != -1) {
                // check if vowels can combine
                return HangulAssembler.combineVowels(currentHangul.jung, jungIdx) != -1;
            }
            return HangulAssembler.getJongIndex(key) > 0;
        } else if (currentHangul.cho != -1 && currentHangul.jung != -1 && currentHangul.jong != -1) {
            // have complete syllable: check if this is combinable jong only
            int jongIdx = HangulAssembler.getJongIndex(key);
            if (jongIdx > 0) {
                return HangulAssembler.combineJong(currentHangul.jong, jongIdx) != -1;
            }
        }
        return false;
    }

    private boolean processKoreanInput(char key) {
        int choIdx = HangulAssembler.getChoIndex(key);
        int jungIdx = HangulAssembler.getJungIndex(key);
        int jongIdx = HangulAssembler.getJongIndex(key);

        if (currentHangul.cho == -1) {
            // starting new character
            if (choIdx != -1) {
                currentHangul.cho = choIdx;
                char jamo = HangulAssembler.convertKeyToKorean(key);
                inputField.writeText(String.valueOf(jamo));
                composing = true;
                return true;
            } else if (jungIdx != -1) {
                // starting with vowel
                char jamo = HangulAssembler.convertKeyToKorean(key);
                inputField.writeText(String.valueOf(jamo));
                currentHangul.clear();
                composing = false;
                return true;
            }
        } else if (currentHangul.jung == -1) {
            // have consonant: expecting vowel
            if (jungIdx != -1) {
                currentHangul.jung = jungIdx;
                char korean = currentHangul.toChar();
                inputField.writeText(String.valueOf(korean));
                composing = true;
                return true;
            } else {
                // not a vowel, start new character
                composing = false;
                currentHangul.clear();
                return processKoreanInput(key);
            }
        } else if (currentHangul.jong == -1) {
            // have consonant+vowel: maybe add final consonant or combine vowels
            if (jungIdx != -1) {
                // check if can combine vowels
                int combinedJung = HangulAssembler.combineVowels(currentHangul.jung, jungIdx);
                if (combinedJung != -1) {
                    currentHangul.jung = combinedJung;
                    char korean = currentHangul.toChar();
                    inputField.writeText(String.valueOf(korean));
                    composing = true;
                    return true;
                } else {
                    // can't combine: start new syllable
                    composing = false;
                    currentHangul.clear();
                    return processKoreanInput(key);
                }
            } else if (jongIdx > 0) {
                currentHangul.jong = jongIdx;
                char korean = currentHangul.toChar();
                inputField.writeText(String.valueOf(korean));
                composing = true;
                return true;
            } else {
                // start new syllable
                composing = false;
                currentHangul.clear();
                return processKoreanInput(key);
            }
        } else {
            // complete syllable with jong
            if (jongIdx > 0) {
                // try to combine jong
                int combinedJong = HangulAssembler.combineJong(currentHangul.jong, jongIdx);
                if (combinedJong != -1) {
                    currentHangul.jong = combinedJong;
                    char korean = currentHangul.toChar();
                    inputField.writeText(String.valueOf(korean));
                    composing = true;
                    return true;
                }
            }

            if (jungIdx != -1) {
                // vowel typed after complete syllable: steal the jong
                int stolenCho = HangulAssembler.jongToCho(currentHangul.jong);

                if (stolenCho != -1) {
                    // simple jong that can be converted to cho
                    String text = inputField.getText();
                    int cursor = inputField.getCursorPosition();

                    text = text.substring(0, cursor - 1) + text.substring(cursor);
                    inputField.setText(text);
                    inputField.setCursorPosition(cursor - 1);

                    currentHangul.jong = -1;
                    char korean = currentHangul.toChar();
                    inputField.writeText(String.valueOf(korean));

                    currentHangul.clear();
                    currentHangul.cho = stolenCho;
                    currentHangul.jung = jungIdx;
                    korean = currentHangul.toChar();
                    inputField.writeText(String.valueOf(korean));
                    composing = true;
                    return true;
                } else {
                    // complex jong - need to decompose
                    int simplifiedJong = HangulAssembler.decomposeComplexJong(currentHangul.jong);
                    if (simplifiedJong != -1) {
                        // decompose complex jong and steal the second part
                        // ㄶ -> ㄴ (keep) + ㅎ (steal)
                        // ㄺ -> ㄹ (keep) + ㄱ (steal)
                        int secondPart = getSecondPartOfComplexJong(currentHangul.jong);
                        if (secondPart != -1) {
                            String text = inputField.getText();
                            int cursor = inputField.getCursorPosition();

                            text = text.substring(0, cursor - 1) + text.substring(cursor);
                            inputField.setText(text);
                            inputField.setCursorPosition(cursor - 1);

                            currentHangul.jong = simplifiedJong;
                            char korean = currentHangul.toChar();
                            inputField.writeText(String.valueOf(korean));

                            currentHangul.clear();
                            currentHangul.cho = secondPart;
                            currentHangul.jung = jungIdx;
                            korean = currentHangul.toChar();
                            inputField.writeText(String.valueOf(korean));
                            composing = true;
                            return true;
                        }
                    }
                }
            }
            // start new syllable
            composing = false;
            currentHangul.clear();
            return processKoreanInput(key);
        }

        return false;
    }

    private int getSecondPartOfComplexJong(int jongIdx) {
        switch(jongIdx) {
            case 3: return 9;   // ㄳ -> ㅅ becomes ㅅ cho
            case 5: return 12;  // ㄵ -> ㅈ becomes ㅈ cho
            case 6: return 18;  // ㄶ -> ㅎ becomes ㅎ cho
            case 9: return 0;   // ㄺ -> ㄱ becomes ㄱ cho
            case 10: return 6;  // ㄻ -> ㅁ becomes ㅁ cho
            case 11: return 7;  // ㄼ -> ㅂ becomes ㅂ cho
            case 12: return 9;  // ㄽ -> ㅅ becomes ㅅ cho
            case 13: return 16; // ㄾ -> ㅌ becomes ㅌ cho
            case 14: return 17; // ㄿ -> ㅍ becomes ㅍ cho
            case 15: return 18; // ㅀ -> ㅎ becomes ㅎ cho
            case 18: return 9;  // ㅄ -> ㅅ becomes ㅅ cho
        }
        return -1;
    }
}