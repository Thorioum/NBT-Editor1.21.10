package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.hideflags;

import java.util.LinkedHashMap;
import java.util.Map;

import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.hideflags.HideFlag;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.hideflags.NBTHideFlag;

import net.minecraft.world.item.ItemStack;

public class HideFlagsNBTTagReference implements TagReference<Map<HideFlag, Boolean>, ItemStack> {


	@Override
	public Map<HideFlag, Boolean> get(ItemStack object) {
		return Map.of();
	}

	@Override
	public void set(ItemStack object, Map<HideFlag, Boolean> value) {

	}
}
