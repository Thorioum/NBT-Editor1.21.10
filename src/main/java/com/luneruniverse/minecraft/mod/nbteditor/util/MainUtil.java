package com.luneruniverse.minecraft.mod.nbteditor.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.zip.ZipException;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.async.UpdateCheckerThread;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.ActionResult;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVComponentType;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVGlStateManager;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMatrix4f;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.NBTManagers;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.shaders.MVShader;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.serialization.Dynamic;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fStack;

public class MainUtil {
	
	public static final Minecraft client = Minecraft.getInstance();
	
	// Same as ClientPlayerInteractionManager#clickCreativeSlot, but without a feature flag check
	// Also includes survival bypass
	/**
	 * @param item
	 * @param slot Format: container
	 */
	public static void clickCreativeStack(ItemStack item, int slot) {
		if (NBTEditorClient.SERVER_CONN.isEditingAllowed())
			MVMisc.sendC2SPacket(new ServerboundSetCreativeModeSlotPacket(slot, item.copy()));
	}
	public static void dropCreativeStack(ItemStack item) {
		if (NBTEditorClient.SERVER_CONN.isEditingAllowed() && !item.isEmpty())
			MVMisc.sendC2SPacket(new ServerboundSetCreativeModeSlotPacket(-1, item.copy()));
	}
	
	public static void saveItem(InteractionHand hand, ItemStack item) {
		client.player.setItemInHand(hand, item.copy());
		clickCreativeStack(item, hand == InteractionHand.OFF_HAND ? SlotUtil.createOffHandInContainer() :
			SlotUtil.createHotbarInContainer(client.player.getInventory().selected));
	}
	public static void saveItem(EquipmentSlot slot, ItemStack item) {
		if (slot == EquipmentSlot.MAINHAND)
			saveItem(InteractionHand.MAIN_HAND, item);
		else if (slot == EquipmentSlot.OFFHAND)
			saveItem(InteractionHand.OFF_HAND, item);
		else {
			MVMisc.setArmor(slot, item.copy());
			clickCreativeStack(item, SlotUtil.createArmorInContainer(slot));
		}
	}
	
	/**
	 * @param slot Format: inv
	 * @param item
	 */
	public static void saveItem(int slot, ItemStack item) {
		client.player.getInventory().setItem(slot, item.copy());
		clickCreativeStack(item, SlotUtil.invToContainer(slot));
	}
	
	public static void get(ItemStack item, boolean dropIfNoSpace) {
		Inventory inv = client.player.getInventory();
		item = item.copy();
		
		int slot = inv.getSlotWithRemainingSpace(item);
		if (slot == -1)
			slot = inv.getFreeSlot();
		if (slot == -1) {
			if (dropIfNoSpace) {
				if (item.getCount() > item.getMaxStackSize())
					item.setCount(item.getMaxStackSize());
				dropCreativeStack(item);
			}
		} else {
			item.setCount(item.getCount() + inv.getItem(slot).getCount());
			int overflow = 0;
			if (item.getCount() > item.getMaxStackSize()) {
				overflow = item.getCount() - item.getMaxStackSize();
				item.setCount(item.getMaxStackSize());
			}
			saveItem(slot, item);
			if (overflow != 0) {
				item = item.copy();
				item.setCount(overflow);
				get(item, false);
			}
		}
	}
	public static void getWithMessage(ItemStack item) {
		get(item, true);
		client.player.sendSystemMessage(TextInst.translatable("nbteditor.get.item").append(item.getDisplayName()));
	}
	
	
	
	private static final Identifier LOGO = IdentifierInst.of("nbteditor", "textures/logo.png");
	private static final Identifier LOGO_UPDATE_AVAILABLE = IdentifierInst.of("nbteditor", "textures/logo_update_available.png");
	public static void renderLogo(Matrix3x2fStack matrices) {
		MVDrawableHelper.drawTexture(matrices,
				UpdateCheckerThread.UPDATE_AVAILABLE ? LOGO_UPDATE_AVAILABLE : LOGO, 16, 16, 0, 0, 32, 32, 32, 32);
	}
	
	
	
