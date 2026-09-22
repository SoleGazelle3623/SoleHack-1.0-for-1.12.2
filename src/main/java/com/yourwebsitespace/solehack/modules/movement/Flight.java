package com.yourwebsitespace.solehack.modules.movement;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Flight extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    public Flight() {
        super("Flight", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        // Allows the player to fly
        mc.player.capabilities.allowFlying = true;
        mc.player.capabilities.isFlying = true;
    }

    @Override
    protected void onUpdate() {

    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        // Restores normal movement state
        if (!mc.player.isCreative() && !mc.player.isSpectator()) {
            mc.player.capabilities.allowFlying = false;
            mc.player.capabilities.isFlying = false;
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (mc.player == null || !this.isEnabled()) return;

        // Keeps flight active and sets speed
        mc.player.capabilities.allowFlying = true;
        mc.player.capabilities.setFlySpeed(0.05f);
    }
}
