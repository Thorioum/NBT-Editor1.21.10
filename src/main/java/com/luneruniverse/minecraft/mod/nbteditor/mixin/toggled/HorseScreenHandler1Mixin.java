package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMixinLink;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;

@Mixin(targets = {"net.minecraft.world.inventory.HorseInventoryMenu$1"})
public class HorseScreenHandler1Mixin {
    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true, remap = false)
	@SuppressWarnings("target")
	private void canInsert(ItemStack item, CallbackInfoReturnable<Boolean> info) {
		ServerMixinLink.slotCanInsertOrTake((Slot) (Object) this, info, false);
	}
}
