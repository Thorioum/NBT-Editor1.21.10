package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.MVNbtCompoundParent;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.NBTManagers;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;

public class SlotKeyNbtListContainerIO implements ContainerIO<ListTag> {
	
	private final int numSlots;
	private final Identifier[] textures;
	
	public SlotKeyNbtListContainerIO(int numSlots) {
		this.numSlots = numSlots;
		this.textures = new Identifier[numSlots];
	}
	
	public ContainerIO<CompoundTag> forNbtCompound(String key) {
		return DelegateContainerIO.map(this, nbt -> nbt.getListOrEmpty(key), (nbt, list) -> nbt.put(key, list));
	}
	public ContainerIO<CompoundTag> forNbtCompoundItems() {
		return forNbtCompound("Items");
	}
	
	@Override
	public boolean isSupported(ListTag container) {
		for (Tag itemNbtElement : container) {
			if (itemNbtElement instanceof CompoundTag itemNbt) {
				if (!itemNbt.contains("Slot"))
					return false;
				int slot = itemNbt.getIntOr("Slot",-1);
				if (slot < 0 || slot >= numSlots)
					return false;
			} else {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int getMaxSlots(ListTag container) {
		return numSlots;
	}
	
	@Override
	public Identifier[] getTextures(ListTag container) {
		return textures;
	}
	
	@Override
	public ItemStack[] read(ListTag container) {
		ItemStack[] contents = new ItemStack[numSlots];
		for (Tag itemNbtElement : container) {
			CompoundTag itemNbt = (CompoundTag) itemNbtElement;
			contents[itemNbt.getIntOr("Slot",-1)] = NBTManagers.ITEM.deserializeOrElse(itemNbt, ItemStack.EMPTY);
		}
		return contents;
	}
	
	@Override
	public int write(ListTag container, ItemStack[] contents) {
		container.clear();
		for (int i = 0; i < contents.length; i++) {
			ItemStack item = contents[i];
			if (item == null || item.isEmpty())
				continue;
			CompoundTag itemNbt = NBTManagers.ITEM.serialize(item,true);
			itemNbt.putByte("Slot", (byte) i);
			container.add(itemNbt);
		}
		return numSlots;
	}
	
	@Override
	public int getNumWritten(ListTag container, ItemStack[] contents) {
		return numSlots;
	}
	
	@Override
	public int getWrittenSlotIndex(ListTag container, ItemStack[] contents, int slot) {
		return slot;
	}
	
}
