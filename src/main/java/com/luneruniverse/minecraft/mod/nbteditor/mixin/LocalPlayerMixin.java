package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;

import net.minecraft.client.player.LocalPlayer;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
	@Inject(method = "clientSideCloseContainer", at = @At("HEAD"))
	private void closeScreen(CallbackInfo info) {
		NBTEditorClient.SERVER_CONN.closeContainerScreen();
	}
}
