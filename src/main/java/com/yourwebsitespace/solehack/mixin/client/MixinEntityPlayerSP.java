package com.yourwebsitespace.solehack.mixin.client;

import com.yourwebsitespace.solehack.modules.movement.Step;
import com.yourwebsitespace.solehack.modules.movement.Velocity;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP {

    // 1. Velocity: Prevents being pushed out of blocks / suffocation
    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    private void onPushOutOfBlocks(double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (Velocity.INSTANCE != null && Velocity.INSTANCE.isEnabled() && Velocity.INSTANCE.blocks) {
            cir.setReturnValue(false);
        }
    }

    // 2. Step: Dynamically updates your step height every tick
    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"))
    private void onUpdateWalkingPlayerHead(CallbackInfo ci) {
        if (Step.INSTANCE != null && Step.INSTANCE.isEnabled()) {
            EntityPlayerSP player = (EntityPlayerSP) (Object) this;
            player.stepHeight = Step.INSTANCE.height;
        }
    }
}