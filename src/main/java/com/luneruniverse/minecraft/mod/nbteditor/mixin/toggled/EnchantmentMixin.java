package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;

@Mixin(Enchantment.class)
public class EnchantmentMixin {
	@Inject(method = "getFullname(Lnet/minecraft/core/Holder;I)Lnet/minecraft/network/chat/Component;", at = @At("HEAD"), cancellable = true)
	private static void getName(Holder<Enchantment> enchantment, int level, CallbackInfoReturnable<Component> info) {
		info.setReturnValue(ConfigScreen.getEnchantNameWithMax(enchantment.value(), level));
	}
}
