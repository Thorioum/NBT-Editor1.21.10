package com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVClientNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SetSlotC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;

public class ServerItemReference implements ItemReference {
	
	private final AbstractContainerScreen<?> screen;
	private final int slot;
	
	/**
	 * @param screen
	 * @param slot Format: generic container
	 */
	public ServerItemReference(AbstractContainerScreen<?> screen, int slot) {
		if (screen.getMenu().getSlot(slot).container == MainUtil.client.player.getInventory())
			throw new IllegalArgumentException("The slot cannot be in the player's inventory!");
		
		this.screen = screen;
		this.slot = slot;
	}
	
	public int getSlot() {
		return slot;
	}
	
	@Override
	public boolean exists() {
		return NBTEditorClient.CURSOR_MANAGER.getCurrentRoot() == screen &&
				!NBTEditorClient.CURSOR_MANAGER.isCurrentRootClosed();
	}
	
	@Override
	public ItemStack getItem() {
		return screen.getMenu().getSlot(slot).getItem();
	}
	
	@Override
	public void saveItem(ItemStack toSave, Runnable onFinished) {
		screen.getMenu().getSlot(slot).set(toSave);
		if (NBTEditorClient.SERVER_CONN.isContainerScreen())
			MVClientNetworking.send(new SetSlotC2SPacket(slot, toSave.copy()));
		onFinished.run();
	}
	
	@Override
	public boolean isLocked() {
		return false;
	}
	
	@Override
	public boolean isLockable() {
		return false;
	}
	
	@Override
	public int getBlockedSlot() {
		return -1;
	}
	
	@Override
	public void showParent() {
		NBTEditorClient.CURSOR_MANAGER.showBranch(screen);
	}
	
}
