package com.luneruniverse.minecraft.mod.nbteditor.screens.nbtfolder;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTValue;
import com.luneruniverse.minecraft.mod.nbteditor.util.ClassMap;

import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.StringTag;

public interface NBTFolder<T extends Tag> {
	
	public interface Constructor<T extends Tag> {
		public NBTFolder<T> create(Supplier<T> get, Consumer<T> set);
	}
	public static final ClassMap<Tag, Constructor<?>> TYPES = getTypesMap();
	private static ClassMap<Tag, Constructor<?>> getTypesMap() {
		ClassMap<Tag, Constructor<?>> output = new ClassMap<>();
		output.put(CollectionTag.class, (Constructor<CollectionTag>) ListNBTFolder::new);
		output.put(CompoundTag.class, (Constructor<CompoundTag>) CompoundNBTFolder::new);
		output.put(StringTag.class, (Constructor<StringTag>) StringNBTFolder::new);
		return output;
	}
	@SuppressWarnings("unchecked")
	public static <T extends Tag> NBTFolder<T> get(Class<T> nbt, Supplier<T> get, Consumer<T> set) {
		Constructor<?> constructor = TYPES.get(nbt);
		if (constructor == null)
			return null;
		return ((Constructor<T>) constructor).create(get, set);
	}
	@SuppressWarnings("unchecked")
	public static <T extends Tag> NBTFolder<? extends T> get(T nbt) {
		AtomicReference<T> ref = new AtomicReference<>(nbt);
		return get((Class<T>) nbt.getClass(), ref::getPlain, ref::setPlain);
	}
	
	public T getNBT();
	public void setNBT(T value);
	
	public List<NBTValue> getEntries(NBTEditorScreen<?> screen);
	public boolean hasEmptyKey();
	
	public Tag getValue(String key);
	public void setValue(String key, Tag value);
	
	public void addKey(String key);
	public void removeKey(String key);
	
	public Optional<String> getNextKey(Optional<String> pastingKey);
	public Predicate<String> getKeyValidator(boolean renaming);
	public boolean handlesDuplicateKeys();
	
	public default NBTFolder<?> getSubFolder(String key) {
		Tag value = getValue(key);
		if (value == null)
			return null;
		return getSubFolder(key, value.getClass());
	}
	private <T2 extends Tag> NBTFolder<T2> getSubFolder(String key, Class<T2> clazz) {
		return get(clazz, () -> clazz.cast(getValue(key)), newValue -> setValue(key, newValue));
	}
	
}
