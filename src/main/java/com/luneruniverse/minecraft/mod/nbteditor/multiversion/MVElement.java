package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.util.WeakHashMap;

import net.minecraft.client.gui.Element;

public interface MVElement extends Element {

	final WeakHashMap<MVElement, Boolean> _focused = new WeakHashMap<>();
	final WeakHashMap<MVElement, Boolean> _multiFocused = new WeakHashMap<>();
	@Deprecated(since = "Added 1.19.4, not supported in earlier versions")
	@Override
	default void setFocused(boolean focused) {
		_focused.put(this, focused);
	}
	@Deprecated(since = "Added 1.19.4, not supported in earlier versions")
    @Override
	default boolean isFocused() {
		return _focused.getOrDefault(this, false);
	}
	default void setMultiFocused(boolean focused) {
		Boolean prevFocused = _multiFocused.put(this, focused);
		onMultiFocusedSet(focused, prevFocused == null ? false : prevFocused);
	}
	default boolean isMultiFocused() {
		return _multiFocused.getOrDefault(this, false);
	}
	default void onMultiFocusedSet(boolean focused, boolean prevFocused) {}
	
	default boolean method_25401(double mouseX, double mouseY, double amount) {
		return mouseScrolled(mouseX, mouseY, 0, amount);
	}
	default boolean mouseScrolled(double mouseX, double mouseY, double xAmount, double yAmount) {
		return false;
	}
	
}
