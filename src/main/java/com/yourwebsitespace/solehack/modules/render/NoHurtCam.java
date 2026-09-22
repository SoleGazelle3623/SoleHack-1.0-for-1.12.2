package com.yourwebsitespace.solehack.modules.render;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class NoHurtCam extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    public NoHurtCam() {
        super("NoHurtCam", Category.RENDER);
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.player == null || mc.world == null || event.phase != TickEvent.Phase.START) {
            return;
        }

        if (!this.isEnabled()) {
            return;
        }

        // Check if the game engine has initialized a damage animation frame
        if (mc.player.hurtTime > 0) {
            // Forcefully zero out the hurt indicators to stop the camera from shaking
            mc.player.hurtTime = 0;
            mc.player.maxHurtTime = 0;
            mc.player.attackedAtYaw = 0.0f;
        }
    }

    @Override
    protected void onUpdate() {}
}
