package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Attempt;

import net.minecraft.nbt.CompoundTag;

public interface DeserializableNBTManager<T> extends NBTManager<T> {
	public Attempt<T> tryDeserialize(CompoundTag nbt);
	public default T deserialize(CompoundTag nbt, boolean requireSuccess) throws IllegalStateException {
		Attempt<T> attempt = tryDeserialize(nbt);
		return requireSuccess ? attempt.getSuccessOrThrow() : attempt.getAttemptOrThrow();
	}
	public default T deserializeOrElse(CompoundTag nbt, T defaultValue) {
		return tryDeserialize(nbt).value().orElse(defaultValue);
	}
}
