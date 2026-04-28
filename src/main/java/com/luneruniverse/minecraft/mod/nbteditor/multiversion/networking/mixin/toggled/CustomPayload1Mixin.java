package com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacketCustomPayload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

@SuppressWarnings("deprecation")
@Mixin(targets = "net.minecraft.network.protocol.common.custom.CustomPacketPayload$1")
public class CustomPayload1Mixin {
	@Inject(method = "findCodec", at = @At("HEAD"), cancellable = true)
	private void getCodec(Identifier id, CallbackInfoReturnable<StreamCodec<FriendlyByteBuf, MVPacketCustomPayload>> info) {
		if (!MVNetworking.isPacket(id))
			return;
		info.setReturnValue(StreamCodec.ofMember((packet, payload) -> packet.getPacket().write(payload),
				payload -> new MVPacketCustomPayload(MVNetworking.readPacket(id, payload))));
	}
}
