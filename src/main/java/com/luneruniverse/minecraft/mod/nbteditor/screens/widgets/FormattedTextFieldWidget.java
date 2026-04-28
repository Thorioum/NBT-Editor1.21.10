package com.luneruniverse.minecraft.mod.nbteditor.screens.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.EditableText;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTextEvents;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.OverlaySupportingScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigValueDropdown;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.StyleUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.TextUtil;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;

public class FormattedTextFieldWidget extends GroupWidget {
	
	private static class InternalTextFieldWidget extends MultiLineTextFieldWidget {
		private static class EventEditorWidget extends GroupWidget implements InitializableOverlay<Screen> {
			public enum ClickAction {
				NONE(null),
				OPEN_URL(MVTextEvents.ClickAction.OPEN_URL),
				RUN_COMMAND(MVTextEvents.ClickAction.RUN_COMMAND),
				SUGGEST_COMMAND(MVTextEvents.ClickAction.SUGGEST_COMMAND),
				CHANGE_PAGE(MVTextEvents.ClickAction.CHANGE_PAGE),
				COPY_TO_CLIPBOARD(MVTextEvents.ClickAction.COPY_TO_CLIPBOARD);
				
				public static ClickAction get(MVTextEvents.ClickAction<?> value) {
					for (ClickAction action : values()) {
						if (action.value == value)
							return action;
					}
					if (value == MVTextEvents.ClickAction.OPEN_FILE)
						return NONE;
					throw new IllegalArgumentException("Invalid ClickAction: " + value);
				}
				
				public final MVTextEvents.ClickAction<?> value;
				private ClickAction(MVTextEvents.ClickAction<?> value) {
					this.value = value;
				}
				
				@Override
				public String toString() {
					if (this == NONE)
						return "none";
					return value.getName();
				}
			}
			public enum HoverAction {
				NONE(null),
				SHOW_TEXT(MVTextEvents.HoverAction.SHOW_TEXT),
				SHOW_ITEM(MVTextEvents.HoverAction.SHOW_ITEM),
				SHOW_ENTITY(MVTextEvents.HoverAction.SHOW_ENTITY);
				
				public static HoverAction get(MVTextEvents.HoverAction<?> value) {
					for (HoverAction action : values()) {
						if (action.value == value)
							return action;
					}
					throw new IllegalArgumentException("Invalid HoverAction: " + value);
				}
				
				public final MVTextEvents.HoverAction<?> value;
				private HoverAction(MVTextEvents.HoverAction<?> value) {
					this.value = value;
				}
				
				@Override
				public String toString() {
					if (this == NONE)
						return "none";
					return value.getName();
				}
			}
			public interface EventPairCallback {
				public void onEventChange(ClickEvent clickEvent, HoverEvent hoverEvent);
			}
			
			private int x;
			private int y;
			private final ConfigValueDropdown<ClickAction> clickActionDropdown;
			private final TranslatedGroupWidget clickActionField;
			private final NamedTextFieldWidget clickValueField;
			private final ConfigValueDropdown<HoverAction> hoverActionDropdown;
			private final TranslatedGroupWidget hoverActionField;
			private final NamedTextFieldWidget hoverValueField;
			private final Button ok;
			private final Button cancel;
			
