package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.nbt.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtFormatException;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonParseException;
import com.luneruniverse.minecraft.mod.nbteditor.util.TextUtil;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public class TextInst {
	
	public static Component of(String msg) {
		return Component.nullToEmpty(msg);
	}
	public static EditableText literal(String msg) {
		return new EditableText(Version.<MutableComponent>newSwitch()
				.range("1.19.0", null, () -> Component.literal(msg))
				.range(null, "1.18.2", () -> Reflection.newInstance("net.minecraft.class_2585", new Class[] {String.class}, msg)) // new LiteralText(msg)
				.get());
	}
	public static EditableText translatable(String key, Object... args) {
		return new EditableText(Version.<MutableComponent>newSwitch()
				.range("1.20.3", null, () -> Component.translatableEscape(key, args))
				.range("1.19.0", "1.20.2", () -> Component.translatable(key, args))
				.range(null, "1.18.2", () -> Reflection.newInstance("net.minecraft.class_2588", new Class[] {String.class, Object[].class}, key, args)) // new TranslatableText(key, args)
				.get());
	}
	
	public static EditableText copy(Component text) {
		return new EditableText(text.copy());
	}
	public static EditableText copyContentOnly(Component text) {
		return new EditableText(text.plainCopy());
	}
	
	public static EditableText bracketed(Component text) {
		return translatable("chat.square_brackets", text);
	}
	
	
	/**
	 * <strong>CONSIDER USING {@link TextUtil#fromStringSafely(String, boolean)}</strong>
	 */
	public static @Nullable Component fromString(String str, boolean eitherFormat) throws IllegalArgumentException {
		return Version.<Component>newSwitch()
				.range("1.21.5", null, () -> {
					IllegalArgumentException wrapper;
					try {
						return fromSNbt(str);
					} catch (CommandSyntaxException | NbtFormatException e) {
						wrapper = new IllegalArgumentException("Failed to parse text");
						wrapper.addSuppressed(e);
						if (!eitherFormat)
							throw wrapper;
					}
					
					try {
						return fromJson(str);
					} catch (JsonParseException e) {
						wrapper.addSuppressed(e);
						throw wrapper;
					}
				})
				.range(null, "1.21.4", () -> {
					IllegalArgumentException wrapper;
					try {
						return fromJson(str);
					} catch (JsonParseException e) {
						wrapper = new IllegalArgumentException("Failed to parse text");
						wrapper.addSuppressed(e);
						if (!eitherFormat)
							throw wrapper;
					}
					
					try {
						return fromSNbt(str);
					} catch (CommandSyntaxException | NbtFormatException e) {
						wrapper.addSuppressed(e);
						throw wrapper;
					}
				})
				.get();
	}
	public static String toString(Component text) throws IllegalArgumentException {
		try {
			return Version.<String>newSwitch()
					.range("1.21.5", null, () -> toSNbt(text))
					.range(null, "1.21.4", () -> toJson(text))
					.get();
		} catch (NbtFormatException | JsonParseException e) {
			throw new IllegalArgumentException("Failed to stringify text", e);
		}
	}
	
	public static @Nullable Component fromMinecraft(Tag mc) throws IllegalArgumentException {
		try {
			return Version.<Component>newSwitch()
					.range("1.21.5", null, () -> fromNbt(mc))
					.range(null, "1.21.4", () -> {
						if (!(mc instanceof StringTag mcStr))
							throw new IllegalArgumentException("Failed to parse text: not a string");
						return fromJson(MVMisc.value(mcStr));
					})
					.get();
		} catch (NbtFormatException | JsonParseException e) {
			throw new IllegalArgumentException("Failed to parse text", e);
		}
	}
	public static Tag toMinecraft(Component text) throws IllegalArgumentException {
		try {
			return Version.<Tag>newSwitch()
					.range("1.21.5", null, () -> toNbt(text))
					.range(null, "1.21.4", () -> StringTag.valueOf(toJson(text)))
					.get();
		} catch (NbtFormatException | JsonParseException e) {
			throw new IllegalArgumentException("Failed to stringify text", e);
		}
	}
	
	/**
	 * <strong>CONSIDER USING {@link TextUtil#fromSNbtSafely(String)}</strong>
	 */
	public static Component fromSNbt(String snbt) throws CommandSyntaxException, NbtFormatException {
		return fromNbt(MVMisc.parseNbt(snbt));
	}
	public static String toSNbt(Component text) throws NbtFormatException {
		return toNbt(text).toString();
	}
	
	/**
	 * <strong>CONSIDER USING {@link TextUtil#fromJsonSafely(String)}</strong>
	 */
	public static String textToJson(Component text) {
		Tag t = ComponentSerialization.CODEC.encodeStart((MainUtil.client.getConnection() == null ? VanillaRegistries.createLookup() : MainUtil.client.getConnection().registryAccess()).createSerializationContext(NbtOps.INSTANCE),text).result().orElse(new CompoundTag());
		return t.toString();
	}
	public static Component asText(String textString) {
		if(textString.startsWith("'")) textString = textString.substring(1,textString.length()-1);
		String compoundString = "{a:" + textString + "}";
		try {
			CompoundTag nbt = TagParser.parseCompoundFully(compoundString);
			Tag textNbt = nbt.get("a");
			return ComponentSerialization.CODEC.decode((MainUtil.client.getConnection() == null ? VanillaRegistries.createLookup() : MainUtil.client.getConnection().registryAccess()).createSerializationContext(NbtOps.INSTANCE),textNbt).getOrThrow().getFirst();
		} catch (Exception e) {}
		return null;
	}

	public static @Nullable Component fromJson(String json) throws JsonParseException {
		return Version.<Component>newSwitch()
				.range("1.20.5", null, () -> asText(json))
				.get();
	}

	public static String toJson(Component text) throws JsonParseException {
		return Version.<String>newSwitch()
				.range("1.20.5", null, () -> textToJson(text))
				.get();
	}
	
	public static Component fromNbt(Tag nbt) throws NbtFormatException {
		return Attempt.ofResult(ComponentSerialization.CODEC.parse(NbtOps.INSTANCE, nbt)).getSuccessOrThrow(NbtFormatException::new);
	}
	public static Tag toNbt(Component text) throws NbtFormatException {
		return Attempt.ofResult(ComponentSerialization.CODEC.encodeStart(NbtOps.INSTANCE, text)).getSuccessOrThrow(NbtFormatException::new);
	}
	
}
