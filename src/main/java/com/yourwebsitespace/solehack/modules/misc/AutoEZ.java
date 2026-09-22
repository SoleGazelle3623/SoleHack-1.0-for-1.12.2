package com.yourwebsitespace.solehack.modules.misc;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoEZ extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    // Customization fields
    public String message = "You just got Nae Nae'd by SoleHack";
    public double trackingRange = 10.0;

    public AutoEZ() {
        super("AutoEZ", Category.MISC);
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
    public void onDeath(LivingDeathEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (!this.isEnabled()) return;

        // Check if the entity that died is a player
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer deadPlayer = (EntityPlayer) event.getEntityLiving();

            // Make sure it's not you
            if (deadPlayer == mc.player) return;

            // Verify you were close enough
            if (mc.player.getDistance(deadPlayer) <= trackingRange) {
                sendAutoEzMessage(deadPlayer.getName());
            }
        }
    }

    private void sendAutoEzMessage(String playerName) {
        String finalMessage = message.replace("{player}", playerName);
        mc.player.connection.sendPacket(new CPacketChatMessage(finalMessage));
    }

    @Override
    protected void onUpdate() {}
}