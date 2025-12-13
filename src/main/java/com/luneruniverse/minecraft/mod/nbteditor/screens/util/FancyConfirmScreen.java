package com.luneruniverse.minecraft.mod.nbteditor.screens.util;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IgnoreCloseScreenPacket;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

public class FancyConfirmScreen extends ConfirmScreen implements IgnoreCloseScreenPacket {
	
	private Screen parent;
	
	public FancyConfirmScreen(BooleanConsumer callback, Text title, Text message, Text yesTranslated, Text noTranslated) {
		super(callback, title, message, yesTranslated, noTranslated);
		parent = MainUtil.client.currentScreen;
	}
	public FancyConfirmScreen(BooleanConsumer callback, Text title, Text message) {
		super(callback, title, message);
		parent = MainUtil.client.currentScreen;
	}
	
	public FancyConfirmScreen setParent(Screen parent) {
		this.parent = parent;
		return this;
	}
	
	@Override
	protected void init() {
		if (parent != null)
			parent.init(client, width, height);
		super.init();
	}
	
	@Override
	public void render(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		if (parent != null)
			parent.render(matrices, -314, -314, delta);
		
		matrices.pushMatrix();
		matrices.translate(0.0f, 0.0f);
		MVDrawableHelper.super_render(FancyConfirmScreen.class, this, matrices, mouseX, mouseY, delta);
		MainUtil.renderLogo(matrices);
		matrices.popMatrix();
	}
	public final void method_25394(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		render(matrices, mouseX, mouseY, delta);
	}
	@Override
	public final void render(DrawContext context, int mouseX, int mouseY, float delta) {
		render(MVDrawableHelper.getMatrices(context), mouseX, mouseY, delta);
	}
	
	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		if (MainUtil.client.world == null)
			super.renderBackground(context, mouseX, mouseY, delta);
		else
			renderInGameBackground(context);
	}
	
}
