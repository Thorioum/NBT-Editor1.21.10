package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;

@Mixin(ItemStack.class)
public class ItemStackMixin {
	// TODO(Ravel): target method method_7950 with the signature not found
// TODO(Ravel): target method method_7950 with the signature not found
    @Inject(at = @At("RETURN"), method = "method_7950(Lnet/minecraft/class_1657;Lnet/minecraft/class_1836;)Ljava/util/List;", remap = false, require = 0)
	@SuppressWarnings("target")
	private void getTooltip(Player player, TooltipFlag context, CallbackInfoReturnable<List<Component>> info) {
		MixinLink.modifyTooltip((ItemStack) (Object) this, info.getReturnValue());
	}
}
