package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

public interface ConfigValue<T, V extends ConfigValue<T, V>> extends Configurable<V> {
	public T getDefaultValue();
	public void setValue(T value);
	public T getConfigValue();
	public default T getValidValue() {
		return isValueValid() ? getConfigValue() : getDefaultValue();
	}
	public V addValueListener(ConfigValueListener<V> listener);
}
