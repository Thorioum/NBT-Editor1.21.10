package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

public class MVScreen extends Screen implements OldEventBehavior, IgnoreCloseScreenPacket {
	
	protected MVScreen(Text title) {
		super(title);
	}
	
	@Override
	public void render(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		MVDrawableHelper.super_render(MVScreen.class, this, matrices, mouseX, mouseY, delta);
	}
	public final void method_25394(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		render(matrices, mouseX, mouseY, delta);
	}
	@Override
	public final void render(DrawContext context, int mouseX, int mouseY, float delta) {
		render(MVDrawableHelper.getMatrices(context), mouseX, mouseY, delta);
	}
	
	// Needed for some reason ...
	// Copied from MultiVersionScreenParent
	// Prevents crash in 1.17 that's trying to find this method
	@Override
	public void renderBackground(Matrix3x2fStack matrices) {
		MVDrawableHelper.renderBackground((Screen) this, matrices);
	}
	
	public void setInitialFocus(Element element) {
		MVMisc.setInitialFocus(this, element, super::setInitialFocus);
	}
	@Override
	protected void setInitialFocus() {}
	
}
