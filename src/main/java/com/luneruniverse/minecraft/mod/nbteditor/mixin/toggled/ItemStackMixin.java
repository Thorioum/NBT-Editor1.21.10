package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;

@Mixin(ItemStack.class)
public class ItemStackMixin {
	
	@Inject(method = "getTooltipLines", at = @At("RETURN"))
	private void getTooltip(Item.TooltipContext context, Player player, TooltipFlag type, CallbackInfoReturnable<List<Component>> info) {
		MixinLink.modifyTooltip((ItemStack) (Object) this, info.getReturnValue());
	}
	
	@Shadow
	private @Final PatchedDataComponentMap components;
	
	@Inject(method = "applyComponentsAndValidate", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/component/PatchedDataComponentMap;applyPatch(Lnet/minecraft/core/component/DataComponentPatch;)V"))
	private void applyChanges(DataComponentPatch changes, CallbackInfo info) {
		if (MixinLink.SET_CHANGES.contains(Thread.currentThread())) {
			MixinLink.SET_CHANGES.remove(Thread.currentThread());
			components.restorePatch(DataComponentPatch.EMPTY);
		}
	}
	
}
