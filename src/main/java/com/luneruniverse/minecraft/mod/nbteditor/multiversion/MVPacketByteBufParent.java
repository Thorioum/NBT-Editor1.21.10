package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public interface MVPacketByteBufParent {
	
	public default FriendlyByteBuf writeBoolean(boolean value) {
		throw new RuntimeException("Missing implementation for MVPacketByteBufParent#writeBoolean");
	}
	
	public default FriendlyByteBuf writeDouble(double value) {
		throw new RuntimeException("Missing implementation for MVPacketByteBufParent#writeDouble");
	}
	
	public default Identifier readIdentifier() {
		throw new RuntimeException("Missing implementation for MVPacketByteBufParent#readIdentifier");
	}
	public default FriendlyByteBuf writeIdentifier(Identifier id) {
		throw new RuntimeException("Missing implementation for MVPacketByteBufParent#writeIdentifier");
	}
	
	public default <T> ResourceKey<T> readRegistryKey(ResourceKey<? extends Registry<T>> registryRef) {
		throw new RuntimeException("Missing implementation for MVPacketByteBufParent#readRegistryKey");
	}
	public default void writeRegistryKey(ResourceKey<?> key) {
		throw new RuntimeException("Missing implementation for MVPacketByteBufParent#writeRegistryKey");
	}
	
	public default FriendlyByteBuf writeNbtCompound(CompoundTag element) {
		throw new RuntimeException("Missing implementation for MVPacketByteBufParent#writeNbtCompound");
	}
	
	public default Vec3 readVec3d() {
		throw new RuntimeException("Missing implementation for MVPacketByteBufParent#readVec3d");
	}
	public default void writeVec3d(Vec3 vector) {
		throw new RuntimeException("Missing implementation for MVPacketByteBufParent#writeVec3d");
	}
	
	public default ItemStack readItemStack() {
		throw new RuntimeException("Missing implementation for MVPacketByteBufParent#readItemStack");
	}
	public default FriendlyByteBuf writeItemStack(ItemStack item) {
		throw new RuntimeException("Missing implementation for MVPacketByteBufParent#writeItemStack");
	}
	
}
