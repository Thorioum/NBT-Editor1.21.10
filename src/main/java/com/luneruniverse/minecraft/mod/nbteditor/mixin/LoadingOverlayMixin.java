package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.luneruniverse.minecraft.mod.nbteditor.misc.ParallelResourceReload;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.DynamicRegistryManagerHolder;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;

@Mixin(LoadingOverlay.class)
public class LoadingOverlayMixin {
	@ModifyVariable(method = "<init>", at = @At("HEAD"), ordinal = 0)
	private static ReloadInstance init_monitor(ReloadInstance monitor) {
		return Version.<ReloadInstance>newSwitch()
				.range("1.20.5", null, () -> new ParallelResourceReload(monitor, DynamicRegistryManagerHolder.loadDefaultManager()))
				.range(null, "1.20.4", monitor)
				.get();
	}
}
