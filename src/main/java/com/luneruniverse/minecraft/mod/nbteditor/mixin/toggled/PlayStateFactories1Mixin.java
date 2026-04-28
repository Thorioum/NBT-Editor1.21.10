package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMVMisc;

import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

@Mixin(targets = "net.minecraft.network.protocol.game.GameProtocols$1")
public class PlayStateFactories1Mixin {
	@Redirect(method = "decode", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/GameProtocols$Context;hasInfiniteMaterials()Z"))
	private boolean decode_isInCreativeMode(GameProtocols.Context context) {
		if (context instanceof ServerGamePacketListenerImpl serverHandler && ServerMVMisc.hasPermissionLevel(serverHandler.player, 2))
			return true;
		return context.hasInfiniteMaterials();
	}
}
