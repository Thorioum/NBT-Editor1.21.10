package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientHandledScreen;
import com.luneruniverse.minecraft.mod.nbteditor.server.NBTEditorServer;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;

@Mixin(Connection.class)
public abstract class ConnectionMixin {
	
	@Shadow
	public abstract PacketFlow getReceiving();
	
	@Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
	private void send(Packet<?> packet, CallbackInfo info) {
		if (getReceiving() != PacketFlow.CLIENTBOUND)
			return;
		
		if (MainUtil.client.screen instanceof ClientHandledScreen) {
			if (packet instanceof ServerboundContainerClickPacket slotPacket) {
				info.cancel();
				NBTEditor.LOGGER.warn("Tried to send a slot click packet while on a ClientHandledScreen: slot=" +
						MVMisc.getSlot(slotPacket) + ", button=" + MVMisc.getButton(slotPacket) + ", action=" +
						MVMisc.getActionType(slotPacket));
			}
		}
	}
	
	@Inject(method = "<init>", at = @At("HEAD"))
	private static void init(PacketFlow side, CallbackInfo info) {
		// When on a dedicated server, all threads are already server threads
		if (side == PacketFlow.SERVERBOUND)
			NBTEditorServer.registerServerThread(Thread.currentThread());
	}
	
}
