package com.luneruniverse.minecraft.mod.nbteditor.packets;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistryKeys;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacket;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class GetBlockC2SPacket implements MVPacket {
	
	public static final Identifier ID = IdentifierInst.of("nbteditor", "get_block");
	
	private final int requestId;
	private final ResourceKey<Level> world;
	private final BlockPos pos;
	
	public GetBlockC2SPacket(int requestId, ResourceKey<Level> world, BlockPos pos) {
		this.requestId = requestId;
		this.world = world;
		this.pos = pos;
	}
	public GetBlockC2SPacket(FriendlyByteBuf payload) {
		this.requestId = payload.readVarInt();
		this.world = payload.readResourceKey(Registries.DIMENSION);
		this.pos = payload.readBlockPos();
	}
	
	public int getRequestId() {
		return requestId;
	}
	public ResourceKey<Level> getWorld() {
		return world;
	}
	public BlockPos getPos() {
		return pos;
	}
	
	@Override
	public void write(FriendlyByteBuf payload) {
		payload.writeVarInt(requestId);
		payload.writeResourceKey(world);
		payload.writeBlockPos(pos);
	}
	
	@Override
	public Identifier getPacketId() {
		return ID;
	}
	
}
