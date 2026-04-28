package com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacket;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacketCustomPayload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;

@SuppressWarnings("deprecation")
@Mixin(ClientboundCustomPayloadPacket.class)
public class ClientboundCustomPayloadPacketMixin {
	// TODO(Ravel): target method method_53023 with the signature not found
// TODO(Ravel): target method method_53023 with the signature not found
    @Inject(method = "method_53023(Lnet/minecraft/class_2960;Lnet/minecraft/class_2540;)Lnet/minecraft/class_8710;", at = @At("HEAD"), cancellable = true, remap = false)
	@SuppressWarnings("target")
	private static void readPayload(Identifier id, FriendlyByteBuf payload, CallbackInfoReturnable<CustomPacketPayload> info) {
		MVPacket packet = MVNetworking.readPacket(id, payload);
		if (packet != null)
			info.setReturnValue(new MVPacketCustomPayload(packet));
	}
}
