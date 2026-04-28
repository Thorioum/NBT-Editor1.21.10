package com.luneruniverse.minecraft.mod.nbteditor.screens.nbtfolder;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTValue;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class CompoundNBTFolder implements NBTFolder<CompoundTag> {
	
	private final Supplier<CompoundTag> get;
	private final Consumer<CompoundTag> set;
	
	public CompoundNBTFolder(Supplier<CompoundTag> get, Consumer<CompoundTag> set) {
		this.get = get;
		this.set = set;
	}
	
	@Override
	public CompoundTag getNBT() {
		return get.get();
	}
	
	@Override
	public void setNBT(CompoundTag value) {
		set.accept(value);
	}
	
	@Override
	public List<NBTValue> getEntries(NBTEditorScreen<?> screen) {
		CompoundTag nbt = getNBT();
		return nbt.keySet().stream().map(key -> new NBTValue(screen, key, nbt.get(key))).collect(Collectors.toList());
	}
	
	@Override
	public boolean hasEmptyKey() {
		return getNBT().contains("");
	}
	
	@Override
	public Tag getValue(String key) {
		return getNBT().get(key);
	}
	
	@Override
	public void setValue(String key, Tag value) {
		CompoundTag nbt = getNBT();
		nbt.put(key, value);
		setNBT(nbt);
	}
	
	@Override
	public void addKey(String key) {
		CompoundTag nbt = getNBT();
		nbt.putInt(key, 0);
		setNBT(nbt);
	}
	
	@Override
	public void removeKey(String key) {
		CompoundTag nbt = getNBT();
		nbt.remove(key);
		setNBT(nbt);
	}
	
	@Override
	public Optional<String> getNextKey(Optional<String> pastingKey) {
		return pastingKey.map(key -> {
			CompoundTag nbt = getNBT();
			if (nbt.contains(key)) {
				key += " - Copy";
				String baseKey = key;
				for (int i = 2; nbt.contains(key); i++)
					key = baseKey + " (" + i + ")";
			}
			return key;
		});
	}
	
	@Override
	public Predicate<String> getKeyValidator(boolean renaming) {
		return key -> true;
	}
	
	@Override
	public boolean handlesDuplicateKeys() {
		return false;
	}
	
}
