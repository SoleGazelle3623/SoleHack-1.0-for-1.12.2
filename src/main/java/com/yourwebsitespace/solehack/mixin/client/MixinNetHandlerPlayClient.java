package com.yourwebsitespace.solehack.mixin.client;

import com.yourwebsitespace.solehack.modules.misc.Blink;
import com.yourwebsitespace.solehack.modules.misc.PacketLimiter;
import com.yourwebsitespace.solehack.modules.movement.Velocity;
import com.yourwebsitespace.solehack.modules.render.TotemPopVisual;
import com.yourwebsitespace.solehack.modules.exploits.AntiHunger;
import com.yourwebsitespace.solehack.modules.exploits.ColorSigns;
import com.yourwebsitespace.solehack.modules.exploits.XCarry;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {

    @Inject(method = "handleEntityStatus", at = @At("HEAD"))
    private void onHandleEntityStatus(SPacketEntityStatus packetIn, CallbackInfo ci) {
        if (TotemPopVisual.INSTANCE != null) {
            TotemPopVisual.INSTANCE.onPacketReceive(packetIn);
        }
    }

    @Inject(method = "handleEntityVelocity", at = @At("HEAD"), cancellable = true)
    private void onHandleEntityVelocity(SPacketEntityVelocity packetIn, CallbackInfo ci) {
        if (Velocity.INSTANCE != null && Velocity.INSTANCE.onPacketReceive(packetIn)) {
            ci.cancel();
        }
    }

    @Inject(method = "handleExplosion", at = @At("HEAD"), cancellable = true)
    private void onHandleExplosion(SPacketExplosion packetIn, CallbackInfo ci) {
        if (Velocity.INSTANCE != null && Velocity.INSTANCE.onPacketReceive(packetIn)) {
            ci.cancel();
        }
    }
    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        // Blink check
        if (Blink.INSTANCE != null && Blink.INSTANCE.isEnabled()) {
            if (!Blink.INSTANCE.onPacketSend(packet)) {
                ci.cancel();
            }
        }

        // XCarry check (cancels closing any window so crafting grid items stay locked)
        if (XCarry.INSTANCE != null && XCarry.INSTANCE.isEnabled()) {
            if (packet instanceof net.minecraft.network.play.client.CPacketCloseWindow) {
                ci.cancel();
            }
        }
        // PacketLimiter check
        if (PacketLimiter.INSTANCE != null && PacketLimiter.INSTANCE.isEnabled()) {
            if (!PacketLimiter.INSTANCE.onPacketSend(packet)) {
                ci.cancel();
            }
        }

        // AntiHunger check
        if (AntiHunger.INSTANCE != null && AntiHunger.INSTANCE.isEnabled()) {
            if (!AntiHunger.INSTANCE.onPacketSend(packet)) {
                ci.cancel();
            }
        }

        // ColorSigns check
        if (ColorSigns.INSTANCE != null && ColorSigns.INSTANCE.isEnabled()) {
            if (!ColorSigns.INSTANCE.onPacketSend(packet)) {
                ci.cancel();
            }
        }

        // Chat command check
        if (packet instanceof net.minecraft.network.play.client.CPacketChatMessage) {
            net.minecraft.network.play.client.CPacketChatMessage messagePacket = (net.minecraft.network.play.client.CPacketChatMessage) packet;

            // Safety check: Only process if the CommandManager actually exists
            if (com.yourwebsitespace.solehack.command.CommandManager.INSTANCE != null) {
                if (com.yourwebsitespace.solehack.command.CommandManager.INSTANCE.processCommand(messagePacket.getMessage())) {
                    ci.cancel();
                }
            }
        }}}