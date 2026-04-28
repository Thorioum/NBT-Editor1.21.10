package com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

public class MVNetworking {
	
	private static final Map<Identifier, Function<FriendlyByteBuf, MVPacket>> constructors = new HashMap<>();
	
	public static void registerPacket(Identifier id, Function<FriendlyByteBuf, MVPacket> constructor) {
		constructors.put(id, constructor);
	}
	
	public static boolean isPacket(Identifier id) {
		return constructors.containsKey(id);
	}
	
	public static MVPacket readPacket(Identifier id, FriendlyByteBuf payload) {
		Function<FriendlyByteBuf, MVPacket> constructor = constructors.get(id);
		if (constructor == null)
			return null;
		return constructor.apply(payload);
	}
	
}
