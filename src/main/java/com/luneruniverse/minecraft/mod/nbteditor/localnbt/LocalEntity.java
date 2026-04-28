package com.luneruniverse.minecraft.mod.nbteditor.localnbt;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMatrix4f;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVQuaternionf;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTextEvents;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.NBTManagers;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.EntityReference;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SummonEntityC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ViewEntityS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.ItemTagReferences;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.joml.Matrix3x2fStack;

public class LocalEntity implements LocalNBT {
	
	public static LocalEntity deserialize(CompoundTag nbt, int defaultDataVersion) {
		CompoundTag tag = nbt.getCompoundOrEmpty("tag");
		tag.putString("id", nbt.getStringOr("id",""));
		tag = MainUtil.updateDynamic(References.ENTITY, tag, nbt.get("DataVersion"), defaultDataVersion);
		String id = tag.getStringOr("id","");
		tag.remove("id");
		return new LocalEntity(MVRegistry.ENTITY_TYPE.get(IdentifierInst.of(id)), tag);
	}
	
	private EntityType<?> entityType;
	private CompoundTag nbt;
	
	private Entity cachedEntity;
	private CompoundTag cachedNbt;
	
	public LocalEntity(EntityType<?> entityType, CompoundTag nbt) {
		this.entityType = entityType;
		this.nbt = nbt;
	}
	
	private Entity getCachedEntity() {
		if (cachedEntity != null && cachedEntity.getType() == entityType && Objects.equals(cachedNbt, nbt))
			return cachedEntity;
		
		cachedEntity = ServerMVMisc.createEntity(entityType, MainUtil.client.level);
		NBTManagers.ENTITY.setNbt(cachedEntity, nbt);
		
		cachedNbt = nbt.copy();
		
		return cachedEntity;
	}
	
	@Override
	public boolean isEmpty(Identifier id) {
		return false;
	}
	
	@Override
	public Component getName() {
		return MainUtil.getNbtNameSafely(nbt, "CustomName", () -> TextInst.of(getDefaultName()));
	}
	@Override
	public void setName(Component name) {
		if (name == null)
			getOrCreateNBT().remove("CustomName");
		else
			getOrCreateNBT().put("CustomName", TextInst.toMinecraft(name));
	}
	@Override
	public String getDefaultName() {
		return entityType.getDescription().getString();
	}
	
	@Override
	public Identifier getId() {
		return MVRegistry.ENTITY_TYPE.getId(entityType);
	}
	@Override
	public void setId(Identifier id) {
		this.entityType = MVRegistry.ENTITY_TYPE.get(id);
	}
	@Override
	public Set<Identifier> getIdOptions() {
		return MVRegistry.ENTITY_TYPE.getIds();
	}
	
	public EntityType<?> getEntityType() {
		return entityType;
	}
	public void setEntityType(EntityType<?> entityType) {
		this.entityType = entityType;
	}
	
	@Override
	public CompoundTag getNBT() {
		return nbt;
	}
	@Override
	public void setNBT(CompoundTag nbt) {
		this.nbt = nbt;
	}
	@Override
	public CompoundTag getOrCreateNBT() {
		return nbt;
	}
	
	@Override
	public void renderIcon(Matrix3x2fStack matrices, int x, int y, float tickDelta) {
		//no
	}
	
