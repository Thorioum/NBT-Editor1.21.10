package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVButtonWidget;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMatrix4f;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix3x2fStack;

public class ConfigValueDropdown<T> extends MVButtonWidget implements ConfigValue<T, ConfigValueDropdown<T>> {
	
	public static <T> ConfigValueDropdown<T> forList(T value, T defaultValue, List<T> allValues) {
		return new ConfigValueDropdown<>(value, defaultValue, allValues, new ArrayList<>());
	}
	public static <T> ConfigValueDropdown<T> forList(T value, T defaultValue, List<T> allValues, List<T> importantValues) {
		return new ConfigValueDropdown<>(value, defaultValue, allValues, importantValues);
	}
	public static <T> ConfigValueDropdown<T> forList(T value, T defaultValue, List<T> allValues, Predicate<T> importantValues) {
		return new ConfigValueDropdown<>(value, defaultValue, allValues, allValues.stream().filter(importantValues).toList());
	}
	
	public static <T extends Enum<T>> ConfigValueDropdown<T> forEnum(T value, T defaultValue, Class<T> enumClass) {
		return new ConfigValueDropdown<>(value, defaultValue, Arrays.asList(enumClass.getEnumConstants()), new ArrayList<>());
	}
	public static <T extends Enum<T>> ConfigValueDropdown<T> forEnum(T value, T defaultValue, Class<T> enumClass, List<T> importantValues) {
		return new ConfigValueDropdown<>(value, defaultValue, Arrays.asList(enumClass.getEnumConstants()), importantValues);
	}
	public static <T extends Enum<T>> ConfigValueDropdown<T> forEnum(T value, T defaultValue, Class<T> enumClass, Predicate<T> importantValues) {
		return new ConfigValueDropdown<>(value, defaultValue, Arrays.asList(enumClass.getEnumConstants()), Arrays.stream(enumClass.getEnumConstants()).filter(importantValues).toList());
	}
	
	public static <T extends Enum<T>> ConfigValueDropdown<T> forFilteredEnum(T value, T defaultValue, Class<T> enumClass, Predicate<T> filter) {
		return new ConfigValueDropdown<>(value, defaultValue, Arrays.stream(enumClass.getEnumConstants()).filter(filter).toList(), new ArrayList<>());
	}
	public static <T extends Enum<T>> ConfigValueDropdown<T> forFilteredEnum(T value, T defaultValue, Class<T> enumClass, Predicate<T> filter, List<T> importantValues) {
		return new ConfigValueDropdown<>(value, defaultValue, Arrays.stream(enumClass.getEnumConstants()).filter(filter).toList(), importantValues.stream().filter(filter).toList());
	}
	public static <T extends Enum<T>> ConfigValueDropdown<T> forFilteredEnum(T value, T defaultValue, Class<T> enumClass, Predicate<T> filter, Predicate<T> importantValues) {
		return new ConfigValueDropdown<>(value, defaultValue, Arrays.stream(enumClass.getEnumConstants()).filter(filter).toList(), Arrays.stream(enumClass.getEnumConstants()).filter(filter.and(importantValues)).toList());
	}
	
	protected T value;
	protected boolean open;
	protected final T defaultValue;
	protected final List<T> allValues;
	protected final List<T> importantValues;
	
	protected final List<ConfigValueListener<ConfigValueDropdown<T>>> onChanged;
	
	@SuppressWarnings("unchecked")
	private ConfigValueDropdown(T value, T defaultValue, List<T> allValues, List<T> importantValues) {
		super(0, 0, getMaxWidth(allValues) + MainUtil.client.font.lineHeight * 2, 20, TextInst.of(value.toString()),
				btn -> ((ConfigValueDropdown<T>) btn).open = !((ConfigValueDropdown<T>) btn).open);
		
		this.value = value;
		this.defaultValue = defaultValue;
		this.allValues = allValues;
		this.importantValues = importantValues;
		this.open = false;
		this.onChanged = new ArrayList<>();
	}
	private static int getMaxWidth(List<?> allValues) {
		return allValues.stream().map(Object::toString).mapToInt(MainUtil.client.font::width).max().orElse(0);
	}
	private ConfigValueDropdown(T value, T defaultValue, List<T> allValues, List<T> importantValues, boolean open, List<ConfigValueListener<ConfigValueDropdown<T>>> onChanged) {
		this(value, defaultValue, allValues, importantValues);
		this.open = open;
		this.onChanged.addAll(onChanged);
	}
	
	@Override
	public void renderButton(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		matrices.pushMatrix();
		matrices.translate(0.0f, 0.0f);
		
		super.renderButton(matrices, mouseX, mouseY, delta);
		if (open) {
			MVDrawableHelper.fill(matrices, this.x, this.height, this.x + this.width, allValues.size() * this.height, 0xFF000000);
			boolean xHover = this.active && mouseX >= this.x && mouseX < this.x + this.width;
			int i = 0;
			for (T option : allValues) {
				if (option.equals(value))
					continue;
				int y = this.y + (++i * this.height);
				int color = -1;
				if (xHover && mouseY >= y && mouseY < y + this.height)
					color = 0xFF257789;
				else if (importantValues.contains(option))
					color = 0xFFFFAA00;
				MVDrawableHelper.drawCenteredTextWithShadow(matrices, MainUtil.client.font, TextInst.of(option.toString()),
						this.x + this.width / 2, y + (this.height - MainUtil.client.font.lineHeight) / 2, color);
				if (color != -1 && option instanceof ConfigTooltipSupplier) // Hovering
					((ConfigTooltipSupplier) option).getTooltip().render(matrices, mouseX, mouseY);
			}
		}
		if (isHoveredOrFocused() && value instanceof ConfigTooltipSupplier)
			((ConfigTooltipSupplier) value).getTooltip().render(matrices, mouseX, mouseY);
		
		matrices.popMatrix();
	}
	
	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		boolean output = super.mouseClicked(click, doubled);
		if (!output && this.active && this.visible && open && click.x() >= this.x && click.x() < this.x + this.width) {
			int i = 0;
			for (T option : allValues) {
				if (option.equals(value))
					continue;
				int y = this.y + (++i * this.height);
				if (click.y() >= y && click.y() < y + this.height) {
					this.playDownSound(Minecraft.getInstance().getSoundManager());
					setValue(option);
					open = false;
					return true;
				}
			}
		}
		if (!output && open)
			open = false;
		return output;
	}
	
	public void setOpen(boolean open) {
		this.open = open;
	}
	public boolean isOpen() {
		return open;
	}
	
	@Override
	public T getDefaultValue() {
		return defaultValue;
	}
	
	@Override
	public void setValue(T value) {
		this.value = value;
		setMessage(TextInst.of(value.toString()));
		onChanged.forEach(listener -> listener.onValueChanged(this));
	}
	@Override
	public T getConfigValue() {
		return value;
	}
	@Override
	public boolean isValueValid() {
		return true;
	}
	@Override
	public ConfigValueDropdown<T> addValueListener(ConfigValueListener<ConfigValueDropdown<T>> listener) {
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
	public int getRenderHeight() {
		if (!open)
			return getSpacingHeight();
		
		return allValues.size() * this.height;
	}
	
	@Override
	public ConfigValueDropdown<T> clone(boolean defaults) {
		return new ConfigValueDropdown<>(defaults ? defaultValue : value, defaultValue, allValues, importantValues, open, onChanged);
	}
	
	
	@Override
	public boolean keyPressed(KeyEvent keyInput) {
		return false; // Stop space from triggering the button
	}
	
}
