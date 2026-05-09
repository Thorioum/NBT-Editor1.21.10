package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.components;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Attempt;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.DynamicRegistryManagerHolder;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.DeserializableNBTManager;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtOps;

public class ComponentItemNBTManager implements DeserializableNBTManager<ItemStack> {
	
	@Override
	public Attempt<CompoundTag> trySerialize(ItemStack subject) {
		if (subject.isEmpty())
			return new Attempt<>(new CompoundTag());
		
		DataResult<Tag> result = ItemStack.CODEC.encodeStart(
				(MainUtil.client.getConnection() == null ? VanillaRegistries.createLookup() : MainUtil.client.getConnection().registryAccess()).createSerializationContext(NbtOps.INSTANCE), subject);
		return new Attempt<>(
				result.resultOrPartial().map(nbt -> (CompoundTag) nbt.copy()),
				result.error().map(DataResult.Error::message).orElse(null));
	}
	@Override
	public Attempt<ItemStack> tryDeserialize(CompoundTag nbt) {
		if (nbt.getString("id").filter(id -> id.equals("minecraft:air") || id.equals(":air") || id.equals("air")).isPresent())
			return new Attempt<>(ItemStack.EMPTY);
		if (nbt.getInt("count").filter(count -> count <= 0).isPresent())
			return new Attempt<>(ItemStack.EMPTY);
		
		DataResult<Pair<ItemStack, Tag>> result = ItemStack.OPTIONAL_CODEC.decode(
				(MainUtil.client.getConnection() == null ? VanillaRegistries.createLookup() : MainUtil.client.getConnection().registryAccess()).createSerializationContext(NbtOps.INSTANCE), nbt.copy());
		return new Attempt<>(
				result.resultOrPartial().map(Pair::getFirst).map(item -> {
					if (item.has(DataComponents.MAX_DAMAGE) && item.getOrDefault(DataComponents.MAX_STACK_SIZE, 1) > 1)
						item.remove(DataComponents.MAX_DAMAGE);
					return item;
				}),
				result.error().map(DataResult.Error::message).orElse(null));
	}
	
	@Override
	public boolean hasNbt(ItemStack subject) {
		return !subject.getComponentsPatch().isEmpty();
	}
	@Override
	public CompoundTag getNbt(ItemStack subject) {
		return (CompoundTag) DataComponentPatch.CODEC.encodeStart(
				(MainUtil.client.getConnection() == null ? VanillaRegistries.createLookup() : MainUtil.client.getConnection().registryAccess()).createSerializationContext(NbtOps.INSTANCE), subject.getComponentsPatch()).getOrThrow().copy();
	}
	@Override
	public CompoundTag getOrCreateNbt(ItemStack subject) {
		return getNbt(subject);
	}
	@Override
	public void setNbt(ItemStack subject, CompoundTag nbt) {
		DataComponentPatch components = DataComponentPatch.CODEC.decode(
				(MainUtil.client.getConnection() == null ? VanillaRegistries.createLookup() : MainUtil.client.getConnection().registryAccess()).createSerializationContext(NbtOps.INSTANCE), nbt.copy()).getPartialOrThrow().getFirst();
		Map<DataComponentType<?>, Optional<?>> componentMap = components.entrySet().stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue
				));
		Optional<? extends Integer> maxDamage = (Optional<? extends Integer>) componentMap.get(DataComponents.MAX_DAMAGE);
		Optional<? extends Integer> maxStackSize = (Optional<? extends Integer>) componentMap.get(DataComponents.MAX_STACK_SIZE);
		if (maxDamage != null && maxDamage.isPresent() &&
				(maxStackSize == null ?
						subject.getPrototype().get(DataComponents.MAX_STACK_SIZE) > 1 :
						maxStackSize.isPresent() && maxStackSize.get() > 1)) {
			components = components.forget(component -> component == DataComponents.MAX_DAMAGE);
		}
		MixinLink.setChanges(subject, components);
	}
	
	@Override
	public String getNbtString(ItemStack subject) {
		DataComponentPatch components = subject.getComponentsPatch();
		StringBuilder builder = new StringBuilder("[");
		boolean first = true;
		for (Map.Entry<DataComponentType<?>, Optional<?>> entry : components.entrySet()) {
			if (first)
				first = false;
			else
				builder.append(",");
			entry.getValue().ifPresentOrElse(value -> {
				builder.append(entry.getKey());
				builder.append("=");
				builder.append(encodeComponent(entry.getKey(), value).getPartialOrThrow());
			}, () -> {
				builder.append("!");
				builder.append(entry.getKey());
			});
		}
		builder.append(']');
		return builder.toString();
	}
	@SuppressWarnings("unchecked")
	private <T> DataResult<Tag> encodeComponent(DataComponentType<T> component, Object value) {
		return component.codecOrThrow().encodeStart(
				(MainUtil.client.getConnection() == null ? VanillaRegistries.createLookup() : MainUtil.client.getConnection().registryAccess()).createSerializationContext(NbtOps.INSTANCE), (T) value);
	}
	
}
