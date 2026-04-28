package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;

import net.minecraft.network.chat.Component;

public interface ConfigPathNamed extends ConfigPath {
	public Component getName();
	public void setNamePrefix(Component prefix);
	public Component getNamePrefix();
	public default Component getFullName() {
		Component name = getName();
		Component prefix = getNamePrefix();
		if (name == null)
			return prefix == null ? null : prefix.copy();
		if (prefix == null)
			return name.copy();
		return TextInst.copy(prefix).append(TextInst.copy(name));
	}
}
