package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.hideflags;

import java.util.HashMap;
import java.util.Map;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVComponentType;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.hideflags.ComponentsHideFlag;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.hideflags.HideFlag;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.hideflags.TooltipHideFlag;

import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Unit;

public class HideFlagsComponentsTagReference implements TagReference<Map<HideFlag, Boolean>, ItemStack> {

	@Override
	public Map<HideFlag, Boolean> get(ItemStack object) {
		return Map.of();
	}

	@Override
	public void set(ItemStack object, Map<HideFlag, Boolean> value) {

	}
}
