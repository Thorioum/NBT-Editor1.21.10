package com.luneruniverse.minecraft.mod.nbteditor.packets;

import java.util.UUID;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistryKeys;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public class ViewEntityS2CPacket implements ResponsePacket {
	
	public static final Identifier ID = IdentifierInst.of("nbteditor", "view_entity");
	
	private final int requestId;
	private final ResourceKey<Level> world;
	private final UUID uuid;
	private final Identifier id;
	private final CompoundTag nbt;
	
	public ViewEntityS2CPacket(int requestId, ResourceKey<Level> world, UUID uuid, Identifier id, CompoundTag nbt) {
		if ((world == null) != (uuid == null))
			throw new IllegalArgumentException("world and uuid have to be null together!");
		if ((id == null) != (nbt == null))
			throw new IllegalArgumentException("id and nbt have to be null together!");
		
		this.requestId = requestId;
		this.world = world;
		this.uuid = uuid;
		this.id = id;
		this.nbt = nbt;
	}
	public ViewEntityS2CPacket(FriendlyByteBuf payload) {
		this.requestId = payload.readVarInt();
		if (payload.readBoolean()) {
			this.world = payload.readResourceKey(Registries.DIMENSION);
			this.uuid = payload.readUUID();
		} else {
			this.world = null;
			this.uuid = null;
		}
		if (payload.readBoolean()) {
			this.id = payload.readIdentifier();
			this.nbt = payload.readNbt();
		} else {
			this.id = null;
			this.nbt = null;
		}
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
	public boolean foundEntity() {
		return id != null;
	}
	public Identifier getId() {
		return id;
	}
	public CompoundTag getNbt() {
		return nbt;
	}
	
	@Override
	public void write(FriendlyByteBuf payload) {
		payload.writeVarInt(requestId);
		if (world == null) {
			payload.writeBoolean(false);
		} else {
			payload.writeBoolean(true);
			payload.writeResourceKey(world);
			payload.writeUUID(uuid);
		}
		if (id == null) {
			payload.writeBoolean(false);
		} else {
			payload.writeBoolean(true);
			payload.writeIdentifier(id);
			payload.writeNbt(nbt);
		}
	}
	
	@Override
	public Identifier getPacketId() {
		return ID;
	}
	
}
