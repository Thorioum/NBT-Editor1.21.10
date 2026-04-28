package com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

public interface MVPacket {
	public void write(FriendlyByteBuf payload);
	public Identifier getPacketId();
}
