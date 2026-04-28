package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import org.joml.Matrix3x2fStack;

public interface MVDrawable extends Renderable {
	@Override
	public default void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		extractRenderState(MVDrawableHelper.getMatrices(context), mouseX, mouseY, delta);
	}
	public default void method_25394(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		extractRenderState(matrices, mouseX, mouseY, delta);
	}
	public void extractRenderState(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta);
}
