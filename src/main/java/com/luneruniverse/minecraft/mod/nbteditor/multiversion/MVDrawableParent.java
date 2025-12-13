package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix3x2fStack;

public interface MVDrawableParent {
	public default void render(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		MVDrawableHelper.render((Drawable) this, matrices, mouseX, mouseY, delta);
	}
}
