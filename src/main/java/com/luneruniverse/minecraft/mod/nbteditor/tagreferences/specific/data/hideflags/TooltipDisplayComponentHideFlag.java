package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.hideflags;

import java.util.LinkedHashMap;
import java.util.Map;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;

public class TooltipDisplayComponentHideFlag extends HideFlag {
	
	public static final Map<DataComponentType<?>, HideFlag> FLAGS = new LinkedHashMap<>();
	static {
		MVRegistry.getComponentsRegistry().getEntrySet().stream()
				.map(component -> Map.entry(component.getKey().toString(), component.getValue()))
				.sorted((a, b) -> a.getKey().compareToIgnoreCase(b.getKey()))
				.forEachOrdered(component -> {
					FLAGS.put(component.getValue(), new TooltipDisplayComponentHideFlag(
							TextInst.of(component.getKey()), component.getValue()));
				});
	}
	
	private final Component name;
	private final DataComponentType<?> component;
	
	private TooltipDisplayComponentHideFlag(Component name, DataComponentType<?> component) {
		this.name = name;
		this.component = component;
	}
	
	@Override
	public Component getName() {
		return name;
	}
	
	public DataComponentType<?> getComponent() {
		return component;
	}
	
}
