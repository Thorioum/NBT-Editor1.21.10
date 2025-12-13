package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import java.util.ArrayList;
import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.screens.Tickable;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.Click;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

public class ConfigItem<V extends ConfigValue<?, V>> implements ConfigPath {
	
	private final Text name;
	private final V value;
	private final int valueOffsetX;
	private final int valueOffsetY;
	
	private final List<ConfigValueListener<ConfigValue<?, ?>>> onChanged;
	
	private MVTooltip tooltip;
	
	public ConfigItem(Text name, V value) {
		this.name = name;
		this.value = value;
		this.valueOffsetX = MainUtil.client.textRenderer.getWidth(name) + PADDING;
		this.valueOffsetY = (getSpacingHeight() - value.getSpacingHeight()) / 2;
		
		this.onChanged = new ArrayList<>();
		value.addValueListener(source -> onChanged.forEach(listener -> listener.onValueChanged(source)));
		value.setParent(this);
	}
	private ConfigItem(Text name, V value, List<ConfigValueListener<ConfigValue<?, ?>>> onChanged) {
		this(name, value);
		this.onChanged.addAll(onChanged);
	}
	
	public V getValue() {
		return value;
	}
	
	public ConfigItem<V> setTooltip(MVTooltip tooltip) {
		this.tooltip = tooltip;
		return this;
	}
	public ConfigItem<V> setTooltip(String... keys) {
		setTooltip(new MVTooltip(keys));
		return this;
	}
	
	@Override
	public void render(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		MVDrawableHelper.drawTextWithShadow(matrices, MainUtil.client.textRenderer, name, 0, (getSpacingHeight() - MainUtil.client.textRenderer.fontHeight) / 2, 0xFFFFFFFF);
		
		matrices.pushMatrix();
		matrices.translate(valueOffsetX, valueOffsetY);
		value.render(matrices, mouseX - valueOffsetX, mouseY - valueOffsetY, delta);
		matrices.popMatrix();
		
		if (tooltip != null && mouseX >= 0 && mouseX <= valueOffsetX && isMouseOver(mouseX, mouseY))
			tooltip.render(matrices, mouseX, mouseY);
	}
	
	@Override
	public boolean isValueValid() {
		return value.isValueValid();
	}
	@Override
	public ConfigItem<V> addValueListener(ConfigValueListener<ConfigValue<?, ?>> listener) {
		onChanged.add(listener);
		return this;
	}
	
	@Override
	public int getSpacingWidth() {
		return valueOffsetX + value.getSpacingWidth();
	}
	
	@Override
	public int getSpacingHeight() {
		return Math.max(20, value.getSpacingHeight());
	}
	
	@Override
	public int getRenderWidth() {
		return valueOffsetX + value.getRenderWidth();
	}
	
	@Override
	public int getRenderHeight() {
		return Math.max(getSpacingHeight(), value.getRenderHeight());
	}
	
	@Override
	public ConfigItem<V> clone(boolean defaults) {
		ConfigItem<V> output = new ConfigItem<>(name, value.clone(defaults), onChanged);
		output.tooltip = tooltip;
		return output;
	}
	
	
	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		return value.mouseClicked(new Click(click.x() - valueOffsetX, click.y() - valueOffsetY, click.buttonInfo()),doubled);
	}
	@Override
	public boolean mouseReleased(Click click) {
		return value.mouseReleased(new Click(click.x() - valueOffsetX, click.y() - valueOffsetY, click.buttonInfo()));
	}
	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		value.mouseMoved(mouseX - valueOffsetX, mouseY - valueOffsetY);
	}
	@Override
	public boolean mouseDragged(Click click, double deltaX, double deltaY) {
		return value.mouseDragged(new Click(click.x() - valueOffsetX, click.y() - valueOffsetY, click.buttonInfo()), deltaX, deltaY);
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double xAmount, double yAmount) {
		return value.mouseScrolled(mouseX - valueOffsetX, mouseY - valueOffsetY, xAmount, yAmount);
	}
	
	@Override
	public boolean keyPressed(KeyInput keyInput) {
		return value.keyPressed(keyInput);
	}
	@Override
	public boolean keyReleased(KeyInput keyInput) {
		return value.keyReleased(keyInput);
	}
	@Override
	public boolean charTyped(CharInput charInput) {
		return value.charTyped(charInput);
	}
	
	@Override
	public void tick() {
		if (value instanceof Tickable tickable)
			tickable.tick();
	}
	
}
