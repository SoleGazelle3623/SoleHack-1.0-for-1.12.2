package com.yourwebsitespace.solehack.modules.misc;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.util.UUID;

public class FakePlayer extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();
    private EntityOtherPlayerMP fakePlayerEntity = null;

    private static final int FAKE_PLAYER_ID = -1337;
    private boolean lastState = false;

    public FakePlayer() {
        // UPDATED: Category mapped cleanly to MISC enum definition
        super("FakePlayer", Category.MISC);
    }

    // Standard client execution override hook
    public void onEnable() {
        spawnDummy();
    }

    @Override
    protected void onUpdate() {

    }

    // Standard client removal override hook
    public void onDisable() {
        despawnDummy();
    }

    private void spawnDummy() {
        if (mc.player == null || mc.world == null || fakePlayerEntity != null) {
            return;
        }

        GameProfile profile = new GameProfile(UUID.fromString("b3c4d5e6-a7b8-9c0d-1e2f-3a4b5c6d7e8f"), "SoleHack_Testing");
        fakePlayerEntity = new EntityOtherPlayerMP(mc.world, profile);

        fakePlayerEntity.copyLocationAndAnglesFrom(mc.player);
        fakePlayerEntity.inventory.copyInventory(mc.player.inventory);
        fakePlayerEntity.setHealth(20.0f);

        mc.world.addEntityToWorld(FAKE_PLAYER_ID, fakePlayerEntity);
    }

    private void despawnDummy() {
        if (mc.world != null && fakePlayerEntity != null) {
            mc.world.removeEntityFromWorld(FAKE_PLAYER_ID);
            fakePlayerEntity = null;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.player == null || mc.world == null || event.phase != TickEvent.Phase.START) {
            return;
        }

        // ALTERNATIVE STATE MANAGER SAFETY LAYER:
        // If your framework doesn't call onEnable/onDisable automatically,
        // this loop detects toggles via your base module status flag.
        boolean currentState = this.isEnabled(); // Replace with this.isEnabled() or this.toggled if needed
        if (currentState != lastState) {
            if (currentState) {
                spawnDummy();
            } else {
                despawnDummy();
            }
            lastState = currentState;
        }

        // Automatic world-change safe disposal tracking
        if (fakePlayerEntity != null && !mc.world.loadedEntityList.contains(fakePlayerEntity)) {
            fakePlayerEntity = null;
            lastState = false;
        }
    }
}
