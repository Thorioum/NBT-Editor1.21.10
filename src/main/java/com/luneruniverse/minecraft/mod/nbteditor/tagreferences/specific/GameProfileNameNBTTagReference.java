package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific;

import java.util.Optional;

import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class GameProfileNameNBTTagReference implements TagReference<Optional<String>, CompoundTag> {


	@Override
	public Optional<String> get(CompoundTag object) {
		return Optional.empty();
	}

	@Override
	public void set(CompoundTag object, Optional<String> value) {

	}
}
