package com.yourwebsitespace.solehack.mixin.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketUseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {


    @Inject(method = "sendPacket", at = @At("HEAD"))
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof CPacketUseEntity) {
            CPacketUseEntity useEntity = (CPacketUseEntity) packet;

            if (useEntity.getAction() == CPacketUseEntity.Action.ATTACK) {
                Minecraft mc = Minecraft.getMinecraft();
                if (mc.world != null) {
                    Entity entity = useEntity.getEntityFromWorld(mc.world);
                    if (entity instanceof EntityEnderCrystal) {
                        entity.setDead(); // Fast despawn prediction
                    }
                }
            }
        }
    }
}