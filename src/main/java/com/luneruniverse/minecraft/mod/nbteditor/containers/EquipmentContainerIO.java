package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.NBTManagers;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;

public class EquipmentContainerIO implements ContainerIO<CompoundTag> {
	
	private static final Identifier[] HORSE_ARMOR_TEXTURES = new Identifier[] {
			HELMET_TEXTURE, CHESTPLATE_TEXTURE, LEGGINGS_TEXTURE, BOOTS_TEXTURE,
			SADDLE_TEXTURE, HORSE_ARMOR_TEXTURE, SWORD_TEXTURE, SHIELD_TEXTURE};
	private static final Identifier[] LLAMA_ARMOR_TEXTURES = new Identifier[] {
			HELMET_TEXTURE, CHESTPLATE_TEXTURE, LEGGINGS_TEXTURE, BOOTS_TEXTURE,
			SADDLE_TEXTURE, LLAMA_ARMOR_TEXTURE, SWORD_TEXTURE, SHIELD_TEXTURE};
	private static final String[] KEYS = new String[] {
			"head", "chest", "legs", "boots", "saddle", "body", "mainhand", "offhand"};
	
	private final Identifier[] textures;
	
	public EquipmentContainerIO(boolean llama) {
		textures = (llama ? LLAMA_ARMOR_TEXTURES : HORSE_ARMOR_TEXTURES);
	}
	
	public ContainerIO<CompoundTag> forNbtCompoundEquipment() {
		return DelegateContainerIO.map(this,
				nbt -> nbt.getCompoundOrEmpty("equipment"), (nbt, list) -> nbt.put("equipment", list));
	}
	
	@Override
	public boolean isSupported(CompoundTag container) {
		return true;
	}
	
	@Override
	public int getMaxSlots(CompoundTag container) {
		return 8;
	}
	
	@Override
	public Identifier[] getTextures(CompoundTag container) {
		return textures;
	}
	
	@Override
	public ItemStack[] read(CompoundTag container) {
		ItemStack[] contents = new ItemStack[8];
		for (int i = 0; i < 8; i++) {
			if (container.contains(KEYS[i]))
				contents[i] = NBTManagers.ITEM.deserializeOrElse(container.getCompoundOrEmpty(KEYS[i]), ItemStack.EMPTY);
		}
		return contents;
	}
	
	@Override
	public int write(CompoundTag container, ItemStack[] contents) {
		for (int i = 0; i < 8; i++) {
			ItemStack item = contents[i];
			if (item == null || item.isEmpty())
				container.remove(KEYS[i]);
			else
				container.put(KEYS[i], NBTManagers.ITEM.serialize(item,true));
		}
		return 8;
	}
	
	@Override
	public int getNumWritten(CompoundTag container, ItemStack[] contents) {
		return 8;
	}
	
	@Override
	public int getWrittenSlotIndex(CompoundTag container, ItemStack[] contents, int slot) {
		return slot;
	}
	
}
