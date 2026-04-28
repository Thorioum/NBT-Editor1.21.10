package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMixinLink;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;

@Mixin(BlockEntity.class)
public class BlockEntityMixin {
	// TODO(Ravel): target method method_11007 with the signature not found
// TODO(Ravel): target method method_11007 with the signature not found
    @Inject(method = "method_11007(Lnet/minecraft/class_2487;)Lnet/minecraft/class_2487;", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
	@SuppressWarnings("target")
	private void writeNbt(CompoundTag nbt, CallbackInfoReturnable<CompoundTag> info) {
		if (ServerMixinLink.BLOCK_ENTITY_WRITE_NBT_WITHOUT_IDENTIFYING_DATA.remove(Thread.currentThread()))
			info.setReturnValue(nbt);
	}
}
