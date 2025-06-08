package com.luneruniverse.minecraft.mod.nbteditor.screens.nbtmenugenerators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTValue;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;

public class ListMenuGeneratorSwitch implements MenuGenerator {

	private ListMenuGenerator getRealGen(NbtElement element) {
		return new ListMenuGenerator();
	}
	@Override
	public List<NBTValue> getElements(NBTEditorScreen<?> screen, NbtElement source) {
		return getRealGen(source).getElements(screen, source);
	}
	
	@Override
	public NbtElement getElement(NbtElement source, String key) {
		return getRealGen(source).getElement(source, key);
	}
	
	@Override
	public void setElement(NbtElement source, String key, NbtElement value) {
		getRealGen(source).setElement(source, key, value);
	}
	
	@Override
	public void addElement(NBTEditorScreen<?> screen, NbtElement source, Consumer<String> requestOverwrite, String force) {
		getRealGen(source).addElement(screen, source, requestOverwrite, force);
	}
	
	@Override
	public void removeElement(NbtElement source, String key) {
		getRealGen(source).removeElement(source, key);
	}
	
	@Override
	public void pasteElement(NbtElement source, String key, NbtElement value) {
		getRealGen(source).pasteElement(source, key, value);
	}
	
	@Override
	public boolean renameElement(NbtElement source, String key, String newKey, boolean force) {
		return getRealGen(source).renameElement(source, key, newKey, force);
	}

	
}