	public static void drawWrappingString(Matrix3x2fStack matrices, Font renderer, String text, int x, int y, int maxWidth, int color, boolean centerHorizontal, boolean centerVertical) {
		maxWidth = Math.max(maxWidth, renderer.width("ww"));
		
		// Split into breaking spots
		List<String> parts = new ArrayList<>();
		List<Integer> spaces = new ArrayList<>();
		StringBuilder currentPart = new StringBuilder();
		boolean wasUpperCase = false;
		for (char c : text.toCharArray()) {
			if (c == ' ') {
				wasUpperCase = false;
				parts.add(currentPart.toString());
				currentPart.setLength(0);
				spaces.add(parts.size());
				continue;
			}
			
			boolean upperCase = Character.isUpperCase(c);
			if (upperCase != wasUpperCase && !currentPart.isEmpty()) { // Handle NBTEditor; output NBT, Editor; not N, B, T, Editor AND Handle MinionYT; output Minion YT
				if (wasUpperCase) {
					parts.add(currentPart.substring(0, currentPart.length() - 1));
					currentPart.delete(0, currentPart.length() - 1);
				} else {
					parts.add(currentPart.toString());
					currentPart.setLength(0);
				}
			}
			wasUpperCase = upperCase;
			currentPart.append(c);
		}
		if (!currentPart.isEmpty())
			parts.add(currentPart.toString());
		
		// Generate lines, maximizing the number of parts per line
		List<String> lines = new ArrayList<>();
		String line = "";
		int i = 0;
		for (String part : parts) {
			String partAddition = (!line.isEmpty() && spaces.contains(i) ? " " : "") + part;
			if (renderer.width(line + partAddition) > maxWidth) {
				if (!line.isEmpty()) {
					lines.add(line);
					line = "";
				}
				
				if (renderer.width(part) > maxWidth) {
					while (true) {
						int numChars = 1;
						while (renderer.width(part.substring(0, numChars)) < maxWidth)
							numChars++;
						numChars--;
						lines.add(part.substring(0, numChars));
						part = part.substring(numChars);
						if (renderer.width(part) < maxWidth) {
							line = part;
							break;
						}
					}
				} else
					line = part;
			} else
				line += partAddition;
			i++;
		}
		if (!line.isEmpty())
			lines.add(line);
		
		
		// Draw the lines
		for (i = 0; i < lines.size(); i++) {
			line = lines.get(i);
			int offsetY = i * renderer.lineHeight + (centerVertical ? -renderer.lineHeight * lines.size() / 2 : 0);
			if (centerHorizontal)
				MVDrawableHelper.drawCenteredTextWithShadow(matrices, renderer, TextInst.of(line), x, y + offsetY, color);
			else
				MVDrawableHelper.drawTextWithShadow(matrices, renderer, TextInst.of(line), x, y + offsetY, color);
		}
	}
	
	
	public static String colorize(String text) {
		StringBuilder output = new StringBuilder();
		boolean colorCode = false;
		for (char c : text.toCharArray()) {
			if (c == '&')
				colorCode = true;
			else {
				if (colorCode) {
					colorCode = false;
					if ((c + "").replaceAll("[0-9a-fA-Fk-oK-OrR]", "").isEmpty())
						output.append('§');
					else
						output.append('&');
				}
				
				output.append(c);
			}
		}
		if (colorCode)
			output.append('&');
		return output.toString();
	}
	public static String stripColor(String text) {
		return text.replaceAll("\\xA7[0-9a-fA-Fk-oK-OrR]", "");
	}
	
	
	public static Component getBaseItemNameSafely(ItemStack item) {
		if (NBTManagers.COMPONENTS_EXIST) {
			Component name = item.get(DataComponents.ITEM_NAME);
			if (name != null)
				return name;
		}
		return MVMisc.getName(item);
	}
	public static Component getCustomItemNameSafely(ItemStack item) {
		if (NBTManagers.COMPONENTS_EXIST)
			return item.getHoverName();
		return null;
	}
	public static Component getNbtNameSafely(CompoundTag nbt, String key, Supplier<Component> defaultName) {
		if (nbt != null) {
			Tag textNbt = nbt.get(key);
			if (textNbt != null) {
				try {
					Component text = TextInst.fromMinecraft(textNbt);
					if (text != null)
						return text;
				} catch (IllegalArgumentException e) {}
			}
		}
		return defaultName.get();
	}
	
	
	public static DyeColor getDyeColor(ChatFormatting color) {
		switch (color) {
			case AQUA:
				return DyeColor.LIGHT_BLUE;
			case BLACK:
				return DyeColor.BLACK;
			case BLUE:
				return DyeColor.BLUE;
			case DARK_AQUA:
				return DyeColor.CYAN;
			case DARK_BLUE:
				return DyeColor.BLUE;
			case DARK_GRAY:
				return DyeColor.GRAY;
			case DARK_GREEN:
				return DyeColor.GREEN;
			case DARK_PURPLE:
				return DyeColor.PURPLE;
			case DARK_RED:
				return DyeColor.RED;
			case GOLD:
				return DyeColor.ORANGE;
			case GRAY:
				return DyeColor.LIGHT_GRAY;
			case GREEN:
				return DyeColor.LIME;
			case LIGHT_PURPLE:
				return DyeColor.PINK;
			case RED:
				return DyeColor.RED;
			case WHITE:
				return DyeColor.WHITE;
			case YELLOW:
				return DyeColor.YELLOW;
			default:
				return DyeColor.BROWN;
		}
	}
	
	
	public static ItemStack copyAirable(ItemStack item) {
		if (NBTManagers.COMPONENTS_EXIST) {
			ItemStack output = item.transmuteCopy(item.getItem(), item.getCount());
			output.setPopTime(item.getPopTime());
			return output;
		}
		
		ItemStack output = new ItemStack(item.getItem(), item.getCount());
		output.setPopTime(item.getPopTime());
		if (!item.getComponentsPatch().isEmpty())
			output.applyComponents(item.getComponentsPatch());
		return output;
	}
	
	
	public static ItemStack setType(Item type, ItemStack item, int count) {
		if (NBTManagers.COMPONENTS_EXIST)
			return item.transmuteCopy(type, count);
		
		CompoundTag fullData = NBTManagers.ITEM.serialize(item,true);
		fullData.putString("id", MVRegistry.ITEM.getId(type).toString());
		fullData.putInt("Count", count);
		return NBTManagers.ITEM.deserialize(fullData, true);
	}
	public static ItemStack setType(Item type, ItemStack item) {
		return setType(type, item, item.getCount());
	}
	
	
	@SuppressWarnings("unchecked")
	public static <T> Event<T> newEvent(Class<T> clazz) {
		return EventFactory.createArrayBacked(clazz, listeners -> {
			return (T) Proxy.newProxyInstance(MainUtil.class.getClassLoader(), new Class<?>[] {clazz}, (obj, method, args) -> {
				for (T listener : listeners) {
					ActionResult result = (ActionResult) method.invoke(listener, args);
					if (result != ActionResult.PASS)
						return result;
				}
				return ActionResult.PASS;
			});
		});
	}
	
	
	public static CompoundTag readNBT(InputStream in) throws IOException {
		byte[] data = in.readAllBytes();
		try {
			return MVMisc.readCompressedNbt(new ByteArrayInputStream(data));
		} catch (ZipException e) {
			return MVMisc.readNbt(new ByteArrayInputStream(data));
		}
	}
	
	
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss", Locale.ROOT);
	public static String getFormattedCurrentTime() {
		return DATE_TIME_FORMATTER.format(ZonedDateTime.now());
	}
	
	
	public static boolean equals(double a, double b, double epsilon) {
		return Math.abs(a - b) <= epsilon;
	}
	public static boolean equals(double a, double b) {
		return equals(a, b, 1E-5);
	}
	
	
	public static BufferedImage scaleImage(BufferedImage img, int width, int height) {
		Image temp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = output.createGraphics();
		g.drawImage(temp, 0, 0, null);
		g.dispose();
		return output;
	}
	
	
	public static int[] getMousePos() {
		double scale = client.getWindow().getGuiScale();
		int x = (int) (client.mouseHandler.xpos() / scale);
		int y = (int) (client.mouseHandler.ypos() / scale);
		return new int[] {x, y};
	}
	
	
	public static void mapMatrices(Matrix3x2fStack matrices,
			int fromX, int fromY, int fromWidth, int fromHeight,
			int toX, int toY, int toWidth, int toHeight) {
		matrices.translate(toX, toY);
		matrices.scale((float) toWidth / fromWidth, (float) toHeight / fromHeight);
		matrices.translate(-fromX, -fromY);
	}
	
	
	public static Predicate<String> intPredicate(Supplier<Integer> min, Supplier<Integer> max, boolean allowEmpty) {
		return str -> {
			if (str.isEmpty())
				return allowEmpty;
			if (str.equals("+"))
				return allowEmpty && (max == null || max.get() >= 0);
			if (str.equals("-"))
				return allowEmpty && (min == null || min.get() <= 0);
			try {
				int value = Integer.parseInt(str);
				return (min == null || min.get() <= value) && (max == null || value <= max.get());
			} catch (NumberFormatException e) {
				return false;
			}
		};
	}
	public static Predicate<String> intPredicate(Integer min, Integer max, boolean allowEmpty) {
		return intPredicate(() -> min, () -> max, allowEmpty);
	}
	public static Predicate<String> intPredicate() {
		return intPredicate((Supplier<Integer>) null, null, true);
	}
	
