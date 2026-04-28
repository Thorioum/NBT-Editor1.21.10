package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.DynamicRegistryManagerHolder;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.RegistryCache;

import net.minecraft.core.Holder;

@Mixin(targets = "net.minecraft.core.Registry$1")
public class Registry1Mixin {
	
	@ModifyVariable(method = "getId", at = @At("HEAD"))
	private Holder<?> getRawId(Holder<?> entry) {
		if (entry instanceof Holder.Reference<?> ref && DynamicRegistryManagerHolder.isOwnedByDefaultManager(ref)) {
			Holder.Reference<?> convertedRef = RegistryCache.convertManagerWithCache(ref);
			if (convertedRef != null)
				return convertedRef;
		}
		
		return entry;
	}
	
}
