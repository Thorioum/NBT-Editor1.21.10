package com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin_1_20_1 {
	@Shadow
	public ServerPlayer player;
	
	@Inject(method = "<init>(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/class_2535;Lnet/minecraft/class_3222;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_2535;method_10763(Lnet/minecraft/class_2547;)V"), remap = false)
	@SuppressWarnings("target")
	private void init(MinecraftServer server, Connection connection, ServerPlayer player, CallbackInfo info) {
		this.player = player;
		player.connection = (ServerGamePacketListenerImpl) (Object) this;
	}
}
