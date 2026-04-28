package com.luneruniverse.minecraft.mod.nbteditor.screens.widgets;

import java.util.Arrays;

import net.minecraft.client.input.KeyEvent;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.TextUtil;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AlertWidget extends GroupWidget implements InitializableOverlay<Screen> {
	
	private final Runnable onClose;
	private final Component[] lines;
	private int x;
	private int y;
	
	public AlertWidget(Runnable onClose, Component... lines) {
		this.onClose = onClose;
		this.lines = Arrays.stream(lines).flatMap(line -> TextUtil.splitText(line).stream()).toArray(Component[]::new);
	}
	
	@Override
	public void init(Screen parent, int width, int height) {
		clearWidgets();
		
		x = width / 2;
		y = height / 2 - lines.length * MainUtil.client.font.lineHeight / 2;
		
		addWidget(MVMisc.newButton(width / 2 - 50, height - 28, 100, 20, TextInst.translatable("nbteditor.ok"), btn -> {
			onClose.run();
		}));
	}
	
	@Override
	public void extractRenderState(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		MainUtil.client.screen.extractBackground(MVDrawableHelper.getDrawContext(matrices), mouseX, mouseY, delta);
		for (int i = 0; i < lines.length; i++) {
			MVDrawableHelper.drawCenteredTextWithShadow(matrices, MainUtil.client.font, lines[i],
					x, y + i * MainUtil.client.font.lineHeight, -1);
		}
		super.extractRenderState(matrices, mouseX, mouseY, delta);
		MainUtil.renderLogo(matrices);
	}
	
	@Override
	public boolean keyPressed(KeyEvent keyInput) {
		int keyCode = keyInput.key();
		if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
			onClose.run();
			return true;
		}
		
		return super.keyPressed(keyInput);
	}
	
}
