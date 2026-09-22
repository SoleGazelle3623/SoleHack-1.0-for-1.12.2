package com.yourwebsitespace.solehack.modules.movement;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Anchor extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    // 5b5t Optimization Settings
    public int pitchActivationThreshold = 80; // Only activates if you look down past this angle (prevents unintended pulls)
    public boolean pullForceful = true;       // Increases gravity velocity downward for an instant snap

    public Anchor() {
        super("Anchor", Category.MOVEMENT);
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

        // Safety verification: Pause logic if player is flying, swimming, or on a ladder
        if (mc.player.capabilities.isFlying || mc.player.isInWater() || mc.player.isInLava() || mc.player.isOnLadder()) {
            return;
        }

        // Pitch Angle Check: Only engage if the user actively looks down to enter a hole
        if (mc.player.rotationPitch < pitchActivationThreshold) {
            return;
        }

        BlockPos playerPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);

        // Scan downwards up to 3 blocks beneath the player to look for a secure defensive hole layout
        for (int i = 0; i < 3; i++) {
            BlockPos checkPos = playerPos.down(i);

            if (isValidHole(checkPos)) {
                // ANCHOR SNAP TRIGGER: Halt all horizontal momentum completely to center the player over the gap
                mc.player.motionX = 0.0;
                mc.player.motionZ = 0.0;

                if (pullForceful) {
                    // Inject a strict downward velocity vector to pull the model down quickly
                    mc.player.motionY = -0.6;
                }
                break;
            }
        }
    }

    private boolean isValidHole(BlockPos pos) {
        // The core space of the hole must be empty air
        if (!mc.world.isAirBlock(pos) || !mc.world.isAirBlock(pos.up())) {
            return false;
        }

        // Define the 5 defensive blocks enclosing the hole
        BlockPos[] sides = new BlockPos[] {
                pos.down(),
                pos.north(),
                pos.south(),
                pos.east(),
                pos.west()
        };

        for (BlockPos side : sides) {
            net.minecraft.block.Block block = mc.world.getBlockState(side).getBlock();
            // A defensive hole must be completely enclosed by obsidian or bedrock
            if (block != Blocks.OBSIDIAN && block != Blocks.BEDROCK) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onUpdate() {}
}
