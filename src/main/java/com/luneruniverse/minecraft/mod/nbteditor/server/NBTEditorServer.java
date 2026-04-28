package com.luneruniverse.minecraft.mod.nbteditor.server;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.NBTManagers;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVServerNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.packets.GetBlockC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.GetEntityC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.GetLecternBlockC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.OpenEnderChestC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ProtocolVersionS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SetBlockC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SetCursorC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SetEntityC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SetSlotC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SummonEntityC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ViewBlockS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ViewEntityS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.util.BlockStateProperties;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.Slot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.IdentifierException;
import net.minecraft.world.phys.Vec3;

public class NBTEditorServer implements MVServerNetworking.PlayNetworkStateEvents.Start {
	
	public static final int PROTOCOL_VERSION = 1;
	public static boolean IS_DEDICATED = true;
	
	private static final WeakHashMap<Thread, Boolean> serverThreads = new WeakHashMap<>();
	public static void registerServerThread(Thread thread) {
		serverThreads.put(thread, true);
	}
	public static void unregisterServerThread(Thread thread) {
		serverThreads.remove(thread);
	}
	public static boolean isOnServerThread() {
		if (IS_DEDICATED)
			return true;
		if (MainUtil.client.getSingleplayerServer() == null)
			return false;
		if (MainUtil.client.getSingleplayerServer().isSameThread())
			return true;
		return serverThreads.containsKey(Thread.currentThread());
	}
	
	public NBTEditorServer() {
		MVServerNetworking.registerListener(SetCursorC2SPacket.ID, this::onSetCursorPacket);
		MVServerNetworking.registerListener(SetSlotC2SPacket.ID, this::onSetSlotPacket);
		MVServerNetworking.registerListener(OpenEnderChestC2SPacket.ID, this::onOpenEnderChestPacket);
		MVServerNetworking.registerListener(GetBlockC2SPacket.ID, this::onGetBlockPacket);
		MVServerNetworking.registerListener(GetLecternBlockC2SPacket.ID, this::onGetLecternBlockPacket);
		MVServerNetworking.registerListener(GetEntityC2SPacket.ID, this::onGetEntityPacket);
		MVServerNetworking.registerListener(SetBlockC2SPacket.ID, this::onSetBlockPacket);
		MVServerNetworking.registerListener(SetEntityC2SPacket.ID, this::onSetEntityPacket);
		MVServerNetworking.registerListener(SummonEntityC2SPacket.ID, this::onSummonEntityPacket);
		
		MVServerNetworking.PlayNetworkStateEvents.Start.EVENT.register(this);
	}
	
	@Override
	public void onPlayStart(ServerPlayer player) {
		MVServerNetworking.send(player, new ProtocolVersionS2CPacket(PROTOCOL_VERSION));
	}
	
	private void onSetCursorPacket(SetCursorC2SPacket packet, ServerPlayer player) {
		if (!ServerMVMisc.hasPermissionLevel(player, 2))
			return;
		
		MainUtil.setCursorStackSilently(player.containerMenu, packet.getItem());
	}
	
	private void onSetSlotPacket(SetSlotC2SPacket packet, ServerPlayer player) {
		if (!ServerMVMisc.hasPermissionLevel(player, 2))
			return;
		if (player.containerMenu == player.inventoryMenu)
			return;
		
		Slot slot = player.containerMenu.getSlot(packet.getSlot());
		if (slot.container == player.getInventory())
			return;
		
		slot.set(packet.getItem());
	}
	
	private void onOpenEnderChestPacket(OpenEnderChestC2SPacket packet, ServerPlayer player) {
		if (!ServerMVMisc.hasPermissionLevel(player, 2))
			return;
		
		player.openMenu(new SimpleMenuProvider((syncId, inventory, player2) ->
				ChestMenu.threeRows(syncId, inventory, player.getEnderChestInventory()),
				TextInst.translatable("container.enderchest")));
	}
	
	private void onGetBlockPacket(GetBlockC2SPacket packet, ServerPlayer player) {
		if (!ServerMVMisc.hasPermissionLevel(player, 2))
			return;
		
		ServerLevel world = player.server.getLevel(packet.getWorld());
		if (world != null) {
			BlockEntity blockEntity = world.getBlockEntity(packet.getPos());
			if (blockEntity != null) {
				sendViewBlockPacket(packet.getRequestId(), blockEntity, player);
				return;
			}
		}
		
		MVServerNetworking.send(player, new ViewBlockS2CPacket(packet.getRequestId(), packet.getWorld(), packet.getPos(), null, null, null));
	}
	private void onGetLecternBlockPacket(GetLecternBlockC2SPacket packet, ServerPlayer player) {
		if (!ServerMVMisc.hasPermissionLevel(player, 2))
			return;
		
		if (player.containerMenu instanceof LecternMenu handler) {
			// Get the LecternBlockEntity from the inventory's synthetic reference to its enclosing class
			Container inv = handler.lectern;
			LecternBlockEntity lectern = Reflection.getField(inv.getClass(), "field_17391", "Lnet/minecraft/class_3722;").get(inv);
			if (lectern != null) {
				sendViewBlockPacket(packet.getRequestId(), lectern, player);
				return;
			}
		}
		
		MVServerNetworking.send(player, new ViewBlockS2CPacket(packet.getRequestId(), null, null, null, null, null));
	}
	private void sendViewBlockPacket(int requestId, BlockEntity blockEntity, ServerPlayer player) {
		MVServerNetworking.send(player,
				new ViewBlockS2CPacket(requestId, blockEntity.getLevel().dimension(), blockEntity.getBlockPos(),
						MVRegistry.BLOCK.getId(blockEntity.getBlockState().getBlock()),
						new BlockStateProperties(blockEntity.getBlockState()),
						NBTManagers.BLOCK_ENTITY.getNbt(blockEntity)));
	}
	
