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

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

public class SuggestingTextFieldWidget extends NamedTextFieldWidget {
	
	private final ChatInputSuggestor suggestor;
	private BiFunction<String, Integer, CompletableFuture<Suggestions>> suggestions;
	
	public SuggestingTextFieldWidget(Screen screen, int x, int y, int width, int height, TextFieldWidget copyFrom) {
		super(x, y, width, height, copyFrom);
		suggestor = new ChatInputSuggestor(MainUtil.client, screen, this, MainUtil.client.textRenderer, false, true, 0, 7, false, 0x80000000) {
			@Override
			public void refresh() {
				if (!this.completingSuggestions) {
					SuggestingTextFieldWidget.this.setSuggestion(null);
					this.window = null;
				}
				this.messages.clear();
				if (this.window == null || !this.completingSuggestions) {
					if (suggestions == null)
						this.pendingSuggestions = new SuggestionsBuilder("", 0).buildFuture();
					else
						this.pendingSuggestions = suggestions.apply(SuggestingTextFieldWidget.this.text, SuggestingTextFieldWidget.this.getCursor());
					this.pendingSuggestions.thenRun(() -> {
						if (!this.pendingSuggestions.isDone())
							return;
						showCommandSuggestions();
					});
				}
			}
			@Override
			protected OrderedText provideRenderText(String original, int firstCharacterIndex) {
				return OrderedText.styledForwardsVisitedString(original, Style.EMPTY);
			}
		};
		suggestor.parse = new ParseResults<>(null);
		
		setChangedListener(null);
	}
	public SuggestingTextFieldWidget(Screen screen, int x, int y, int width, int height) {
		this(screen, x, y, width, height, null);
	}
	
	@Override
	public void setChangedListener(Consumer<String> listener) {
		super.setChangedListener(str -> {
			suggestor.refresh();
			if (listener != null)
				listener.accept(str);
		});
	}
	
	@Override
	public SuggestingTextFieldWidget name(Text name) {
		super.name(name);
		return this;
	}
	
	public SuggestingTextFieldWidget suggest(BiFunction<String, Integer, CompletableFuture<Suggestions>> suggestions) {
		this.suggestions = suggestions;
		suggestor.refresh();
		return this;
	}
	
	private static final Supplier<Reflection.MethodInvoker> ChatInputSuggestor_render =
			Reflection.getOptionalMethod(ChatInputSuggestor.class, "method_23923", MethodType.methodType(void.class, MatrixStack.class, int.class, int.class));
	@Override
	public void render(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		if (!isDropdownOnly())
			super.render(matrices, mouseX, mouseY, delta);
		matrices.pushMatrix();
		matrices.translate(0, 0);
		Version.newSwitch()
				.range("1.20.0", null, () -> suggestor.render(MVDrawableHelper.getDrawContext(matrices), mouseX, mouseY))
				.range(null, "1.19.4", () -> ChatInputSuggestor_render.get().invoke(suggestor, matrices, mouseX, mouseY))
				.run();
		matrices.popMatrix();
	}
	@Override
	protected boolean shouldShowName() {
		return suggestor.window == null;
	}
	public boolean isDropdownOnly() {
		return false;
	}
	public Point getSpecialDropdownPos() {
		return null;
	}
	
	@Override
	public boolean mouseClicked(Click click, boolean b) {
		return suggestor.mouseClicked(click) || !isDropdownOnly() && super.mouseClicked(click,b);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return suggestor.mouseScrolled(verticalAmount) || !isDropdownOnly() && super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}
	
	@Override
	public boolean keyPressed(KeyInput keyInput) {
		if (!isMultiFocused())
			return false;
		return suggestor.keyPressed(keyInput) || !isDropdownOnly() && super.keyPressed(keyInput);
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		if (suggestor.window != null) {
			if (suggestor.window.area.contains((int) mouseX, (int) mouseY))
				return true;
		}
		return !isDropdownOnly() && super.isMouseOver(mouseX, mouseY);
	}
	
	@Override
	public void onMultiFocusedSet(boolean focused, boolean prevFocused) {
		suggestor.setWindowActive(focused);
		suggestor.refresh();
	}
	
}
