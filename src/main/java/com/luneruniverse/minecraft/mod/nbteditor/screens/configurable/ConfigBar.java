package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import net.minecraft.network.chat.Component;

public class ConfigBar extends ConfigGroupingHorizontal<String, ConfigBar> {
	
	public ConfigBar(Component name) {
		super(name, ConfigBar::new);
	}
	public ConfigBar() {
		this(null);
	}
	
}