			public EventEditorWidget(ClickEvent clickEvent, HoverEvent hoverEvent, EventPairCallback onDone) {
				MVTextEvents.ClickAction<?> clickAction = (clickEvent == null ? null : MVTextEvents.ClickAction.getAction(clickEvent));
				String clickValue = (clickEvent == null ? "" : clickAction.getStringifiedValue(clickEvent));
				MVTextEvents.HoverAction<?> hoverAction = (hoverEvent == null ? null : MVTextEvents.HoverAction.getAction(hoverEvent));
				String hoverValue = (hoverEvent == null ? "" : hoverAction.getStringifiedValue(hoverEvent));
				
				clickActionDropdown = ConfigValueDropdown.forEnum(ClickAction.get(clickAction), ClickAction.NONE, ClickAction.class);
				clickActionDropdown.setWidth(150);
				clickActionField = addElement(TranslatedGroupWidget.forWidget(clickActionDropdown, 0, 0, 0));
				clickValueField = addWidget(new NamedTextFieldWidget(0, 0, 150, 16))
						.name(TextInst.translatable("nbteditor.formatted_text.click_event_value"));
				clickValueField.setMaxLength(Integer.MAX_VALUE);
				clickValueField.setValue(clickValue);
				
				hoverActionDropdown = ConfigValueDropdown.forEnum(HoverAction.get(hoverAction), HoverAction.NONE, HoverAction.class);
				hoverActionDropdown.setWidth(150);
				hoverActionDropdown.addValueListener(value -> updateOk());
				hoverActionField = addElement(TranslatedGroupWidget.forWidget(hoverActionDropdown, 0, 0, 0));
				hoverValueField = addWidget(new NamedTextFieldWidget(0, 0, 150, 16))
						.name(TextInst.translatable("nbteditor.formatted_text.hover_event_value"));
				hoverValueField.setMaxLength(Integer.MAX_VALUE);
				hoverValueField.setValue(hoverValue);
				hoverValueField.setResponder(str -> updateOk());
				
				ok = addWidget(MVMisc.newButton(0, 0, 150, 20, TextInst.translatable("nbteditor.ok"), btn -> {
					onDone.onEventChange(
							clickActionDropdown.getValidValue() == ClickAction.NONE ? null :
								clickActionDropdown.getValidValue().value.newEventParse(clickValueField.getValue()).get(),
							hoverActionDropdown.getValidValue() == HoverAction.NONE ? null :
								hoverActionDropdown.getValidValue().value.newEventParse(hoverValueField.getValue()).get());
					OverlaySupportingScreen.setOverlayStatic(null);
				}));
				cancel = addWidget(MVMisc.newButton(0, 0, 150, 20, TextInst.translatable("nbteditor.cancel"), btn -> {
					OverlaySupportingScreen.setOverlayStatic(null);
				}));
				
				addDrawable(hoverActionField);
				addDrawable(clickActionField);
			}
			
			@Override
			public void init(Screen parent, int width, int height) {
				x = width / 2;
				y = height / 2;
				
				clickActionField.setTranslation(x - 152, y - 34, 0);
				clickValueField.x = x + 2;
				clickValueField.y = y - 32;
				
				hoverActionField.setTranslation(x - 152, y - 12, 0);
				hoverValueField.x = x + 2;
				hoverValueField.y = y - 10;
				
				ok.x = x - 152;
				ok.y = y + 12;
				
				cancel.x = x + 2;
				cancel.y = y + 12;
			}
			
			private void updateOk() {
				ok.active = (clickActionDropdown.getValidValue() == ClickAction.NONE ||
						clickActionDropdown.getValidValue().value.newEventParse(clickValueField.getValue()).isPresent()) &&
						(hoverActionDropdown.getValidValue() == HoverAction.NONE ||
						hoverActionDropdown.getValidValue().value.newEventParse(hoverValueField.getValue()).isPresent());
			}
			
			@Override
			public void extractRenderState(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
				MVDrawableHelper.drawCenteredTextWithShadow(matrices, MainUtil.client.font,
						TextInst.translatable("nbteditor.formatted_text.events"),
						x, y - 38 - MainUtil.client.font.lineHeight, -1);
				super.extractRenderState(matrices, mouseX, mouseY, delta);
			}
			
			@Override
			public boolean keyPressed(KeyEvent keyInput) {
				if (keyInput.key() == GLFW.GLFW_KEY_ESCAPE) {
					OverlaySupportingScreen.setOverlayStatic(null);
					return true;
				}
				if (keyInput.key() == GLFW.GLFW_KEY_ENTER) {
					if (ok.active)
						ok.onPress(keyInput);
					return true;
				}
				
				return super.keyPressed(keyInput);
			}
		}
		
		public static InternalTextFieldWidget create(InternalTextFieldWidget prev, int x, int y, int width, int height,
                                                     Component text, boolean newLines, Style base, Consumer<Component> onChange) {
			if (prev == null)
				return new InternalTextFieldWidget(x, y, width, height, text, newLines, base, onChange);
			if (prev.allowsNewLines() != newLines)
				throw new IllegalArgumentException("Cannot convert to/from newLines on FormattedTextFieldWidget");
			if (!StyleUtil.identical(prev.base, base))
				throw new IllegalArgumentException("Cannot change base on FormattedTextFieldWidget");
			prev.setTextChangeListener(onChange);
			prev.ignoreNextSetText = true;
			MultiLineTextFieldWidget.create(prev, x, y, width, height, text.getString(),
					str -> prev.text, newLines, str -> prev.onChange.accept(prev.text));
			prev.setFormattedText(text);
			return prev;
		}
		
