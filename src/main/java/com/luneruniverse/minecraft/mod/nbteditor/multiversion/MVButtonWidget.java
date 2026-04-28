package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.input.InputWithModifiers;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;

public class MVButtonWidget extends AbstractButton {
	
	@FunctionalInterface
	public interface PressAction {
		public void onPress(MVButtonWidget button);
	}
	
	private final PressAction onPress;
	private final MVTooltip tooltip;
	
	public MVButtonWidget(int x, int y, int width, int height, Component text, PressAction onPress, MVTooltip tooltip) {
		super(x, y, width, height, text);
		this.onPress = onPress;
		this.tooltip = tooltip;
		if (tooltip != null) {
			Version.newSwitch()
					.range("1.19.3", null, () -> setTooltip(tooltip.toNewTooltip()))
					.range(null, "1.19.2", () -> {})
					.run();
		}
	}
	public MVButtonWidget(int x, int y, int width, int height, Component text, PressAction onPress) {
		this(x, y, width, height, text, onPress, null);
	}
	
	@Override
	public void onPress(InputWithModifiers a) {
		onPress.onPress(this);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {
		defaultButtonNarrationText(builder);
	}
	
	public void extractRenderState(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		super.extractRenderState(MVDrawableHelper.getDrawContext(matrices),mouseX,mouseY,delta);
	}
	public void method_25394(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		extractRenderState(matrices, mouseX, mouseY, delta);
	}


	public void renderButton(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		this.extractDefaultSprite(MVDrawableHelper.getDrawContext(matrices));
		this.extractDefaultLabel(MVDrawableHelper.getDrawContext(matrices).textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
	}
	@Override
	public void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		renderButton(MVDrawableHelper.getMatrices(context), mouseX, mouseY, delta);
	}

	
}
