package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMixinLink;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;

@Mixin(targets = "net.minecraft.world.inventory.InventoryMenu$1")
public class PlayerScreenHandler1Mixin {
	// TODO(Ravel): target method method_7680 with the signature not found
    @Inject(method = "method_7680(Lnet/minecraft/class_1799;)Z", at = @At("HEAD"), cancellable = true, remap = false)
	@SuppressWarnings("target")
	private void canInsert(ItemStack stack, CallbackInfoReturnable<Boolean> info) {
		ServerMixinLink.slotCanInsertOrTake((Slot) (Object) this, info, true);
	}
	// TODO(Ravel): target method method_7674 with the signature not found
    @Inject(method = "method_7674(Lnet/minecraft/class_1657;)Z", at = @At("HEAD"), cancellable = true, remap = false)
	@SuppressWarnings("target")
	private void canTakeItems(Player player, CallbackInfoReturnable<Boolean> info) {
		ServerMixinLink.slotCanInsertOrTake((Slot) (Object) this, info, true);
	}
}
