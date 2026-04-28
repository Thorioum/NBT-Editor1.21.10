package com.luneruniverse.minecraft.mod.nbteditor.packets;

import java.util.UUID;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistryKeys;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacket;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public class GetEntityC2SPacket implements MVPacket {
	
	public static final Identifier ID = IdentifierInst.of("nbteditor", "get_entity");
	
	private final int requestId;
	private final ResourceKey<Level> world;
	private final UUID uuid;
	
	public GetEntityC2SPacket(int requestId, ResourceKey<Level> world, UUID uuid) {
		this.requestId = requestId;
		this.world = world;
		this.uuid = uuid;
	}
	public GetEntityC2SPacket(FriendlyByteBuf payload) {
		this.requestId = payload.readVarInt();
		this.world = payload.readResourceKey(Registries.DIMENSION);
		this.uuid = payload.readUUID();
	}
	
	public int getRequestId() {
		return requestId;
	}
	public ResourceKey<Level> getWorld() {
		return world;
	}
	public UUID getUUID() {
		return uuid;
	}
	
	@Override
	public void write(FriendlyByteBuf payload) {
		payload.writeVarInt(requestId);
		payload.writeResourceKey(world);
		payload.writeUUID(uuid);
	}
	
	@Override
	public Identifier getPacketId() {
		return ID;
	}
	
}
