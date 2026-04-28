package com.luneruniverse.minecraft.mod.nbteditor.containers;

import java.util.Arrays;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;

public class ContainerComponentContainerIO implements ContainerIO<ItemStack> {
	
	private final int numSlots;
	private final Identifier[] textures;
	
	public ContainerComponentContainerIO(int numSlots) {
		this.numSlots = numSlots;
		this.textures = new Identifier[numSlots];
	}
	
	@Override
	public boolean isSupported(ItemStack container) {
		ItemContainerContents component = container.get(DataComponents.CONTAINER);
		return component == null || component.allItemsCopyStream().count() <= numSlots;
	}
	
	@Override
	public int getMaxSlots(ItemStack container) {
		return numSlots;
	}
	
	@Override
	public Identifier[] getTextures(ItemStack container) {
		return textures;
	}
	
	@Override
	public ItemStack[] read(ItemStack container) {
		return container.get(DataComponents.CONTAINER).allItemsCopyStream().toArray(ItemStack[]::new);
	}
	
	@Override
	public int write(ItemStack container, ItemStack[] contents) {
		container.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(Arrays.asList(contents)));
		return numSlots;
	}
	
	@Override
	public int getNumWritten(ItemStack container, ItemStack[] contents) {
		return numSlots;
	}
	
	@Override
	public int getWrittenSlotIndex(ItemStack container, ItemStack[] contents, int slot) {
		return slot;
	}
	
}
