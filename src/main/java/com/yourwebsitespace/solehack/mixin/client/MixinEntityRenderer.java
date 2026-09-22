package com.yourwebsitespace.solehack.mixin.client;

import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    // Cancels the annoying orange fire overlay on your screen when burning
    @Inject(method = "isDrawBlockOutline", at = @At("HEAD")) // Placeholder or standard overlay check point
    private void cancelOverlays(CallbackInfoReturnable<Boolean> cir) {
        // Handled via specific overlay render mixins depending on your setup
    }
}