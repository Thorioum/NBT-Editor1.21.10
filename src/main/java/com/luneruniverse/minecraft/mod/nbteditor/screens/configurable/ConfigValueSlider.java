package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.AbstractSliderButton;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;

public class ConfigValueSlider<T extends Number> extends AbstractSliderButton implements ConfigValue<T, ConfigValueSlider<T>> {
	
	public static ConfigValueSlider<Integer> forInt(int width, int value, int defaultValue, int min, int max, int step, Function<Integer, Component> msg) {
		return new ConfigValueSlider<>(width, value, defaultValue, min, max, step, msg, Double::intValue, null);
	}
	public static ConfigValueSlider<Double> forDouble(int width, double value, double defaultValue, double min, double max, double step, Function<Double, Component> msg) {
		return new ConfigValueSlider<>(width, value, defaultValue, min, max, step, msg, Double::doubleValue, null);
	}
	
	
	private T actualValue;
	private final T defaultValue;
	private final T min;
	private final T max;
	private final T step;
	private final Function<T, Component> msg;
	private final Function<Double, T> caster;
	
	private final List<ConfigValueListener<ConfigValueSlider<T>>> onChanged;
	
	private ConfigValueSlider(int width, T value, T defaultValue, T min, T max, T step, Function<T, Component> msg, Function<Double, T> caster, List<ConfigValueListener<ConfigValueSlider<T>>> onChanged) {
		super(0, 0, width, 20, msg.apply(value), (value.doubleValue() - min.doubleValue()) / (max.doubleValue() - min.doubleValue()));
		this.actualValue = value;
		this.defaultValue = defaultValue;
		this.min = min;
		this.max = max;
		this.step = step;
		this.msg = msg;
		this.caster = caster;
		this.onChanged = new ArrayList<>();
		if (onChanged != null)
			this.onChanged.addAll(onChanged);
	}
	
	@Override
	public void extractRenderState(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		super.extractRenderState(MVDrawableHelper.getDrawContext(matrices), mouseX, mouseY, delta);
	}
	
	// There is no element focusing in configs, so onDrag is called for everything
	// This makes sure onDrag is only called when the mouse was clicked on the slider
	// Accounts for mouseClicked stopping on the first true
	private boolean clicked = false;
	private double mouseClickX = -1;
	private double mouseClickY = -1;
	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		boolean output = super.mouseClicked(click, doubled);
		if (output) {
			mouseClickX = click.x();
			mouseClickY = click.y();
		}
		return clicked = output;
	}
	@Override
	protected void onDrag(MouseButtonEvent click, double deltaX, double deltaY) {
		if (clicked && MainUtil.equals(click.x(), mouseClickX + deltaX) && MainUtil.equals(click.y(), mouseClickY + deltaY)) {
			mouseClickX += deltaX;
			mouseClickY += deltaY;
			super.onDrag(click, deltaX, deltaY);
		}
	}
	
	@Override
	protected void updateMessage() {
		setMessage(msg.apply(actualValue));
	}
	
	@Override
	protected void applyValue() {
		actualValue = caster.apply((int) (value * (max.doubleValue() - min.doubleValue()) / step.doubleValue()) * step.doubleValue() + min.doubleValue());
		onChanged.forEach(listener -> listener.onValueChanged(this));
	}
	
	
	
	@Override
	public T getDefaultValue() {
		return defaultValue;
	}
	
	@Override
	public void setValue(T value) {
		this.value = (value.doubleValue() - min.doubleValue()) / (max.doubleValue() - min.doubleValue());
		this.actualValue = value;
		onChanged.forEach(listener -> listener.onValueChanged(this));
		updateMessage();
	}
	@Override
	public T getConfigValue() {
		return actualValue;
	}
	@Override
	public boolean isValueValid() {
		return true;
	}
	@Override
	public ConfigValueSlider<T> addValueListener(ConfigValueListener<ConfigValueSlider<T>> listener) {
		onChanged.add(listener);
		return this;
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
	public ConfigValueSlider<T> clone(boolean defaults) {
		return new ConfigValueSlider<T>(this.width, defaults ? defaultValue : actualValue, defaultValue, min, max, step, msg, caster, onChanged);
	}
	
}
