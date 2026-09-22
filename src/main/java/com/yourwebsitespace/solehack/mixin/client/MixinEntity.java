package com.yourwebsitespace.solehack.mixin.client;

import com.yourwebsitespace.solehack.modules.movement.Velocity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Inject(method = "applyEntityCollision", at = @At("HEAD"), cancellable = true)
    private void onApplyEntityCollision(Entity entityIn, CallbackInfo ci) {
        if (Velocity.INSTANCE != null && Velocity.INSTANCE.isEnabled() && Velocity.INSTANCE.noPush) {
            ci.cancel();
        }
    }
}