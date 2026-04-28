package com.luneruniverse.minecraft.mod.nbteditor.screens.widgets;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawable;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVElement;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.screens.OverlaySupportingScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.Tickable;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.TextUtil;
import com.mojang.brigadier.suggestion.Suggestions;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

public class MultiLineTextFieldWidget implements MVDrawable, MVElement, Tickable, NarratableEntry {
	
	private class FindAndReplaceWidget extends TranslatedGroupWidget {
		private static String findValue = "";
		private static String replaceValue = "";
		private static boolean regex = false;
		
		private final NamedTextFieldWidget find;
		private final NamedTextFieldWidget replace;
		private final Button regexBtn;
		private boolean dragging;
		private Matcher lastRegexMatch;
		
		public FindAndReplaceWidget() {
			super(MainUtil.client.getWindow().getGuiScaledWidth() / 2 - 100,
					MainUtil.client.getWindow().getGuiScaledHeight() / 2 - 30, 200);
			find = addWidget(new NamedTextFieldWidget(0, 0, 176, 16)
					.name(TextInst.translatable("nbteditor.multi_line_text.find")));
			replace = addWidget(new NamedTextFieldWidget(0, 20, 200, 16)
					.name(TextInst.translatable("nbteditor.multi_line_text.replace")));
			regexBtn = addWidget(MVMisc.newButton(180, -2, 20, 20,
					TextInst.translatable("nbteditor.multi_line_text.regex." + (regex ? "on" : "off")), btn -> {
				regex = !regex;
				btn.setMessage(TextInst.translatable("nbteditor.multi_line_text.regex." + (regex ? "on" : "off")));
			}, new MVTooltip("nbteditor.multi_line_text.regex")));
			addWidget(MVMisc.newButton(0, 40, 40, 20, TextInst.translatable("nbteditor.multi_line_text.find"), btn -> {
				goToNext(NBTEditor.hasShiftDown(), true);
			}));
			addWidget(MVMisc.newButton(44, 40, 64, 20, TextInst.translatable("nbteditor.multi_line_text.replace"), btn -> {
				if (goToNext(NBTEditor.hasShiftDown(), true))
					replaceSel();
			}));
			addWidget(MVMisc.newButton(112, 40, 64, 20, TextInst.translatable("nbteditor.multi_line_text.replace_all"), btn -> {
				boolean first = true;
				int prevCursor = cursor;
				cursor = 0;
				while (goToNext(false, false)) {
					if (first)
						first = false;
					else {
						undo.remove(0);
						onUndoDiscard();
					}
					replaceSel();
				}
				if (first)
					cursor = prevCursor;
			}));
			addWidget(MVMisc.newButton(180, 40, 20, 20, TextInst.translatable("nbteditor.multi_line_text.x"), btn -> {
				OverlaySupportingScreen.setOverlayStatic(null);
			}));
			
			if (selStart != selEnd)
				findValue = getSelectedText();
			find.setMaxLength(Integer.MAX_VALUE);
			find.setValue(findValue);
			find.setResponder(str -> findValue = str);
			setFocused(find);
			
			replace.setMaxLength(Integer.MAX_VALUE);
			replace.setValue(replaceValue);
			replace.setResponder(str -> replaceValue = str);
		}
		
