package com.luneruniverse.minecraft.mod.nbteditor.packets;

import java.util.UUID;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistryKeys;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacket;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public class SetEntityC2SPacket implements MVPacket {
	
	public static final Identifier ID = IdentifierInst.of("nbteditor", "set_entity");
	
	private final ResourceKey<Level> world;
	private final UUID uuid;
	private final Identifier id;
	private final CompoundTag nbt;
	private final boolean recreate;
	
	public SetEntityC2SPacket(ResourceKey<Level> world, UUID uuid, Identifier id, CompoundTag nbt, boolean recreate) {
		this.world = world;
		this.uuid = uuid;
		this.id = id;
		this.nbt = nbt;
		this.recreate = recreate;
	}
	public SetEntityC2SPacket(FriendlyByteBuf payload) {
		this.world = payload.readResourceKey(Registries.DIMENSION);
		this.uuid = payload.readUUID();
		this.id = payload.readIdentifier();
		this.nbt = payload.readNbt();
		this.recreate = payload.readBoolean();
	}
	
	public ResourceKey<Level> getWorld() {
		return world;
	}
	public UUID getUUID() {
		return uuid;
	}
	public Identifier getId() {
		return id;
	}
	public CompoundTag getNbt() {
		return nbt;
	}
	public boolean isRecreate() {
		return recreate;
	}
	
	@Override
	public void write(FriendlyByteBuf payload) {
		payload.writeResourceKey(world);
		payload.writeUUID(uuid);
		payload.writeIdentifier(id);
		payload.writeNbt(nbt);
		payload.writeBoolean(recreate);
	}
	
	@Override
	public Identifier getPacketId() {
		return ID;
	}
	
}
