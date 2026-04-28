package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.clientchest.ClientChestHelper;
import com.luneruniverse.minecraft.mod.nbteditor.clientchest.ClientChestPage;
import com.luneruniverse.minecraft.mod.nbteditor.clientchest.DynamicItems;
import com.luneruniverse.minecraft.mod.nbteditor.clientchest.PageLoadLevel;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.EditableText;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ClientChestItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ClientChestDataVersionScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.LoadingScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.util.FancyConfirmScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.NamedTextFieldWidget;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.network.chat.Component;

import static com.luneruniverse.minecraft.mod.nbteditor.NBTEditor.hasShiftDown;

public class ClientChestScreen extends ClientHandledScreen {
	
	public static int PAGE = 0;
	public static int prevPageJumpTarget;
	public static int nextPageJumpTarget;
	
	public static void show() {
		LoadingScreen.show(
				ClientChestHelper.getPage(PAGE, PageLoadLevel.DYNAMIC_ITEMS),
				NBTEditorClient.CURSOR_MANAGER::closeRoot,
				(loaded, optional) -> {
					if (optional.isEmpty()) {
						NBTEditorClient.CURSOR_MANAGER.closeRoot();
						return;
					}
					
					ClientChestPage pageData = optional.get();
					
					if (!pageData.isInThisVersion()) {
						NBTEditorClient.CURSOR_MANAGER.closeRoot();
						MainUtil.client.setScreen(new ClientChestDataVersionScreen(pageData.dataVersion()));
						return;
					}
					
					if (MainUtil.client.screen instanceof ClientChestScreen screen) {
						screen.setPageData(pageData);
						MainUtil.setTextFieldValueSilently(screen.pageField, (PAGE + 1) + "", true);
						screen.updatePageNavigation();
					} else {
						ClientChestScreen screen = new ClientChestScreen();
						screen.setPageData(pageData);
						NBTEditorClient.CURSOR_MANAGER.showBranch(screen);
						NBTEditorClient.CLIENT_CHEST.warnIfCorrupt();
					}
				});
	}
	
	private DynamicItems dynamicItems;
	private boolean navigationClicked;
	private NamedTextFieldWidget nameField;
	private Button prevPage;
	private EditBox pageField;
	private Button nextPage;
	private Button prevPageJump;
	private Button nextPageJump;
	
	private ClientChestScreen() {
		super(6, TextInst.translatable("nbteditor.client_chest"));
	}
	private void setPageData(ClientChestPage pageData) {
		ItemStack[] items = pageData.getItemsOrThrow();
		for (int i = 0; i < items.length; i++)
			menu.getSlot(i).set(items[i] == null ? ItemStack.EMPTY : items[i].copy());
		dynamicItems = pageData.dynamicItems();
	}
	
	@Override
	protected void init() {
		this.clearWidgets();
		super.init();
		leftPos += 87 / 2;
		
		nameField = new NamedTextFieldWidget(this.leftPos - 87, this.topPos, 83, 16) {
			@Override
			public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
				boolean output = super.mouseClicked(click, doubled);
				if (output)
					navigationClicked = true;
				return output;
			}
			@Override
			public boolean keyPressed(KeyEvent keyInput) {
				int keyCode = keyInput.key();
				if (keyCode == GLFW.GLFW_KEY_ENTER && !nameField.isValid()) {
					nameField.setValid(true);
					ClientChestHelper.setNameOfPage(PAGE, nameField.getValue());
					return true;
				}
				return super.keyPressed(keyInput);
			}
		}.name(TextInst.translatable("nbteditor.client_chest.page_name"));
		nameField.setMaxLength(Integer.MAX_VALUE);
		nameField.setResponder(name -> {
			if (NBTEditorClient.CLIENT_CHEST.isNameUsedByOther(name, PAGE)) {
				nameField.setValid(false);
				return;
			}
			nameField.setValid(true);
			ClientChestHelper.setNameOfPage(PAGE, name);
		});
		this.addRenderableWidget(nameField);
		
