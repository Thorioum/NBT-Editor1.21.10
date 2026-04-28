package com.luneruniverse.minecraft.mod.nbteditor.misc;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;

import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.async.ItemSize;
import com.luneruniverse.minecraft.mod.nbteditor.commands.get.GetLostItemCommand;
import com.luneruniverse.minecraft.mod.nbteditor.containers.ContainerIOs;
import com.luneruniverse.minecraft.mod.nbteditor.mixin.ChatScreenAccessor;
import com.luneruniverse.minecraft.mod.nbteditor.mixin.AbstractContainerScreenAccessor;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTextEvents;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientHandledScreen;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.ItemTagReferences;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.Enchants;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.hideflags.HideFlag;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.components.EditBox;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.Tag;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.ChatFormatting;

// Non-mixin classes in the mixin package doesn't work well
public class MixinLink {
	
	public static boolean CLIENT_LOADED = false;
	
	
	private static final Map<String, Runnable> events = new HashMap<>();
	public static Style withRunClickEvent(Style style, Runnable onClick) {
		String id = "\0nbteditor_runnable@" + new Random().nextLong(); // \0 is not valid in file paths on most OSs
		events.put(id, onClick);
		return style.withClickEvent(MVTextEvents.ClickAction.OPEN_FILE.newEvent(id));
	}
	public static boolean tryRunClickEvent(String id) {
		Runnable onClick = events.get(id);
		if (onClick != null) {
			onClick.run();
			return true;
		}
		return false;
	}
	
	
	public static File screenshotTarget;
	
	
	public static int[] getTooltipSize(List<ClientTooltipComponent> tooltip) {
		int width = 0;
		int height = (tooltip.size() == 1 ? -2 : 0);
		for (ClientTooltipComponent line : tooltip) {
			width = Math.max(width, line.getWidth(MainUtil.client.font));
			height += MVMisc.getTooltipComponentHeight(line);
		}
		return new int[] {width, height};
	}
	public static void renderTooltipFromComponents(Matrix3x2fStack matrices, int x, int y, int width, int height, int screenWidth, int screenHeight) {
		x -= 5;
		y -= 5;
		width += 10;
		height += 10;
		
		int newX = x;
		int newY = y;
		int newWidth = width;
		int newHeight = height;
		
		if (width > screenWidth || height > screenHeight) {
			double scale = Math.min((double) screenWidth / width, (double) screenHeight / height);
			newWidth = (int) (width * scale);
			newHeight = (int) (height * scale);
			
			int[] mousePos = MainUtil.getMousePos();
			newX = mousePos[0] + 12;
			newY = mousePos[1] - 12;
		}
		
		if (newX < 0)
			newX = 0;
		else if (newX + newWidth > screenWidth)
			newX = screenWidth - newWidth;
		
		if (newY < 0)
			newY = 0;
		else if (newY + newHeight > screenHeight)
			newY = screenHeight - newHeight;
		
		MainUtil.mapMatrices(matrices, x, y, width, height, newX, newY, newWidth, newHeight);
	}
	
	
	public static final Set<Thread> hiddenExceptionHandlers = Collections.synchronizedSet(new HashSet<>());
	@SuppressWarnings("serial")
	public static class HiddenException extends RuntimeException {
		public HiddenException(String msg, Throwable cause) {
			super(msg, cause);
		}
	}
	public interface DangerousRunnable {
		public void run() throws Throwable;
	}
	public static void throwHiddenException(DangerousRunnable toRun) throws Throwable {
		hiddenExceptionHandlers.add(Thread.currentThread());
		try {
			toRun.run();
		} catch (HiddenException e) {
			throw e.getCause();
		} finally {
			hiddenExceptionHandlers.remove(Thread.currentThread());
		}
	}
	
	
	public static void renderChatLimitWarning(ChatScreen source, Matrix3x2fStack matrices) {
		if (!ConfigScreen.isChatLimitExtended())
			return;
		
		EditBox chatField = ((ChatScreenAccessor) source).getInput();
		if (chatField.getValue().length() > 256) {
			MVDrawableHelper.fill(matrices, source.width - 202, source.height - 40, source.width - 2, source.height - 14, 0xAAFFAA00);
			Font textRenderer = MainUtil.client.font;
			MVDrawableHelper.drawCenteredTextWithShadow(matrices, textRenderer, TextInst.translatable("nbteditor.chat_length_warning_1"), source.width - 102, source.height - 40 + textRenderer.lineHeight / 2, 0xFFAA5500);
			MVDrawableHelper.drawCenteredTextWithShadow(matrices, textRenderer, TextInst.translatable("nbteditor.chat_length_warning_2"), source.width - 102, source.height - 28 + textRenderer.lineHeight / 2, 0xFFAA5500);
		}
	}
	
	
	public static final Set<Thread> specialNumbers = Collections.synchronizedSet(new HashSet<>());
	public static Tag parseSpecialElement(StringReader reader) throws CommandSyntaxException {
		specialNumbers.add(Thread.currentThread());
		try {
			return MVMisc.parseNbt(reader);
		} finally {
			specialNumbers.remove(Thread.currentThread());
		}
	}
	
	
	public static void onMouseClick(AbstractContainerScreen<?> source, Slot slot, int slotId, int button, ContainerInput actionType, CallbackInfo info) {
		if (!source.getMenu().getCarried().isEmpty())
			GetLostItemCommand.addToHistory(source.getMenu().getCarried());
		
		boolean creativeInv = (source instanceof CreativeModeInventoryScreen);
		
		if (!creativeInv && !NBTEditorClient.SERVER_CONN.isScreenEditable())
			return;
		
		if (slot instanceof CreativeModeInventoryScreen.SlotWrapper creativeSlot)
			slot = creativeSlot.target;
		
		if (actionType == ContainerInput.PICKUP && slot != null &&
				(slot.container == MainUtil.client.player.getInventory() || !creativeInv) &&
				(!(source instanceof InventoryScreen) || slot.index > 4)) {
			ItemStack cursor = source.getMenu().getCarried();
			ItemStack item = slot.getItem();
			if (cursor == null || cursor.isEmpty() || item == null || item.isEmpty())
				return;
			if (cursor.getItem() == Items.ENCHANTED_BOOK || item.getItem() == Items.ENCHANTED_BOOK) {
				if (cursor.getItem() != Items.ENCHANTED_BOOK) { // Make sure the cursor is an enchanted book
					ItemStack temp = cursor;
					cursor = item;
					item = temp;
				}
				
				Enchants enchants = ItemTagReferences.ENCHANTMENTS.get(item);
				enchants.addEnchants(ItemTagReferences.ENCHANTMENTS.get(cursor).getEnchants());
				ItemTagReferences.ENCHANTMENTS.set(item, enchants);
				
				ItemReference.getContainerItem(source, slot).saveItem(item);
				NBTEditorClient.CURSOR_MANAGER.setCursor(ItemStack.EMPTY);
				
				info.cancel();
			}
		}
	}
	
