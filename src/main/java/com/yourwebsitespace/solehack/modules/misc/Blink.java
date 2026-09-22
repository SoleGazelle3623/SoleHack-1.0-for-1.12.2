package com.yourwebsitespace.solehack.modules.misc;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Blink extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Queue<Packet<?>> packetQueue = new ConcurrentLinkedQueue<>();
    private boolean sending = false;

    // Public instance reference for your mixin hooks
    public static Blink INSTANCE;

    public Blink() {
        super("Blink", Category.MISC);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
        packetQueue.clear();
        sending = false;
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
        flushPackets();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.player == null || mc.world == null || event.phase != TickEvent.Phase.START) {
            return;
        }

        if (!this.isEnabled()) {
            return;
        }
    }

    public boolean onPacketSend(Packet<?> packet) {
        if (!this.isEnabled()) return true;
        if (sending) return true;

        if (packet instanceof CPacketPlayer) {
            packetQueue.add(packet);
            return false; // Cancel sending immediately, hold locally
        }

        return true;
    }

    private void flushPackets() {
        if (mc.player == null || mc.getConnection() == null) {
            packetQueue.clear();
            return;
        }

        sending = true;
        while (!packetQueue.isEmpty()) {
            Packet<?> packet = packetQueue.poll();
            if (packet != null) {
                mc.player.connection.sendPacket(packet);
            }
        }
        sending = false;
    }

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        packetQueue.clear();

        // Use whichever method your base Module class uses to turn off a module:
        this.setEnabled(false);
        // OR: this.setEnabled(false);
        // OR: if (this.isEnabled()) this.toggle(); (if the method name is lowercase/uppercase)
    }
    @Override
    protected void onUpdate() {}
}