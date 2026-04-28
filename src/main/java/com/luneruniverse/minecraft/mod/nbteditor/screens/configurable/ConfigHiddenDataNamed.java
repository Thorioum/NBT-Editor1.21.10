package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import java.util.function.BiFunction;

import net.minecraft.network.chat.Component;

public class ConfigHiddenDataNamed<S extends ConfigPathNamed, D> extends ConfigHiddenData<S, D> implements ConfigPathNamed {
	
	public ConfigHiddenDataNamed(S visible, D data, BiFunction<D, Boolean, D> onClone) {
		super(visible, data, onClone);
	}
	
	@Override
	public Component getName() {
		return visible.getName();
	}
	
	@Override
	public void setNamePrefix(Component prefix) {
		visible.setNamePrefix(prefix);
	}
	
	@Override
	public Component getNamePrefix() {
		return visible.getNamePrefix();
	}
	
	@Override
	public Component getFullName() {
		return visible.getFullName();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ConfigHiddenDataNamed<S, D> clone(boolean defaults) {
		return new ConfigHiddenDataNamed<>((S) visible.clone(defaults), onClone.apply(data, defaults), onClone);
	}
	
}
