package com.pwetutils.mixins;

import com.pwetutils.settings.ModuleSettings;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiChat.class)
public class ChatLimitMixin {
    @Shadow
    protected GuiTextField inputField;

    @Inject(method = "initGui", at = @At("TAIL"))
    public void increaseChatLimit(CallbackInfo ci) {
        if (ModuleSettings.isIncreaseChatLengthEnabled()) {
            inputField.setMaxStringLength(256);
        }
    }
}