	private void onGetEntityPacket(GetEntityC2SPacket packet, ServerPlayer player) {
		if (!ServerMVMisc.hasPermissionLevel(player, 2))
			return;
		
		ServerLevel world = player.server.getLevel(packet.getWorld());
		if (world != null) {
			Entity entity = world.getEntity(packet.getUUID());
			if (entity != null && !(entity instanceof Player)) {
				MVServerNetworking.send(player,
						new ViewEntityS2CPacket(packet.getRequestId(),
								entity.level().dimension(), entity.getUUID(),
								EntityType.getKey(entity.getType()), NBTManagers.ENTITY.getNbt(entity)));
				return;
			}
		}
		
		MVServerNetworking.send(player, new ViewEntityS2CPacket(packet.getRequestId(), packet.getWorld(), packet.getUUID(), null, null));
	}
	
	private void onSetBlockPacket(SetBlockC2SPacket packet, ServerPlayer player) {
		if (!ServerMVMisc.hasPermissionLevel(player, 2))
			return;
		
		ServerLevel world = player.server.getLevel(packet.getWorld());
		if (world == null)
			return;
		
		Block block = MVRegistry.BLOCK.get(packet.getId());
		BlockState state = world.getBlockState(packet.getPos());
		if (state.getBlock() != block) {
			world.removeBlockEntity(packet.getPos());
			world.setBlockAndUpdate(packet.getPos(),
					packet.getState().applyToSafely(block.defaultBlockState()));
		} else {
			if (!new BlockStateProperties(state).equals(packet.getState()))
				world.setBlockAndUpdate(packet.getPos(), packet.getState().applyTo(state));
			if (packet.isRecreate())
				world.removeBlockEntity(packet.getPos());
		}
		
		BlockEntity blockEntity = world.getBlockEntity(packet.getPos());
		if (blockEntity == null)
			return;
		
		NBTManagers.BLOCK_ENTITY.setNbt(blockEntity, packet.getNbt());
		
		if (packet.isTriggerUpdate()) {
			blockEntity.setChanged();
			// Flags arg seems to be unused, and I don't know what it's supposed to be for this
			world.sendBlockUpdated(packet.getPos(), blockEntity.getBlockState(), blockEntity.getBlockState(), 0);
		}
	}
	
	private void onSetEntityPacket(SetEntityC2SPacket packet, ServerPlayer player) {
		if (!ServerMVMisc.hasPermissionLevel(player, 2))
			return;
		
		ServerLevel world = player.server.getLevel(packet.getWorld());
		if (world == null)
			return;
		
		Entity entity = world.getEntity(packet.getUUID());
		if (entity == null)
			return;
		
		UUID newUUID = packet.getUUID();
		if (packet.getNbt().getIntArray("UUID").isPresent()) {
			newUUID = UUIDUtil.uuidFromIntArray(packet.getNbt().getIntArray("UUID").orElseGet(() -> new IntArrayTag(new int[]{0,0,0,0}).getAsIntArray()));
			if (!packet.getUUID().equals(newUUID) && world.getEntity(newUUID) != null) {
				newUUID = packet.getUUID();
				packet.getNbt().put("UUID", new IntArrayTag(UUIDUtil.uuidToIntArray(newUUID)));
			}
		} else
			packet.getNbt().put("UUID", new IntArrayTag(UUIDUtil.uuidToIntArray(packet.getUUID())));
		
		EntityType<?> entityType = MVRegistry.ENTITY_TYPE.get(packet.getId());
		
		if (packet.isRecreate() || !entity.getUUID().equals(newUUID) || entity.getType() != entityType) {
			Entity vehicle = entity.getVehicle();
			Vec3 pos = entity.position();
			float yaw = entity.getYRot();
			float bodyYaw = (entity instanceof LivingEntity livingEntity ? livingEntity.yBodyRot : 0);
			float headYaw = entity.getYHeadRot();
			float pitch = entity.getXRot();
			entity.getPassengersAndSelf().forEach(passengerOrSelf -> {
				passengerOrSelf.stopRiding();
				passengerOrSelf.remove(RemovalReason.DISCARDED);
			});
			entity = ServerMVMisc.createEntity(entityType, world);
			entity.setUUID(newUUID);
			entity.setPos(pos);
			entity.setYRot(yaw);
			entity.setYBodyRot(bodyYaw);
			entity.setYHeadRot(headYaw);
			entity.setXRot(pitch);
			world.addFreshEntity(entity);
			readEntityNbtWithPassengers(world, entity, packet.getNbt());
			if (vehicle != null)
				entity.startRiding(vehicle, true,true);
		} else {
			entity.getEntityData().assignValues(Objects.requireNonNull(new SynchedEntityData.Builder(entity).build().getNonDefaultValues()));
			readEntityNbtWithPassengers(world, entity, packet.getNbt());
		}
	}
	
