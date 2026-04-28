package com.luneruniverse.minecraft.mod.nbteditor.screens.widgets;

import java.awt.Point;
import java.lang.invoke.MethodType;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;

public class SuggestingTextFieldWidget extends NamedTextFieldWidget {
	
	private final CommandSuggestions suggestor;
	private BiFunction<String, Integer, CompletableFuture<Suggestions>> suggestionsFunc;
	
	public SuggestingTextFieldWidget(Screen screen, int x, int y, int width, int height, EditBox copyFrom) {
		super(x, y, width, height, copyFrom);
		suggestor = new CommandSuggestions(MainUtil.client, screen, this, MainUtil.client.font, false, true, 0, 7, false, 0x80000000) {
			@Override
			public void updateCommandInfo() {
				if (!this.keepSuggestions) {
					SuggestingTextFieldWidget.this.setSuggestion(null);
					this.suggestions = null;
				}
				this.commandUsage.clear();
				if (this.suggestions == null || !this.keepSuggestions) {
					if (suggestionsFunc == null)
						this.pendingSuggestions = new SuggestionsBuilder("", 0).buildFuture();
					else
						this.pendingSuggestions = suggestionsFunc.apply(SuggestingTextFieldWidget.this.value, SuggestingTextFieldWidget.this.getCursorPosition());
					this.pendingSuggestions.thenAccept((s) -> {
						if (!this.pendingSuggestions.isDone())
							return;
						updateUsageInfo(currentParse,s);
					});
				}
			}
			@Override
			protected FormattedCharSequence formatChat(String original, int firstCharacterIndex) {
				return FormattedCharSequence.forward(original, Style.EMPTY);
			}
		};
		suggestor.currentParse = new ParseResults<>(null);
		
		setResponder(null);
	}
	public SuggestingTextFieldWidget(Screen screen, int x, int y, int width, int height) {
		this(screen, x, y, width, height, null);
	}
	
	@Override
	public void setResponder(Consumer<String> listener) {
		super.setResponder(str -> {
			suggestor.updateCommandInfo();
			if (listener != null)
				listener.accept(str);
		});
	}
	
	@Override
	public SuggestingTextFieldWidget name(Component name) {
		super.name(name);
		return this;
	}
	
	public SuggestingTextFieldWidget suggest(BiFunction<String, Integer, CompletableFuture<Suggestions>> suggestions) {
		this.suggestionsFunc = suggestions;
		suggestor.updateCommandInfo();
		return this;
	}
	
	private static final Supplier<Reflection.MethodInvoker> ChatInputSuggestor_render =
			Reflection.getOptionalMethod(CommandSuggestions.class, "method_23923", MethodType.methodType(void.class, PoseStack.class, int.class, int.class));
	@Override
	public void extractRenderState(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		if (!isDropdownOnly())
			super.extractRenderState(matrices, mouseX, mouseY, delta);
		matrices.pushMatrix();
		matrices.translate(0, 0);
		Version.newSwitch()
				.range("1.20.0", null, () -> suggestor.extractRenderState(MVDrawableHelper.getDrawContext(matrices), mouseX, mouseY))
				.range(null, "1.19.4", () -> ChatInputSuggestor_render.get().invoke(suggestor, matrices, mouseX, mouseY))
				.run();
		matrices.popMatrix();
	}
	@Override
	protected boolean shouldShowName() {
		return suggestor.suggestions == null;
	}
	public boolean isDropdownOnly() {
		return false;
	}
	public Point getSpecialDropdownPos() {
		return null;
	}
	
	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean b) {
		return suggestor.mouseClicked(click) || !isDropdownOnly() && super.mouseClicked(click,b);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return suggestor.mouseScrolled(verticalAmount) || !isDropdownOnly() && super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}
	
	@Override
	public boolean keyPressed(KeyEvent keyInput) {
		if (!isMultiFocused())
			return false;
		return suggestor.keyPressed(keyInput) || !isDropdownOnly() && super.keyPressed(keyInput);
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		if (suggestor.suggestions != null) {
			if (suggestor.suggestions.rect.contains((int) mouseX, (int) mouseY))
				return true;
		}
		return !isDropdownOnly() && super.isMouseOver(mouseX, mouseY);
	}
	
	@Override
	public void onMultiFocusedSet(boolean focused, boolean prevFocused) {
		suggestor.setAllowSuggestions(focused);
		suggestor.updateCommandInfo();
	}
	
}
