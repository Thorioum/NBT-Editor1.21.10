package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.nbt.*;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonParseException;
import com.luneruniverse.minecraft.mod.nbteditor.util.TextUtil;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public class TextInst {
	
	public static Text of(String msg) {
		return Text.of(msg);
	}
	public static EditableText literal(String msg) {
		return new EditableText(Version.<MutableText>newSwitch()
				.range("1.19.0", null, () -> Text.literal(msg))
				.range(null, "1.18.2", () -> Reflection.newInstance("net.minecraft.class_2585", new Class[] {String.class}, msg)) // new LiteralText(msg)
				.get());
	}
	public static EditableText translatable(String key, Object... args) {
		return new EditableText(Version.<MutableText>newSwitch()
				.range("1.20.3", null, () -> Text.stringifiedTranslatable(key, args))
				.range("1.19.0", "1.20.2", () -> Text.translatable(key, args))
				.range(null, "1.18.2", () -> Reflection.newInstance("net.minecraft.class_2588", new Class[] {String.class, Object[].class}, key, args)) // new TranslatableText(key, args)
				.get());
	}
	
	public static EditableText copy(Text text) {
		return new EditableText(text.copy());
	}
	public static EditableText copyContentOnly(Text text) {
		return new EditableText(text.copyContentOnly());
	}
	
	public static EditableText bracketed(Text text) {
		return translatable("chat.square_brackets", text);
	}
	
	
	/**
	 * <strong>CONSIDER USING {@link TextUtil#fromStringSafely(String, boolean)}</strong>
	 */
	public static @Nullable Text fromString(String str, boolean eitherFormat) throws IllegalArgumentException {
		return Version.<Text>newSwitch()
				.range("1.21.5", null, () -> {
					IllegalArgumentException wrapper;
					try {
						return fromSNbt(str);
					} catch (CommandSyntaxException | InvalidNbtException e) {
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
					} catch (CommandSyntaxException | InvalidNbtException e) {
						wrapper.addSuppressed(e);
						throw wrapper;
					}
				})
				.get();
	}
	public static String toString(Text text) throws IllegalArgumentException {
		try {
			return Version.<String>newSwitch()
					.range("1.21.5", null, () -> toSNbt(text))
					.range(null, "1.21.4", () -> toJson(text))
					.get();
		} catch (InvalidNbtException | JsonParseException e) {
			throw new IllegalArgumentException("Failed to stringify text", e);
		}
	}
	
	public static @Nullable Text fromMinecraft(NbtElement mc) throws IllegalArgumentException {
		try {
			return Version.<Text>newSwitch()
					.range("1.21.5", null, () -> fromNbt(mc))
					.range(null, "1.21.4", () -> {
						if (!(mc instanceof NbtString mcStr))
							throw new IllegalArgumentException("Failed to parse text: not a string");
						return fromJson(MVMisc.value(mcStr));
					})
					.get();
		} catch (InvalidNbtException | JsonParseException e) {
			throw new IllegalArgumentException("Failed to parse text", e);
		}
	}
	public static NbtElement toMinecraft(Text text) throws IllegalArgumentException {
		try {
			return Version.<NbtElement>newSwitch()
					.range("1.21.5", null, () -> toNbt(text))
					.range(null, "1.21.4", () -> NbtString.of(toJson(text)))
					.get();
		} catch (InvalidNbtException | JsonParseException e) {
			throw new IllegalArgumentException("Failed to stringify text", e);
		}
	}
	
	/**
	 * <strong>CONSIDER USING {@link TextUtil#fromSNbtSafely(String)}</strong>
	 */
	public static Text fromSNbt(String snbt) throws CommandSyntaxException, InvalidNbtException {
		return fromNbt(MVMisc.parseNbt(snbt));
	}
	public static String toSNbt(Text text) throws InvalidNbtException {
		return toNbt(text).toString();
	}
	
	/**
	 * <strong>CONSIDER USING {@link TextUtil#fromJsonSafely(String)}</strong>
	 */
	public static String textToJson(Text text) {
		NbtElement t = TextCodecs.CODEC.encodeStart(DynamicRegistryManagerHolder.get().getOps(NbtOps.INSTANCE),text).result().orElse(new NbtCompound());
		return t.toString();
	}
	public static Text asText(String textString) {
		if(textString.startsWith("'")) textString = textString.substring(1,textString.length()-1);
		String compoundString = "{a:" + textString + "}";
		try {
			NbtCompound nbt = StringNbtReader.readCompound(compoundString);
			NbtElement textNbt = nbt.get("a");
			return TextCodecs.CODEC.decode(DynamicRegistryManagerHolder.get().getOps(NbtOps.INSTANCE),textNbt).getOrThrow().getFirst();
		} catch (Exception e) {}
		return null;
	}

	public static @Nullable Text fromJson(String json) throws JsonParseException {
		return Version.<Text>newSwitch()
				.range("1.20.5", null, () -> asText(json))
				.get();
	}

	public static String toJson(Text text) throws JsonParseException {
		return Version.<String>newSwitch()
				.range("1.20.5", null, () -> textToJson(text))
				.get();
	}
	
	public static Text fromNbt(NbtElement nbt) throws InvalidNbtException {
		return Attempt.ofResult(TextCodecs.CODEC.parse(NbtOps.INSTANCE, nbt)).getSuccessOrThrow(InvalidNbtException::new);
	}
	public static NbtElement toNbt(Text text) throws InvalidNbtException {
		return Attempt.ofResult(TextCodecs.CODEC.encodeStart(NbtOps.INSTANCE, text)).getSuccessOrThrow(InvalidNbtException::new);
	}
	
}
