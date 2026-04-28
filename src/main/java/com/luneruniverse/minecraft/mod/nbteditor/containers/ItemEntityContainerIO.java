package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalEntity;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.NBTManagers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;

public record ItemEntityContainerIO(ContainerIO<ItemStack> item, ContainerIO<LocalEntity> entity) {
	
	public static ItemEntityContainerIO forEntityTagIO(ContainerIO<CompoundTag> io, String entityId) {
		return new ItemEntityContainerIO(ContainerIO.forItemStackEntityTag(new EntityContainerIO(false), entityId), ContainerIO.forLocalNBT(io));
	}
	public static ItemEntityContainerIO forEntityTagIO(ContainerIO<CompoundTag> io, EntityType<?> entityId) {
		return forEntityTagIO(io, EntityType.getKey(entityId).toString());
	}
	
	public static ItemEntityContainerIO forKeys(String entityId, String... keys) {
		return new ItemEntityContainerIO(
				ContainerIO.forItemStackEntityTag(new EntityContainerIO(true, keys), entityId),
				ContainerIO.forLocalNBT(new KeysContainerIO(false, keys)));
	}
	public static ItemEntityContainerIO forKeys(EntityType<?> entityId, String... keys) {
		return forKeys(EntityType.getKey(entityId).toString(), keys);
	}
	
	public ItemEntityContainerIO withTextures(Identifier... textures) {
		return new ItemEntityContainerIO(item.withTextures(textures), entity.withTextures(textures));
	}

	public static final HolderLookup.Provider defaultLookup = VanillaRegistries.createLookup();
	public static HolderLookup.Provider lookup() {
		if(Minecraft.getInstance().getConnection() == null) return defaultLookup;
		HolderLookup.Provider networkLookup = Minecraft.getInstance().getConnection().registryAccess();
		return networkLookup == null ? defaultLookup : networkLookup;
	}

	public static class EntityContainerIO implements ContainerIO<TypedEntityData<EntityType<?>>> {

		private final boolean removeWhenEmpty;
		private final String[] keys;
		private final Identifier[] textures;

		public EntityContainerIO(boolean removeWhenEmpty, String... keys) {
			this.removeWhenEmpty = removeWhenEmpty;
			this.keys = keys;
			this.textures = new Identifier[keys.length];
		}

		@Override
		public boolean isSupported(TypedEntityData<EntityType<?>> container) {
			for (String key : keys) {
				Tag itemNbtElement = container.getUnsafe().get(key);
				if (itemNbtElement != null && !(itemNbtElement instanceof CompoundTag))
					return false;
			}
			return true;
		}

		@Override
		public int getMaxSlots(TypedEntityData<EntityType<?>> container) {
			return keys.length;
		}

		@Override
		public Identifier[] getTextures(TypedEntityData<EntityType<?>> container) {
			return textures;
		}

		@Override
		public ItemStack[] read(TypedEntityData<EntityType<?>> container) {
			ItemStack[] contents = new ItemStack[keys.length];
			for (int i = 0; i < keys.length; i++) {
				contents[i] = container.getUnsafe().getCompound(keys[i])
						.map(itemNbt -> NBTManagers.ITEM.deserializeOrElse(itemNbt, ItemStack.EMPTY)).orElse(ItemStack.EMPTY);
			}
			return contents;
		}

		@Override
		public int write(TypedEntityData<EntityType<?>> container, ItemStack[] contents) {
			for (int i = 0; i < keys.length; i++) {
				ItemStack item = contents[i];
				if (item == null || item.isEmpty()) {
					if (removeWhenEmpty) {
						container.getUnsafe().remove(keys[i]);
						continue;
					} else {
						item = ItemStack.EMPTY;
					}
				}
				container.getUnsafe().put(keys[i], ItemStack.CODEC.encodeStart(lookup().createSerializationContext(RegistryOps.create(NbtOps.INSTANCE, lookup())), item).getOrThrow());
			}
			return keys.length;
		}

		@Override
		public int getNumWritten(TypedEntityData<EntityType<?>> container, ItemStack[] contents) {
			return keys.length;
		}

		@Override
		public int getWrittenSlotIndex(TypedEntityData<EntityType<?>> container, ItemStack[] contents, int slot) {
			return slot;
		}

	}

	public static class BlockEntityContainerIO implements ContainerIO<TypedEntityData<BlockEntityType<?>>> {

		private final boolean removeWhenEmpty;
		private final String[] keys;
		private final Identifier[] textures;

		public BlockEntityContainerIO(boolean removeWhenEmpty, String... keys) {
			this.removeWhenEmpty = removeWhenEmpty;
			this.keys = keys;
			this.textures = new Identifier[keys.length];
		}

		@Override
		public boolean isSupported(TypedEntityData<BlockEntityType<?>> container) {
			for (String key : keys) {
				Tag itemNbtElement = container.getUnsafe().get(key);
				if (itemNbtElement != null && !(itemNbtElement instanceof CompoundTag))
					return false;
			}
			return true;
		}

		@Override
		public int getMaxSlots(TypedEntityData<BlockEntityType<?>> container) {
			return keys.length;
		}

		@Override
		public Identifier[] getTextures(TypedEntityData<BlockEntityType<?>> container) {
			return textures;
		}

		@Override
		public ItemStack[] read(TypedEntityData<BlockEntityType<?>> container) {
			ItemStack[] contents = new ItemStack[keys.length];
			for (int i = 0; i < keys.length; i++) {
				contents[i] = container.getUnsafe().getCompound(keys[i])
						.map(itemNbt -> NBTManagers.ITEM.deserializeOrElse(itemNbt, ItemStack.EMPTY)).orElse(ItemStack.EMPTY);
			}
			return contents;
		}

		@Override
		public int write(TypedEntityData<BlockEntityType<?>> container, ItemStack[] contents) {
			for (int i = 0; i < keys.length; i++) {
				ItemStack item = contents[i];
				if (item == null || item.isEmpty()) {
					if (removeWhenEmpty) {
						container.getUnsafe().remove(keys[i]);
						continue;
					} else {
						item = ItemStack.EMPTY;
					}
				}
				container.getUnsafe().put(keys[i], ItemStack.CODEC.encodeStart(lookup().createSerializationContext(RegistryOps.create(NbtOps.INSTANCE, lookup())), item).getOrThrow());
			}
			return keys.length;
		}

		@Override
		public int getNumWritten(TypedEntityData<BlockEntityType<?>> container, ItemStack[] contents) {
			return keys.length;
		}

		@Override
		public int getWrittenSlotIndex(TypedEntityData<BlockEntityType<?>> container, ItemStack[] contents, int slot) {
			return slot;
		}

	}
}
