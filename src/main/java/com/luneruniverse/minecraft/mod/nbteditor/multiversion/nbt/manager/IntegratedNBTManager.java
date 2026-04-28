package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager;

import java.util.function.Consumer;

import net.minecraft.nbt.CompoundTag;

/**
 * Convenience interface to avoid <code>NBTManagers.ITEM.getNbt(item)</code>
 */
public interface IntegratedNBTManager {
	public default CompoundTag nbte$serialize(boolean requireSuccess) {
		throw new RuntimeException("Missing implementation for IntegratedNBTManager#nbte$serialize");
	}
	
	public default boolean nbte$hasNbt() {
		throw new RuntimeException("Missing implementation for IntegratedNBTManager#nbte$hasNbt");
	}
	public default CompoundTag nbte$getNbt() {
		throw new RuntimeException("Missing implementation for IntegratedNBTManager#nbte$getNbt");
	}
	public default CompoundTag nbte$getOrCreateNbt() {
		throw new RuntimeException("Missing implementation for IntegratedNBTManager#nbte$getOrCreateNbt");
	}
	public default void nbte$setNbt(CompoundTag nbt) {
		throw new RuntimeException("Missing implementation for IntegratedNBTManager#nbte$setNbt");
	}
	
	public default void nbte$modifyNbt(Consumer<CompoundTag> modifier) {
		CompoundTag nbt = nbte$getOrCreateNbt();
		modifier.accept(nbt);
		nbte$setNbt(nbt);
	}
	public default void nbte$modifySubNbt(String tag, Consumer<CompoundTag> modifier) {
		CompoundTag nbt = nbte$getOrCreateNbt();
		CompoundTag subNbt = nbt.getCompoundOrEmpty(tag);
		modifier.accept(subNbt);
		nbt.put(tag, subNbt);
		nbte$setNbt(nbt);
	}
}
