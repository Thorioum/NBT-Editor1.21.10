package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.input.AbstractInput;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

public class MVButtonWidget extends PressableWidget {
	
	@FunctionalInterface
	public interface PressAction {
		public void onPress(MVButtonWidget button);
	}
	
	private final PressAction onPress;
	private final MVTooltip tooltip;
	
	public MVButtonWidget(int x, int y, int width, int height, Text text, PressAction onPress, MVTooltip tooltip) {
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
	public MVButtonWidget(int x, int y, int width, int height, Text text, PressAction onPress) {
		this(x, y, width, height, text, onPress, null);
	}
	
	@Override
	public void onPress(AbstractInput a) {
		onPress.onPress(this);
	}
	
	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		appendDefaultNarrations(builder);
	}
	
	@Override
	public void render(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		MVDrawableHelper.super_render(MVButtonWidget.class, this, matrices, mouseX, mouseY, delta);
	}
	public final void method_25394(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		render(matrices, mouseX, mouseY, delta);
	}
	@Override
	public final void render(DrawContext context, int mouseX, int mouseY, float delta) {
		render(MVDrawableHelper.getMatrices(context), mouseX, mouseY, delta);
	}

	public void renderButton(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		Version.newSwitch()
				.range("1.20.0", null, () -> super.renderWidget(MVDrawableHelper.getDrawContext(matrices), mouseX, mouseY, delta))
				.run();
	}
	@Override
	protected final void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		renderButton(MVDrawableHelper.getMatrices(context), mouseX, mouseY, delta);
	}
	public final void method_48579(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		renderButton(matrices, mouseX, mouseY, delta);
	}
	public final void method_25359(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) { // renderButton <= 1.19.3
		renderButton(matrices, mouseX, mouseY, delta);
	}
	private void super_renderButton(String intermediary, MatrixStack matrices, int mouseX, int mouseY, float delta) {
		try {
			MethodHandles.lookup().findSpecial(ClickableWidget.class, intermediary,
					MethodType.methodType(void.class, MatrixStack.class, int.class, int.class, float.class),
					MVButtonWidget.class).invoke(this, matrices, mouseX, mouseY, delta);
		} catch (Throwable e) {
			throw new RuntimeException("Error calling super.renderButton (" + intermediary + ")", e);
		}
	}

	public void method_25352(Matrix3x2fStack matrices, int mouseX, int mouseY) { // renderTooltip
		if (tooltip != null)
			tooltip.render(matrices, mouseX, mouseY);
	}
	
}
