package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific;

import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public class CustomDataNBTTagReference implements TagReference<CompoundTag, ItemStack> {


	@Override
	public CompoundTag get(ItemStack object) {
		return null;
	}

	@Override
	public void set(ItemStack object, CompoundTag value) {

	}
}
