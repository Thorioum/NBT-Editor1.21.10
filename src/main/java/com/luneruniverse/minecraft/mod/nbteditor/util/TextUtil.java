package com.luneruniverse.minecraft.mod.nbteditor.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.JsonParseException;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.fancytext.FancyText;
import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.EditableText;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTextEvents;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.util.FancyConfirmScreen;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.nbt.NbtFormatException;
import net.minecraft.network.chat.FormattedText.StyledContentConsumer;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class TextUtil {
	
	public static List<Component> getLongTranslatableTextLines(String key) {
		List<Component> lines = new ArrayList<>();
		for (int i = 1; i <= 50; i++) {
			Component line = TextInst.translatable(key + "_" + i);
			String str = line.getString();
			if (str.equals(key + "_" + i))
				break;
			
			if (str.startsWith("[LINK] ")) {
				String url = str.substring("[LINK] ".length());
				URI uri;
				try {
					uri = new URI(url);
				} catch (URISyntaxException e) {
					throw new IllegalArgumentException("Invalid link: " + url, e);
				}
				line = TextInst.literal(url)
						.styled(style -> style.withClickEvent(MVTextEvents.ClickAction.OPEN_URL.newEvent(uri))
						.withUnderlined(true).withItalic(true).withColor(ChatFormatting.GOLD));
			}
			if (str.startsWith("[FORMAT] ")) {
				String toFormat = str.substring("[FORMAT] ".length());
				line = FancyText.parse(toFormat);
			}
			lines.add(line);
		}
		return lines;
	}
	public static Component getLongTranslatableText(String key) {
		List<Component> lines = getLongTranslatableTextLines(key);
		if (lines.isEmpty())
			return TextInst.of(key);
		EditableText output = TextInst.copy(lines.get(0));
		for (int i = 1; i < lines.size(); i++)
			output.append("\n").append(lines.get(i));
		return output;
	}
	
	public static Component parseTranslatableFormatted(String key, Object... args) {
		return FancyText.parse(TextInst.translatable(key, args).getString());
	}
	
	public static Component substring(Component text, int start, int end) {
		EditableText output = TextInst.literal("");
		text.visit(new StyledContentConsumer<Boolean>() {
			private int i;
			@Override
			public Optional<Boolean> accept(Style style, String str) {
				if (i + str.length() <= start) {
					i += str.length();
					return Optional.empty();
				}
				if (i >= start) {
					if (end >= 0 && i + str.length() > end)
						return accept(style, str.substring(0, end - i));
					output.append(TextInst.literal(str).fillStyle(style));
					i += str.length();
					if (end >= 0 && i == end)
						return Optional.of(true);
					return Optional.empty();
				} else {
					str = str.substring(start - i);
					i = start;
					accept(style, str);
					return Optional.empty();
				}
			}
		}, Style.EMPTY);
		return output;
	}
	public static Component substring(Component text, int start) {
		return substring(text, start, -1);
	}
	
	public static Component deleteCharAt(Component text, int index) {
		EditableText output = TextInst.literal("");
		AtomicInteger pos = new AtomicInteger(0);
		text.visit((style, str) -> {
			int strLen = str.length();
			if (pos.getPlain() <= index && index < pos.getPlain() + strLen)
				str = new StringBuilder(str).deleteCharAt(index - pos.getPlain()).toString();
			if (!str.isEmpty())
				output.append(TextInst.literal(str).setStyle(style));
			pos.setPlain(pos.getPlain() + strLen);
			return Optional.empty();
		}, Style.EMPTY);
		return output;
	}
	
	public static Component joinLines(List<Component> lines) {
		EditableText output = TextInst.literal("");
		for (int i = 0; i < lines.size(); i++) {
			if (i > 0)
				output.append("\n");
			output.append(lines.get(i));
		}
		return output;
	}
	public static List<Component> splitText(Component text) {
		List<Component> output = new ArrayList<>();
		int i;
		while ((i = text.getString().indexOf('\n')) != -1) {
			output.add(substring(text, 0, i));
			text = substring(text, i + 1);
		}
		output.add(text);
		return output;
	}
	
	public static Component stripInvalidChars(Component text, boolean allowLineBreaks) {
		EditableText output = TextInst.literal("");
		text.visit((style, str) -> {
			output.append(TextInst.literal(MVMisc.stripInvalidChars(str, allowLineBreaks)).setStyle(style));
			return Optional.empty();
		}, Style.EMPTY);
		return output;
	}
	
	public static Component attachFileTextOptions(EditableText link, File file) {
		return link.append(" ").append(TextInst.translatable("nbteditor.file_options.show").styled(style ->
				style.withClickEvent(MVTextEvents.ClickAction.OPEN_FILE.newEvent(
						file.getAbsoluteFile().getParentFile().getAbsolutePath()))))
				.append(" ").append(TextInst.translatable("nbteditor.file_options.delete").styled(style ->
				MixinLink.withRunClickEvent(style, () -> MainUtil.client.setScreen(
						new FancyConfirmScreen(confirmed -> {
							if (confirmed) {
								if (file.exists()) {
									try {
										Files.deleteIfExists(file.toPath());
										MainUtil.client.player.sendSystemMessage(TextInst.translatable("nbteditor.file_options.delete.success", "§6" + file.getName()));
									} catch (IOException e) {
										NBTEditor.LOGGER.error("Error deleting file", e);
										MainUtil.client.player.sendSystemMessage(TextInst.translatable("nbteditor.file_options.delete.error", "§6" + file.getName()));
									}
								} else
									MainUtil.client.player.sendSystemMessage(TextInst.translatable("nbteditor.file_options.delete.missing", "§6" + file.getName()));
							}
							MainUtil.client.setScreen(null);
						}, TextInst.translatable("nbteditor.file_options.delete.title", file.getName()),
								TextInst.translatable("nbteditor.file_options.delete.desc", file.getName()))))));
	}
	
	public static boolean isTextFormatted(Component text, Style base) {
		if (StyleUtil.hasFormatting(text.getStyle(), base))
			return true;
		
		for (Component sibling : text.getSiblings()) {
			if (isTextFormatted(sibling, base))
				return true;
		}
		
		return false;
	}
	
	public static int lastIndexOf(Component text, int ch) {
		AtomicInteger output = new AtomicInteger(-1);
		AtomicInteger pos = new AtomicInteger(0);
		text.visit(str -> {
			int i = str.lastIndexOf(ch);
			if (i != -1)
				output.setPlain(pos.getPlain() + i);
			pos.setPlain(pos.getPlain() + str.length());
			return Optional.empty();
		});
		return output.getPlain();
	}
	
	public static Component fromStringSafely(String str, boolean eitherFormat) {
		try {
			Component output = TextInst.fromString(str, eitherFormat);
			if (output != null)
				return output;
		} catch (IllegalArgumentException e) {}
		return TextInst.of(str);
	}
	public static Component fromSNbtSafely(String snbt) {
		try {
			return TextInst.fromSNbt(snbt);
		} catch (CommandSyntaxException | NbtFormatException e) {}
		return TextInst.of(snbt);
	}
	public static Component fromJsonSafely(String json) {
		try {
			Component output = TextInst.fromJson(json);
			if (output != null)
				return output;
		} catch (JsonParseException e) {}
		return TextInst.of(json);
	}
	
}