	public static void keyPressed(AbstractContainerScreen<?> source, int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> info) {
		boolean creativeInv = (source instanceof CreativeModeInventoryScreen);
		
		Slot hoveredSlot = ((AbstractContainerScreenAccessor) source).getHoveredSlot();
		
		if (hoveredSlot instanceof CreativeModeInventoryScreen.SlotWrapper creativeSlot)
			hoveredSlot = creativeSlot.target;
		
		if (hoveredSlot != null &&
				((creativeInv && hoveredSlot.container == MainUtil.client.player.getInventory()) ||
						(!creativeInv && NBTEditorClient.SERVER_CONN.isScreenEditable())) &&
				(!(source instanceof InventoryScreen) || hoveredSlot.index > 4) &&
				(ConfigScreen.isAirEditable() || hoveredSlot.getItem() != null && !hoveredSlot.getItem().isEmpty())) {
			if (ClientHandledScreen.handleKeybind(keyCode, hoveredSlot.getItem(),
					ItemReference.getContainerItem(source, hoveredSlot))) {
				info.setReturnValue(true);
			}
		}
	}
	
	
	public static final List<ItemStack> ENCHANT_GLINT_FIX = new ArrayList<>();
	
	
	/**
	 * Only in 1.20.5 or higher
	 */
	public static final Cache<BookViewScreen.BookAccess, Boolean> WRITTEN_BOOK_CONTENTS = CacheBuilder.newBuilder().weakKeys().build();
	
	
	public static void modifyTooltip(ItemStack source, List<Component> tooltip) {
		// Tooltips are requested for all items when GameJoinS2CPacket is received to setup the creative inventory's search
		// The world doesn't exist yet, so this causes the game to freeze when an exception from this mixin breaks everything
		if (MainUtil.client.level == null)
			return;
		
		if (HideFlag.TOOLTIP != null && ItemTagReferences.HIDE_FLAGS.get(source).get(HideFlag.TOOLTIP))
			return;
		
		ConfigScreen.ItemSizeFormat sizeConfig = ConfigScreen.getItemSizeFormat();
		if (sizeConfig != ConfigScreen.ItemSizeFormat.HIDDEN) {
			OptionalLong loadingSize = ItemSize.getItemSize(source, sizeConfig.isCompressed());
			String displaySize;
			ChatFormatting sizeFormat;
			if (loadingSize.isEmpty()) {
				displaySize = "...";
				sizeFormat = ChatFormatting.GRAY;
			} else {
				long size = loadingSize.getAsLong();
				int magnitude = sizeConfig.getMagnitude();
				if (magnitude == 0) {
					if (size < 1000)
						magnitude = 1;
					else if (size < 1000000)
						magnitude = 1000;
					else if (size < 1000000000)
						magnitude = 1000000;
					else
						magnitude = 1000000000;
				}
				if (magnitude == 1)
					displaySize = "" + size;
				else
					displaySize = String.format("%.1f", (double) size / magnitude);
				switch (magnitude) {
					case 1 -> {
						displaySize += "B";
						sizeFormat = ChatFormatting.GREEN;
					}
					case 1000 -> {
						displaySize += "KB";
						sizeFormat = ChatFormatting.YELLOW;
					}
					case 1000000 -> {
						displaySize += "MB";
						sizeFormat = ChatFormatting.RED;
					}
					case 1000000000 -> {
						displaySize += "GB";
						sizeFormat = null;
					}
					default -> throw new IllegalStateException("Invalid magnitude!");
				}
			}
			TextColor sizeColor = (sizeFormat != null ? TextColor.fromLegacyFormat(sizeFormat) :
				TextColor.fromRgb(Color.HSBtoRGB((System.currentTimeMillis() % 1000) / 1000.0f, 1, 1)));
			tooltip.add(TextInst.translatable("nbteditor.item_size." + (sizeConfig.isCompressed() ? "compressed" : "uncompressed"),
					TextInst.literal(displaySize).styled(style -> style.withColor(sizeColor))));
		}
		
		if (!ConfigScreen.isKeybindsHidden()) {
			// Checking slots in your hotbar vs item selection is difficult, so the lore is just disabled in non-inventory tabs
			boolean creativeInv = MVMisc.isCreativeInventoryTabSelected();
			
			if (creativeInv || (!(MainUtil.client.screen instanceof CreativeModeInventoryScreen) &&
					NBTEditorClient.SERVER_CONN.isScreenEditable())) {
				tooltip.add(TextInst.translatable("nbteditor.keybind.edit"));
				tooltip.add(TextInst.translatable("nbteditor.keybind.factory"));
				if (ContainerIOs.isSupported(source))
					tooltip.add(TextInst.translatable("nbteditor.keybind.container"));
				if (source.getItem() == Items.ENCHANTED_BOOK)
					tooltip.add(TextInst.translatable("nbteditor.keybind.enchant"));
				tooltip.add(TextInst.translatable("nbteditor.keybind.delete"));
			}
		}
	}
	
	
	public static final WeakHashMap<Runnable, Boolean> CATCH_BYPASSING_TASKS = new WeakHashMap<>();
	public static void executeCrashableTask(Runnable task) {
		CATCH_BYPASSING_TASKS.put(task, true);
		MainUtil.client.execute(task);
	}
	
	
	public static final WeakHashMap<Tooltip, Boolean> NEW_TOOLTIPS = new WeakHashMap<>();
	
	
	// MinecraftClient#thread is set after the ClientModInitializers are run
	public static volatile Thread MAIN_THREAD;
	public static boolean isOnMainThread() {
		return Thread.currentThread() == MAIN_THREAD;
	}
	
	
	public static final Map<Thread, ItemStack> ITEM_BEING_RENDERED = Collections.synchronizedMap(new WeakHashMap<>());
	
	
	public static final Set<Thread> SET_CHANGES = Collections.synchronizedSet(new HashSet<>());
	public static void setChanges(ItemStack item, DataComponentPatch changes) {
		try {
			SET_CHANGES.add(Thread.currentThread());
			item.applyComponentsAndValidate(changes);
		} finally {
			SET_CHANGES.remove(Thread.currentThread());
		}
	}
	
}
