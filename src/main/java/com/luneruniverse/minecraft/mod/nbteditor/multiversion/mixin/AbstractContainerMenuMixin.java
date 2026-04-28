package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.server.level.ServerPlayer;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {
	// <= 1.17.1: patches item getting thrown & deleted when creative inventory is closed
	@Inject(method = "removed", at = @At("HEAD"), cancellable = true)
	private void close(Player player, CallbackInfo info) {
		Version.newSwitch()
				.range("1.18.0", null, () -> {})
				.range(null, "1.17.1", () -> {
					if (!(player instanceof ServerPlayer))
						info.cancel();
				})
				.run();
	}
}
