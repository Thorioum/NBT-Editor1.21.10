package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.ItemTagReferences;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public interface ContainerIO<T> {
	public static final Identifier HELMET_TEXTURE = IdentifierInst.of("minecraft", "container/slot/helmet");
	public static final Identifier CHESTPLATE_TEXTURE = IdentifierInst.of("minecraft", "container/slot/chestplate");
	public static final Identifier LEGGINGS_TEXTURE = IdentifierInst.of("minecraft", "container/slot/leggings");
	public static final Identifier BOOTS_TEXTURE = IdentifierInst.of("minecraft", "container/slot/boots");
	public static final Identifier SADDLE_TEXTURE = IdentifierInst.of("minecraft", "container/slot/saddle");
	public static final Identifier HORSE_ARMOR_TEXTURE = IdentifierInst.of("minecraft", "container/slot/horse_armor");
	public static final Identifier LLAMA_ARMOR_TEXTURE = IdentifierInst.of("minecraft", "container/slot/llama_armor");
	public static final Identifier SWORD_TEXTURE = IdentifierInst.of("minecraft", "container/slot/sword");
	public static final Identifier SHIELD_TEXTURE = IdentifierInst.of("minecraft", "container/slot/shield");
	
	public static final Identifier BREWING_FUEL_TEXTURE = IdentifierInst.of("minecraft", "container/slot/brewing_fuel");
	public static final Identifier POTION_TEXTURE = IdentifierInst.of("minecraft", "container/slot/potion");

	
	public static ContainerIO<ItemStack> forItemStackBlockEntityTag(ContainerIO<TypedEntityData<BlockEntityType<?>>> io, String entityId) {
		return DelegateContainerIO.map(io,
				ItemTagReferences.BLOCK_ENTITY_DATA::get,
                ItemTagReferences.BLOCK_ENTITY_DATA::set);
	}

	public static ContainerIO<ItemStack> forItemStackEntityTag(ContainerIO<TypedEntityData<EntityType<?>>> io, String entityId) {
		return DelegateContainerIO.map(io,
				ItemTagReferences.ENTITY_DATA::get,
                ItemTagReferences.ENTITY_DATA::set);
	}
	public static ContainerIO<ItemStack> forItemStackEntityTag(ContainerIO<TypedEntityData<EntityType<?>>> io, EntityType<?> entityId) {
		return forItemStackEntityTag(io, EntityType.getKey(entityId).toString());
	}
	
	public static <T extends LocalNBT> ContainerIO<T> forLocalNBT(ContainerIO<CompoundTag> io) {
		return DelegateContainerIO.map(io, item -> {
			CompoundTag nbt = item.getNBT();
			if (nbt == null)
				return new CompoundTag();
			return nbt;
		}, (item, nbt) -> item.setNBT(nbt));
	}
	
	/**
	 * @param container
	 * @return If false, none of the other methods can be called safely
	 */
	public boolean isSupported(T container);
	/**
	 * @param container
	 * @return The maximum number of items that may be contained
	 */
	public int getMaxSlots(T container);
	/**
	 * @param container
	 * @return The texture to render in each slot, or null if no texture should be rendered in that slot
	 */
	public Identifier[] getTextures(T container);
	/**
	 * @param container
	 * @return Will not contain null, can be modified without affecting container
	 */
	public ItemStack[] read(T container);
	/**
	 * @param container
	 * @param contents Can contain null, can be modified later without affecting container
	 * @return The number of items in <code>contents</code> that were written, including empty items
	 */
	public int write(T container, ItemStack[] contents);
	/**
	 * @param container
	 * @param contents
	 * @return The number of items in <code>contents</code> that will be written, including empty items
	 */
	public int getNumWritten(T container, ItemStack[] contents);
	/**
	 * @param container
	 * @param contents
	 * @param slot
	 * @return The slot that the item contained within <code>slot</code> will end up in after being written
	 */
	public int getWrittenSlotIndex(T container, ItemStack[] contents, int slot);
	
	public default ContainerIO<T> withTextures(Identifier... textures) {
		return new DelegateContainerIO<>(this) {
			@Override
			public Identifier[] getTextures(T container) {
				return textures;
			}
		};
	}
}
