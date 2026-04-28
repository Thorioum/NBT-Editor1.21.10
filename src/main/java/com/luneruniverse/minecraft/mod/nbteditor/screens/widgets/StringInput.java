package com.luneruniverse.minecraft.mod.nbteditor.screens.widgets;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.suggestion.Suggestions;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class StringInput extends GroupWidget implements InputOverlay.Input<String> {
	
	public static class Builder {
		
		private String defaultValue;
		private Component placeholder;
		private Predicate<String> valueValidator;
		private BiFunction<String, Integer, CompletableFuture<Suggestions>> suggestions;
		
		public Builder() {
			defaultValue = "";
			placeholder = null;
			valueValidator = str -> true;
			suggestions = null;
		}
		
		public Builder withDefault(String defaultValue) {
			this.defaultValue = defaultValue;
			return this;
		}
		public Builder withPlaceholder(Component placeholder) {
			this.placeholder = placeholder;
			return this;
		}
		public Builder withValidator(Predicate<String> valueValidator) {
			this.valueValidator = valueValidator;
			return this;
		}
		public Builder withSuggestions(BiFunction<String, Integer, CompletableFuture<Suggestions>> suggestions) {
			this.suggestions = suggestions;
			return this;
		}
		
		public StringInput build() {
			return new StringInput(defaultValue, placeholder, valueValidator, suggestions);
		}
		
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	private final String defaultValue;
	private final Component placeholder;
	private final Predicate<String> valueValidator;
	private final BiFunction<String, Integer, CompletableFuture<Suggestions>> suggestions;
	private SuggestingTextFieldWidget value;
	private boolean valid;
	
	public StringInput(String defaultValue, Component placeholder, Predicate<String> valueValidator, BiFunction<String, Integer, CompletableFuture<Suggestions>> suggestions) {
		this.defaultValue = defaultValue;
		this.placeholder = placeholder;
		this.valueValidator = valueValidator;
		this.suggestions = suggestions;
	}
	
	@Override
	public void init(int x, int y) {
		clearWidgets();
		
		String prevValue = (value == null ? defaultValue : value.getValue());
		value = new SuggestingTextFieldWidget(MainUtil.client.screen, x, y, getWidth(), getHeight());
		value.setMaxLength(Integer.MAX_VALUE);
		value.setValue(prevValue);
		if (placeholder != null)
			value.name(placeholder);
		if (suggestions != null)
			value.suggest(suggestions);
		addWidget(value);
		setFocused(value);
		
		value.setResponder(str -> valid = valueValidator.test(str));
		valid = valueValidator.test(value.getValue());
	}
	
	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		boolean output = super.mouseClicked(click, doubled);
		if (!output)
			setFocused(null);
		return output;
	}
	
	@Override
	public String getValue() {
		return value.getValue();
	}
	
	@Override
	public boolean isValid() {
		return valid;
	}
	
	@Override
	public int getWidth() {
		return 204;
	}
	
	@Override
	public int getHeight() {
		return 16;
	}
	
}
