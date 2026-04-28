package com.luneruniverse.minecraft.mod.nbteditor.nbtreferences;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalBlock;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVClientNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.GetBlockC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.GetLecternBlockC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SetBlockC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ViewBlockS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.BlockStateProperties;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class BlockReference implements NBTReference<LocalBlock> {
	
	private static CompletableFuture<Optional<BlockReference>> getBlock(Function<Integer, MVPacket> packetFactory) {
		return NBTEditorClient.SERVER_CONN
				.sendRequest(packetFactory, ViewBlockS2CPacket.class)
				.thenApply(optional -> optional.filter(ViewBlockS2CPacket::foundBlock)
						.map(packet -> new BlockReference(packet.getWorld(), packet.getPos(),
								MVRegistry.BLOCK.get(packet.getId()), packet.getState(), packet.getNbt())));
	}
	public static CompletableFuture<Optional<BlockReference>> getBlock(ResourceKey<Level> world, BlockPos pos) {
		return getBlock(requestId -> new GetBlockC2SPacket(requestId, world, pos));
	}
	public static CompletableFuture<Optional<BlockReference>> getLecternBlock() {
		return getBlock(GetLecternBlockC2SPacket::new);
	}
	public static BlockReference getBlockWithoutNBT(BlockPos pos) {
		BlockState state = MainUtil.client.level.getBlockState(pos);
		return new BlockReference(MainUtil.client.level.dimension(), pos,
				state.getBlock(), new BlockStateProperties(state), new CompoundTag());
	}
	
	private final ResourceKey<Level> world;
	private final BlockPos pos;
	private Block block;
	private BlockStateProperties state;
	private CompoundTag nbt;
	
	public BlockReference(ResourceKey<Level> world, BlockPos pos, Block block, BlockStateProperties state, CompoundTag nbt) {
		this.world = world;
		this.pos = pos;
		this.block = block;
		this.state = state;
		this.nbt = nbt;
	}
	
	public ResourceKey<Level> getWorld() {
		return world;
	}
	public BlockPos getPos() {
		return pos;
	}
	
	@Override
	public boolean exists() {
		return true;
	}
	
	@Override
	public LocalBlock getLocalNBT() {
		return new LocalBlock(block, state, nbt);
	}
	@Override
	public void saveLocalNBT(LocalBlock block, Runnable onFinished) {
		this.block = block.getBlock();
		this.state = block.getState();
		this.nbt = block.getNBT();
		MVClientNetworking.send(new SetBlockC2SPacket(world, pos, block.getId(), state.copy(), nbt.copy(),
				ConfigScreen.isRecreateBlocksAndEntities(), ConfigScreen.isTriggerBlockUpdates()));
		onFinished.run();
	}
	
	@Override
	public Identifier getId() {
		return MVRegistry.BLOCK.getId(block);
	}
	@Override
	public CompoundTag getNBT() {
		return nbt;
	}
	@Override
	public void saveNBT(Identifier id, CompoundTag toSave, Runnable onFinished) {
		this.block = MVRegistry.BLOCK.get(id);
		this.nbt = toSave;
		MVClientNetworking.send(new SetBlockC2SPacket(world, pos, id, state.copy(), toSave.copy(),
				ConfigScreen.isRecreateBlocksAndEntities(), ConfigScreen.isTriggerBlockUpdates()));
		onFinished.run();
	}
	
	public Block getBlock() {
		return block;
	}
	
	public BlockStateProperties getState() {
		return state;
	}
	public void saveState(BlockStateProperties state, Runnable onFinished) {
		this.state = state;
		MVClientNetworking.send(new SetBlockC2SPacket(world, pos, MVRegistry.BLOCK.getId(block), state.copy(), nbt.copy(),
				ConfigScreen.isRecreateBlocksAndEntities(), ConfigScreen.isTriggerBlockUpdates()));
		onFinished.run();
	}
	public void saveState(BlockStateProperties state, Component msg) {
		saveState(state, () -> MainUtil.client.player.sendSystemMessage(msg));
	}
	public void saveState(BlockStateProperties state) {
		saveState(state, () -> {});
	}
	
}
