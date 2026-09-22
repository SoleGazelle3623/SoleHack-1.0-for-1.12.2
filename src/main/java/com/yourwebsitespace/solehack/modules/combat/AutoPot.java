package com.yourwebsitespace.solehack.modules.combat;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemSplashPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;

public class AutoPot extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    // 5b5t Optimized default settings
    public double healthThreshold = 14.0; // Automatically splashes when health drops below 7 hearts (14 hp)
    public boolean lockRotations = true;   // Silently forces the client to look down to splash perfectly at your feet
    public long potDelayMs = 120;          // Precision timing delay between pot throws (prevents potion spam waste)

    private long lastPotTime = 0L;

    public AutoPot() {
        super("AutoPot", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
        lastPotTime = 0L;
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

        // Check if player health + absorption hearts drops below threshold limit
        float playerHealth = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        if (playerHealth >= healthThreshold) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        // Hardware timer throttle to ensure stable consumption spacing
        if (currentTime - lastPotTime < potDelayMs) {
            return;
        }

        int potSlot = findHealingPotionSlot();
        if (potSlot == -1) {
            return; // Out of health potions
        }

        int currentSlot = mc.player.inventory.currentItem;

        // 1. Silent Rotation down at your physical feet to maximize absorption rates
        if (lockRotations) {
            // Pitch 90.0f forces look straight down vertically into your player model's hitbox coordinates
            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, 90.0f, mc.player.onGround));
        }

        // 2. Ghost Hand Packet Swapping to prevent inventory flickering animation blocks
        mc.player.connection.sendPacket(new CPacketHeldItemChange(potSlot));

        // 3. Dispatch the instantaneous item use execution packet
        mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
        mc.player.swingArm(EnumHand.MAIN_HAND);

        // 4. Return to your original primary weapon/item index profile instantly
        mc.player.connection.sendPacket(new CPacketHeldItemChange(currentSlot));

        // Update the timestamp tracker
        lastPotTime = currentTime;
    }

    private int findHealingPotionSlot() {
        // Retrieve the standard Instant Health potion instance cleanly (ID 6 in 1.12.2)
        Potion instantHealth = Potion.getPotionById(6);
        if (instantHealth == null) return -1;

        // Scan the player's 9 hotbar slots exclusively
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            // Ensure the item is explicitly a Splash Potion type
            if (!stack.isEmpty() && stack.getItem() instanceof ItemSplashPotion) {
                List<PotionEffect> effects = PotionUtils.getEffectsFromStack(stack);

                for (PotionEffect effect : effects) {
                    // Check if any active effect matches our Instant Health pointer criteria
                    if (effect.getPotion() == instantHealth) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }


    @Override
    protected void onUpdate() {}
}