		private Consumer<Component> onChange;
		private final Style base;
		private final Style baseReset;
		private final List<Style> styles;
		private Style cursorStyle;
		private Component text;
		private boolean ignoreNextEditStyles;
		private boolean ignoreNextSetText;
		private final List<Component> undo;
		private int undoPos;
		
		protected InternalTextFieldWidget(int x, int y, int width, int height, Component text, boolean newLines, Style base, Consumer<Component> onChange) {
			super(x, y, width, height, text.getString(), newLines, null);
			this.onChange = onChange;
			this.base = base;
			this.baseReset = StyleUtil.minus(StyleUtil.RESET_STYLE, base);
			this.styles = new ArrayList<>();
			this.text = text.copy();
			this.undo = new ArrayList<>();
			this.undo.add(text);
			undoPos = 0;
			genStyles(text, base, 0);
			setFormatter(str -> this.text);
			setChangeListener(str -> this.onChange.accept(this.text));
			onEdit("", 0, 0);
			generateLines();
		}
		
		public InternalTextFieldWidget setTextChangeListener(Consumer<Component> onChange) {
			this.onChange = onChange;
			return this;
		}
		
		private void genStyles(Component text, Style parent, int index) {
			int len = MVMisc.getContent(text).length();
			Style style = text.getStyle().applyTo(parent);
			if (len > 0) {
				setStyle(index, style);
				index += len;
			}
			for (Component child : text.getSiblings()) {
				genStyles(child, style, index);
				index += MVMisc.stripInvalidChars(child.getString(), allowsNewLines()).length();
			}
		}
		
		private void setStyle(int index, Style style) {
			while (styles.size() <= index)
				styles.add(styles.size(), null);
			styles.set(index, style);
		}
		
		private Style getStyle(int index) {
			Style output = base;
			for (int i = 0; i <= index && i < styles.size(); i++) {
				Style style = styles.get(i);
				if (style != null)
					output = style;
			}
			return output;
		}
		
		private void applyFormatting(ChatFormatting formatting) {
			int start = getSelStart();
			int end = getSelEnd();
			
			if (start == end) {
				if (cursorStyle == null)
					cursorStyle = getStyle(start == 0 ? 0 : start - 1);
				if (StyleUtil.hasFormatting(cursorStyle, formatting))
					cursorStyle = withoutFormatting(cursorStyle, formatting);
				else
					cursorStyle = withFormatting(cursorStyle, formatting);
				return;
			}
			
			Style startStyle = getStyle(start);
			Style endStyle = getStyle(end);
			
			boolean filled = StyleUtil.hasFormatting(startStyle, formatting);
			setStyle(start, withFormatting(startStyle, formatting));
			for (int i = start + 1; i < end && i < styles.size(); i++) {
				Style style = styles.get(i);
				if (style != null && !StyleUtil.hasFormatting(style, formatting)) {
					styles.set(i, withFormatting(style, formatting));
					filled = false;
				}
			}
			setStyle(end, endStyle);
			
			if (filled) {
				setStyle(start, withoutFormatting(startStyle, formatting));
				for (int i = start + 1; i < end && i < styles.size(); i++) {
					Style style = styles.get(i);
					if (style != null)
						styles.set(i, withoutFormatting(style, formatting));
				}
			}
			
			markUndo();
			onEdit("", start, 0);
			generateLines();
			onChange.accept(text);
		}
		private Style withFormatting(Style style, ChatFormatting formatting) {
			if (formatting == ChatFormatting.RESET)
				return baseReset;
			return style.applyFormat(formatting);
		}
		private Style withoutFormatting(Style style, ChatFormatting formatting) {
			return StyleUtil.minusFormatting(style, StyleUtil.RESET_STYLE.withColor(base.getColor()), formatting);
		}
		
