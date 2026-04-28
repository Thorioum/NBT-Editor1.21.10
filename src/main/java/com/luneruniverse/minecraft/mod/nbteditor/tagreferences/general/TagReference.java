package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;

public interface TagReference<T, O> {
	public static <T1, T2, O> TagReference<T2, O> mapValue(Function<T1, T2> getter, Function<T2, T1> setter, TagReference<T1, O> tagRef) {
		return new TagReference<>() {
			@Override
			public T2 get(O object) {
				return getter.apply(tagRef.get(object));
			}
			@Override
			public void set(O object, T2 value) {
				tagRef.set(object, setter.apply(value));
			}
		};
	}
	public static <T, O1, O2> TagReference<T, O2> mapObject(Function<O2, O1> getter, BiConsumer<O2, O1> setter, TagReference<T, O1> tagRef) {
		return new TagReference<>() {
			@Override
			public T get(O2 object) {
				return tagRef.get(getter.apply(object));
			}
			@Override
			public void set(O2 object, T value) {
				O1 internalObject = getter.apply(object);
				tagRef.set(internalObject, value);
				setter.accept(object, internalObject);
			}
		};
	}
	
	public static <T> TagReference<T, ItemStack> forItems(Supplier<T> defaultValue, TagReference<T, CompoundTag> tagRef) {
		return null;
	}
	public static <T, O extends LocalNBT> TagReference<T, O> forLocalNBT(Supplier<T> defaultValue, TagReference<T, CompoundTag> tagRef) {
		return new TagReference<>() {
			@Override
			public T get(O object) {
				CompoundTag nbt = object.getNBT();
				if (nbt == null)
					return defaultValue.get();
				return tagRef.get(nbt);
			}
			@Override
			public void set(O object, T value) {
				object.modifyNBT(nbt -> { tagRef.set(nbt, value); });
			}
		};
	}
	@SuppressWarnings("unchecked")
	public static <C, O> TagReference<List<C>, O> forLists(Class<C> clazz, TagReference<C[], O> tagRef) {
		return mapValue(
				array -> new ArrayList<>(Arrays.asList(array)),
				list -> list == null ? null : list.toArray(len -> (C[]) Array.newInstance(clazz, len)),
				tagRef);
	}
	public static <C, O> TagReference<List<C>, O> forLists(Function<Tag, C> getter, Function<C, Tag> setter, TagReference<ListTag, O> tagRef) {
		return null;
	}
	public static <V, O> TagReference<Map<String, V>, O> forMaps(Function<Tag, V> getter, Function<V, Tag> setter, TagReference<CompoundTag, O> tagRef) {
		return mapValue(
				compound -> {
					Map<String, V> output = new HashMap<>();
					for (String key : compound.keySet()) {
						V entryValue = getter.apply(compound.get(key));
						if (entryValue != null)
							output.put(key, entryValue);
					}
					return output;
				},
				map -> {
					if (map == null)
						return null;
					CompoundTag compound = new CompoundTag();
					map.forEach((key, entryValue) -> {
						Tag entryValueNbt = setter.apply(entryValue);
						if (entryValueNbt != null)
							compound.put(key, entryValueNbt);
					});
					return compound;
				},
				tagRef);
	}
	public static <T> TagReference<T, CompoundTag> alsoRemove(String path, TagReference<T, CompoundTag> tagRef) {
		return new TagReference<>() {
			@Override
			public T get(CompoundTag object) {
				return tagRef.get(object);
			}
			@Override
			public void set(CompoundTag object, T value) {
				tagRef.set(object, value);
				
				String[] pathParts = path.split("/");
				CompoundTag nbt = object;
				for (int i = 0; i < pathParts.length - 1; i++)
					nbt = nbt.getCompoundOrEmpty(pathParts[i]);
				nbt.remove(pathParts[pathParts.length - 1]);
			}
		};
	}
	
	/**
	 * If T is a collection, this should return a mutable copy
	 */
	public T get(O object);
	public void set(O object, T value);
	
	public default void modify(O object, UnaryOperator<T> modifier) {
		set(object, modifier.apply(get(object)));
	}
	public default void modify(O object, Consumer<T> modifier) {
		modify(object, value -> {
			modifier.accept(value);
			return value;
		});
	}
}
