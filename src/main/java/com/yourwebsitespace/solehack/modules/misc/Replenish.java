package com.yourwebsitespace.solehack.modules.misc;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;

public class Replenish extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    // 5b5t Optimization Settings
    public int threshold = 8;        // Refills a stack when it drops below this number
    public int delayTicksSetting = 2; // Delays inventory actions to prevent anti-cheat inventory kicks

    private int delayTicks = 0;
    // Map to cache what item type originally belonged in each hotbar slot index
    private final Map<Integer, ItemStack> hotbarCache = new HashMap<>();

    public Replenish() {
        super("Replenish", Category.MISC);
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
        delayTicks = 0;
        hotbarCache.clear();
        if (mc.player != null) {
            cacheHotbar();
        }
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
        hotbarCache.clear();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.player == null || mc.world == null || event.phase != TickEvent.Phase.START) {
            return;
        }

        if (!this.isEnabled()) {
            return;
        }

        // Bypasses execution if the player is currently sorting items inside a chest/container GUI manually
        if (mc.currentScreen instanceof GuiContainer) {
            return;
        }

        // Action timer to throttle inventory click transactions safely
        if (delayTicks > 0) {
            delayTicks--;
            return;
        }

        // Continually keep our master template cache updated if slots hold fresh full stacks
        cacheHotbar();

        // Scan the 9 hotbar slot allocations exclusively
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            // Skip processing if slot is completely empty and we have no historical memory of what belonged there
            if (stack.isEmpty() && (!hotbarCache.containsKey(i) || hotbarCache.get(i).isEmpty())) {
                continue;
            }

            // Check if the current stack count has dropped below our configured threshold limit
            if (stack.getCount() < threshold || stack.isEmpty()) {
                ItemStack targetTemplate = hotbarCache.get(i);
                if (targetTemplate == null || targetTemplate.isEmpty()) continue;

                // Search the player's primary upper main inventory matrix for an identical replacement item
                int inventorySlot = findReplacementSlot(targetTemplate);

                if (inventorySlot != -1) {
                    // Convert the target container indices cleanly to match vanilla window inventory slot mappings
                    // In 1.12.2, Minecraft inventory tracking slots run from index 9 to 35, while hotbar runs 0-8
                    int mcInventorySlot = inventorySlot < 9 ? inventorySlot + 36 : inventorySlot;

                    // FIXED: Removed the unused 'mcHotbarSlot' variable declaration to clear the linter warning completely
                    // Execute a silent window interaction transaction sequence
                    // ClickType.QUICK_MOVE (Shift+Click) transfers matching stacks directly down into the empty hotbar index
                    mc.playerController.windowClick(
                            mc.player.inventoryContainer.windowId,
                            mcInventorySlot,
                            0,
                            ClickType.QUICK_MOVE,
                            mc.player
                    );

                    // Impose an anti-kick delay buffer lock before executing subsequent hotbar checks
                    delayTicks = delayTicksSetting;
                    break;
                }
            }
        }
    }

    private void cacheHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            // Cache or update the memory map only if the slot contains a valid, non-depleted target item stack
            if (!stack.isEmpty() && stack.getCount() >= threshold) {
                hotbarCache.put(i, stack.copy());
            }
        }
    }

    private int findReplacementSlot(ItemStack template) {
        // Scan the primary main inventory storage array (Slots 9 through 35 represent upper main inventory storage)
        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == template.getItem()) {
                // Confirm metadata values match explicitly (e.g., differentiating colored Shulkers or Potion variants)
                if (stack.getItemDamage() == template.getItemDamage()) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    protected void onUpdate() {}
}
