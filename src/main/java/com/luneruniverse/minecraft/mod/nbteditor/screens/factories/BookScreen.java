package com.luneruniverse.minecraft.mod.nbteditor.screens.factories;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.input.KeyEvent;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItem;
import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.EditableText;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTextEvents;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.LocalEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigValueDropdown;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.AlertWidget;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.FormattedTextFieldWidget;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.GroupWidget;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.ImageToLoreWidget;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.NamedTextFieldWidget;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.TranslatedGroupWidget;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.WrittenBookTagReferences;

import net.minecraft.client.gui.screens.inventory.BookViewScreen.BookAccess;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class BookScreen extends LocalEditorScreen<LocalItem> {
	
	private enum Generation {
		ORIGINAL,
		COPY_OF_ORIGINAL,
		COPY_OF_COPY,
		TATTERED;
		
		@Override
		public String toString() {
			return TextInst.translatable("book.generation." + ordinal()).getString();
		}
	}
	
	private int page;
	private GroupWidget group;
	private NamedTextFieldWidget title;
	private NamedTextFieldWidget author;
	private GroupWidget gen;
	private FormattedTextFieldWidget contents;
	
	public BookScreen(ItemReference ref, int page) {
		super(TextInst.of("Book"), ref);
		this.page = page;
	}
	public BookScreen(ItemReference ref) {
		this(ref, 0);
	}
	
	private String getBookTitle() {
		return WrittenBookTagReferences.TITLE.get(localNBT.getEditableItem());
	}
	private void setBookTitle(String title) {
		WrittenBookTagReferences.TITLE.set(localNBT.getEditableItem(), title);
		checkSave();
	}
	
	private String getAuthor() {
		return WrittenBookTagReferences.AUTHOR.get(localNBT.getEditableItem());
	}
	private void setAuthor(String author) {
		WrittenBookTagReferences.AUTHOR.set(localNBT.getEditableItem(), author);
		checkSave();
	}
	
	private Generation getGeneration() {
		int gen = WrittenBookTagReferences.GENERATION.get(localNBT.getEditableItem());
		if (gen < 0 || gen >= 4)
			return Generation.TATTERED;
		return Generation.values()[gen];
	}
	private void setGeneration(Generation gen) {
		WrittenBookTagReferences.GENERATION.set(localNBT.getEditableItem(), gen.ordinal());
		checkSave();
	}
	
	private int getPageCount() {
		return WrittenBookTagReferences.PAGES.get(localNBT.getEditableItem()).size();
	}
	private Component getPage() {
		List<Component> pages = WrittenBookTagReferences.PAGES.get(localNBT.getEditableItem());
		return page < pages.size() ? pages.get(page) : TextInst.of("");
	}
	private void setPage(Component contents) {
		List<Component> pages = WrittenBookTagReferences.PAGES.get(localNBT.getEditableItem());
		if (page < pages.size())
			pages.set(page, contents);
		else {
			while (page > pages.size())
				pages.add(TextInst.of(""));
			pages.add(contents);
		}
		WrittenBookTagReferences.PAGES.set(localNBT.getEditableItem(), pages);
		
		checkSave();
	}
	
	private void addPage() {
		List<Component> pages = WrittenBookTagReferences.PAGES.get(localNBT.getEditableItem());
		if (page < pages.size()) {
			pages.add(page, TextInst.of(""));
			WrittenBookTagReferences.PAGES.set(localNBT.getEditableItem(), pages);
			checkSave();
			refresh();
		}
	}
	private void removePage() {
		List<Component> pages = WrittenBookTagReferences.PAGES.get(localNBT.getEditableItem());
		if (page < pages.size()) {
			pages.remove(page);
			WrittenBookTagReferences.PAGES.set(localNBT.getEditableItem(), pages);
			checkSave();
			refresh();
		}
	}
	
	private void back() {
		if (page > 0) {
			page--;
			refresh();
		}
	}
	private void forward() {
		page++;
		refresh();
	}
	
	private void refresh() {
		contents = null;
		clearWidgets();
		init();
	}
	
	private BookAccess getPreviewItem() {
		List<Component> pages = WrittenBookTagReferences.PAGES.get(localNBT.getEditableItem());
		pages.replaceAll(this::makePreviewText);
		return MVMisc.getBookContents(pages);
	}
	private Component makePreviewText(Component text) {
		EditableText output = TextInst.copy(text);
		output.setStyle(makePreviewStyle(output.getStyle()));
		output.getSiblings().replaceAll(this::makePreviewText);
		return output;
	}
	private Style makePreviewStyle(Style style) {
		if (style.getClickEvent() == null)
			return style;
		return MixinLink.withRunClickEvent(style, () -> {
			MVTextEvents.ClickAction<?> clickAction = MVTextEvents.ClickAction.getAction(style.getClickEvent());
			net.minecraft.client.gui.screens.inventory.BookViewScreen preview = getOverlay();
			setOverlay(new AlertWidget(
					() -> setOverlayScreen(preview, 500),
					TextInst.translatable("nbteditor.book.preview.click.title"),
					TextInst.of(""),
					TextInst.translatable("nbteditor.book.preview.click.action", clickAction.getName()),
					TextInst.of(""),
					TextInst.translatable("nbteditor.book.preview.click.value", clickAction.getStringifiedValue(style.getClickEvent()))),
					500);
		});
	}
	
	@Override
	protected void initEditor() {
		MVMisc.setKeyboardRepeatEvents(true);
		
		group = new GroupWidget();
		addRenderableWidget(group);
		
		title = group.addWidget(new NamedTextFieldWidget(16, 64 + 2, 100, 16)
				.name(TextInst.translatable("nbteditor.book.title")));
		title.setMaxLength(32);
		title.setValue(getBookTitle());
		title.setResponder(this::setBookTitle);
		
		author = group.addWidget(new NamedTextFieldWidget(16 + 108, 64 + 2, 100, 16)
				.name(TextInst.translatable("nbteditor.book.author")));
		author.setMaxLength(Integer.MAX_VALUE);
		author.setValue(getAuthor());
		author.setResponder(this::setAuthor);
		
		gen = group.addElement(TranslatedGroupWidget.forWidget(
				ConfigValueDropdown.forEnum(getGeneration(), Generation.ORIGINAL, Generation.class)
						.addValueListener(value -> setGeneration(value.getValidValue())), 16 + 108 * 2, 64, 0));
		
		group.addWidget(MVMisc.newButton(16 + 108 * 3 - 4, 64, 20, 20,
				TextInst.translatable("nbteditor.book.add"), btn -> addPage()));
		group.addWidget(MVMisc.newButton(16 + 108 * 3 + 20, 64, 20, 20,
				TextInst.translatable("nbteditor.book.remove"), btn -> removePage()));
		group.addWidget(MVMisc.newButton(16 + 108 * 3 + 44, 64, 20, 20,
				TextInst.translatable("nbteditor.book.preview.icon"),
				btn -> {
					net.minecraft.client.gui.screens.inventory.BookViewScreen preview =
							new net.minecraft.client.gui.screens.inventory.BookViewScreen(getPreviewItem()) {
						@Override
						public boolean keyPressed(KeyEvent keyInput) {
							if (keyInput.key() == GLFW.GLFW_KEY_ESCAPE) {
								setOverlay(null);
								return true;
							}
							return super.keyPressed(keyInput);
						}
					};
					setOverlayScreen(preview, 200);
					preview.setPage(page);
				},
				new MVTooltip("nbteditor.book.preview")));
		
		contents = group.addWidget(FormattedTextFieldWidget.create(contents, 16 + 24, 64 + 24, width - 32 - 24 * 2,
				height - 80 - 24, getPage(), true, Style.EMPTY.withColor(ChatFormatting.BLACK), this::setPage));
		contents.setBackgroundColor(0xFFFDF8EB);
		contents.setCursorColor(0xFF000000);
		contents.setSelectionColor(0x55000000);
		contents.setShadow(false);
		
		group.addDrawable(gen);
		
		EditableText prevKeybind = TextInst.translatable("nbteditor.keybind.page.down");
		EditableText nextKeybind = TextInst.translatable("nbteditor.keybind.page.up");
		if (ConfigScreen.isInvertedPageKeybinds()) {
			EditableText temp = prevKeybind;
			prevKeybind = nextKeybind;
			nextKeybind = temp;
		}
		
		group.addWidget(MVMisc.newButton(16, 64 + 24, 20, height - 80 - 24,
				TextInst.translatable("nbteditor.book.back"), btn -> back(),
				ConfigScreen.isKeybindsHidden() ? null : new MVTooltip(TextInst.literal("")
						.append(prevKeybind).append(TextInst.translatable("nbteditor.keybind.page.prev")))))
				.active = (page > 0);
		group.addWidget(MVMisc.newButton(width - 16 - 20, 64 + 24, 20, height - 80 - 24,
				TextInst.translatable("nbteditor.book.forward"), btn -> forward(),
				ConfigScreen.isKeybindsHidden() ? null : new MVTooltip(TextInst.literal("")
						.append(nextKeybind).append(TextInst.translatable("nbteditor.keybind.page.next")))));
	}
	
	@Override
	protected void renderEditor(Matrix3x2fStack matrices, int fdf8eb, int mouseY, float delta) {
		MVDrawableHelper.drawTextWithShadow(matrices, font, TextInst.translatable("nbteditor.book.page", page + 1, getPageCount()),
				16 + 108 * 3 - 4 + 24 * 3, 64 + 10 - font.lineHeight / 2, -1);
	}
	
	@Override
	public boolean keyPressed(KeyEvent keyInput) {
		int keyCode = keyInput.key();
		if (getOverlay() != null)
			return super.keyPressed(keyInput);
		if (super.keyPressed(keyInput))
			return true;
		
		if (keyCode == GLFW.GLFW_KEY_PAGE_UP || keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
			boolean prev = (keyCode == GLFW.GLFW_KEY_PAGE_DOWN);
			if (ConfigScreen.isInvertedPageKeybinds())
				prev = !prev;
			if (prev)
				back();
			else
				forward();
			return true;
		}
		
		return false;
	}
	
	@Override
	public void onFilesDrop(List<Path> paths) {
		List<Component> lines = new ArrayList<>();
		lines.add(getPage());
		ImageToLoreWidget.openImportFiles(paths, (file, imgLines) -> lines.addAll(imgLines), () -> {
			if (lines.size() > 1)
				contents.setText(lines);
		});
	}
	
	@Override
	public void removed() {
		MVMisc.setKeyboardRepeatEvents(false);
	}
	
}
