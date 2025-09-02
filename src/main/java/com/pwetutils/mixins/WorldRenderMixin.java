package com.pwetutils.mixins;

import com.pwetutils.listener.HologramImageListener;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class WorldRenderMixin {
    @Inject(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;disableFog()V", shift = At.Shift.BEFORE))
    private void onRenderWorld(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        HologramImageListener.renderHologram(partialTicks);
    }
}