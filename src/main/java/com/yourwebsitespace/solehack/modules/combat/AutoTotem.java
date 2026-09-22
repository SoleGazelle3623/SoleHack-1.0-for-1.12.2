package com.yourwebsitespace.solehack.modules.combat;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AutoTotem extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    // Tracks remaining totems in your inventory
    public int totemsRemaining = 0;

    public AutoTotem() {
        super("AutoTotem", Category.COMBAT);
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

        // Safety: Do not shuffle slots around if viewing an inventory chest/container screen
        if (mc.currentScreen instanceof GuiContainer) {
            return;
        }

        // Count current storage capacity metrics
        totemsRemaining = getTotemCount();

        // Check if offhand is already holding a totem
        if (mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING) {
            return;
        }

        // Find a raw replacement slot mapping
        int totemSlot = findTotemSlot();
        if (totemSlot == -1) return;

        // Execute inventory fast-click packets to swap the item into offhand (Slot 45)
        // Step 1: Click the item inside the inventory to pick it up on the cursor
        mc.playerController.windowClick(0, totemSlot, 0, ClickType.PICKUP, mc.player);

        // Step 2: Click the offhand slot (slot 45) to place the totem there
        mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);

        // Step 3: Put whatever was originally in the offhand back into the vacant slot
        mc.playerController.windowClick(0, totemSlot, 0, ClickType.PICKUP, mc.player);
    }

    private int findTotemSlot() {
        // Iterate through standard player inventory spaces (excluding armor slots)
        for (int i = 9; i < 45; i++) {
            ItemStack stack = mc.player.inventoryContainer.getSlot(i).getStack();
            if (!stack.isEmpty() && stack.getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }
        return -1;
    }

    private int getTotemCount() {
        int count = 0;
        for (int i = 0; i < 45; i++) {
            ItemStack stack = mc.player.inventoryContainer.getSlot(i).getStack();
            if (!stack.isEmpty() && stack.getItem() == Items.TOTEM_OF_UNDYING) {
                count += stack.getCount();
            }
        }
        if (mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING) {
            count += mc.player.getHeldItemOffhand().getCount();
        }
        return count;
    }

    @Override
    protected void onUpdate() {}
}