		private void applyStyleChange(UnaryOperator<Style> changer, boolean regenerateLines) {
			int start = getSelStart();
			int end = getSelEnd();
			
			if (start == end) {
				if (cursorStyle == null)
					cursorStyle = getStyle(start == 0 ? 0 : start - 1);
				cursorStyle = changer.apply(cursorStyle);
				return;
			}
			
			Style startStyle = getStyle(start);
			Style endStyle = getStyle(end);
			
			setStyle(start, changer.apply(startStyle));
			for (int i = start + 1; i < end && i < styles.size(); i++) {
				Style style = styles.get(i);
				if (style != null)
					styles.set(i, changer.apply(style));
			}
			setStyle(end, endStyle);
			
			markUndo();
			onEdit("", start, 0);
			if (regenerateLines)
				generateLines();
			onChange.accept(text);
		}
		
		private void applyColor(ChatFormatting color, boolean shadow) {
			if (shadow) {
				int shadowColor = (MVMisc.scaleRgb(color.getColor(), 0.25) | 0xFF000000);
				applyStyleChange(style -> style.withShadowColor(shadowColor), true);
			} else
				applyFormatting(color);
		}
		
		public void setFormattedText(Component text) {
			styles.clear();
			genStyles(text, base, 0);
			ignoreNextEditStyles = true;
			setText(text.getString());
			ignoreNextEditStyles = false; // If onEdit doesn't get called since text is the same
		}
		public Component getFormattedText() {
			return text;
		}
		@Override
		public void setText(String text) {
			if (ignoreNextSetText) {
				ignoreNextSetText = false;
				return;
			}
			super.setText(text);
		}
		
		private Style getInitialCustomStyle() {
			return (getSelStart() == getSelEnd() ?
					(cursorStyle == null ? getStyle(getSelStart() == 0 ? 0 : getSelStart() - 1) : cursorStyle) :
						getStyle(getSelStart()));
		}
		private void showCustomColor(boolean shadow) {
			Style initialStyle = getInitialCustomStyle();
			int initialColor = (initialStyle.getColor() == null ?
					(base.getColor() == null ? -1 : base.getColor().getValue()) : initialStyle.getColor().getValue());
			if (shadow) {
				int initialShadow = (initialStyle.getShadowColor() == null ?
						(base.getShadowColor() == null ? MVMisc.scaleRgb(initialColor, 0.25) : base.getShadowColor()) : initialStyle.getShadowColor());
				InputOverlay.show(
						TextInst.translatable("nbteditor.formatted_text.custom_color.shadow"),
						new ColorSelectorWidget.ColorSelectorInput(initialShadow),
						rgb -> applyStyleChange(style -> style.withShadowColor(rgb | 0xFF000000), true));
			} else {
				InputOverlay.show(
						TextInst.translatable("nbteditor.formatted_text.custom_color"),
						new ColorSelectorWidget.ColorSelectorInput(initialColor),
						rgb -> applyStyleChange(style -> style.withColor(rgb), true));
			}
		}
		private void showEvents() {
			Style initialStyle = getInitialCustomStyle();
			OverlaySupportingScreen.setOverlayStatic(new EventEditorWidget(
					initialStyle.getClickEvent(), initialStyle.getHoverEvent(),
					(clickEvent, hoverEvent) -> applyStyleChange(
							style -> style.withClickEvent(clickEvent).withHoverEvent(hoverEvent), false)),
					200);
		}
		private void showInsertion() {
			Style initialStyle = getInitialCustomStyle();
			InputOverlay.show(
					TextInst.translatable("nbteditor.formatted_text.insertion"),
					StringInput.builder()
							.withDefault(initialStyle.getInsertion() == null ? "" : initialStyle.getInsertion())
							.build(),
					insertion -> applyStyleChange(style -> style.withInsertion(insertion.isEmpty() ? null : insertion), false));
		}
		private String getFontString(Style s) {
			if(s == null) return "";
			FontDescription font = s.getFont();
			if(font instanceof FontDescription.Resource f) return f.id().toString();
			if(font instanceof FontDescription.AtlasSprite f) return f.spriteId().toString();
			return "";
		}
		private void showFont() {
			Style initialStyle = getInitialCustomStyle();
			InputOverlay.show(
					TextInst.translatable("nbteditor.formatted_text.font"),
					StringInput.builder()
							.withDefault(getFontString(initialStyle))
							.withValidator(font -> font.isEmpty() || IdentifierInst.isValid(font))
							.withSuggestions((str, cursor) -> {
								SuggestionsBuilder builder = new SuggestionsBuilder(str, 0);
								for (Identifier font : MainUtil.client.fontManager.fontSets.keySet()) {
									String fontStr = font.toString();
									if (fontStr.startsWith(str))
										builder.suggest(fontStr);
								}
								return builder.buildFuture();
							})
							.build(),
					font -> applyStyleChange(style -> style.withFont(font.isEmpty() ? null : new FontDescription.Resource(IdentifierInst.of(font))), true));
		}
		
