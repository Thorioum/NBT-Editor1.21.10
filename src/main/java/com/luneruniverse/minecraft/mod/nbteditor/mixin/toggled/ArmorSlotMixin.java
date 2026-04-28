package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMixinLink;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;

@Mixin(targets = "net.minecraft.world.inventory.ArmorSlot")
public class ArmorSlotMixin {
	@Shadow
	private @Final LivingEntity owner;
	
	@Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
	private void canInsert(ItemStack stack, CallbackInfoReturnable<Boolean> info) {
		if (owner instanceof Player)
			ServerMixinLink.slotCanInsertOrTake((Slot) (Object) this, info, true);
		else if (owner instanceof AbstractHorse)
			ServerMixinLink.slotCanInsertOrTake((Slot) (Object) this, info, false);
	}
	@Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
	private void canTakeItems(Player player, CallbackInfoReturnable<Boolean> info) {
		if (owner instanceof Player)
			ServerMixinLink.slotCanInsertOrTake((Slot) (Object) this, info, true);
		else if (owner instanceof AbstractHorse)
			ServerMixinLink.slotCanInsertOrTake((Slot) (Object) this, info, false);
	}
}
