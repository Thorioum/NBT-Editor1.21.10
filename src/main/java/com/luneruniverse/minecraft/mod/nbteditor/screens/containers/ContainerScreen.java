package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.containers.ContainerIOs;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ContainerItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.factories.LocalFactoryScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.network.chat.Component;

public class ContainerScreen<L extends LocalNBT> extends ClientHandledScreen {
	
	public static <L extends LocalNBT> void show(NBTReference<L> ref) {
		if (!ref.exists() || !ContainerIOs.isSupported(ref.getLocalNBT())) {
			ref.showParent();
			return;
		}
		
		NBTEditorClient.CURSOR_MANAGER.showBranch(new ContainerScreen<>(ref));
	}
	
	private final Component unsavedTitle;
	
	private final NBTReference<L> ref;
	private final L localNBT;
	private final int numSlots;
	private boolean saved;
	
	private boolean navigationClicked;
	
	private ContainerScreen(NBTReference<L> ref) {
		super(3, TextInst.translatable("nbteditor.container.title").append(ref.getLocalNBT().getName()));
		
		this.unsavedTitle = TextInst.copy(title).append("*");
		
		this.ref = ref;
		this.localNBT = LocalNBT.copy(ref.getLocalNBT());
		this.numSlots = ContainerIOs.getMaxSlots(localNBT);
		this.saved = true;
		
		setSlotTextures(ContainerIOs.getTextures(localNBT));
		
		ItemStack[] contents = ContainerIOs.read(localNBT);
		for (int i = 0; i < contents.length; i++)
			menu.getSlot(i).set(contents[i] == null ? ItemStack.EMPTY : contents[i].copy());
	}
	
	@Override
	protected void init() {
		super.init();
		
		if (ref instanceof ItemReference item && item.isLockable()) {
			this.addRenderableWidget(MVMisc.newButton(16, 64, 83, 20, ConfigScreen.isLockSlots() ? TextInst.translatable("nbteditor.client_chest.slots.unlock") : TextInst.translatable("nbteditor.client_chest.slots.lock"), btn -> {
				navigationClicked = true;
				if (ConfigScreen.isLockSlotsRequired()) {
					btn.active = false;
					ConfigScreen.setLockSlots(true);
				} else
					ConfigScreen.setLockSlots(!ConfigScreen.isLockSlots());
				btn.setMessage(ConfigScreen.isLockSlots() ? TextInst.translatable("nbteditor.client_chest.slots.unlock") : TextInst.translatable("nbteditor.client_chest.slots.lock"));
			})).active = !ConfigScreen.isLockSlotsRequired();
		}
		
		addRenderableWidget(MVMisc.newTexturedButton(width - 36, 22, 20, 20, 20,
				LocalFactoryScreen.FACTORY_ICON,
				btn -> minecraft.setScreen(new LocalFactoryScreen<>(ref)),
				new MVTooltip("nbteditor.factory")));
	}
	
	@Override
	protected Component getRenderedTitle() {
		return saved ? title : unsavedTitle;
	}
	
	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		navigationClicked = false;
		return super.mouseClicked(click, doubled);
	}
	
	@Override
	protected void slotClicked(Slot slot, int slotId, int button, ContainerInput actionType) {
		if (navigationClicked)
			return;
		
		super.slotClicked(slot, slotId, button, actionType);
	}
	@Override
	public boolean allowEnchantmentCombine() {
		return true;
	}
	@Override
	public LockedSlotsInfo getLockedSlotsInfo() {
		LockedSlotsInfo info = (ref instanceof ItemReference itemRef && itemRef.isLocked()
				? LockedSlotsInfo.ITEMS_LOCKED : LockedSlotsInfo.NONE).copy();
		if (ref instanceof ItemReference itemRef)
			info.addPlayerSlot(itemRef);
		
		for (int slot = numSlots; slot < 27; slot++)
			info.addContainerSlot(slot);
		
		return info;
	}
	@Override
	public void onChange() {
		save();
	}
	private void save() {
		ItemStack[] contents = new ItemStack[this.menu.getContainer().getContainerSize()];
		for (int i = 0; i < contents.length; i++)
			contents[i] = this.menu.getContainer().getItem(i);
		ContainerIOs.write(localNBT, contents);
		
		saved = false;
		ref.saveLocalNBT(localNBT, () -> {
			saved = true;
		});
	}
	
	public boolean keyPressed(KeyEvent keyInput) {
		if (MainUtil.client.options.keyInventory.matches(keyInput)) {
			ref.showParent();
			return true;
		}
		
		if (hoveredSlot != null && (hoveredSlot.index < numSlots || hoveredSlot.container != this.menu.getContainer())) {
			if (keyInput.key() != GLFW.GLFW_KEY_DELETE || !getLockedSlotsInfo().isBlocked(hoveredSlot, true)) {
				if (handleKeybind(keyInput.key(), hoveredSlot, () -> show(ref), slot -> getContainerRef(slot.getContainerSlot())))
					return true;
			}
		}
		
		return super.keyPressed(keyInput);
	}
	private ContainerItemReference<L> getContainerRef(int slot) {
		ItemStack[] contents = new ItemStack[this.menu.getContainer().getContainerSize()];
		for (int i = 0; i < contents.length; i++)
			contents[i] = this.menu.getContainer().getItem(i);
		return new ContainerItemReference<>(ref, ContainerIOs.getWrittenSlotIndex(localNBT, contents, slot));
	}
	
	@Override
	protected void containerTick() {
		if (!ref.exists())
			ref.showParent();
	}
	
	@Override
	public boolean isPauseScreen() {
		return true;
	}
	
	public NBTReference<L> getReference() {
		return ref;
	}
	
	@Override
	public void onClose() {
		ref.escapeParent();
	}
	@Override
	public void removed() {
		for (int i = numSlots; i < 27; i++) { // Items that will get deleted
			ItemStack item = this.menu.getContainer().getItem(i);
			if (item != null && !item.isEmpty())
				MainUtil.get(item, true);
		}
	}
	
}
