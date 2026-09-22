package com.yourwebsitespace.solehack.mixin.client;

import com.yourwebsitespace.solehack.modules.misc.MultiTask;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {

    @Inject(method = "isHittingBlock", at = @At("HEAD"), cancellable = true)
    private void onIsHittingBlock(CallbackInfoReturnable<Boolean> cir) {
        if (MultiTask.INSTANCE != null && MultiTask.INSTANCE.isEnabled()) {
            // Trick the game into thinking you aren't currently locked to block mining,
            // allowing you to use items (eat/drink) at the same time.
            cir.setReturnValue(false);
        }
    }
}