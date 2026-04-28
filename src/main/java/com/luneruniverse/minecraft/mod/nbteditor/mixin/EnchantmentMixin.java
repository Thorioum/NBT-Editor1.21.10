package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.network.chat.Component;

@Mixin(Enchantment.class)
public class EnchantmentMixin {
	// TODO(Ravel): target method method_8179 with the signature not found
// TODO(Ravel): target method method_8179 with the signature not found
    @Inject(method = "method_8179(I)Lnet/minecraft/class_2561;", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
	@SuppressWarnings("target")
	private void getName(int level, CallbackInfoReturnable<Component> info) {
		info.setReturnValue(ConfigScreen.getEnchantNameWithMax((Enchantment) (Object) this, level));
	}
}
