package com.yourwebsitespace.solehack.mixin.mixins;

import com.yourwebsitespace.solehack.modules.combat.CrystalAura;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Shadow
    public int rightClickDelayTimer;

    @Inject(method = "rightClickMouse", at = @At("HEAD"))
    private void onRightClickMouse(CallbackInfo info) {
        if (CrystalAura.INSTANCE != null && CrystalAura.INSTANCE.isEnabled()) {
            this.rightClickDelayTimer = 0;
        }
    }
}