	@Override
	public Optional<ItemStack> toItem(boolean cleanup) {
		ItemStack output = null;
		for (Item item : MVRegistry.ITEM) {
			if (item instanceof SpawnEggItem spawnEggItem && MVMisc.getEntityType(new ItemStack(spawnEggItem)) == entityType)
				output = new ItemStack(spawnEggItem);
		}
		if (output == null) {
			if (entityType == EntityType.ARMOR_STAND)
				output = new ItemStack(Items.ARMOR_STAND);
			else if (entityType == EntityType.ITEM_FRAME)
				output = new ItemStack(Items.ITEM_FRAME);
			else if (entityType == EntityType.GLOW_ITEM_FRAME)
				output = new ItemStack(Items.GLOW_ITEM_FRAME);
			else if (entityType == EntityType.PAINTING)
				output = new ItemStack(Items.PAINTING);
			else {
				output = Version.<ItemStack>newSwitch()
						.range("1.20.3", null, () -> {
							if (entityType == EntityType.COMMAND_BLOCK_MINECART)
								return new ItemStack(Items.COMMAND_BLOCK_MINECART);
							if (entityType == EntityType.FURNACE_MINECART)
								return new ItemStack(Items.FURNACE_MINECART);
							if (entityType == EntityType.MINECART)
								return new ItemStack(Items.MINECART);
							if (entityType == EntityType.CHEST_MINECART)
								return new ItemStack(Items.CHEST_MINECART);
							if (entityType == EntityType.HOPPER_MINECART)
								return new ItemStack(Items.HOPPER_MINECART);
							if (entityType == EntityType.TNT_MINECART)
								return new ItemStack(Items.TNT_MINECART);
							if (getCachedEntity() instanceof AbstractBoat)
								return new ItemStack(MVMisc.getBoatItem(entityType, nbt));
							return new ItemStack(Items.PIG_SPAWN_EGG);
						})
						.range(null, "1.20.2", () -> new ItemStack(Items.PIG_SPAWN_EGG))
						.get();
			}
		}
		
		CompoundTag nbt = this.nbt.copy();
		nbt.putString("id", getId().toString());
		
		if (cleanup) {
			nbt.remove("Passengers"); // Passengers don't work on spawn eggs
			nbt.remove("UUID");
			nbt.remove("Pos");
			if (entityType == EntityType.ITEM_FRAME || entityType == EntityType.GLOW_ITEM_FRAME ||
					entityType == EntityType.PAINTING) {
				nbt.remove("Rotation");
				Version.newSwitch()
						.range("1.21.5", null, () -> nbt.remove("block_pos"))
						.range(null, "1.21.4", () -> {
							nbt.remove("TileX");
							nbt.remove("TileY");
							nbt.remove("TileZ");
						})
						.run();
				if (entityType == EntityType.PAINTING)
					nbt.remove("facing");
				else
					nbt.remove("Facing");
			}
		}
		
		ItemTagReferences.ENTITY_DATA.set(output, TypedEntityData.of(BuiltInRegistries.ENTITY_TYPE.getValue(getId()),nbt));
		
		return Optional.of(output);
	}
	@Override
	public CompoundTag serialize() {
		CompoundTag output = new CompoundTag();
		output.putString("id", getId().toString());
		output.put("tag", nbt);
		output.putString("type", "entity");
		return output;
	}
	@Override
	public Component toHoverableText() {
		UUID uuid = UUIDUtil.uuidFromIntArray(nbt.getIntArray("UUID").orElseGet(() -> new IntArrayTag(new int[]{0,0,0,0}).getAsIntArray()));
		return TextInst.bracketed(getName()).styled(
				style -> style.withHoverEvent(MVTextEvents.HoverAction.SHOW_ENTITY.newEvent(new HoverEvent.EntityTooltipInfo(
						entityType, uuid, MainUtil.getNbtNameSafely(nbt, "CustomName", () -> null)))));
	}
	
	public CompletableFuture<Optional<EntityReference>> summon(ResourceKey<Level> world, Vec3 pos) {
		return NBTEditorClient.SERVER_CONN
				.sendRequest(requestId -> new SummonEntityC2SPacket(requestId, world, pos, getId(), nbt.copy()), ViewEntityS2CPacket.class)
				.thenApply(optional -> optional.filter(ViewEntityS2CPacket::foundEntity)
						.map(packet -> {
							EntityReference ref = new EntityReference(packet.getWorld(), packet.getUUID(),
									MVRegistry.ENTITY_TYPE.get(packet.getId()), packet.getNbt());
							MainUtil.client.player.sendSystemMessage(TextInst.translatable("nbteditor.get.entity")
									.append(ref.getLocalNBT().toHoverableText()));
							return ref;
						}));
	}
	
	@Override
	public LocalEntity copy() {
		return new LocalEntity(entityType, nbt.copy());
	}
	
	@Override
	public boolean equals(Object nbt) {
		if (nbt instanceof LocalEntity entity)
			return this.entityType == entity.entityType && this.nbt.equals(entity.nbt);
		return false;
	}
	
}
