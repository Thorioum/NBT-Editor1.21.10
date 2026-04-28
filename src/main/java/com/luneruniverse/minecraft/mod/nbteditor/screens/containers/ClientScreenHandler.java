package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.resources.Identifier;

public class ClientScreenHandler extends ChestMenu {
	
	public static final int SYNC_ID = -2718;
	
	public ClientScreenHandler(int rows) {
		super(switch (rows) {
			case 1 -> MenuType.GENERIC_9x1;
			case 2 -> MenuType.GENERIC_9x2;
			case 3 -> MenuType.GENERIC_9x3;
			case 4 -> MenuType.GENERIC_9x4;
			case 5 -> MenuType.GENERIC_9x5;
			case 6 -> MenuType.GENERIC_9x6;
			default -> throw new IllegalArgumentException("Invalid row count: " + rows);
		}, SYNC_ID, MainUtil.client.player.getInventory(), new SimpleContainer(rows * 9), rows);
		
		slots.replaceAll(ClientScreenHandlerSlot::new);
	}
	
	public void setScreen(ClientHandledScreen screen) {
		slots.forEach(slot -> ((ClientScreenHandlerSlot) slot).setScreen(screen));
	}
	
	public void setSlotTextures(Identifier... textures) {
		for (int i = 0; i < textures.length; i++)
			((ClientScreenHandlerSlot) slots.get(i)).setTexture(textures[i]);
	}
	
	@Override
	protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
		boolean changed = false;
		List<Integer> indices = IntStream.range(startIndex, endIndex).collect(ArrayList::new, List::add, List::addAll);
		if (fromLast)
			Collections.reverse(indices);
		
		if (stack.isStackable()) {
			for (int i : indices) {
				if (stack.isEmpty())
					break;
				Slot slot = slots.get(i);
				ItemStack slotStack = slot.getItem();
				
				if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(stack, slotStack)) {
					int total = slotStack.getCount() + stack.getCount();
					int max = slot.getMaxStackSize(slotStack);
					if (total <= max) {
						stack.setCount(0);
						slotStack.setCount(total);
						slot.setChanged();
						changed = true;
					} else if (slotStack.getCount() < max) {
						stack.shrink(max - slotStack.getCount());
						slotStack.setCount(max);
						slot.setChanged();
						changed = true;
					}
				}
			}
		}
		
		if (!stack.isEmpty()) {
			for (int i : indices) {
				Slot slot = slots.get(i);
				ItemStack slotStack = slot.getItem();
				
				if (slotStack.isEmpty() && slot.mayPlace(stack)) {
					int max = slot.getMaxStackSize(stack);
					slot.set(stack.split(Math.min(stack.getCount(), max)));
					slot.setChanged();
					changed = true;
					break;
				}
			}
		}
		
		return changed;
	}
	
}
