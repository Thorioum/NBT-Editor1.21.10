package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import java.util.function.Function;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.commands.get.GetLostItemCommand;
import com.luneruniverse.minecraft.mod.nbteditor.containers.ContainerIOs;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IgnoreCloseScreenPacket;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.OldEventBehavior;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.InventoryItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.factories.LocalFactoryScreen;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.ItemTagReferences;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.Enchants;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import static com.luneruniverse.minecraft.mod.nbteditor.NBTEditor.hasControlDown;
import static com.luneruniverse.minecraft.mod.nbteditor.NBTEditor.hasShiftDown;

public class ClientHandledScreen extends ContainerScreen implements OldEventBehavior, IgnoreCloseScreenPacket {
	
	private static final Identifier TEXTURE = IdentifierInst.of("textures/gui/container/generic_54.png");
	
	public static boolean handleKeybind(int keyCode, Slot hoveredSlot, Runnable parent, Function<Slot, ItemReference> containerRef) {
		if (hoveredSlot != null &&
				(ConfigScreen.isAirEditable() || hoveredSlot.getItem() != null && !hoveredSlot.getItem().isEmpty())) {
			ItemReference ref;
			if (hoveredSlot.container == MainUtil.client.player.getInventory()) {
				ref = new InventoryItemReference(hoveredSlot.getContainerSlot());
				if (parent != null)
					((InventoryItemReference) ref).setParent(parent);
			} else
				ref = containerRef.apply(hoveredSlot);
			return handleKeybind(keyCode, hoveredSlot.getItem(), ref);
		}
		return false;
	}
	public static boolean handleKeybind(int keyCode, ItemStack item, ItemReference ref) {
		if (keyCode == GLFW.GLFW_KEY_DELETE) {
			if (item == null || item.isEmpty())
				return false;
			GetLostItemCommand.addToHistory(item);
			ref.saveItem(ItemStack.EMPTY);
			return true;
		}
		if (keyCode != GLFW.GLFW_KEY_SPACE)
			return false;
		
		boolean notAir = item != null && !item.isEmpty();
		if (hasControlDown()) {
			if (notAir && ContainerIOs.isSupported(item))
				com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ContainerScreen.show(ref);
		} else if (hasShiftDown()) {
			if (notAir)
				MainUtil.client.setScreen(new LocalFactoryScreen<>(ref));
		} else
			MainUtil.client.setScreen(new NBTEditorScreen<>(ref));
		
		return true;
	}
	
	private ServerInventoryManager serverInv;
	
	protected ClientHandledScreen(int rows, Component title) {
		super(new ClientScreenHandler(rows), MainUtil.client.player.getInventory(), title);
		((ClientScreenHandler) menu).setScreen(this);
		menu.suppressRemoteUpdates();
	}
	
	protected void setSlotTextures(Identifier... textures) {
		((ClientScreenHandler) menu).setSlotTextures(textures);
	}
	
	public ServerInventoryManager getServerInventoryManager() {
		return serverInv;
	}
	
	@Override
	protected void init() {
		super.init();
		serverInv = new ServerInventoryManager();
	}
	
