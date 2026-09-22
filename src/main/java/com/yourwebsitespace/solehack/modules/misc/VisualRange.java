package com.yourwebsitespace.solehack.modules.misc; // FIXED: Matches your actual 'misc' folder path perfectly

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VisualRange extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    // Configuration Settings
    public boolean playSoundAlert = true;

    // Cache to track players currently inside your local grid matrix
    private final List<UUID> knownPlayers = new ArrayList<>();

    public VisualRange() {
        super("VisualRange", Category.MISC);
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
        knownPlayers.clear();
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
        knownPlayers.clear();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.player == null || mc.world == null || event.phase != TickEvent.Phase.START) {
            return;
        }

        if (!this.isEnabled()) {
            return;
        }

        List<UUID> currentPlayersInRender = new ArrayList<>();

        // 1. SCAN PHASE: Check all active player entities currently loaded in the world chunk grid
        for (EntityPlayer player : mc.world.playerEntities) {
            if (player == mc.player || player.isDead || player.getHealth() <= 0) {
                continue; // Skip ourselves and non-active targets
            }

            UUID playerUUID = player.getUniqueID();
            currentPlayersInRender.add(playerUUID);

            // If a player UUID is NOT in our historical memory list, they just entered your visual range!
            if (!knownPlayers.contains(playerUUID)) {
                knownPlayers.add(playerUUID);
                logAlert(player.getName(), true); // Line 69 FIXED: Successfully passes boolean state data
            }
        }

        // 2. SCRUB PHASE: Identify players who left your chunk range boundaries
        knownPlayers.removeIf(uuid -> {
            if (!currentPlayersInRender.contains(uuid)) {
                // Fetch name mapping out of the engine's player cache before wiping it
                EntityPlayer offlinePlayer = mc.world.getPlayerEntityByUUID(uuid);
                String name = (offlinePlayer != null) ? offlinePlayer.getName() : "A player";

                logAlert(name, false); // Line 81 FIXED: Successfully passes boolean state data
                return true; // Removes from tracking array cleanly
            }
            return false;
        });
    }

    // FIXED: Swapped parameter 'String entering' to 'boolean entering' on Line 92 to resolve incompatible type crashes
    private void logAlert(String name, boolean entering) {
        String message;

        // Build beautiful client-side custom text components
        if (entering) {
            message = TextFormatting.GREEN + name + TextFormatting.GRAY + " has entered your visual range!";

            // Play a crisp vanilla notification chime locally to give an instant audio alert
            if (playSoundAlert) {
                mc.world.playSound(mc.player.posX, mc.player.posY, mc.player.posZ,
                        SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.0f, false);
            }
        } else {
            message = TextFormatting.RED + name + TextFormatting.GRAY + " has left your visual range.";
        }

        // Print the alert directly into your private chat screen GUI (visible ONLY to you)
        mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(
                TextFormatting.DARK_RED + "[SoleHack] " + TextFormatting.RESET + message));
    }

    @Override
    protected void onUpdate() {}
}