		@Override
		protected void renderHighlightsBelow(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
			Style initialStyle = getStyle(0);
			int start = (initialStyle.getClickEvent() != null || initialStyle.getHoverEvent() != null || initialStyle.getInsertion() != null ? 0 : -1);
			for (int i = 0; i < styles.size(); i++) {
				Style style = styles.get(i);
				if (style == null)
					continue;
				if (style.getClickEvent() != null || style.getHoverEvent() != null || style.getInsertion() != null) {
					if (start == -1)
						start = i;
				} else if (start != -1) {
					renderHighlight(matrices, start, i, 0x55FFAA00);
					start = -1;
				}
			}
			if (start != -1)
				renderHighlight(matrices, start, getText().length(), 0x55FFAA00);
		}
		
		@Override
		public boolean keyPressed(KeyEvent keyInput) {
			if (super.keyPressed(keyInput))
				return true;
			
			if (NBTEditor.hasControlDown() && !NBTEditor.hasShiftDown()) {
				ChatFormatting formatting = switch (keyInput.key()) {
					case GLFW.GLFW_KEY_B -> ChatFormatting.BOLD;
					case GLFW.GLFW_KEY_I -> ChatFormatting.ITALIC;
					case GLFW.GLFW_KEY_U -> ChatFormatting.UNDERLINE;
					case GLFW.GLFW_KEY_D -> ChatFormatting.STRIKETHROUGH;
					case GLFW.GLFW_KEY_K -> ChatFormatting.OBFUSCATED;
					case GLFW.GLFW_KEY_BACKSLASH -> ChatFormatting.RESET;
					default -> null;
				};
				if (formatting != null) {
					applyFormatting(formatting);
					return true;
				}
			}
			
			if (NBTEditor.hasControlDown() && NBTEditor.hasShiftDown()) {
				switch (keyInput.key()) {
					case GLFW.GLFW_KEY_C -> showCustomColor(hasShadowKeyDown());
					case GLFW.GLFW_KEY_E -> showEvents();
					case GLFW.GLFW_KEY_I -> showInsertion();
					case GLFW.GLFW_KEY_F -> showFont();
				}
			}
			
			return false;
		}
		
		@Override
		protected void onCursorMove(int cursor, int selStart, int selEnd) {
			if (getCursor() == cursor && getSelStart() == selStart && getSelEnd() == selEnd)
				return;
			cursorStyle = null;
		}
		
