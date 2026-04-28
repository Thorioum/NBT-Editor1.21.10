package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.ChatFormatting;

/**
 * A wrapper for MutableText, since it is changed from an interface (1.18) to a class (1.19)
 */
public class EditableText implements Component {
	
	private MutableComponent value;
	
	public EditableText(MutableComponent value) {
		this.value = value;
	}
	
	public MutableComponent getInternalValue() {
		return value;
	}
	
	private static final Cache<String, Reflection.MethodInvoker> methodCache = CacheBuilder.newBuilder().build();
	@SuppressWarnings("unchecked")
	private <R> R call(boolean mutable, String method, Supplier<MethodType> type, Object... args) {
		try {
			Object output = methodCache.get(method, () -> Reflection.getMethod(mutable ? MutableComponent.class : Component.class, method, type.get())).invoke(value, args);
			if (output instanceof MutableComponent && mutable) {
				if (output == value)
					output = this;
				else
					output = new EditableText(value);
			}
			return (R) output;
		} catch (ExecutionException | UncheckedExecutionException e) {
			throw new RuntimeException("Error invoking method", e);
		}
	}
	
	// Text
	@Override
	public FormattedCharSequence getVisualOrderText() {
		return call(false, "getVisualOrderText", () -> MethodType.methodType(FormattedCharSequence.class));
	}
	
	@Override
	public ComponentContents getContents() {
		return call(false, "getContents", () -> MethodType.methodType(ComponentContents.class));
	}
	
	@Override
	public List<Component> getSiblings() {
		return call(false, "getSiblings", () -> MethodType.methodType(List.class));
	}
	
	@Override
	public Style getStyle() {
		return call(false, "getStyle", () -> MethodType.methodType(Style.class));
	}
	
	// 1.18 Text
	public String method_10851() { // asString
		return call(false, "method_10851", () -> MethodType.methodType(String.class));
	}
	
	public MutableComponent method_27662() { // copy
		return call(false, "method_27662", () -> MethodType.methodType(MutableComponent.class));
	}
	
	public MutableComponent method_27661() { // shallowCopy
		return call(false, "method_27661", () -> MethodType.methodType(MutableComponent.class));
	}
	
	public <T> Optional<T> method_27660(FormattedText.StyledContentConsumer<T> visitor, Style style) { // visitSelf
		return call(false, "method_27660", () -> MethodType.methodType(Optional.class, FormattedText.StyledContentConsumer.class, Style.class), visitor, style);
	}
	
	public <T> Optional<T> method_27659(FormattedText.ContentConsumer<T> visitor) { // visitSelf
		return call(false, "method_27659", () -> MethodType.methodType(Optional.class, FormattedText.ContentConsumer.class), visitor);
	}
	
	// Mutable Text
	public EditableText setStyle(Style style) {
		value.setStyle(style);
		return this;
	}
	
	public EditableText append(String text) {
		value.append(text);
		return this;
	}
	
	public EditableText append(Component text) {
		value.append(text);
		return this;
	}
	
	public EditableText styled(UnaryOperator<Style> styleUpdater) {
		value.withStyle(styleUpdater);
		return this;
	}
	
	public EditableText fillStyle(Style styleOverride) {
		value.withStyle(styleOverride);
		return this;
	}
	
	public EditableText formatted(ChatFormatting... formattings) {
		value.withStyle(formattings);
		return this;
	}
	
	// Other
	@Override
	public boolean equals(Object obj) {
		try {
			return (boolean) Object.class.getMethod("equals", Object.class).invoke(value, obj);
		} catch (Exception e) {
			throw new RuntimeException("Error invoking equals method", e);
		}
	}
	
}
