package com.luneruniverse.minecraft.mod.nbteditor.packets;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistryKeys;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacket;
import com.luneruniverse.minecraft.mod.nbteditor.util.BlockStateProperties;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class SetBlockC2SPacket implements MVPacket {
	
	public static final Identifier ID = IdentifierInst.of("nbteditor", "set_block");
	
	private final ResourceKey<Level> world;
	private final BlockPos pos;
	private final Identifier id;
	private final BlockStateProperties state;
	private final CompoundTag nbt;
	private final boolean recreate;
	private final boolean triggerUpdate;
	
	public SetBlockC2SPacket(ResourceKey<Level> world, BlockPos pos, Identifier id,
                             BlockStateProperties state, CompoundTag nbt, boolean recreate, boolean triggerUpdate) {
		this.world = world;
		this.pos = pos;
		this.id = id;
		this.state = state;
		this.nbt = nbt;
		this.recreate = recreate;
		this.triggerUpdate = triggerUpdate;
	}
	public SetBlockC2SPacket(FriendlyByteBuf payload) {
		this.world = payload.readResourceKey(Registries.DIMENSION);
		this.pos = payload.readBlockPos();
		this.id = payload.readIdentifier();
		this.state = new BlockStateProperties(payload);
		this.nbt = payload.readNbt();
		this.recreate = payload.readBoolean();
		this.triggerUpdate = payload.readBoolean();
	}
	
	public ResourceKey<Level> getWorld() {
		return world;
	}
	public BlockPos getPos() {
		return pos;
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
	public boolean isRecreate() {
		return recreate;
	}
	public boolean isTriggerUpdate() {
		return triggerUpdate;
	}
	
	@Override
	public void write(FriendlyByteBuf payload) {
		payload.writeResourceKey(world);
		payload.writeBlockPos(pos);
		payload.writeIdentifier(id);
		state.writeToPayload(payload);
		payload.writeNbt(nbt);
		payload.writeBoolean(recreate);
		payload.writeBoolean(triggerUpdate);
	}
	
	@Override
	public Identifier getPacketId() {
		return ID;
	}
	
}
