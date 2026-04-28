package com.luneruniverse.minecraft.mod.nbteditor.screens.widgets;

import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.client.input.KeyEvent;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.ScreenTexts;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.OverlayScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.OverlaySupportingScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;

public class ImportPosWidget extends GroupWidget implements InitializableOverlay<Screen> {
	
	public static void openImportPos(BlockPos defaultPos, Consumer<BlockPos> posConsumer) {
		OverlayScreen.setOverlayOrScreen(new ImportPosWidget(defaultPos, optional -> {
			OverlaySupportingScreen.setOverlayStatic(null);
			optional.ifPresent(posConsumer);
		}), 500, true);
	}
	
	public static record ImageToLoreOptions(Integer width, Integer height) {}
	
	private final BlockPos defaultPos;
	private final Consumer<Optional<BlockPos>> posConsumer;
	private final Font textRenderer;
	private int width;
	private int height;
	private NamedTextFieldWidget x;
	private NamedTextFieldWidget y;
	private NamedTextFieldWidget z;
	
	public ImportPosWidget(BlockPos defaultPos, Consumer<Optional<BlockPos>> posConsumer) {
		this.defaultPos = defaultPos;
		this.posConsumer = posConsumer;
		this.textRenderer = MainUtil.client.font;
	}
	
	@Override
	public void init(Screen parent, int width, int height) {
		clearWidgets();
		
		this.width = width;
		this.height = height;
		
		boolean firstInit = (x == null);
		
		x = addWidget(new NamedTextFieldWidget(width / 2 - 102, height / 2 - 18, 65, 16, x)
				.name(TextInst.translatable("nbteditor.nbt.import.pos.x")));
		y = addWidget(new NamedTextFieldWidget(width / 2 - 33, height / 2 - 18, 65, 16, y)
				.name(TextInst.translatable("nbteditor.nbt.import.pos.y")));
		z = addWidget(new NamedTextFieldWidget(width / 2 + 36, height / 2 - 18, 66, 16, z)
				.name(TextInst.translatable("nbteditor.nbt.import.pos.z")));
		
		//x.setFilter(MainUtil.intPredicate());
		//y.setFilter(MainUtil.intPredicate());
		//z.setFilter(MainUtil.intPredicate());
		
		if (firstInit) {
			x.setValue("" + defaultPos.getX());
			y.setValue("" + defaultPos.getY());
			z.setValue("" + defaultPos.getZ());
		}
		
		addWidget(MVMisc.newButton(width / 2 - 102, height / 2 + 2, 100, 20, ScreenTexts.DONE, btn -> done()));
		addWidget(MVMisc.newButton(width / 2 + 2, height / 2 + 2, 100, 20, ScreenTexts.CANCEL, btn -> posConsumer.accept(Optional.empty())));
	}
	
	@Override
	public void extractRenderState(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		MainUtil.client.screen.extractBackground(MVDrawableHelper.getDrawContext(matrices),mouseX, mouseY, delta);
		super.extractRenderState(matrices, mouseX, mouseY, delta);
		MVDrawableHelper.drawCenteredTextWithShadow(matrices, textRenderer, TextInst.translatable("nbteditor.nbt.import.pos"),
				width / 2, height / 2 - textRenderer.lineHeight - 22, -1);
		MainUtil.renderLogo(matrices);
	}
	
	@Override
	public boolean keyPressed(KeyEvent keyInput) {
		int keyCode = keyInput.key();
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			OverlaySupportingScreen.setOverlayStatic(null);
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_ENTER) {
			done();
			return true;
		}
		
		return super.keyPressed(keyInput);
	}
	
	private void done() {
		int xValue = MainUtil.parseDefaultInt(x.getValue(), defaultPos.getX());
		int yValue = MainUtil.parseDefaultInt(y.getValue(), defaultPos.getY());
		int zValue = MainUtil.parseDefaultInt(z.getValue(), defaultPos.getZ());
		posConsumer.accept(Optional.of(new BlockPos(xValue, yValue, zValue)));
	}
	
}
