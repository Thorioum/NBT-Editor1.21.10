package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix3x2fStack;

public interface MVDrawable extends Drawable {
	@Override
	public default void render(DrawContext context, int mouseX, int mouseY, float delta) {
		render(MVDrawableHelper.getMatrices(context), mouseX, mouseY, delta);
	}
	public default void method_25394(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		render(matrices, mouseX, mouseY, delta);
	}
	public void render(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta);
}
