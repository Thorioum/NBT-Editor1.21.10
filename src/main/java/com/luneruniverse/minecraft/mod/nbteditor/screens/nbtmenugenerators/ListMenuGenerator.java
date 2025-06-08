package com.luneruniverse.minecraft.mod.nbteditor.screens.nbtmenugenerators;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTValue;

import net.minecraft.nbt.*;

public class ListMenuGenerator implements MenuGenerator {
	
	public ListMenuGenerator() {

	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<NBTValue> getElements(NBTEditorScreen<?> screen, NbtElement source) {
		AbstractNbtList nbt = (AbstractNbtList) source;
		List<NBTValue> output = new ArrayList<>();
		for (int i = 0; i < nbt.size(); i++)
			output.add(new NBTValue(screen, i + "", nbt.method_10534(i), nbt));
		return output;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public NbtElement getElement(NbtElement source, String key) {
		try {
			return ((AbstractNbtList) source).method_10534(Integer.parseInt(key));
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setElement(NbtElement source, String key, NbtElement value) {
		try {
			AbstractNbtList list = (AbstractNbtList) source;
			int index = Integer.parseInt(key);
			if (list.size() == 1 && index == 0 && list instanceof NbtList) {
				NbtList nonGenericList = (NbtList) list;
				nonGenericList.remove(0);
				nonGenericList.add(value);
			} else {
				list.setElement(index, value);
			}
		} catch (NumberFormatException e) {
			NBTEditor.LOGGER.error("Error while modifying a list", e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void addElement(NBTEditorScreen<?> screen, NbtElement source, Consumer<String> requestOverwrite, String force) {
		((AbstractNbtList) source).addElement(((AbstractNbtList) source).size(),NbtInt.of(0));
		requestOverwrite.accept(null);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void removeElement(NbtElement source, String key) {
		try {
			((AbstractNbtList) source).method_10536(Integer.parseInt(key));
		} catch (NumberFormatException e) {
			NBTEditor.LOGGER.error("Error while modifying a list", e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void pasteElement(NbtElement source, String key, NbtElement value) {
		AbstractNbtList list = (AbstractNbtList) source;
		list.addElement(list.size(),value);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean renameElement(NbtElement source, String key, String newKey, boolean force) {
		AbstractNbtList list = (AbstractNbtList) source;
		try {
			NbtElement value = getElement(source, key);
			int keyInt = Integer.parseInt(key);
			int newKeyInt = Integer.parseInt(newKey);
			if (newKeyInt < 0)
				throw new NumberFormatException(newKeyInt + " is less than 0!");
			
			if (newKeyInt >= list.size()) {
				list.method_10536(keyInt);
				list.addElement(list.size(),value);
			} else {
				list.method_10536(keyInt);
				list.addElement(newKeyInt, value);
			}
			
			return true;
		} catch (NumberFormatException e) {
			NBTEditor.LOGGER.error("Error while modifying a list", e);
			return true;
		}
	}
	
}