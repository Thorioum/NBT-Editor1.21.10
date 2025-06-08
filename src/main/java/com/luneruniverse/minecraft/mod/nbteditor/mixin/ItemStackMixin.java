package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import java.util.List;

import com.mojang.serialization.DataResult;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.MergedComponentMap;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

@Mixin(ItemStack.class)
public class ItemStackMixin {
	@Shadow @Final private MergedComponentMap components;

	@Inject(at = @At("RETURN"), method = "method_7950(Lnet/minecraft/class_1657;Lnet/minecraft/class_1836;)Ljava/util/List;", remap = false, require = 0)
	@SuppressWarnings("target")
	private void getTooltip(PlayerEntity player, TooltipType context, CallbackInfoReturnable<List<Text>> info) {
		MixinLink.modifyTooltip((ItemStack) (Object) this, info.getReturnValue());
	}

	@Overwrite
	public static DataResult<Unit> validateComponents(ComponentMap components) {
		if (components.contains(DataComponentTypes.MAX_DAMAGE) && components.getOrDefault(DataComponentTypes.MAX_STACK_SIZE, 1) > 1) {
			return DataResult.error(() -> "Item cannot be both damageable and stackable");
		} else {
			ContainerComponent containerComponent = components.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);

			for (ItemStack itemStack : containerComponent.iterateNonEmpty()) {
				int i = itemStack.getCount();
				int j = itemStack.getMaxCount();
				if (i > j) {
					itemStack.set(DataComponentTypes.MAX_STACK_SIZE, 99);
				}
			}

			return DataResult.success(Unit.INSTANCE);
		}
	}
}