		pageField = new EditBox(font, this.leftPos - 63, this.topPos + 22, 35, 16, TextInst.of("")) {
			@Override
			public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
				boolean output = super.mouseClicked(click, doubled);
				if (output)
					navigationClicked = true;
				return output;
			}
		};
		pageField.setMaxLength((NBTEditorClient.CLIENT_CHEST.getPageCount() + "").length());
		pageField.setValue((PAGE + 1) + "");
		pageField.setResponder(str -> {
			if (str.isEmpty() || str.equals("+") || str.equals("-"))
				return;
			
			int intVal = Integer.parseInt(str);
			if (intVal > 0) {
				PAGE = intVal - 1;
				show();
			}
		});
		//pageField.setFilter(MainUtil.intPredicate(() -> 0, NBTEditorClient.CLIENT_CHEST::getPageCount, true));
		this.addRenderableWidget(pageField);
		
		EditableText prevKeybind = TextInst.translatable("nbteditor.keybind.page.down");
		EditableText nextKeybind = TextInst.translatable("nbteditor.keybind.page.up");
		if (ConfigScreen.isInvertedPageKeybinds()) {
			EditableText temp = prevKeybind;
			prevKeybind = nextKeybind;
			nextKeybind = temp;
		}
		
		this.addRenderableWidget(prevPage = MVMisc.newButton(this.leftPos - 87, this.topPos + 20, 20, 20, TextInst.of("<"), btn -> {
			navigationClicked = true;
			prevPage();
		}, ConfigScreen.isKeybindsHidden() ? null : new MVTooltip(TextInst.literal("")
				.append(prevKeybind).append(TextInst.translatable("nbteditor.keybind.page.prev")))));
		
		this.addRenderableWidget(nextPage = MVMisc.newButton(this.leftPos - 24, this.topPos + 20, 20, 20, TextInst.of(">"), btn -> {
			navigationClicked = true;
			nextPage();
		}, ConfigScreen.isKeybindsHidden() ? null : new MVTooltip(TextInst.literal("")
				.append(nextKeybind).append(TextInst.translatable("nbteditor.keybind.page.next")))));
		
		this.addRenderableWidget(prevPageJump = MVMisc.newButton(this.leftPos - 87, this.topPos + 44, 39, 20, TextInst.of("<<"), btn -> {
			navigationClicked = true;
			prevPageJump();
		}, ConfigScreen.isKeybindsHidden() ? null : new MVTooltip(TextInst.translatable("nbteditor.keybind.page.shift")
				.append(prevKeybind).append(TextInst.translatable("nbteditor.keybind.page.prev_jump")))));
		
		this.addRenderableWidget(nextPageJump = MVMisc.newButton(this.leftPos - 43, this.topPos + 44, 39, 20, TextInst.of(">>"), btn -> {
			navigationClicked = true;
			nextPageJump();
		}, ConfigScreen.isKeybindsHidden() ? null : new MVTooltip(TextInst.translatable("nbteditor.keybind.page.shift")
				.append(nextKeybind).append(TextInst.translatable("nbteditor.keybind.page.next_jump")))));
		
		this.addRenderableWidget(MVMisc.newButton(this.leftPos - 87, this.topPos + 68, 83, 20, ConfigScreen.isLockSlots() ? TextInst.translatable("nbteditor.client_chest.slots.unlock") : TextInst.translatable("nbteditor.client_chest.slots.lock"), btn -> {
			navigationClicked = true;
			if (ConfigScreen.isLockSlotsRequired()) {
				btn.active = false;
				ConfigScreen.setLockSlots(true);
			} else
				ConfigScreen.setLockSlots(!ConfigScreen.isLockSlots());
			btn.setMessage(ConfigScreen.isLockSlots() ? TextInst.translatable("nbteditor.client_chest.slots.unlock") : TextInst.translatable("nbteditor.client_chest.slots.lock"));
		})).active = !ConfigScreen.isLockSlotsRequired();
		
		this.addRenderableWidget(MVMisc.newButton(this.leftPos - 87, this.topPos + 92, 83, 20, TextInst.translatable("nbteditor.client_chest.reload_page"), btn -> {
			navigationClicked = true;
			LoadingScreen.show(ClientChestHelper.reloadPage(PAGE), this::onClose, (loaded, pageData) -> show());
		}));
		
		this.addRenderableWidget(MVMisc.newButton(this.leftPos - 87, this.topPos + 116, 83, 20, TextInst.translatable("nbteditor.client_chest.clear_page"), btn -> {
			navigationClicked = true;
			minecraft.setScreen(new FancyConfirmScreen(value -> {
				if (value) {
					menu.getContainer().clearContent();
					dynamicItems = new DynamicItems();
					save();
				}
				
				minecraft.setScreen(ClientChestScreen.this);
			}, TextInst.translatable("nbteditor.client_chest.clear_page.title"), TextInst.translatable("nbteditor.client_chest.clear_page.desc"),
					TextInst.translatable("nbteditor.client_chest.clear_page.yes"), TextInst.translatable("nbteditor.client_chest.clear_page.no")));
		}));
		
		
		updatePageNavigation();
	}
	public void updatePageNavigation() {
		nameField.setValue(NBTEditorClient.CLIENT_CHEST.getNameFromPage(PAGE));
		
		int[] jumps = NBTEditorClient.CLIENT_CHEST.getNearestPOIs(PAGE);
		int maxPage = NBTEditorClient.CLIENT_CHEST.getPageCount() - 1;
		prevPageJumpTarget = jumps[0] == -1 ? (PAGE == 0 ? -1 : 0) : jumps[0];
		nextPageJumpTarget = jumps[1] == -1 ? (PAGE == maxPage ? -1 : maxPage) : jumps[1];
		
		prevPage.active = PAGE != 0;
		nextPage.active = PAGE != maxPage;
		prevPageJump.active = prevPageJumpTarget != -1;
		nextPageJump.active = nextPageJumpTarget != -1;
	}
	
	@Override
	protected void containerTick() {
		nameField.tick();
		//pageField.tick();
	}
	
	public boolean keyPressed(KeyEvent keyInput) {
		int keyCode = keyInput.key();
		navigationClicked = false;
		
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			onClose();
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_PAGE_UP || keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
			boolean prev = (keyCode == GLFW.GLFW_KEY_PAGE_DOWN);
			if (ConfigScreen.isInvertedPageKeybinds())
				prev = !prev;
			boolean jump = hasShiftDown();
			if (prev) {
				if (jump)
					prevPageJump();
				else
					prevPage();
			} else {
				if (jump)
					nextPageJump();
				else
					nextPage();
			}
			return true;
		}
		
		if (hoveredSlot != null) {
			boolean lockedSlot = (hoveredSlot.container == menu.getContainer() &&
					dynamicItems.isSlotLocked(hoveredSlot.getContainerSlot()));
			if (!lockedSlot || keyCode == GLFW.GLFW_KEY_DELETE) {
				if (handleKeybind(keyCode, hoveredSlot, ClientChestScreen::show, slot -> new ClientChestItemReference(PAGE, slot.getContainerSlot()))) {
					if (keyCode == GLFW.GLFW_KEY_DELETE && lockedSlot)
						dynamicItems.remove(hoveredSlot.getContainerSlot());
					return true;
				}
			}
		}
		
		return !this.nameField.keyPressed(keyInput) && !this.nameField.canConsumeInput() &&
				!this.pageField.keyPressed(keyInput) && !this.pageField.canConsumeInput()
				? super.keyPressed(keyInput) : true;
	}
	
	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		navigationClicked = false;
		MVMisc.setKeyboardRepeatEvents(this.nameField.mouseClicked(click, doubled) ||
				this.pageField.mouseClicked(click, doubled));
		super.mouseClicked(click, doubled);
		return true;
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
		LockedSlotsInfo info = (ConfigScreen.isLockSlots() ? LockedSlotsInfo.ITEMS_LOCKED : LockedSlotsInfo.NONE).copy();
		
		for (int slot : dynamicItems.getLockedSlots())
			info.addContainerSlot(slot);
		
		return info;
	}
	@Override
	public void onChange() {
		save();
	}
	
	private void save() {
		ItemStack[] items = new ItemStack[54];
		for (int i = 0; i < menu.getContainer().getContainerSize(); i++)
			items[i] = menu.getContainer().getItem(i).copy();
		
		ClientChestHelper.setPage(PAGE, items, dynamicItems);
	}
	
	@Override
	protected Component getRenderedTitle() {
		EditableText title = TextInst.copy(this.title).append(" (" + (PAGE + 1) + ")");
		return NBTEditorClient.CLIENT_CHEST.isProcessingPage(PAGE) ? title.append("*") : title;
	}
	
	@Override
	public boolean isPauseScreen() {
		return true;
	}
	
	@Override
	public void removed() {
		MVMisc.setKeyboardRepeatEvents(false);
	}
	
	private void prevPage() {
		if (!prevPage.active)
			return;
		PAGE--;
		show();
	}
	private void nextPage() {
		if (!nextPage.active)
			return;
		PAGE++;
		show();
	}
	
	private void prevPageJump() {
		if (!prevPageJump.active)
			return;
		PAGE = prevPageJumpTarget;
		show();
	}
	private void nextPageJump() {
		if (!nextPageJump.active)
			return;
		PAGE = nextPageJumpTarget;
		show();
	}
	
}
