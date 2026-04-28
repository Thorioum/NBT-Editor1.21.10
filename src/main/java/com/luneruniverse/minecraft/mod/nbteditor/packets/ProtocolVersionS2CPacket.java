package com.luneruniverse.minecraft.mod.nbteditor.packets;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

public class ProtocolVersionS2CPacket implements MVPacket {
	
	public static final Identifier ID = IdentifierInst.of("nbteditor", "protocol_version");
	
	private final int version;
	
	public ProtocolVersionS2CPacket(int version) {
		this.version = version;
	}
	public ProtocolVersionS2CPacket(FriendlyByteBuf payload) {
		this.version = payload.readVarInt();
	}
	
	public int getVersion() {
		return version;
	}
	
	@Override
	public void write(FriendlyByteBuf payload) {
		payload.writeVarInt(version);
	}
	
	@Override
	public Identifier getPacketId() {
		return ID;
	}
	
}
