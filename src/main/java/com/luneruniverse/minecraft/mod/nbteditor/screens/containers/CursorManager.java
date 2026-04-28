package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVClientNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SetCursorC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class CursorManager {
	
	private AbstractContainerScreen<?> currentRoot;
	private boolean currentRootIsInventory;
	private boolean currentRootHasServerCursor;
	private boolean currentRootClosed;
	private AbstractContainerScreen<?> currentBranch;
	
	public CursorManager() {}
	
	public boolean isBranched() {
		return currentRoot != null && currentRoot != currentBranch;
	}
	public AbstractContainerScreen<?> getCurrentRoot() {
		return currentRoot;
	}
	public boolean isCurrentRootClosed() {
		return currentRootClosed;
	}
	public AbstractContainerScreen<?> getCurrentBranch() {
		return currentBranch;
	}
	
	public void onNoScreenSet() {
		currentRoot = null;
		currentRootClosed = false;
		currentBranch = null;
	}
	
	public void onHandledScreenSet(AbstractContainerScreen<?> screen) {
		if (screen == currentBranch)
			return;
		
		currentRoot = screen;
		currentRootIsInventory = (currentRoot.getMenu() == MainUtil.client.player.inventoryMenu ||
				currentRoot instanceof CreativeModeInventoryScreen);
		currentRootHasServerCursor = !(screen instanceof CreativeModeInventoryScreen);
		currentRootClosed = false;
		currentBranch = screen;
	}
	
	public void onCloseScreenPacket() {
		if (currentRoot == null || currentRootIsInventory)
			return;
		
		currentRootClosed = true;
	}
	
	private void transferCursorTo(AbstractContainerScreen<?> branch) {
		if (currentBranch == branch)
			return;
		
		AbstractContainerMenu handler = branch.getMenu();
		AbstractContainerMenu currentHandler = currentBranch.getMenu();
		
		MainUtil.setCursorStackSilently(handler, currentHandler.getCarried());
		MainUtil.setCursorStackSilently(currentHandler, ItemStack.EMPTY);
		
		if (currentRootHasServerCursor) {
			if (branch == currentRoot)
				MVClientNetworking.send(new SetCursorC2SPacket(handler.getCarried().copy()));
			else if (currentBranch == currentRoot)
				MVClientNetworking.send(new SetCursorC2SPacket(ItemStack.EMPTY));
		}
	}
	
	public void showBranch(AbstractContainerScreen<?> branch) {
		if (currentRoot == null) {
			if (MVMisc.hasCreativeInventory()) {
				currentRoot = MVMisc.newCreativeModeInventoryScreen(MainUtil.client.player);
				currentRootHasServerCursor = false;
			} else {
				currentRoot = new InventoryScreen(MainUtil.client.player);
				currentRootHasServerCursor = true;
			}
			currentRootIsInventory = true;
			currentRootClosed = false;
			currentBranch = currentRoot;
		}
		if (branch == null)
			branch = currentRoot;
		
		if (currentRootClosed && branch == currentRoot) {
			closeRoot();
			return;
		}
		
		transferCursorTo(branch);
		currentBranch = branch;
		MainUtil.client.player.containerMenu = branch.getMenu();
		branch.skipNextRelease = true;
		MainUtil.client.setScreen(branch);
	}
	public void showRoot() {
		showBranch(currentRoot);
	}
	
	public void closeRoot() {
		if (currentRoot == null) {
			MainUtil.client.setScreen(null);
			return;
		}
		
		if (currentRootClosed) {
			if (currentBranch != currentRoot) {
				ItemStack cursor = currentBranch.getMenu().getCarried();
				if (currentRootHasServerCursor) {
					if (Version.<Boolean>newSwitch()
							.range("1.17.1", null, true)
							.range(null, "1.17", false)
							.get()) {
						MainUtil.get(cursor, true);
					} else {
						MainUtil.dropCreativeStack(cursor);
					}
					cursor = ItemStack.EMPTY;
				}
				MainUtil.setCursorStackSilently(currentRoot.getMenu(), cursor);
			}
			MainUtil.client.player.clientSideCloseContainer(); // will trigger #onNoScreenSet()
			return;
		}
		
		transferCursorTo(currentRoot);
		MainUtil.client.player.closeContainer(); // will trigger #onNoScreenSet()
	}
	
	public void setCursor(ItemStack item) {
		if (currentRoot == null)
			throw new IllegalStateException("There is no root to set the cursor of");
		
		MainUtil.setCursorStackSilently(currentBranch.getMenu(), item);
		
		if (currentRootHasServerCursor && currentBranch == currentRoot)
			MVClientNetworking.send(new SetCursorC2SPacket(item.copy()));
	}
	
}
