package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific;

import java.lang.invoke.MethodType;
import java.util.Optional;
import java.util.UUID;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;
import com.mojang.authlib.GameProfile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class GameProfileNBTTagReference implements TagReference<Optional<GameProfile>, CompoundTag> {


	@Override
	public Optional<GameProfile> get(CompoundTag object) {
		return Optional.empty();
	}

	@Override
	public void set(CompoundTag object, Optional<GameProfile> value) {

	}
}
