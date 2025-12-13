package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.luneruniverse.minecraft.mod.nbteditor.util.OrderedMap;

import net.minecraft.client.gui.Click;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

public abstract class ConfigGrouping<K, T extends ConfigGrouping<K, T>> implements ConfigPathNamed {
	
	protected interface Constructor<K, T extends ConfigGrouping<K, T>> {
		T newInstance(Text name);
	}
	
	protected final Text name;
	protected final OrderedMap<K, ConfigPath> paths;
	private final Constructor<K, T> cloneImpl;
	
	protected Text namePrefix;
	protected final List<ConfigValueListener<ConfigValue<?, ?>>> onChanged;
	
	protected ConfigGrouping(Text name, Constructor<K, T> cloneImpl) {
		this.name = name;
		this.paths = new OrderedMap<>();
		this.cloneImpl = cloneImpl;
		this.onChanged = new ArrayList<>();
	}
	
	public Text getName() {
		return name;
	}
	@Override
	public void setNamePrefix(Text prefix) {
		namePrefix = prefix;
	}
	@Override
	public Text getNamePrefix() {
		return namePrefix;
	}
	
	@SuppressWarnings("unchecked")
	public T setConfigurable(K key, ConfigPath path) {
		paths.put(key, path);
		path.addValueListener(source -> onChanged.forEach(listener -> listener.onValueChanged(source)));
		path.setParent(this);
		return (T) this;
	}
	public ConfigPath getConfigurable(K key) {
		return paths.get(key);
	}
	public Map<K, ConfigPath> getConfigurables() {
		return Collections.unmodifiableMap(paths);
	}
	@SuppressWarnings("unchecked")
	public T sort(Comparator<K> sorter) {
		paths.sort(sorter);
		return (T) this;
	}
	@SuppressWarnings("unchecked")
	public T setSorter(Comparator<K> sorter) {
		paths.setSorter(sorter);
		return (T) this;
	}
	
	@Override
	public boolean isValueValid() {
		return paths.values().stream().allMatch(ConfigPath::isValueValid);
	}
	@SuppressWarnings("unchecked")
	@Override
	public T addValueListener(ConfigValueListener<ConfigValue<?, ?>> listener) {
		onChanged.add(listener);
		return (T) this;
	}
	
	@Override
	public T clone(boolean defaults) {
		T output = cloneImpl.newInstance(name);
		paths.forEach((key, path) -> output.setConfigurable(key, path.clone(defaults)));
		output.onChanged.addAll(onChanged);
		return output;
	}
	
	
	// Make sure subclasses offset the mouse properly
	@Override
	public abstract boolean mouseClicked(Click click, boolean b);
	@Override
	public abstract boolean mouseReleased(Click click);
	@Override
	public abstract void mouseMoved(double mouseX, double mouseY);
	@Override
	public abstract boolean mouseDragged(Click click, double deltaX, double deltaY);
	@Override
	public abstract boolean mouseScrolled(double mouseX, double mouseY, double xAmount, double yAmount);
	
	@Override
	public boolean keyPressed(KeyInput keyInput) {
		for (ConfigPath path : new ArrayList<>(paths.values())) {
			if (path.keyPressed(keyInput))
				return true;
		}
		return false;
	}
	@Override
	public boolean keyReleased(KeyInput keyInput) {
		for (ConfigPath path : new ArrayList<>(paths.values())) {
			if (path.keyReleased(keyInput))
				return true;
		}
		return false;
	}
	@Override
	public boolean charTyped(CharInput charInput) {
		for (ConfigPath path : new ArrayList<>(paths.values())) {
			if (path.charTyped(charInput))
				return true;
		}
		return false;
	}
	
	@Override
	public void tick() {
		for (ConfigPath path : new ArrayList<>(paths.values()))
			path.tick();
	}
}
