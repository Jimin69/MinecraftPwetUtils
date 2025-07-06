package com.pwetutils.mixins;

import com.pwetutils.listener.ChatOverlayListener;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiNewChat.class)
public class ChatRenderMixin {
    @Inject(method = "drawChat", at = @At("HEAD"), cancellable = true)
    private void onDrawChat(int updateCounter, CallbackInfo ci) {
        if (ChatOverlayListener.settingsOpen) {
            ci.cancel();
        }
    }

    @Inject(method = "getChatComponent", at = @At("HEAD"), cancellable = true)
    private void onGetChatComponent(int mouseX, int mouseY, CallbackInfoReturnable<IChatComponent> cir) {
        if (ChatOverlayListener.settingsOpen) {
            cir.setReturnValue(null);
        }
    }
}