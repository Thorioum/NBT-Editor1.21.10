package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVButtonWidget;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;

import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

public class ConfigButton extends MVButtonWidget implements ConfigPath {
	
	@FunctionalInterface
	public interface PressAction {
		public void onPress(ConfigButton button);
	}
	
	private final PressAction onPress;
	private final MVTooltip tooltip;
	
	public ConfigButton(int width, Component message, PressAction onPress, MVTooltip tooltip) {
		super(0, 0, width, 20, message, btn -> onPress.onPress((ConfigButton) btn), tooltip);
		this.onPress = onPress;
		this.tooltip = tooltip;
	}
	public ConfigButton(int width, Component message, PressAction onPress) {
		this(width, message, onPress, MVTooltip.EMPTY);
	}
	
	@Override
	public boolean isValueValid() {
		return true;
	}
	@Override
	public ConfigButton addValueListener(ConfigValueListener<ConfigValue<?, ?>> listener) {
		return this; // "Value" never changes
	}
	
	
	@Override
	public int getSpacingWidth() {
		return this.width;
	}
	
	@Override
	public int getSpacingHeight() {
		return this.height;
	}
	
	@Override
	public ConfigButton clone(boolean defaults) {
		return new ConfigButton(this.width, this.getMessage(), onPress, tooltip);
	}
	
	
	@Override
	public boolean keyPressed(KeyEvent keyInput) {
		return false; // Stop space from triggering the button
	}
	
	@Override
	public void tick() {}
	
}
