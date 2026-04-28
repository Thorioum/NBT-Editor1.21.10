package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import com.luneruniverse.minecraft.mod.nbteditor.mixin.EditBoxMixin;
import com.luneruniverse.minecraft.mod.nbteditor.screens.Tickable;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix3x2fStack;

public class MVTextFieldWidget extends EditBox implements Tickable, MVElement {
	
	/**
	 * The selection highlight doesn't move when {@link PoseStack#translate(double, double, double)} is called <br />
	 * Via {@link EditBoxMixin}, the vertex calls are redirected to take this matrix into account
	 * As of 1.19.4, this is fixed
	 */

	protected MVTooltip tooltip;
	
	public MVTextFieldWidget(int x, int y, int width, int height, EditBox copyFrom) {
		super(MainUtil.client.font, x, y, width, height, copyFrom, TextInst.of(""));
	}
	public MVTextFieldWidget(int x, int y, int width, int height) {
		super(MainUtil.client.font, x, y, width, height, TextInst.of(""));
	}
	
	public MVTextFieldWidget tooltip(MVTooltip tooltip) {
		this.tooltip = tooltip;
		Version.newSwitch()
				.range("1.19.3", null, () -> setTooltip(tooltip == null ? null : tooltip.toNewTooltip()))
				.range(null, "1.19.2", () -> {})
				.run();
		return this;
	}
	
	
	public void extractRenderState(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		super.extractWidgetRenderState(MVDrawableHelper.getDrawContext(matrices),mouseX,mouseY,delta);
	}
	public final void method_25394(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		extractRenderState(matrices, mouseX, mouseY, delta);
	}
	@Override
	public final void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		extractRenderState(MVDrawableHelper.getMatrices(context), mouseX, mouseY, delta);
	}
	
	public void method_25352(Matrix3x2fStack matrices, int mouseX, int mouseY) { // renderTooltip
		if (tooltip != null)
			tooltip.render(matrices, mouseX, mouseY);
	}
	
	@Override
	@Deprecated
	public void setFocused(boolean focused) {
		setMultiFocused(focused);
	}
	@Override
	@Deprecated
	public boolean isFocused() {
		return isMultiFocused();
	}

	@Override
	public void tick() {

	}
}
