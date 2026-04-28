package com.luneruniverse.minecraft.mod.nbteditor.packets;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistryKeys;
import com.luneruniverse.minecraft.mod.nbteditor.util.BlockStateProperties;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class ViewBlockS2CPacket implements ResponsePacket {
	
	public static final Identifier ID = IdentifierInst.of("nbteditor", "view_block");
	
	private final int requestId;
	private final ResourceKey<Level> world;
	private final BlockPos pos;
	private final Identifier id;
	private final BlockStateProperties state;
	private final CompoundTag nbt;
	
	public ViewBlockS2CPacket(int requestId, ResourceKey<Level> world, BlockPos pos, Identifier id, BlockStateProperties state, CompoundTag nbt) {
		if ((world == null) != (pos == null))
			throw new IllegalArgumentException("world and pos have to be null together!");
		if ((id == null) != (state == null) || (id == null) != (nbt == null))
			throw new IllegalArgumentException("id, state, and nbt have to be null together!");
		
		this.requestId = requestId;
		this.world = world;
		this.pos = pos;
		this.id = id;
		this.state = state;
		this.nbt = nbt;
	}
	public ViewBlockS2CPacket(FriendlyByteBuf payload) {
		this.requestId = payload.readVarInt();
		if (payload.readBoolean()) {
			this.world = payload.readResourceKey(Registries.DIMENSION);
			this.pos = payload.readBlockPos();
		} else {
			this.world = null;
			this.pos = null;
		}
		if (payload.readBoolean()) {
			this.id = payload.readIdentifier();
			this.state = new BlockStateProperties(payload);
			this.nbt = payload.readNbt();
		} else {
			this.id = null;
			this.state = null;
			this.nbt = null;
		}
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
	public boolean foundBlock() {
		return id != null;
	}
	public Identifier getId() {
		return id;
	}
	public BlockStateProperties getState() {
		return state;
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
			payload.writeBlockPos(pos);
		}
		if (id == null) {
			payload.writeBoolean(false);
		} else {
			payload.writeBoolean(true);
			payload.writeIdentifier(id);
			state.writeToPayload(payload);
			payload.writeNbt(nbt);
		}
	}
	
	@Override
	public Identifier getPacketId() {
		return ID;
	}
	
}