	protected void drawBackground(Matrix3x2fStack matrices, float delta, int mouseX, int mouseY) {
		MVDrawableHelper.drawTexture(matrices, TEXTURE, leftPos, topPos, 0, 0, imageWidth, menu.getRowCount() * 18 + 17);
		MVDrawableHelper.drawTexture(matrices, TEXTURE, leftPos, topPos + menu.getRowCount() * 18 + 17, 0, 126, imageWidth, 96);
		
		if (showLogo())
			MainUtil.renderLogo(matrices);
	}
	@Override
	public final void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		drawBackground(MVDrawableHelper.getMatrices(context), delta, mouseX, mouseY);
	}
	protected final void method_2389(Matrix3x2fStack matrices, float delta, int mouseX, int mouseY) {
		drawBackground(matrices, delta, mouseX, mouseY);
	}
	protected boolean showLogo() {
		return true;
	}
	
	protected void drawForeground(Matrix3x2fStack matrices, int mouseX, int mouseY) {
		getLockedSlotsInfo().renderLockedHighlights(matrices, menu, true, false, true);
		
		MVDrawableHelper.drawTextWithoutShadow(matrices, font, getRenderedTitle(), titleLabelX, titleLabelY, 4210752);
		MVDrawableHelper.drawTextWithoutShadow(matrices, font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 4210752);
	}
	@Override
	protected final void extractLabels(GuiGraphicsExtractor context, int mouseX, int mouseY) {
		drawForeground(MVDrawableHelper.getMatrices(context), mouseX, mouseY);
	}
	protected final void method_2388(Matrix3x2fStack matrices, int mouseX, int mouseY) {
		drawForeground(matrices, mouseX, mouseY);
	}
	protected Component getRenderedTitle() {
		return title;
	}
	
	public void setInitialFocus(GuiEventListener element) {
		MVMisc.setInitialFocus(this, element, super::setInitialFocus);
	}
	@Override
	protected void setInitialFocus() {}
	
	public boolean isPauseScreen() {
		return false;
	}
	
	@Override
	public final void tick() {
		super.tick();
		Version.newSwitch()
				.range("1.17.1", null, () -> {})
				.range(null, "1.17", () -> {
					if (minecraft.player.isAlive() && !minecraft.player.isRemoved())
						containerTick();
				})
				.run();
	}
	@Override
	protected void containerTick() {}
	
	public void onClose() {
		NBTEditorClient.CURSOR_MANAGER.closeRoot();
	}
	@Override
	public void removed() {
		serverInv = null;
		// Don't always drop cursor in older versions
	}
	
	
	@Override
	protected void slotClicked(Slot slot, int slotId, int button, ContainerInput actionType) {
		if (slot != null) {
			LockedSlotsInfo lockedSlotsInfo = getLockedSlotsInfo();
			if (lockedSlotsInfo.isBlocked(slot, button, actionType, false)) {
				if (lockedSlotsInfo.isCopyLockedItem() && slot.container != minecraft.player.getInventory()) {
					switch (actionType) {
						case PICKUP, PICKUP_ALL -> {
							ItemStack item = slot.getItem();
							if (item.isEmpty())
								break;
							if (!menu.getCarried().isEmpty() &&
									!ItemStack.isSameItemSameComponents(item, menu.getCarried())) {
								GetLostItemCommand.loseItem(menu.getCarried());
								menu.setCarried(ItemStack.EMPTY);
							}
							ItemStack cursor = menu.getCarried();
							if (!cursor.isEmpty()) {
								cursor.setCount(Math.min(cursor.getMaxStackSize(), cursor.getCount() + item.getCount()));
								menu.setCarried(cursor);
							} else
								menu.setCarried(item.copy());
							serverInv.updateServer();
						}
						case CLONE -> {
							ItemStack item = slot.getItem();
							if (item.isEmpty())
								break;
							if (!menu.getCarried().isEmpty())
								break;
							item = item.copy();
							item.setCount(item.getMaxStackSize());
							menu.setCarried(item);
							serverInv.updateServer();
						}
						case QUICK_MOVE -> {
							ItemStack prevItem = slot.getItem().copy();
							ClientScreenHandlerSlot.unlockDuring(() -> menu.clicked(slot.index, button, actionType, MainUtil.client.player));
							slot.set(prevItem);
							serverInv.updateServer();
						}
						case THROW -> {
							ItemStack item = slot.getItem();
							if (button == 0) {
								item = item.copy();
								item.setCount(1);
							}
							MainUtil.dropCreativeStack(item);
						}
						case SWAP -> {}
						case QUICK_CRAFT -> throw new IllegalArgumentException("Invalid SlotActionType: " + actionType);
					}
				}
				return;
			}
		}
		
		if (!(this instanceof CursorHistoryScreen))
			GetLostItemCommand.addToHistory(menu.getCarried());
		
		if (!(slot != null && allowEnchantmentCombine() && NBTEditor.hasControlDown() && tryCombineEnchantments(slot, actionType)))
			menu.clicked(slot == null ? slotId : slot.index, button, actionType, MainUtil.client.player);
		
		if (!(this instanceof CursorHistoryScreen))
			GetLostItemCommand.addToHistory(menu.getCarried());
		
		serverInv.updateServer();
		onChange();
	}
	
	private boolean tryCombineEnchantments(Slot slot, ContainerInput actionType) {
		if (actionType == ContainerInput.PICKUP && slot != null) {
			ItemStack cursor = menu.getCarried();
			ItemStack item = slot.getItem();
			if (cursor == null || cursor.isEmpty() || item == null || item.isEmpty())
				return false;
			if (cursor.getItem() == Items.ENCHANTED_BOOK || item.getItem() == Items.ENCHANTED_BOOK) {
				if (cursor.getItem() != Items.ENCHANTED_BOOK) { // Make sure the cursor is an enchanted book
					ItemStack temp = cursor;
					cursor = item;
					item = temp;
				}
				
				Enchants enchants = ItemTagReferences.ENCHANTMENTS.get(item);
				enchants.addEnchants(ItemTagReferences.ENCHANTMENTS.get(cursor).getEnchants());
				ItemTagReferences.ENCHANTMENTS.set(item, enchants);
				
				slot.set(item);
				menu.setCarried(ItemStack.EMPTY);
				return true;
			}
		}
		
		return false;
	}
	public boolean allowEnchantmentCombine() {
		return false;
	}
	
	public LockedSlotsInfo getLockedSlotsInfo() {
		return LockedSlotsInfo.NONE;
	}
	public void onChange() {
		
	}
	
}
