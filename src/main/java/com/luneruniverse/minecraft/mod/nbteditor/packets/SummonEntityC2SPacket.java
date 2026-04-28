package com.luneruniverse.minecraft.mod.nbteditor.packets;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistryKeys;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacket;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

public class SummonEntityC2SPacket implements MVPacket {
	
	public static final Identifier ID = IdentifierInst.of("nbteditor", "summon_entity");
	
	private final int requestId;
	private final ResourceKey<Level> world;
	private final Vector3f pos;
	private final Identifier id;
	private final CompoundTag nbt;
	
	public SummonEntityC2SPacket(int requestId, ResourceKey<Level> world, Vec3 pos, Identifier id, CompoundTag nbt) {
		this.requestId = requestId;
		this.world = world;
		this.pos = pos.toVector3f();
		this.id = id;
		this.nbt = nbt;
	}
	public SummonEntityC2SPacket(FriendlyByteBuf payload) {
		this.requestId = payload.readVarInt();
		this.world = payload.readResourceKey(Registries.DIMENSION);
		this.pos = payload.readVector3f();
		this.id = payload.readIdentifier();
		this.nbt = payload.readNbt();
	}
	
	public int getRequestId() {
		return requestId;
	}
	public ResourceKey<Level> getWorld() {
		return world;
	}
	public Vec3 getPos() {
		return new Vec3(pos);
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
		payload.writeResourceKey(world);
		payload.writeVector3f(pos);
		payload.writeIdentifier(id);
		payload.writeNbt(nbt);
	}
	
	@Override
	public Identifier getPacketId() {
		return ID;
	}
	
}