		@Override
		protected void onEdit(String insertedText, int pos, int overwrittenLen) {
			while (undoPos > 0) {
				undo.remove(0);
				undoPos--;
			}
			
			if (ignoreNextEditStyles)
				ignoreNextEditStyles = false;
			else if (overwrittenLen == 0) {
				Style style = getStyle(pos);
				setStyle(pos, style);
				for (int i = 0; i < insertedText.length(); i++)
					styles.add(pos, null);
				if (cursorStyle != null)
					setStyle(pos, cursorStyle);
				else if (pos == 0)
					setStyle(0, style);
			} else {
				Style startStyle = getStyle(pos);
				Style endStyle = null;
				for (int i = 0; i < overwrittenLen && pos < styles.size(); i++) {
					Style style = styles.remove(pos);
					if (style != null)
						endStyle = style;
				}
				if (endStyle != null && (pos >= styles.size() || styles.get(pos) == null))
					setStyle(pos, endStyle);
				if (pos < styles.size()) {
					for (int i = 0; i < insertedText.length(); i++)
						styles.add(pos, null);
				}
				if (!insertedText.isEmpty())
					setStyle(pos, startStyle);
			}
			
			String afterEdit = new StringBuilder(getText()).replace(pos, pos + overwrittenLen, insertedText).toString();
			EditableText text = TextInst.literal("");
			String part = "";
			Style style = !styles.isEmpty() && styles.get(0) != null ? styles.get(0) : base;
			for (int i = 0; i < afterEdit.length(); i++) {
				Style newStyle = (i == 0 || i >= styles.size() ? null : styles.get(i));
				if (newStyle != null) {
					if (newStyle.equals(style))
						styles.set(i, null);
					else {
						text.append(TextInst.literal(part).setStyle(style));
						part = "";
						style = newStyle;
					}
				}
				part += afterEdit.charAt(i);
			}
			if (!part.isEmpty())
				text.append(TextInst.literal(part).setStyle(style));
			undo.add(0, text);
			this.text = text;
			
			while (styles.size() > afterEdit.length())
				styles.remove(afterEdit.length());
		}
		
		@Override
		protected void onUndo(String newText) {
			if (undoPos < undo.size() - 1) {
				text = undo.get(++undoPos);
				styles.clear();
				genStyles(text, Style.EMPTY, 0);
			}
		}
		
		@Override
		protected void onRedo(String newText) {
			if (undoPos > 0) {
				text = undo.get(--undoPos);
				styles.clear();
				genStyles(text, Style.EMPTY, 0);
			}
		}
		
		@Override
		protected void onUndoDiscard() {
			while (undoPos > 0) {
				undo.remove(0);
				undoPos--;
			}
			undo.remove(0);
		}
		
		@Override
		protected String onCopy(String text, int pos, int len) {
			return TextInst.toString(TextUtil.substring(this.text, pos, pos + len));
		}
		
		@Override
		protected String onPaste(String text, int pos, int overwrittenLen) {
			try {
				Component textValue = pasteFilter(TextUtil.fromStringSafely(text, true));
				String textValueStr = textValue.getString();
				int textLen = textValueStr.length();
				
				Style endStyle = getStyle(pos);
				for (int i = 0; i < overwrittenLen && pos < styles.size(); i++) {
					Style style = styles.remove(pos);
					if (style != null)
						endStyle = style;
				}
				if (endStyle != null && (pos >= styles.size() || styles.get(pos) == null))
					setStyle(pos, endStyle);
				if (pos < styles.size()) {
					for (int i = 0; i < textLen; i++)
						styles.add(pos, null);
				}
				
				genStyles(textValue, Style.EMPTY, pos);
				ignoreNextEditStyles = true;
				
				return textValueStr;
			} catch (Exception e) {
				return text;
			}
		}
		private Component pasteFilter(Component toPaste) {
			toPaste = TextUtil.stripInvalidChars(toPaste, allowsNewLines());
			int numNewLines = getNumNewLines(getText());
			int toPasteNewLines = getNumNewLines(toPaste);
			while (numNewLines + toPasteNewLines + 1 > maxLines) {
				int i = TextUtil.lastIndexOf(toPaste, '\n');
				if (i == -1)
					break;
				toPaste = TextUtil.deleteCharAt(toPaste, i);
				toPasteNewLines--;
			}
			return toPaste;
		}
		private int getNumNewLines(Component text) {
			AtomicInteger output = new AtomicInteger(0);
			text.visit(str -> {
				int numNewLines = getNumNewLines(str);
				if (numNewLines != 0)
					output.setPlain(output.getPlain() + numNewLines);
				return Optional.empty();
			});
			return output.getPlain();
		}
	}
	
