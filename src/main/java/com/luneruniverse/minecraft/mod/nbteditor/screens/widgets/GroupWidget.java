package com.luneruniverse.minecraft.mod.nbteditor.screens.widgets;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.*;
import com.luneruniverse.minecraft.mod.nbteditor.screens.Tickable;

import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratableEntry.NarrationPriority;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix3x2fStack;

public class GroupWidget extends AbstractContainerEventHandler implements MVDrawable, MVElement, Tickable, NarratableEntry, OldEventBehavior {
	
	private final List<Renderable> drawables;
	private final List<GuiEventListener> elements;
	private final List<Tickable> tickables;
	
	public GroupWidget() {
		this.drawables = new ArrayList<>();
		this.elements = new ArrayList<>();
		this.tickables = new ArrayList<>();
	}
	
	public <T extends Renderable> T addDrawable(T drawable) {
		if (!this.drawables.contains(drawable))
			this.drawables.add(drawable);
		return drawable;
	}
	
	public <T extends GuiEventListener> T addElement(T element) {
		if (!this.elements.contains(element))
			this.elements.add(element);
		return element;
	}
	
	public <T extends Tickable> T addTickable(T tickable) {
		if (!this.tickables.contains(tickable))
			this.tickables.add(tickable);
		return tickable;
	}
	
	public <T extends Renderable & GuiEventListener> T addWidget(T widget) {
		addDrawable(widget);
		addElement(widget);
		if (widget instanceof Tickable tickable)
			addTickable(tickable);
		return widget;
	}
	
	public boolean removeDrawable(Renderable drawable) {
		return this.drawables.remove(drawable);
	}
	
	public boolean removeElement(GuiEventListener element) {
		return this.elements.remove(element);
	}
	
	public boolean removeTickable(Tickable tickable) {
		return this.tickables.remove(tickable);
	}
	
	public <T extends Renderable & GuiEventListener> boolean removeWidget(T widget) {
		return removeDrawable(widget) | removeElement(widget) |
				(widget instanceof Tickable tickable && removeTickable(tickable));
	}
	
	public boolean filterDrawables(Predicate<Renderable> filter) {
		return this.drawables.removeIf(filter.negate());
	}
	
	public boolean filterElements(Predicate<GuiEventListener> filter) {
		return this.elements.removeIf(filter.negate());
	}
	
	public boolean filterTickables(Predicate<Tickable> filter) {
		return this.tickables.removeIf(filter.negate());
	}
	
	public boolean filterWidgets(Predicate<Object> filter) {
		Set<Object> widgets = new HashSet<>();
		widgets.addAll(drawables);
		widgets.addAll(elements);
		widgets.addAll(tickables);
		widgets.removeIf(filter.negate());
		boolean output = drawables.retainAll(widgets);
		output |= elements.retainAll(widgets);
		return output | tickables.retainAll(widgets);
	}
	
	public boolean clearDrawables() {
		if (drawables.isEmpty())
			return false;
		drawables.clear();
		return true;
	}
	
	public boolean clearElements() {
		if (elements.isEmpty())
			return false;
		elements.clear();
		return true;
	}
	
	public boolean clearTickables() {
		if (tickables.isEmpty())
			return false;
		tickables.clear();
		return true;
	}
	
	public boolean clearWidgets() {
		return clearDrawables() | clearElements() | clearTickables();
	}
	
	@Override
	public void extractRenderState(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		for (Renderable drawable : drawables)
			drawable.extractRenderState(MVDrawableHelper.getDrawContext(matrices), mouseX, mouseY, delta);
	}
	
	@Override
	public void tick() {
		for (Tickable tickable : tickables)
			tickable.tick();
	}
	
	@Override
	public List<? extends GuiEventListener> children() {
		return new ArrayList<>(elements);
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		for (GuiEventListener element : elements) {
			if (element.isMouseOver(mouseX, mouseY))
				return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void setFocused(boolean focused) {
		MVElement.super.setFocused(focused);
	}
	@SuppressWarnings("deprecation")
	@Override
	public boolean isFocused() {
		return MVElement.super.isFocused();
	}
	public boolean method_25401(double mouseX, double mouseY, double amount) {
		return Version.<Boolean>newSwitch()
				.range("1.20.2", null, () -> MVElement.super.method_25401(mouseX, mouseY, amount))
				.range(null, "1.20.1", () -> super_mouseScrolled(mouseX, mouseY, amount))
				.get();
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double xAmount, double yAmount) {
		return super.mouseScrolled(mouseX, mouseY, xAmount, yAmount);
	}
	private boolean super_mouseScrolled(double mouseX, double mouseY, double amount) {
		try {
			return (boolean) MethodHandles.privateLookupIn(GroupWidget.class, MethodHandles.lookup())
					.findSpecial(AbstractContainerEventHandler.class, "method_25401",
					MethodType.methodType(boolean.class, double.class, double.class, double.class),
					GroupWidget.class).invoke(this, mouseX, mouseY, amount);
		} catch (Throwable e) {
			throw new RuntimeException("Error calling super.mouseScrolled", e);
		}
	}
	
	
	
	@Override
	public NarrationPriority narrationPriority() {
		return NarrationPriority.NONE;
	}
	
	@Override
	public void updateNarration(NarrationElementOutput var1) {
		
	}
	
}
