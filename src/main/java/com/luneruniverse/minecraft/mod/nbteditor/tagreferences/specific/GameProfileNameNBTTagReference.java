package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific;

import java.util.Optional;

import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;

public class GameProfileNameNBTTagReference implements TagReference<Optional<String>, NbtCompound> {
	
	@Override
	public Optional<String> get(NbtCompound object) {
		if (object.get("SkullOwner") instanceof NbtString)
			return Optional.of(object.getString("SkullOwner").orElse(""));
		if (object.get("SkullOwner") instanceof NbtCompound) {
			NbtCompound skullOwner = object.getCompound("SkullOwner").orElse(new NbtCompound());
			if (skullOwner.get("Name") instanceof NbtString)
				return Optional.of(skullOwner.getString("Name").orElse(""));
			return Optional.empty();
		}
		return Optional.empty();
	}
	
	@Override
	public void set(NbtCompound object, Optional<String> value) {
		value.ifPresentOrElse(name -> object.putString("SkullOwner", name), () -> object.remove("SkullOwner"));
	}
	
}
