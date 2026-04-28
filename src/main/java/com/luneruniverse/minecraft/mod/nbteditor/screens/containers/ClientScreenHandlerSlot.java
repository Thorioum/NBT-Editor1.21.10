package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.resources.Identifier;

public class ClientScreenHandlerSlot extends Slot {
	
	private static final Set<Thread> UNLOCKED_THREADS = Collections.synchronizedSet(new HashSet<>());
	public static void unlockDuring(Runnable callback) {
		UNLOCKED_THREADS.add(Thread.currentThread());
		try {
			callback.run();
		} finally {
			UNLOCKED_THREADS.remove(Thread.currentThread());
		}
	}
	
	private ClientHandledScreen screen;
	private Identifier texture;
	
	public ClientScreenHandlerSlot(Slot slot) {
		super(slot.container, slot.getContainerSlot(), slot.x, slot.y);
		this.index = slot.index;
	}
	
	public void setScreen(ClientHandledScreen screen) {
		this.screen = screen;
	}
	
	public void setTexture(Identifier texture) {
		this.texture = texture;
	}
	
	private boolean isBlocked() {
		if (UNLOCKED_THREADS.contains(Thread.currentThread()))
			return false;
		
		return screen.getLockedSlotsInfo().isBlocked(this, false);
	}
	
	@Override
	public boolean mayPlace(ItemStack stack) {
		if (isBlocked())
			return false;
		return super.mayPlace(stack);
	}
	
	@Override
	public boolean mayPickup(Player playerEntity) {
		if (isBlocked())
			return false;
		return super.mayPickup(playerEntity);
	}
	
	// Prevent quick moving identical items into slot
	@Override
	public int getMaxStackSize() {
		if (isBlocked())
			return getItem().getCount();
		return super.getMaxStackSize();
	}
	
	@Override
	public Identifier getNoItemIcon() {
		return texture;
	}
	
}
