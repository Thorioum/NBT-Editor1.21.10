package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific;

import java.lang.invoke.MethodType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.MVNbtCompoundParent;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.CustomPotionContents;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public class CustomPotionContentsNBTTagReference implements TagReference<CustomPotionContents, ItemStack> {


	@Override
	public CustomPotionContents get(ItemStack object) {
		return null;
	}

	@Override
	public void set(ItemStack object, CustomPotionContents value) {

	}
}