	public static FormattedTextFieldWidget create(FormattedTextFieldWidget prev, int x, int y, int width, int height,
                                                  Component text, boolean newLines, Style base, Consumer<Component> onChange) {
		if (prev == null)
			return new FormattedTextFieldWidget(x, y, width, height, text, newLines, base, onChange);
		prev.x = x;
		prev.y = y;
		prev.width = width;
		prev.height = height;
		prev.field = InternalTextFieldWidget.create(prev.field, x, y + 24, width, height - 24, text, newLines, base, onChange);
		prev.clearWidgets();
		prev.init();
		prev.addElement(prev.field);
		prev.addTickable(prev.field);
		prev.setText(text);
		prev.setChangeListener(onChange);
		if (prev.isMultiFocused())
			prev.setMultiFocused(false);
		return prev;
	}
	public static FormattedTextFieldWidget create(FormattedTextFieldWidget prev, int x, int y, int width, int height,
                                                  List<Component> lines, Style base, Consumer<List<Component>> onChange) {
		return create(prev, x, y, width, height, TextUtil.joinLines(lines), true, base, text -> onChange.accept(TextUtil.splitText(text)));
	}
	
	private static boolean hasShadowKeyDown() {
		return StyleUtil.SHADOW_COLOR_EXISTS && NBTEditor.hasAltDown();
	}
	
	private int x;
	private int y;
	private int width;
	private int height;
	private InternalTextFieldWidget field;
	private ButtonDropdownWidget colors;
	private Button font;
	private int lastFont;
	private long lastFontChange;
	
