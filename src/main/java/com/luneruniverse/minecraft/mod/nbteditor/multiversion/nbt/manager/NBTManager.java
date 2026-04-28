package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Attempt;

import net.minecraft.nbt.CompoundTag;

/**
 * The NBT returned from the methods is a copy and the NBT passed into the methods will be copied
 */
public interface NBTManager<T> {
	public Attempt<CompoundTag> trySerialize(T subject);
	public default CompoundTag serialize(T subject, boolean requireSuccess) throws IllegalStateException {
		Attempt<CompoundTag> attempt = trySerialize(subject);
		return requireSuccess ? attempt.getSuccessOrThrow() : attempt.getAttemptOrThrow();
	}
	
	/**
	 * Note: If this returns false, {@link #getNbt(T)} may still return an empty {@link CompoundTag} rather than null!
	 */
	public boolean hasNbt(T subject);
	public CompoundTag getNbt(T subject);
	public CompoundTag getOrCreateNbt(T subject);
	public void setNbt(T subject, CompoundTag nbt);
	
	public default String getNbtString(T subject) {
		CompoundTag nbt = getNbt(subject);
		if (nbt == null)
			return "";
		return nbt.toString();
	}
}
