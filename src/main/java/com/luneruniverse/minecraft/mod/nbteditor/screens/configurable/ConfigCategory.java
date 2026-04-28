package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import net.minecraft.network.chat.Component;

public class ConfigCategory extends ConfigGroupingVertical<String, ConfigCategory> {
	
	public ConfigCategory(Component name) {
		super(name, ConfigCategory::new);
	}
	public ConfigCategory() {
		this(null);
	}

}
