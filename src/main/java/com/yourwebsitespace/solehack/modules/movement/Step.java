package com.yourwebsitespace.solehack.modules.movement;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;

public class Step extends Module {

    public static Step INSTANCE;
    private final Minecraft mc = Minecraft.getMinecraft();

    public float height = 1.9f;
    private float oldStepHeight = 0.6f;

    public Step() {
        super("Step", Category.MOVEMENT);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            oldStepHeight = mc.player.stepHeight;
            mc.player.stepHeight = height;
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.stepHeight = oldStepHeight;
        }
    }

    @Override
    public void onUpdate() {
        if (mc.player == null) return;

        // Keep stepHeight applied if changed by water/effects
        if (mc.player.stepHeight != height) {
            mc.player.stepHeight = height;
        }
    }
}