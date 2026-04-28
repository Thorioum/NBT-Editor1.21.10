package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.client.input.MouseButtonEvent;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class MVSliderWidget extends MVButtonWidget {
	
	private static final Identifier HANDLE = IdentifierInst.of("nbteditor", "textures/slider_handle.png");
	private static final Identifier HANDLE_HIGHLIGHTED = IdentifierInst.of("nbteditor", "textures/slider_handle_highlighted.png");
	
	private double value;
	private final Supplier<Component> msg;
	private final Consumer<Double> onValue;
	
	public MVSliderWidget(int x, int y, int width, int height, double value, Supplier<Component> msg, Consumer<Double> onValue, MVTooltip tooltip) {
		super(x, y, width, height, msg.get(), btn -> {}, tooltip);
		this.value = value;
		this.msg = msg;
		this.onValue = onValue;
	}
	public MVSliderWidget(int x, int y, int width, int height, double value, Supplier<Component> msg, Consumer<Double> onValue) {
		this(x, y, width, height, value, msg, onValue, null);
	}
	
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = Mth.clamp(value, 0, 1);
		onValue.accept(this.value);
		setMessage(msg.get());
	}
	private void setValueFromMouse(double mouseX) {
		setValue((mouseX - x - 4) / (width - 8));
	}
	
	@Override
	public void renderButton(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		if (renderSlider(matrices, mouseX, mouseY, delta)) {
			MVDrawableHelper.drawTexture(matrices, this.isHovered || this.isFocused() ? HANDLE_HIGHLIGHTED : HANDLE,
					x + (int) (value * (width - 8)), y, 0, 0, 8, 20, 8, 20);
			MVDrawableHelper.drawCenteredTextWithShadow(matrices, MainUtil.client.font, getMessage(),
					x + width / 2, y + height / 2 - MainUtil.client.font.lineHeight / 2, -1);
		} else {
			new AbstractSliderButton(x, y, width, height, getMessage(), value) {
				@Override
				protected void updateMessage() {}
				@Override
				protected void applyValue() {}
			}.extractRenderState(MVDrawableHelper.getDrawContext(matrices), mouseX, mouseY, delta);
		}
	}
	protected boolean renderSlider(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		return false;
	}
	
	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (click.button() != GLFW.GLFW_MOUSE_BUTTON_1 || !isMouseOver(click.x(), click.y()))
			return false;
		setValueFromMouse(click.x());
		return true;
	}
	
	@Override
	public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
		if (click.button() != GLFW.GLFW_MOUSE_BUTTON_1)
			return false;
		setValueFromMouse(click.x());
		return true;
	}
	
	@Override
	public boolean mouseReleased(MouseButtonEvent click) {
		if (click.button() != GLFW.GLFW_MOUSE_BUTTON_1)
			return false;
		playDownSound(Minecraft.getInstance().getSoundManager());
		return true;
	}
	
}
