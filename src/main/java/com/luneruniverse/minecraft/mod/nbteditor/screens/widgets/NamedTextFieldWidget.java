package com.luneruniverse.minecraft.mod.nbteditor.screens.widgets;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTextFieldWidget;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;

public class NamedTextFieldWidget extends MVTextFieldWidget {
	
	protected Component name;
	protected boolean valid;
	
	public NamedTextFieldWidget(int x, int y, int width, int height, EditBox copyFrom) {
		super(x, y, width, height, copyFrom);
		valid = true;
	}
	public NamedTextFieldWidget(int x, int y, int width, int height) {
		this(x, y, width, height, null);
	}
	
	@Override
	public NamedTextFieldWidget tooltip(MVTooltip tooltip) {
		super.tooltip(tooltip);
		return this;
	}
	public NamedTextFieldWidget name(Component name) {
		this.name = name;
		return this;
	}
	
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	public boolean isValid() {
		return valid;
	}
	
	
	@Override
	public void extractRenderState(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		if (name != null && shouldShowName())
			setSuggestion(value.isEmpty() ? name.getString() : null);
		super.extractRenderState(matrices, mouseX, mouseY, delta);
	}
	
	protected boolean shouldShowName() {
		return true;
	}
	
}
