package com.yourwebsitespace.solehack.modules.misc;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Random;

public class Announcer extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Random random = new Random();

    // 5b5t Anti-Spam Safety Settings
    public long messageDelayMs = 15000; // Limits announcements to once every 15 seconds to prevent spam kicks
    public double walkThresholdBlocks = 50.0; // Distance required to announce movement

    private long lastMessageTime = 0L;

    // Movement distance tracking variables
    private double lastX = 0.0;
    private double lastZ = 0.0;
    private double distanceWalked = 0.0;

    public Announcer() {
        super("Announcer", Category.MISC);
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
        lastMessageTime = 0L;
        distanceWalked = 0.0;
        if (mc.player != null) {
            lastX = mc.player.posX;
            lastZ = mc.player.posZ;
        }
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

        // Initialize coordinates if they reset
        if (lastX == 0.0 && lastZ == 0.0) {
            lastX = mc.player.posX;
            lastZ = mc.player.posZ;
            return;
        }

        // Calculate horizontal movement delta values
        double deltaX = mc.player.posX - lastX;
        double deltaZ = mc.player.posZ - lastZ;
        distanceWalked += Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        lastX = mc.player.posX;
        lastZ = mc.player.posZ;

        // Trigger movement announcement if the accumulation crosses the parameter goal threshold
        if (distanceWalked >= walkThresholdBlocks) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastMessageTime >= messageDelayMs) {

                String[] walkMessages = new String[] {
                        "I just traveled " + String.format("%.1f", distanceWalked) + " blocks thanks to SoleHack!",
                        "Walking around with SoleHack's peak movement modules!",
                        "Just cleared another " + String.format("%.1f", distanceWalked) + " blocks smoothly."
                };

                sendAnnouncerMessage(walkMessages[random.nextInt(walkMessages.length)]);
                distanceWalked = 0.0;
                lastMessageTime = currentTime;
            }
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (mc.player == null || !this.isEnabled() || event.getPlayer() != mc.player) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMessageTime >= messageDelayMs) {
            String blockName = event.getState().getBlock().getLocalizedName();

            String[] breakMessages = new String[] {
                    "SoleHack helped me tear down that " + blockName + " block effortlessly!",
                    "Just destroyed a block of " + blockName + " at high packet speed.",
                    "Mining blocks with raw exploit optimization!"
            };

            sendAnnouncerMessage(breakMessages[random.nextInt(breakMessages.length)]);
            lastMessageTime = currentTime;
        }
    }

    @SubscribeEvent
    public void onItemUse(PlayerInteractEvent.RightClickItem event) {
        if (mc.player == null || !this.isEnabled() || event.getEntityPlayer() != mc.player) {
            return;
        }

        if (event.getHand() != EnumHand.MAIN_HAND) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMessageTime >= messageDelayMs) {
            ItemStack stack = event.getItemStack();
            if (stack.isEmpty()) return;

            // Only announce consumables like food or potions to prevent right-click item chatter spam
            if (stack.getItem() instanceof net.minecraft.item.ItemFood || stack.getItem() instanceof net.minecraft.item.ItemPotion) {
                String itemName = stack.getDisplayName();

                String[] useMessages = new String[] {
                        "Gulp! Just consumed some " + itemName + " to sustain my health totals.",
                        "Using " + itemName + " mid-combat using SoleHack configurations.",
                        "Refreshing my active status effects with some " + itemName + "."
                };

                sendAnnouncerMessage(useMessages[random.nextInt(useMessages.length)]);
                lastMessageTime = currentTime;
            }
        }
    }

    private void sendAnnouncerMessage(String rawText) {
        // Formats message and prevents it from overstepping vanilla length bounds
        String finalMessage = "[SoleHack] " + rawText;
        if (finalMessage.length() > 256) {
            finalMessage = finalMessage.substring(0, 256);
        }

        // Send down the server's network chat channel stream
        mc.player.sendChatMessage(finalMessage);
    }

    @Override
    protected void onUpdate() {}
}
