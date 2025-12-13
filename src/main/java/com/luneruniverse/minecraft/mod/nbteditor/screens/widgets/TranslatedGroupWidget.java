package com.luneruniverse.minecraft.mod.nbteditor.screens.widgets;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix3x2fStack;

public class TranslatedGroupWidget extends GroupWidget {
	
	public static <T extends Drawable & Element> TranslatedGroupWidget forWidget(T widget, double x, double y, double z) {
		TranslatedGroupWidget output = new TranslatedGroupWidget(x, y, z) {
			@Override
			protected void renderPre(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
				setFocused(isMultiFocused() ? widget : null);
			}
		};
		output.addWidget(widget);
		return output;
	}
	
	private double x;
	private double y;
	private double z;
	
	public TranslatedGroupWidget(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public TranslatedGroupWidget() {
		this(0, 0, 0);
	}
	
	public TranslatedGroupWidget setTranslation(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	public TranslatedGroupWidget addTranslation(double x, double y, double z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}
	
	@Override
	public final void render(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		matrices.pushMatrix();
		matrices.translate((float) x, (float) y);
		mouseX -= (int) x;
		mouseY -= (int) y;
		renderPre(matrices, mouseX, mouseY, delta);
		super.render(matrices, mouseX, mouseY, delta);
		renderPost(matrices, mouseX, mouseY, delta);
		matrices.popMatrix();
	}
	protected void renderPre(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {}
	protected void renderPost(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {}
	
	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		click = new Click(click.x() - x, click.y() - y,click.buttonInfo());

		return mouseClickedPre(click.x(),click.y(),click.button()) ||
				super.mouseClicked(click, doubled) ||
				mouseClickedPost(click.x(),click.y(),click.button());
	}
	protected boolean mouseClickedPre(double mouseX, double mouseY, int button) {
		return false;
	}
	protected boolean mouseClickedPost(double mouseX, double mouseY, int button) {
		return false;
	}
	
	@Override
	public boolean mouseReleased(Click click) {
		click = new Click(click.x() - x, click.y() - y,click.buttonInfo());

		return mouseReleasedPre(click.x(),click.y(),click.button()) ||
				super.mouseReleased(click) ||
				mouseReleasedPost(click.x(),click.y(),click.button());
	}
	protected boolean mouseReleasedPre(double mouseX, double mouseY, int button) {
		return false;
	}
	protected boolean mouseReleasedPost(double mouseX, double mouseY, int button) {
		return false;
	}
	
	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		mouseX -= x;
		mouseY -= y;
		mouseMovedPre(mouseX, mouseY);
		super.mouseMoved(mouseX, mouseY);
		mouseMovedPost(mouseX, mouseY);
	}
	protected void mouseMovedPre(double mouseX, double mouseY) {}
	protected void mouseMovedPost(double mouseX, double mouseY) {}
	
	@Override
	public boolean mouseDragged(Click click, double deltaX, double deltaY) {
		click = new Click(click.x() - x, click.y() - y,click.buttonInfo());

		return mouseDraggedPre(click.x(),click.y(),click.button(), deltaX, deltaY) ||
				super.mouseDragged(click, deltaX, deltaY) ||
				mouseDraggedPost(click.x(),click.y(),click.button(), deltaX, deltaY);
	}
	protected boolean mouseDraggedPre(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		return false;
	}
	protected boolean mouseDraggedPost(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		return false;
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double xAmount, double yAmount) {
		mouseX -= x;
		mouseY -= y;
		return mouseScrolledPre(mouseX, mouseY, xAmount, yAmount) ||
				super.mouseScrolled(mouseX, mouseY, xAmount, yAmount) ||
				mouseScrolledPost(mouseX, mouseY, xAmount, yAmount);
	}
	protected boolean mouseScrolledPre(double mouseX, double mouseY, double xAmount, double yAmount) {
		return false;
	}
	protected boolean mouseScrolledPost(double mouseX, double mouseY, double xAmount, double yAmount) {
		return false;
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		mouseX -= x;
		mouseY -= y;
		return isMouseOverPre(mouseX, mouseY) ||
				super.isMouseOver(mouseX, mouseY);
	}
	protected boolean isMouseOverPre(double mouseX, double mouseY) {
		return false;
	}
	
}
