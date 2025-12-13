package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import java.util.function.BiFunction;

import net.minecraft.client.gui.Click;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix3x2fStack;

public class ConfigHiddenData<S extends ConfigPath, D> implements ConfigPath {
	
	protected final S visible;
	protected D data;
	protected final BiFunction<D, Boolean, D> onClone;
	
	public ConfigHiddenData(S visible, D data, BiFunction<D, Boolean, D> onClone) {
		this.visible = visible;
		this.data = data;
		this.onClone = onClone;
		visible.setParent(this);
	}
	
	public S getVisible() {
		return visible;
	}
	public void setData(D data) {
		this.data = data;
	}
	public D getData() {
		return data;
	}
	
	@Override
	public void render(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		visible.render(matrices, mouseX, mouseY, delta);
	}
	
	@Override
	public boolean isValueValid() {
		return visible.isValueValid();
	}
	
	@Override
	public ConfigPath addValueListener(ConfigValueListener<ConfigValue<?, ?>> listener) {
		visible.addValueListener(listener);
		return this;
	}
	
	@Override
	public int getSpacingWidth() {
		return visible.getSpacingWidth();
	}
	
	@Override
	public int getRenderWidth() {
		return visible.getRenderWidth();
	}
	
	@Override
	public int getSpacingHeight() {
		return visible.getSpacingHeight();
	}
	
	@Override
	public int getRenderHeight() {
		return visible.getRenderHeight();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ConfigHiddenData<S, D> clone(boolean defaults) {
		return new ConfigHiddenData<>((S) visible.clone(defaults), onClone.apply(data, defaults), onClone);
	}
	
	
	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		return visible.mouseClicked(click, doubled);
	}
	@Override
	public boolean mouseReleased(Click click) {
		return visible.mouseReleased(click);
	}
	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		visible.mouseMoved(mouseX, mouseY);
	}
	@Override
	public boolean mouseDragged(Click click, double deltaX, double deltaY) {
		return visible.mouseDragged(click, deltaX, deltaY);
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double xAmount, double yAmount) {
		return visible.mouseScrolled(mouseX, mouseY, xAmount, yAmount);
	}
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return visible.isMouseOver(mouseX, mouseY);
	}
	
	@Override
	public boolean keyPressed(KeyInput keyInput) {
		return visible.keyPressed(keyInput);
	}
	@Override
	public boolean keyReleased(KeyInput keyInput) {
		return visible.keyReleased(keyInput);
	}
	@Override
	public boolean charTyped(CharInput charInput) {
		return visible.charTyped(charInput);
	}
	
	@Override
	public void tick() {
		visible.tick();
	}
	
}
