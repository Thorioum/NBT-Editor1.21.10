package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMixinLink;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.FurnaceFuelSlot;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.ShulkerBoxSlot;
import net.minecraft.world.inventory.Slot;

@Mixin(value = {ShulkerBoxSlot.class, FurnaceFuelSlot.class, FurnaceResultSlot.class}, targets = {"net.minecraft.world.inventory.BrewingStandMenu$PotionSlot", "net.minecraft.world.inventory.BrewingStandMenu$IngredientsSlot", "net.minecraft.world.inventory.BrewingStandMenu$FuelSlot"})
public class ManySlotMixin {
	@Inject(method = "mayPlace(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
	private void canInsert(ItemStack item, CallbackInfoReturnable<Boolean> info) {
		ServerMixinLink.slotCanInsertOrTake((Slot) (Object) this, info, false);
	}
}
