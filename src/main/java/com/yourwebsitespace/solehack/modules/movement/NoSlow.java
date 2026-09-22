package com.yourwebsitespace.solehack.modules.movement;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.MovementInput;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

public class NoSlow extends Module {

    public static NoSlow INSTANCE;
    private final Minecraft mc = Minecraft.getMinecraft();

    public boolean items = true;
    public boolean soulSand = true;
    public boolean webs = true;

    // Reflection field setup using Forge's ReflectionHelper
    // "isInWeb" = deobfuscated development name, "field_70134_J" = SRG/production name
    private final Field isInWebField = ReflectionHelper.findField(Entity.class, "isInWeb", "field_70134_J");

    public NoSlow() {
        super("NoSlow", Category.MOVEMENT);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    // Bypasses item usage slowdown (eating, drinking, bow, shield)
    @SubscribeEvent
    public void onInputUpdate(InputUpdateEvent event) {
        if (!this.isEnabled() || !items || mc.player == null) return;

        if (mc.player.isHandActive() && !mc.player.isRiding()) {
            MovementInput input = event.getMovementInput();
            input.moveStrafe *= 5.0f;
            input.moveForward *= 5.0f;
        }
    }

    @Override
    protected void onUpdate() {
        if (!this.isEnabled() || mc.player == null || mc.world == null) return;

        // Bypasses Cobweb slowdown using Reflection to bypass protected access
        if (webs) {
            try {
                if (isInWebField.getBoolean(mc.player)) {
                    isInWebField.setBoolean(mc.player, false);
                }
            } catch (Exception ignored) {
                // Safely catches reflection exceptions without crashing
            }
        }

        // Bypasses Soul Sand slowdown
        if (soulSand && mc.world.getBlockState(mc.player.getPosition()).getBlock() == Blocks.SOUL_SAND) {
            mc.player.motionX *= 2.5D;
            mc.player.motionZ *= 2.5D;
        }
    }
}