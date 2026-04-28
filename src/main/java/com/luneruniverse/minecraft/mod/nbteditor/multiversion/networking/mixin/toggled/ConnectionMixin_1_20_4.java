package com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVClientNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVServerNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.server.NBTEditorServer;
import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMixinLink;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.PacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

@Mixin(Connection.class)
public class ConnectionMixin_1_20_4 {
	@Shadow
	private PacketFlow receiving;
	@Shadow
	private PacketListener packetListener;
	
	private PacketListener prevListener;
	
	// TODO(Ravel): target method method_10763 with the signature not found
// TODO(Ravel): target method method_10763 with the signature not found
    @Inject(method = "method_10763(Lnet/minecraft/class_2547;)V", at = @At("HEAD"), remap = false)
	@SuppressWarnings("target")
	private void setPacketListener_head(PacketListener listener, CallbackInfo info) {
		prevListener = packetListener;
	}
	
	// TODO(Ravel): target method method_10763 with the signature not found
// TODO(Ravel): target method method_10763 with the signature not found
    @Inject(method = "method_10763(Lnet/minecraft/class_2547;)V", at = @At("RETURN"), remap = false)
	@SuppressWarnings("target")
	private void setPacketListener_return(PacketListener listener, CallbackInfo info) {
		if (receiving == PacketFlow.CLIENTBOUND && !NBTEditorServer.IS_DEDICATED) {
			if (ServerMixinLink.isInstanceOfClientPlayNetworkHandlerSafely(listener))
				MVClientNetworking.onPlayStart((ClientPacketListener) listener);
			else if (ServerMixinLink.isInstanceOfClientPlayNetworkHandlerSafely(prevListener))
				MVClientNetworking.onPlayStop();
		}
		if (receiving == PacketFlow.SERVERBOUND) {
			if (listener instanceof ServerGamePacketListenerImpl handler)
				MVServerNetworking.onPlayStart(handler.player);
			else if (prevListener instanceof ServerGamePacketListenerImpl handler)
				MVServerNetworking.onPlayStop(handler.player);
		}
		prevListener = null;
	}
}