		private boolean goToNext(boolean backward, boolean wrap) {
			if (findValue.isEmpty())
				return false;
			if (backward) {
				if (cursor == 0 || !goToRange(text, findValue, cursor - 1, true))
					return wrap && goToRange(text, findValue, text.length(), true);
			} else {
				if (!goToRange(text, findValue, cursor, false))
					return wrap && goToRange(text, findValue, 0, false);
			}
			return true;
		}
		private boolean goToRange(String str, String expr, int start, boolean last) {
			if (regex) {
				if (last)
					str = str.substring(0, start);
				try {
					Matcher matcher = Pattern.compile(expr).matcher(str);
					if (!matcher.find(last ? 0 : start))
						return false;
					int numMatches = 0;
					do {
						numMatches++;
						selStart = matcher.start();
						selEnd = matcher.end();
						if (selStart == selEnd)
							return false;
					} while (last && matcher.find());
					if (last) {
						matcher.reset();
						for (int i = 0; i < numMatches; i++)
							matcher.find();
					}
					lastRegexMatch = matcher;
				} catch (PatternSyntaxException e) {
					return false;
				}
			} else {
				int i = last ? str.substring(0, start).lastIndexOf(expr) : str.indexOf(expr, start);
				if (i == -1)
					return false;
				selStart = i;
				selEnd = selStart + expr.length();
			}
			cursor = selEnd;
			cursorX = -1;
			return true;
		}
		private void replaceSel() {
			if (selStart == selEnd)
				return;
			if (!regex) {
				write(replaceValue);
				return;
			}
			StringBuilder replacement = new StringBuilder();
			try {
				lastRegexMatch.appendReplacement(replacement, replaceValue);
				replacement.delete(0, lastRegexMatch.start());
			} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
				replacement.setLength(0);
				replacement.append(replaceValue);
			}
			write(replacement.toString());
		}
		
		@Override
		public void renderPre(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
			MVDrawableHelper.fill(matrices, -16, -16, 216, 76, 0xC8101010);
		}
		
		@Override
		protected boolean mouseClickedPre(double mouseX, double mouseY, int button) {
			if (isMouseOver(mouseX, mouseY) && !(mouseX >= 0 && mouseX <= 200 && mouseY >= 0 && mouseY <= 60))
				dragging = true;
			return false;
		}
		@Override
		protected boolean mouseReleasedPre(double mouseX, double mouseY, int button) {
			dragging = false;
			return false;
		}
		@Override
		public boolean mouseDraggedPre(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
			if (dragging)
				addTranslation(deltaX, deltaY, 0);
			return false;
		}
		
		@Override
		public boolean keyPressed(KeyEvent keyInput) {
			int keyCode = keyInput.key();
			if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
				OverlaySupportingScreen.setOverlayStatic(null);
				return true;
			}
			if (keyCode == GLFW.GLFW_KEY_ENTER) {
				goToNext(NBTEditor.hasShiftDown(), true);
				return true;
			}
			if (keyCode == GLFW.GLFW_KEY_TAB) {
				if (getFocused() == find)
					setFocused(replace);
				else
					setFocused(find);
				return true;
			}
			if (keyCode == GLFW.GLFW_KEY_R && NBTEditor.hasControlDown() && !NBTEditor.hasShiftDown() && !NBTEditor.hasAltDown()) {
				regex = !regex;
				regexBtn.setMessage(TextInst.translatable("nbteditor.multi_line_text.regex." + (regex ? "on" : "off")));
				return true;
			}
			
