package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.NBTManagers;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;

public class KeysContainerIO implements ContainerIO<CompoundTag> {
	
	private final boolean removeWhenEmpty;
	private final String[] keys;
	private final Identifier[] textures;
	
	public KeysContainerIO(boolean removeWhenEmpty, String... keys) {
		this.removeWhenEmpty = removeWhenEmpty;
		this.keys = keys;
		this.textures = new Identifier[keys.length];
	}
	
	@Override
	public boolean isSupported(CompoundTag container) {
		for (String key : keys) {
			Tag itemNbtElement = container.get(key);
			if (itemNbtElement != null && !(itemNbtElement instanceof CompoundTag))
				return false;
		}
		return true;
	}
	
	@Override
	public int getMaxSlots(CompoundTag container) {
		return keys.length;
	}
	
	@Override
	public Identifier[] getTextures(CompoundTag container) {
		return textures;
	}
	
	@Override
	public ItemStack[] read(CompoundTag container) {
		ItemStack[] contents = new ItemStack[keys.length];
		for (int i = 0; i < keys.length; i++) {
			contents[i] = container.getCompound(keys[i])
					.map(itemNbt -> NBTManagers.ITEM.deserializeOrElse(itemNbt, ItemStack.EMPTY)).orElse(ItemStack.EMPTY);
		}
		return contents;
	}
	
	@Override
	public int write(CompoundTag container, ItemStack[] contents) {
		for (int i = 0; i < keys.length; i++) {
			ItemStack item = contents[i];
			if (item == null || item.isEmpty()) {
				if (removeWhenEmpty) {
					container.remove(keys[i]);
					continue;
				} else {
					item = ItemStack.EMPTY;
				}
			}
			container.put(keys[i], NBTManagers.ITEM.serialize(item,true));
		}
		return keys.length;
	}
	
	@Override
	public int getNumWritten(CompoundTag container, ItemStack[] contents) {
		return keys.length;
	}
	
	@Override
	public int getWrittenSlotIndex(CompoundTag container, ItemStack[] contents, int slot) {
		return slot;
	}
	
}
