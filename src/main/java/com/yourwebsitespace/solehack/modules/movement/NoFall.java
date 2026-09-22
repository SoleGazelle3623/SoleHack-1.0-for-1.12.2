package com.yourwebsitespace.solehack.modules.movement;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class NoFall extends Module {

    public static NoFall INSTANCE;
    private final Minecraft mc = Minecraft.getMinecraft();

    public Mode mode = Mode.PACKET;

    public enum Mode {
        PACKET,  // Standard packet ground-spoofing
        CATCH    // Triggers right before hitting the ground (more subtle)
    }

    public NoFall() {
        super("NoFall", Category.MOVEMENT);
        INSTANCE = this;
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

        if (!this.isEnabled() || mc.player.isCreative() || mc.player.isSpectator()) {
            return;
        }

        // Only process when falling significantly
        if (mc.player.fallDistance > 2.0f) {
            if (mode == Mode.PACKET) {
                // Force onGround state packet to mitigate server-side fall distance calculation
                mc.player.connection.sendPacket(new CPacketPlayer(true));
            } else if (mode == Mode.CATCH) {
                // Send ground spoof packet only when close to collision
                if (mc.world.collidesWithAnyBlock(mc.player.getEntityBoundingBox().offset(0, -0.5, 0))) {
                    mc.player.connection.sendPacket(new CPacketPlayer(true));
                    mc.player.fallDistance = 0;
                }
            }
        }
    }

    @Override
    protected void onUpdate() {}
}