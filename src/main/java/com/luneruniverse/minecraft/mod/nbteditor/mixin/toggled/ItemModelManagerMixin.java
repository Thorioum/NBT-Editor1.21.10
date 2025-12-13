package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import net.minecraft.util.HeldItemContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;

import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(ItemModelManager.class)
public class ItemModelManagerMixin {
	
	@Inject(method = "update", at = @At("HEAD"))
	private void update(ItemRenderState renderState, ItemStack stack, ItemDisplayContext displayContext, World world, HeldItemContext heldItemContext, int seed, CallbackInfo ci) {
		MixinLink.ITEM_BEING_RENDERED.put(Thread.currentThread(), stack);
	}
	
}
