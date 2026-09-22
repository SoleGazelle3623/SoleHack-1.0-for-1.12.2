package com.yourwebsitespace.solehack.modules.combat;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AutoMend extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    // Configurable thresholds for 5b5t pvp
    public double repairThresholdPercent = 65.0; // Starts mending if durability drops below 65%
    public double safetyMaxPercent = 95.0;       // Stops mending at 95% to prevent over-splashing waste
    public boolean lockRotations = true;          // Silently forces looking down so XP hits your feet perfectly

    private boolean isMending = false;

    public AutoMend() {
        super("AutoMend", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
        isMending = false;
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
        isMending = false;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.player == null || mc.world == null || event.phase != TickEvent.Phase.START) {
            return;
        }

        if (!this.isEnabled()) {
            isMending = false;
            return;
        }

        // Evaluate overall lowest armor piece status percentage
        double lowestArmorDurability = getLowestArmorPercentage();

        // Condition Check: Trigger mending engine loop states safely
        if (!isMending && lowestArmorDurability < repairThresholdPercent && lowestArmorDurability > 0) {
            isMending = true;
        }

        // Condition Check: Terminate if armor reaches max configuration goal or runs out
        if (isMending && (lowestArmorDurability >= safetyMaxPercent || lowestArmorDurability <= 0)) {
            isMending = false;
            return;
        }

        if (isMending) {
            int xpSlot = findXpBottleSlot();
            if (xpSlot == -1) {
                isMending = false; // Stop if completely out of Experience Bottles
                return;
            }

            int currentSlot = mc.player.inventory.currentItem;

            // 1. Silent Rotation down at your feet to maximize mending collection speed
            if (lockRotations) {
                // Pitch 90.0f forces looking completely vertical into your player model's shoes
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, 90.0f, mc.player.onGround));
            }

            // 2. Ghost Hand Packet Swapping to fire layout intervals instantly
            mc.player.connection.sendPacket(new CPacketHeldItemChange(xpSlot));

            // 3. Dispatch the action use packet stream right into the server
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
            mc.player.swingArm(EnumHand.MAIN_HAND);

            // 4. Return to your original weapon or item profile index
            mc.player.connection.sendPacket(new CPacketHeldItemChange(currentSlot));
        }
    }

    private double getLowestArmorPercentage() {
        double minPercent = 100.0;
        boolean hasArmor = false;

        // Iterate through all 4 armor gear array elements equipped on your player model
        for (ItemStack armorStack : mc.player.getArmorInventoryList()) {
            if (armorStack.isEmpty()) continue;
            hasArmor = true;

            double maxDamage = armorStack.getMaxDamage();
            double currentDamage = armorStack.getItemDamage();
            double durabilityPercent = ((maxDamage - currentDamage) / maxDamage) * 100.0;

            if (durabilityPercent < minPercent) {
                minPercent = durabilityPercent;
            }
        }
        return hasArmor ? minPercent : 0.0;
    }

    private int findXpBottleSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == Items.EXPERIENCE_BOTTLE) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void onUpdate() {}
}
