package com.luneruniverse.minecraft.mod.nbteditor.misc;

import java.lang.invoke.MethodType;
import java.lang.reflect.Proxy;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;

import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtType;
import net.minecraft.nbt.NbtTypes;

public class NbtTypeModifier {
	
	public static void loadClass() {}
	
	private static final NbtType<?>[] VALUES = NbtTypes.VALUES;
	public static final NbtType<NbtString> NBT_STRING_TYPE = makeMutable(NbtString.TYPE);
	
	@SuppressWarnings("unchecked")
	public static <T extends NbtElement> NbtType<T> makeMutable(NbtType<T> type) {
		return type;
	}
	
}