	private void onSummonEntityPacket(SummonEntityC2SPacket packet, ServerPlayer player) {
		if (!ServerMVMisc.hasPermissionLevel(player, 2))
			return;
		
		ServerLevel world = player.server.getLevel(packet.getWorld());
		if (world == null) {
			MVServerNetworking.send(player, new ViewEntityS2CPacket(packet.getRequestId(), null, null, null, null));
			return;
		}
		
		UUID uuid = UUID.randomUUID();
		if (packet.getNbt().get("UUID") instanceof IntArrayTag) {
			UUID nbtUUID = UUIDUtil.uuidFromIntArray(packet.getNbt().getIntArray("UUID").orElse(new int[]{0,0,0,0}));
			if (world.getEntity(nbtUUID) == null)
				uuid = nbtUUID;
			else
				packet.getNbt().putIntArray("UUID", UUIDUtil.uuidToIntArray(uuid));
		}
		
		Entity entity = ServerMVMisc.createEntity(MVRegistry.ENTITY_TYPE.get(packet.getId()), world);
		entity.setUUID(uuid);
		entity.setPos(packet.getPos());
		packet.getNbt().put("Pos", Stream.of(packet.getPos().x, packet.getPos().y, packet.getPos().z)
				.map(DoubleTag::valueOf).collect(ListTag::new, ListTag::add, ListTag::addAll));
		world.addFreshEntity(entity);
		readEntityNbtWithPassengers(world, entity, packet.getNbt());
		
		MVServerNetworking.send(player,
				new ViewEntityS2CPacket(packet.getRequestId(), entity.level().dimension(), entity.getUUID(),
						EntityType.getKey(entity.getType()), NBTManagers.ENTITY.getNbt(entity)));
	}
	
	private void readEntityNbtWithPassengers(ServerLevel world, Entity entity, CompoundTag nbt) {
		NBTManagers.ENTITY.setNbt(entity, nbt);
		
		Map<UUID, Entity> passengers = entity.getPassengers().stream().collect(Collectors.toMap(Entity::getUUID, Function.identity()));
		ListTag passengersNbt = nbt.getListOrEmpty("Passengers");
		Set<UUID> passengerUUIDs = new HashSet<>();
		
		for (Tag passengerNbtElement : passengersNbt) {
			CompoundTag passengerNbt = (CompoundTag) passengerNbtElement;
			UUID passengerUUID = passengerNbt.contains("UUID") ? UUIDUtil.uuidFromIntArray(passengerNbt.getIntArray("UUID").orElse(new int[]{0,0,0,0})) : null;
			if (passengerUUID == null || !passengerUUIDs.add(passengerUUID)) {
				passengerUUID = UUID.randomUUID();
				passengerNbt.putIntArray("UUID", UUIDUtil.uuidToIntArray(passengerUUID));
			}
			Entity passenger = passengers.get(passengerUUID);
			
			Identifier passengerId = null;
			if (passengerNbt.contains("id")) {
				try {
					passengerId = IdentifierInst.of(passengerNbt.getStringOr("id",""));
					if (!MVRegistry.ENTITY_TYPE.containsId(passengerId))
						passengerId = null;
				} catch (IdentifierException e) {}
			}
			if (passengerId != null && passenger != null && !EntityType.getKey(passenger.getType()).equals(passengerId)) {
				passenger.getPassengersAndSelf().forEach(passengerOrSelf -> {
					passengerOrSelf.stopRiding();
					passengerOrSelf.remove(RemovalReason.DISCARDED);
				});
				passenger = null;
			}
			
			if (passenger == null) {
				if (passengerId == null)
					continue;
				EntityType<?> passengerType = MVRegistry.ENTITY_TYPE.get(passengerId);
				if (world.getEntity(passengerUUID) != null) {
					passengerUUID = UUID.randomUUID();
					passengerNbt.putIntArray("UUID", UUIDUtil.uuidToIntArray(passengerUUID));
				}
				passenger = ServerMVMisc.createEntity(passengerType, world);
				passenger.setUUID(passengerUUID);
				passenger.startRiding(entity, true,true);
				world.addFreshEntity(passenger);
			}
			
			readEntityNbtWithPassengers(world, passenger, passengerNbt);
		}
		
		passengers.keySet().removeAll(passengerUUIDs);
		for (Entity passenger : passengers.values()) {
			passenger.getPassengersAndSelf().forEach(passengerOrSelf -> {
				passengerOrSelf.stopRiding();
				passengerOrSelf.remove(RemovalReason.DISCARDED);
			});
		}
	}
	
}
