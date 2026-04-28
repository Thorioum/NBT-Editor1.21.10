package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMixinLink;

import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
	@Shadow
	public ServerPlayer player;

    @Redirect(method = "handleSetCreativeModeSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;hasInfiniteMaterials()Z"))
	@Group(name = "onCreativeInventoryAction_isInCreativeMode", min = 1)
	private boolean onCreativeInventoryAction_isInCreativeMode(ServerPlayer player) {
		return player.hasInfiniteMaterials() || ServerMVMisc.hasPermissionLevel(player, 2);
	}
	
	@Inject(method = "handleContainerClick", at = @At("HEAD"))
	private void onClickSlot(ServerboundContainerClickPacket packet, CallbackInfo info) {
		ServerMixinLink.NO_SLOT_RESTRICTIONS_PLAYERS.put(player, ConfigScreen.isNoSlotRestrictions());
	}
	
	@Inject(method = "handleContainerClose", at = @At("RETURN"))
	private void onCloseHandledScreen(ServerboundContainerClosePacket packet, CallbackInfo info) {
		// In singleplayer, paused screens will delay sending the updated cursor (air) to the client
		// This forces the updated cursor to be sent anyway
		player.containerMenu.broadcastChanges();
	}
}