	protected FormattedTextFieldWidget(int x, int y, int width, int height, Component text, boolean newLines, Style base, Consumer<Component> onChange) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.field = InternalTextFieldWidget.create(null, x, y + 24, width, height - 24, text, newLines, base, onChange);
		init();
		addElement(field);
		addTickable(field);
	}
	private void init() {
		if (width < 16 * 20 + (ConfigScreen.isHideFormatButtons() ? 0 : 20 + 4 + 5 * 20 + (4 + 20) * 2)) {
			colors = addElement(new ButtonDropdownWidget(x, y, 20, 20, TextInst.literal("⬛").formatted(ChatFormatting.AQUA), 20, 20));
			for (ChatFormatting formatting : ChatFormatting.values()) {
				if (!formatting.isColor())
					break;
				colors.addButton(TextInst.literal("⬛").formatted(formatting), btn -> {
					field.applyColor(formatting, hasShadowKeyDown());
					colors.setOpen(false);
				}, createColorButtonTooltip(formatting));
			}
			colors.build();
		} else {
			colors = null;
			int i = 0;
			for (ChatFormatting formatting : ChatFormatting.values()) {
				if (!formatting.isColor())
					break;
				addWidget(MVMisc.newButton(x + i * 20, y, 20, 20, TextInst.literal("⬛").formatted(formatting),
						btn -> field.applyColor(formatting, hasShadowKeyDown()), createColorButtonTooltip(formatting)));
				i++;
			}
		}
		
		if (!ConfigScreen.isHideFormatButtons()) {
			int afterColorsX = x + (colors == null ? 16 * 20 : 20);
			
			int i = 0;
			for (ChatFormatting formatting : new ChatFormatting[] {ChatFormatting.BOLD, ChatFormatting.ITALIC, ChatFormatting.UNDERLINE,
					ChatFormatting.STRIKETHROUGH, ChatFormatting.OBFUSCATED, ChatFormatting.RESET}) {
				Component btnText;
				MVTooltip btnTooltip;
				if (formatting == ChatFormatting.RESET) {
					btnText = TextInst.of("");
					if (ConfigScreen.isKeybindsHidden())
						btnTooltip = new MVTooltip(TextInst.of(formatting.getName()));
					else {
						btnTooltip = new MVTooltip(
								TextInst.of(formatting.getName()),
								TextInst.translatable("nbteditor.keybind.formatted_text.reset"));
					}
				} else {
					btnText = TextInst.literal(formatting.name().substring(0, 1)).formatted(formatting);
					btnTooltip = new MVTooltip(TextInst.of(formatting.getName()));
				}
				addWidget(MVMisc.newButton(
						afterColorsX + 24 + i * 20 + (formatting == ChatFormatting.RESET ? 4 + 20 * 3 + 4 : 0), y, 20, 20,
						btnText, btn -> field.applyFormatting(formatting), btnTooltip));
				i++;
			}
			
			addWidget(MVMisc.newButton(afterColorsX, y, 20, 20,
					TextInst.literal("⬛").setStyle(Style.EMPTY.withColor(0x9999C0).applyFormat(ChatFormatting.ITALIC)),
					btn -> field.showCustomColor(hasShadowKeyDown()),
					createFormatButtonTooltip("custom_color", true)));
			
			addWidget(MVMisc.newButton(afterColorsX + 24 + 5 * 20 + 4, y, 20, 20,
					TextInst.literal("E"),
					btn -> field.showEvents(),
					createFormatButtonTooltip("events", false)));
			addWidget(MVMisc.newButton(afterColorsX + 24 + 5 * 20 + 4 + 20, y, 20, 20,
					TextInst.literal("I"),
					btn -> field.showInsertion(),
					createFormatButtonTooltip("insertion", false)));
			font = addWidget(MVMisc.newButton(afterColorsX + 24 + 5 * 20 + 4 + 20 * 2, y, 20, 20,
					TextInst.literal("F"), // Gets replaced before rendering
					btn -> field.showFont(),
					createFormatButtonTooltip("font", false)));
		}
	}
	private MVTooltip createColorButtonTooltip(ChatFormatting color) {
		Component name = TextInst.of(color.getName());
		if (ConfigScreen.isKeybindsHidden() || !StyleUtil.SHADOW_COLOR_EXISTS)
			return new MVTooltip(name);
		return new MVTooltip(name, TextInst.translatable("nbteditor.keybind.formatted_text.shadow"));
	}
	private MVTooltip createFormatButtonTooltip(String name, boolean color) {
		String nameKey = "nbteditor.formatted_text." + name;
		if (ConfigScreen.isKeybindsHidden())
			return new MVTooltip(nameKey);
		String keybindKey = "nbteditor.keybind.formatted_text." + name;
		if (color && StyleUtil.SHADOW_COLOR_EXISTS)
			return new MVTooltip(nameKey, keybindKey, "nbteditor.keybind.formatted_text.shadow");
		return new MVTooltip(nameKey, keybindKey);
	}
	
	public FormattedTextFieldWidget setChangeListener(Consumer<Component> onChange) {
		field.setTextChangeListener(onChange);
		return this;
	}
	public FormattedTextFieldWidget setMaxLines(int maxLines) {
		field.setMaxLines(maxLines);
		return this;
	}
	public FormattedTextFieldWidget setBackgroundColor(int bgColor) {
		field.setBackgroundColor(bgColor);
		return this;
	}
	public FormattedTextFieldWidget setCursorColor(int cursorColor) {
		field.setCursorColor(cursorColor);
		return this;
	}
	public FormattedTextFieldWidget setSelectionColor(int selColor) {
		field.setSelectionColor(selColor);
		return this;
	}
	public FormattedTextFieldWidget setShadow(boolean shadow) {
		field.setShadow(shadow);
		return this;
	}
	public FormattedTextFieldWidget setOverscroll(boolean overscroll) {
		field.setOverscroll(overscroll);
		return this;
	}
	
	public FormattedTextFieldWidget suggest(Screen screen, BiFunction<String, Integer, CompletableFuture<Suggestions>> suggestions) {
		field.suggest(screen, suggestions);
		return this;
	}
	
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	
	public void setText(Component text) {
		field.setFormattedText(text);
	}
	public void setText(List<Component> lines) {
		setText(TextUtil.joinLines(lines));
	}
	public Component getText() {
		return field.getFormattedText();
	}
	
	public List<Component> getTextLines() {
		return TextUtil.splitText(getText());
	}
	
	@Override
	public void extractRenderState(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		setFocused(isMultiFocused() ? field : null);
		field.extractRenderState(matrices, mouseX, mouseY, delta);
		
		if (colors != null) {
			matrices.pushMatrix();
			matrices.translate(0.0f, 0.0f);
			colors.extractRenderState(matrices, mouseX, mouseY, delta);
			matrices.popMatrix();
		}
		
		if (font != null) {
			long time = System.currentTimeMillis();
			if (lastFontChange < time - 200) {
				lastFontChange = time;
				lastFont += (int) (Math.floor(Math.random() * 2) + 1);
				font.setMessage(TextInst.literal(lastFont % 3 + "")
						.styled(style -> style.withFont(new FontDescription.Resource(IdentifierInst.of("nbteditor", "fancy_f")))));
			}
		}
		
		super.extractRenderState(matrices, mouseX, mouseY, delta);
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}
	
}