			return super.keyPressed(keyInput);
		}
		
		@Override
		public boolean isMouseOver(double mouseX, double mouseY) {
			return mouseX >= -16 && mouseX <= 216 && mouseY >= -16 && mouseY <= 76;
		}
	}
	
	private static final Font textRenderer = MainUtil.client.font;
	
	public static MultiLineTextFieldWidget create(MultiLineTextFieldWidget prev, int x, int y, int width, int height,
                                                  String text, Function<String, Component> formatter, boolean newLines, Consumer<String> onChange) {
		if (prev == null)
			return new MultiLineTextFieldWidget(x, y, width, height, text, formatter, newLines, onChange);
		if (prev.newLines != newLines)
			throw new IllegalArgumentException("Cannot convert to/from newLines on MultiLineTextFieldWidget");
		prev.x = x;
		prev.y = y;
		prev.width = width;
		prev.height = height;
		prev.setText(text);
		prev.setFormatter(formatter);
		prev.setChangeListener(onChange);
		prev.generateLines();
		if (prev.isMultiFocused())
			prev.setMultiFocused(false);
		prev.scrollBar = new ScrollBarWidget(x + width - 8, y, height,
				() -> prev.scroll, scroll -> prev.scroll = scroll, prev::getMaxScroll);
		return prev;
	}
	public static MultiLineTextFieldWidget create(MultiLineTextFieldWidget prev, int x, int y, int width, int height,
			String text, boolean newLines, Consumer<String> onChange) {
		return create(prev, x, y, width, height, text, null, newLines, onChange);
	}
	
	private int x;
	private int y;
	private int width;
	private int height;
	private String text;
	private Function<String, Component> formatter;
	private final boolean newLines;
	private Consumer<String> onChange;
	protected int maxLines;
	private int bgColor;
	private int cursorColor;
	private int selColor;
	private boolean shadow;
	
	private final List<Component> lines;
	private final List<Component> renderedLines;
	private final List<Map.Entry<String, Integer>> undo;
	private int undoPos;
	private int cursor;
	private int selStart;
	private int selEnd;
	private int cursorBlinkTracker;
	private int cursorX;
	private boolean overscroll;
	private int scroll;
	private ScrollBarWidget scrollBar;
	
	private SuggestingTextFieldWidget suggestor;
	
	protected MultiLineTextFieldWidget(int x, int y, int width, int height, String text,
                                       Function<String, Component> formatter, boolean newLines, Consumer<String> onChange) {
		if(newLines) text = MVMisc.stripInvalidChars(text, true);

		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.text = text;
		this.formatter = formatter;
		this.newLines = newLines;
		this.onChange = str -> {
			generateLines();
			onChange.accept(str);
		};
		this.maxLines = Integer.MAX_VALUE;
		this.bgColor = 0x55000000;
		this.cursorColor = 0xFFFFFFFF;
		this.selColor = 0x55FFFFFF;
		this.shadow = true;
		
		this.lines = new ArrayList<>();
		this.renderedLines = new ArrayList<>();
		this.undo = new ArrayList<>();
		this.undo.add(0, Map.entry(text, text.length()));
		this.undoPos = 0;
		setCursor(text.length());
		this.cursorX = -1;
		this.overscroll = true;
		this.scrollBar = new ScrollBarWidget(x + width - 8, y, height,
				() -> scroll, scroll -> this.scroll = scroll, this::getMaxScroll);
		
		generateLines();
	}
	protected MultiLineTextFieldWidget(int x, int y, int width, int height, String text, boolean newLines, Consumer<String> onChange) {
		this(x, y, width, height, text, null, newLines, onChange);
	}
	
	public MultiLineTextFieldWidget setFormatter(Function<String, Component> formatter) {
		this.formatter = formatter;
		generateLines();
		return this;
	}
	public MultiLineTextFieldWidget setChangeListener(Consumer<String> onChange) {
		this.onChange = str -> {
			generateLines();
			onChange.accept(str);
		};
		return this;
	}
	public MultiLineTextFieldWidget setMaxLines(int maxLines) {
		this.maxLines = maxLines;
		return this;
	}
	public MultiLineTextFieldWidget setBackgroundColor(int bgColor) {
		this.bgColor = bgColor;
		return this;
	}
	public MultiLineTextFieldWidget setCursorColor(int cursorColor) {
		this.cursorColor = cursorColor;
		return this;
	}
	public MultiLineTextFieldWidget setSelectionColor(int selColor) {
		this.selColor = selColor;
		return this;
	}
	public MultiLineTextFieldWidget setShadow(boolean shadow) {
		this.shadow = shadow;
		return this;
	}
	public MultiLineTextFieldWidget setOverscroll(boolean overscroll) {
		this.overscroll = overscroll;
		return this;
	}
	
	public MultiLineTextFieldWidget suggest(Screen screen, BiFunction<String, Integer, CompletableFuture<Suggestions>> suggestions) {
		if (suggestor == null) {
			suggestor = new SuggestingTextFieldWidget(screen, x, y, width, height) {
				@Override
				public boolean isDropdownOnly() {
					return true;
				}
				@Override
				public Point getSpecialDropdownPos() {
					Point output = getXYPos(cursor);
					output.y += textRenderer.lineHeight * 1.5 + scroll;
					return output;
				}
			};
			suggestor.setMaxLength(Integer.MAX_VALUE);
		}
		suggestor.suggest(suggestions);
		return this;
	}
	private void syncToSuggestor() {
		if (!suggestor.value.equals(text))
			suggestor.setValue(text);
		
		if (suggestor.getCursorPosition() != cursor)
			MVMisc.setCursor(suggestor, cursor);
		
		boolean focus = isMultiFocused();
		if (suggestor.isMultiFocused() != focus)
			suggestor.setMultiFocused(focus);
	}
	private void syncFromSuggestor() {
		if (!suggestor.value.equals(text))
			setText(suggestor.value);
		
		if (suggestor.getCursorPosition() != cursor)
			setCursor(suggestor.getCursorPosition());
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
	
	public void setText(String text) {
		if (this.text.equals(text))
			return;
		selStart = 0;
		selEnd = this.text.length();
		write(text);
	}
	public String getText() {
		return text;
	}
	
	public boolean allowsNewLines() {
		return newLines;
	}
	
	@Override
	public void extractRenderState(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		MVDrawableHelper.fill(matrices, x, y, x + width, y + height, bgColor);
		
		MVDrawableHelper.enableScissor(matrices, x, y, width, height);
		
		matrices.pushMatrix();
		matrices.translate(0.0F, scroll);
		
		renderHighlightsBelow(matrices, mouseX, mouseY, delta);
		
		int yOffset = y;
		for (Component line : renderedLines) {
			MVDrawableHelper.drawText(matrices, textRenderer, line, x + textRenderer.lineHeight, yOffset + textRenderer.lineHeight, -1, shadow);
			yOffset += textRenderer.lineHeight * 1.5;
		}
		
		Version.newSwitch()
				.range("1.20.0", null, () -> matrices.translate(0.0f, 0.0f))
				.range(null, "1.19.4", () -> {})
				.run();
		
		renderHighlightsAbove(matrices, mouseX, mouseY, delta);
		renderHighlight(matrices, getSelStart(), getSelEnd(), selColor);
		
		if (isMultiFocused() && cursorBlinkTracker / 6 % 2 == 0) {
			Point cursor = getXYPos(this.cursor);
			MVDrawableHelper.fill(matrices, cursor.x, cursor.y, cursor.x + 1, cursor.y + textRenderer.lineHeight, cursorColor);
		}
		
		matrices.popMatrix();
		
		scrollBar.extractRenderState(matrices, mouseX, mouseY, delta);
		
		if (suggestor != null) {
			syncToSuggestor();
			suggestor.extractRenderState(matrices, mouseX, mouseY, delta);
		}
		
		MVDrawableHelper.disableScissor(matrices);
	}
	protected void renderHighlightsBelow(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {}
	protected void renderHighlightsAbove(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {}
	protected void renderHighlight(Matrix3x2fStack matrices, int start, int end, int color) {
		Point startPos = getXYPos(start);
		Point endPos = getXYPos(end);
		if (startPos.y == endPos.y)
			MVDrawableHelper.fill(matrices, startPos.x, startPos.y, endPos.x, endPos.y + textRenderer.lineHeight, color);
		else {
			int line = 0;
			int lineY;
			while ((lineY = startPos.y + line * (int) (textRenderer.lineHeight * 1.5)) < endPos.y) {
				Point lineStart = line == 0 ? startPos : new Point(x + textRenderer.lineHeight, lineY);
				MVDrawableHelper.fill(matrices, lineStart.x, lineStart.y, x + width - textRenderer.lineHeight, lineStart.y + textRenderer.lineHeight, color);
				line++;
			}
			MVDrawableHelper.fill(matrices, x + textRenderer.lineHeight, lineY, endPos.x, endPos.y + textRenderer.lineHeight, color);
		}
	}
	
	@Override
	public void tick() {
		cursorBlinkTracker++;
	}
	
	protected void generateLines() {
		lines.clear();
		renderedLines.clear();
		
		String text = this.text;
		Component formattedText = (formatter == null ? TextInst.of(text) : formatter.apply(text));
		boolean endsWithNewLine = false;
		while (!text.isEmpty()) {
			if (text.charAt(0) == '\n') {
				endsWithNewLine = true;
				lines.add(TextInst.of("\n"));
				renderedLines.add(TextInst.of(""));
				text = text.substring(1);
				formattedText = TextUtil.substring(formattedText, 1);
				continue;
			}
			int charPos = 1;
			while (textRenderer.width(TextUtil.substring(formattedText, 0, charPos)) < width - textRenderer.lineHeight * 2) {
				charPos++;
				if (text.length() < charPos || text.charAt(charPos - 1) == '\n')
					break;
			}
			endsWithNewLine = charPos - 1 < text.length() && text.charAt(charPos - 1) == '\n';
			int extraPos = charPos - 1 + (endsWithNewLine ? 1 : 0);
			lines.add(TextUtil.substring(formattedText, 0, extraPos));
			renderedLines.add(TextUtil.substring(formattedText, 0, charPos - 1));
			text = text.substring(extraPos);
			formattedText = TextUtil.substring(formattedText, extraPos);
		}
		if (endsWithNewLine) {
			Component emptyLine = TextInst.of("");
			lines.add(emptyLine);
			renderedLines.add(emptyLine);
		}
	}
	
	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (suggestor != null) {
			syncToSuggestor();
			if (suggestor.mouseClicked(click, doubled)) {
				syncFromSuggestor();
				return true;
			}
		}
		if (!isMouseOver(click.x(), click.y()))
			return false;
		
		if (scrollBar.mouseClicked(click, doubled))
			return true;
		
		setCursor(getCharPos(click.x(), click.y() - scroll), true);
		cursorX = -1;
		return true;
	}
	
	@Override
	public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
		if (scrollBar.mouseDragged(click, deltaX, deltaY))
			return true;
		if (!isMouseOver(click.x(), click.y()))
			return false;
		
		int selEnd = getCharPos(click.x(), click.y() - scroll);
		onCursorMove(selEnd, Math.min(selStart, selEnd), Math.max(selStart, selEnd));
		this.selEnd = selEnd;
		cursor = selEnd;
		cursorX = -1;
		return true;
	}
	public int getSelStart() {
		return Math.min(selStart, selEnd);
	}
	public int getSelEnd() {
		return Math.max(selStart, selEnd);
	}
	
	public int getCursor() {
		return cursor;
	}
	
	private int getCharPos(int mouseX, int mouseY) {
		if (lines.isEmpty())
			return 0;
		
		int line = (mouseY - y - textRenderer.lineHeight) / (int) (textRenderer.lineHeight * 1.5);
		if (line >= lines.size())
			line = lines.size() - 1;
		
		Component lineValue = renderedLines.get(line);
		int lineLen = lineValue.getString().length();
		int charPos = 0;
		while (textRenderer.width(TextUtil.substring(lineValue, 0, charPos)) < mouseX - x - textRenderer.lineHeight) {
			charPos++;
			if (charPos > lineLen)
				break;
		}
		if (charPos != 0)
			charPos--;
		
		for (int i = 0; i < line; i++)
			charPos += lines.get(i).getString().length();
		
		return charPos;
	}
	private int getCharPos(double mouseX, double mouseY) {
		return getCharPos((int) mouseX, (int) mouseY);
	}
	private Point getXYPos(int charPos) {
		int lineX = 0;
		int lineY = 0;
		if (charPos >= text.length()) {
			lineX = lines.isEmpty() ? 0 : textRenderer.width(lines.get(lines.size() - 1));
			lineY = lines.isEmpty() ? 0 : lines.size() - 1;
		} else {
			int i = 0;
			for (Component line : lines) {
				int lineLen = line.getString().length();
				if (lineLen <= charPos) {
					lineY++;
					charPos -= lineLen;
				} else {
					lineX = textRenderer.width(TextUtil.substring(lines.get(i), 0, charPos));
					break;
				}
				i++;
			}
		}
		
		return new Point(lineX + textRenderer.lineHeight + x, lineY * (int) (textRenderer.lineHeight * 1.5) + textRenderer.lineHeight + y);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double xAmount, double yAmount) {
		if (suggestor != null) {
			syncToSuggestor();
			if (suggestor.mouseScrolled(mouseX, mouseY, xAmount, yAmount))
				return true;
		}
		if (!isMouseOver(mouseX, mouseY))
			return false;
		
		return scrollBar.mouseScrolled(mouseX, mouseY, xAmount, yAmount);
	}
	private int getMaxScroll() {
		return -Math.max(0, lines.size() * (int) (textRenderer.lineHeight * 1.5) +
				textRenderer.lineHeight + (overscroll ? height / 3 : 0) - height);
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}
	
	public void setCursor(int cursor, boolean select) {
		int selStart = this.selStart;
		int selEnd = this.selEnd;
		if (select && NBTEditor.hasShiftDown())
			selEnd = cursor;
		else {
			selStart = cursor;
			selEnd = cursor;
		}
		onCursorMove(cursor, Math.min(selStart, selEnd), Math.max(selStart, selEnd));
		this.cursor = cursor;
		this.selStart = selStart;
		this.selEnd = selEnd;
	}
	public void setCursor(int cursor) {
		setCursor(cursor, false);
	}
	
	private void write(String text) {
		while (undoPos > 0) {
			undo.remove(0);
			undoPos--;
		}
		
		text = MVMisc.stripInvalidChars(text, newLines);
		onEdit(text, getSelStart(), getSelEnd() - getSelStart());
		this.text = new StringBuilder(this.text).replace(getSelStart(), getSelEnd(), text).toString();
		setCursor(getSelStart() + text.length());
		undo.add(0, Map.entry(this.text, cursor));
		onChange.accept(this.text);
	}
	
	public String getSelectedText() {
		return text.substring(getSelStart(), getSelEnd());
	}
	
	private void moveCursorUp() {
		Point pos = getXYPos(cursor);
		if (cursorX == -1)
			cursorX = pos.x;
		if (pos.y == getXYPos(0).y) {
			setCursor(0, true);
			cursorX = -1;
		} else
			setCursor(getCharPos(cursorX, pos.y - (int) (textRenderer.lineHeight * 1.5)), true);
	}
	private void moveCursorDown() {
		Point pos = getXYPos(cursor);
		if (cursorX == -1)
			cursorX = pos.x;
		if (pos.y == getXYPos(text.length()).y) {
			setCursor(text.length(), true);
			cursorX = -1;
		} else
			setCursor(getCharPos(cursorX, pos.y + (int) (textRenderer.lineHeight * 1.5)), true);
	}
	
	@Override
	public boolean keyPressed(KeyEvent keyInput) {
		int keyCode = keyInput.key();
		if (suggestor != null && (keyCode != GLFW.GLFW_KEY_UP && keyCode != GLFW.GLFW_KEY_DOWN || NBTEditor.hasAltDown())) {
			syncToSuggestor();
			if (suggestor.keyPressed(keyInput)) {
				syncFromSuggestor();
				return true;
			}
		}
		if (NBTEditor.isSelectAll(keyCode)) {
			onCursorMove(text.length(), 0, text.length());
			selStart = 0;
			selEnd = text.length();
			cursor = selEnd;
			cursorX = -1;
			return true;
		}
		if (NBTEditor.isCopy(keyCode)) {
			MainUtil.client.keyboardHandler.setClipboard(onCopy(getSelectedText(), getSelStart(), getSelEnd() - getSelStart()));
			return true;
		}
		if (NBTEditor.isPaste(keyCode)) {
			this.write(pasteFilter(onPaste(MainUtil.client.keyboardHandler.getClipboard(), getSelStart(), getSelEnd() - getSelStart())));
			cursorX = -1;
			return true;
		}
		if (NBTEditor.isCut(keyCode)) {
			MainUtil.client.keyboardHandler.setClipboard(onCopy(getSelectedText(), getSelStart(), getSelEnd() - getSelStart()));
			this.write("");
			cursorX = -1;
			return true;
		}
		if (isUndo(keyCode)) {
			if (undoPos < undo.size() - 1) {
				Map.Entry<String, Integer> undoData = undo.get(++undoPos);
				onUndo(undoData.getKey());
				text = undoData.getKey();
				setCursor(undoData.getValue());
				cursorX = -1;
				onChange.accept(text);
			}
			return true;
		}
		if (isRedo(keyCode)) {
			if (undoPos > 0) {
				Map.Entry<String, Integer> undoData = undo.get(--undoPos);
				onRedo(undoData.getKey());
				text = undoData.getKey();
				setCursor(undoData.getValue());
				cursorX = -1;
				onChange.accept(text);
			}
			return true;
		}
		if (isFind(keyCode)) {
			OverlaySupportingScreen.setOverlayStatic(new FindAndReplaceWidget());
			return true;
		}
		switch (keyCode) {
			case GLFW.GLFW_KEY_LEFT: {
				if (NBTEditor.hasControlDown()) {
					this.setCursor(this.getWordSkipPosition(true, false), true);
				} else {
					this.moveCursor(-1);
				}
				cursorX = -1;
				return true;
			}
			case GLFW.GLFW_KEY_RIGHT: {
				if (NBTEditor.hasControlDown()) {
					this.setCursor(this.getWordSkipPosition(false, false), true);
				} else {
					this.moveCursor(1);
				}
				cursorX = -1;
				return true;
			}
			case GLFW.GLFW_KEY_UP: {
				if (NBTEditor.hasControlDown())
					setCursor(0, true);
				else
					moveCursorUp();
				return true;
			}
			case GLFW.GLFW_KEY_DOWN: {
				if (NBTEditor.hasControlDown())
					setCursor(text.length(), true);
				else
					moveCursorDown();
				return true;
			}
			case GLFW.GLFW_KEY_BACKSPACE: {
				this.erase(true);
				cursorX = -1;
				return true;
			}
			case GLFW.GLFW_KEY_DELETE: {
				this.erase(false);
				cursorX = -1;
				return true;
			}
			case GLFW.GLFW_KEY_HOME: {
				setCursor(0, true);
				cursorX = -1;
				return true;
			}
			case GLFW.GLFW_KEY_END: {
				setCursor(text.length(), true);
				cursorX = -1;
				return true;
			}
			case GLFW.GLFW_KEY_ENTER: {
				if (newLines && getNumNewLines(text) + 1 < maxLines) {
					write("\n");
					cursorX = -1;
				}
				return true;
			}
		}
		return false;
	}
	protected String pasteFilter(String toPaste) {
		toPaste = MVMisc.stripInvalidChars(toPaste, newLines);
		int numNewLines = getNumNewLines(text);
		int toPasteNewLines = getNumNewLines(toPaste);
		while (numNewLines + toPasteNewLines + 1 > maxLines) {
			int i = toPaste.lastIndexOf('\n');
			if (i == -1)
				break;
			toPaste = new StringBuilder(toPaste).deleteCharAt(i).toString();
			toPasteNewLines--;
		}
		return toPaste;
	}
	protected int getNumNewLines(String str) {
		int numNewLines = 0;
		for (char c : str.toCharArray()) {
			if (c == '\n')
				numNewLines++;
		}
		return numNewLines;
	}
	
	public static boolean isUndo(int code) {
		return code == GLFW.GLFW_KEY_Z && NBTEditor.hasControlDown() && !NBTEditor.hasShiftDown() && !NBTEditor.hasAltDown();
	}
	public static boolean isRedo(int code) {
		return code == GLFW.GLFW_KEY_Y && NBTEditor.hasControlDown() && !NBTEditor.hasShiftDown() && !NBTEditor.hasAltDown();
	}
	public static boolean isFind(int code) {
		return code == GLFW.GLFW_KEY_F && NBTEditor.hasControlDown() && !NBTEditor.hasShiftDown() && !NBTEditor.hasAltDown();
	}
	
	// passOneSpace requires that one section of whitespace is passed, either at the end or beginning of the search
	// if false, any beginning whitespace is passed, then everything until, but not including, the end whitespace is passed
	private int getWordSkipPosition(boolean backward, boolean passOneSpace) {
		boolean findingText = true;
		boolean foundSpace = false;
		boolean findingText2 = false;
		for (int i = cursor + (backward ? -1 : 0); 0 <= i && i < text.length(); i += (backward ? -1 : 1)) {
			char c = text.charAt(i);
			if (c == ' ' || c == '\n') {
				if (findingText2)
					;
				else if (findingText)
					foundSpace = true;
				else if (foundSpace || !passOneSpace) {
					if (backward)
						return i + 1;
					return i;
				} else
					findingText2 = true;
			} else if (findingText2) {
				if (backward)
					return i + 1;
				return i;
			} else
				findingText = false;
		}
		if (backward)
			return 0;
		return text.length();
	}
	
	private void moveCursor(int offset) {
		this.setCursor(this.getCursorPosWithOffset(offset), true);
	}
	
	private int getCursorPosWithOffset(int offset) {
		return Util.offsetByCodepoints(this.text, NBTEditor.hasShiftDown() ? cursor : (offset > 0 ? getSelEnd() : getSelStart()), offset);
	}
	
	private void erase(boolean backwards) {
		if (NBTEditor.hasControlDown()) {
			this.eraseWords(backwards);
		} else {
			this.eraseCharacters(backwards ? -1 : 1);
		}
	}
	private void eraseWords(boolean backwards) {
		if (this.text.isEmpty()) {
			return;
		}
		if (selStart != selEnd) {
			this.write("");
			return;
		}
		this.eraseCharacters(this.getWordSkipPosition(backwards, true) - getSelStart());
	}
	private void eraseCharacters(int characterOffset) {
		while (undoPos > 0) {
			undo.remove(0);
			undoPos--;
		}
		
		int k;
		if (this.text.isEmpty()) {
			return;
		}
		if (selStart != selEnd) {
			this.write("");
			return;
		}
		int i = this.getCursorPosWithOffset(characterOffset);
		int j = Math.min(i, getSelStart());
		if (j == (k = Math.max(i, getSelStart()))) {
			return;
		}
		onEdit("", j, k - j);
		String string = new StringBuilder(this.text).delete(j, k).toString();
		this.text = string;
		this.setCursor(j);
		undo.add(0, Map.entry(this.text, cursor));
		onChange.accept(this.text);
	}
	
	@Override
	public boolean charTyped(CharacterEvent charInput) {
		if (MVMisc.isValidChar((char) charInput.codepoint())) {
			this.write(Character.toString(charInput.codepoint()));
			cursorX = -1;
			return true;
		}
		return false;
	}
	
	
	protected void onCursorMove(int cursor, int selStart, int selEnd) {}
	protected void onEdit(String insertedText, int pos, int overwrittenLen) {}
	protected void onUndo(String newText) {}
	protected void onRedo(String newText) {}
	protected void onUndoDiscard() {}
	protected String onCopy(String text, int pos, int len) {
		return text;
	}
	protected String onPaste(String text, int pos, int overwrittenLen) {
		return text;
	}
	protected void markUndo() {
		while (undoPos > 0) {
			undo.remove(0);
			undoPos--;
		}
		undo.add(0, Map.entry(this.text, cursor));
	}
	
	
	
	@Override
	public NarrationPriority narrationPriority() {
		return NarrationPriority.NONE;
	}
	
	@Override
	public void updateNarration(NarrationElementOutput var1) {
		
	}
	
}
