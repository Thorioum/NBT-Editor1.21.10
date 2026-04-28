package com.luneruniverse.minecraft.mod.nbteditor.containers;

import java.util.Arrays;
import java.util.Optional;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.NBTManagers;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;

public class OrderNbtListContainerIO implements ContainerIO<ListTag> {
	
	private final int maxSlots;
	private final Identifier[] textures;
	
	public OrderNbtListContainerIO(int maxSlots) {
		this.maxSlots = maxSlots;
		this.textures = new Identifier[maxSlots];
	}
	
	public ContainerIO<CompoundTag> forNbtCompound(String key) {
		return DelegateContainerIO.map(this, nbt -> nbt.getListOrEmpty(key), (nbt, list) -> nbt.put(key, list));
	}
	public ContainerIO<CompoundTag> forNbtCompoundItems() {
		return forNbtCompound("Items");
	}
	
	@Override
	public boolean isSupported(ListTag container) {
		byte ht = (byte) 0;
		for (Tag element : container) {
			if (ht == 0)
				ht = element.getId();
			else if (ht != element.getId()){}
		}

		return container.size() <= maxSlots && Optional.of(ht).filter(
				heldType -> heldType == 0 || heldType == Tag.TAG_COMPOUND).isPresent();
	}
	
	@Override
	public int getMaxSlots(ListTag container) {
		return maxSlots;
	}
	
	@Override
	public Identifier[] getTextures(ListTag container) {
		return textures;
	}
	
	@Override
	public ItemStack[] read(ListTag container) {
		return container.stream().map(itemNbt -> NBTManagers.ITEM.deserializeOrElse(
				(CompoundTag) itemNbt, ItemStack.EMPTY)).toArray(ItemStack[]::new);
	}
	
	@Override
	public int write(ListTag container, ItemStack[] contents) {
		container.clear();
		Arrays.stream(contents).filter(item -> item != null && !item.isEmpty())
				.map(item -> NBTManagers.ITEM.serialize(item,true)).forEach(container::add);
		return contents.length;
	}
	
	@Override
	public int getNumWritten(ListTag container, ItemStack[] contents) {
		return contents.length;
	}
	
	@Override
	public int getWrittenSlotIndex(ListTag container, ItemStack[] contents, int slot) {
		int output = slot;
		for (int i = 0; i < slot; i++) {
			if (contents[i] == null || contents[i].isEmpty())
				output--;
		}
		return output;
	}
	
}