	public static Integer parseOptionalInt(String str) {
		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	public static int parseDefaultInt(String str, int defaultValue) {
		Integer output = parseOptionalInt(str);
		if (output == null)
			return defaultValue;
		return output;
	}
	
	
	public static void fillShader(Matrix3x2fStack matrices, MVShader shader, Consumer<VertexConsumer> data, int x, int y, int width, int height) {
		int x1 = x;
		int y1 = y;
		int x2 = x + width;
		int y2 = y + height;
		
		VertexConsumer vertexConsumer = MVMisc.beginDrawingShader(matrices, shader);
		
		data.accept(vertexConsumer);
		MVMisc.nextVertex(vertexConsumer);
		
		data.accept(vertexConsumer);
		MVMisc.nextVertex(vertexConsumer);
		
		data.accept(vertexConsumer);
		MVMisc.nextVertex(vertexConsumer);
		
		data.accept(vertexConsumer);
		MVMisc.nextVertex(vertexConsumer);
		
		MVGlStateManager._disableDepthTest();
		MVMisc.endDrawingShader(matrices, vertexConsumer);
		MVGlStateManager._enableDepthTest();
	}
	
	
	// Based on DataFixTypes
	@SuppressWarnings("unchecked")
	public static <T extends Tag> T update(TypeReference typeRef, T nbt, int oldVersion) {
		return (T) client.getFixerUpper().update(typeRef, new Dynamic<>(NbtOps.INSTANCE, nbt), oldVersion, Version.getDataVersion()).getValue();
	}
	/**
	 * If dataVersionTag is not null and a number, this updates from that - otherwise, this updates from defaultOldVersion
	 */
	public static <T extends Tag> T updateDynamic(TypeReference typeRef, T nbt, Tag dataVersionTag, int defaultOldVersion) {
		int dataVersion = defaultOldVersion;
		if (dataVersionTag != null && dataVersionTag instanceof NumericTag num)
			dataVersion = num.intValue();
		else if (dataVersion == -1)
			return nbt;
		return update(typeRef, nbt, dataVersion);
	}
	/**
	 * If a DataVersion tag exists, this updates from that - otherwise, this updates from defaultOldVersion
	 */
	public static CompoundTag updateDynamic(TypeReference typeRef, CompoundTag nbt, int defaultOldVersion) {
		return updateDynamic(typeRef, nbt, nbt.get("DataVersion"), defaultOldVersion);
	}
	/**
	 * If a DataVersion tag exists, this updates from that - otherwise, nbt is returned
	 */
	public static CompoundTag updateDynamic(TypeReference typeRef, CompoundTag nbt) {
		return updateDynamic(typeRef, nbt, -1);
	}
	
	public static CompoundTag fillId(CompoundTag nbt, String id) {
		if (!NBTManagers.COMPONENTS_EXIST)
			return nbt;
		if (!nbt.contains("id"))
			nbt.putString("id", id);
		return nbt;
	}
	
	public static String addNamespace(String component) {
		if (component.contains(":"))
			return component;
		if (component.startsWith("!"))
			return "!minecraft:" + component.substring(1);
		return "minecraft:" + component;
	}
	
	public static <T> CompletableFuture<T> mergeFutures(List<CompletableFuture<T>> futures) {
		CompletableFuture<T> output = new CompletableFuture<>();
		output.thenAccept(value -> futures.forEach(future -> future.complete(value)));
		output.exceptionally(e -> {
			futures.forEach(future -> future.completeExceptionally(e));
			return null;
		});
		return output;
	}
	
	public static void setTextFieldValueSilently(EditBox widget, String text, boolean scrollToEnd) {
		widget.value = text;
		int cursor = (scrollToEnd ? text.length() : 0);
		widget.setCursorPosition(cursor);
		widget.setHighlightPos(cursor);
	}
	
	public static void setCursorStackSilently(AbstractContainerMenu handler, ItemStack item) {
		handler.setCarried(item);
		MVMisc.setPreviousCursorStack(handler, item);
	}
	
}
