package com.luneruniverse.minecraft.mod.nbteditor.screens;

import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.InitializableOverlay;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

public class OverlaySupportingScreen extends TickableSupportingScreen {
	
	public static <T extends Drawable & Element> T setOverlayStatic(T overlay, double z) {
		return ((OverlaySupportingScreen) MainUtil.client.currentScreen).setOverlay(overlay, z);
	}
	public static <T extends Drawable & Element> T setOverlayStatic(T overlay) {
		return setOverlayStatic(overlay, 0);
	}
	public static <T extends Screen> T setOverlayScreenStatic(T overlay, double z) {
		return ((OverlaySupportingScreen) MainUtil.client.currentScreen).setOverlayScreen(overlay, z);
	}
	public static <T extends Screen> T setOverlayScreenStatic(T overlay) {
		return setOverlayScreenStatic(overlay, 0);
	}
	
	private Element overlay; // extends Drawable & Element
	private Screen overlayScreen;
	private double overlayZ;
	
	protected OverlaySupportingScreen(Text title) {
		super(title);
	}
	
	public <T extends Drawable & Element> T setOverlay(T overlay, double z) {
		this.overlay = overlay;
		this.overlayScreen = null;
		this.overlayZ = z;
		if (overlay instanceof InitializableOverlay<?> initable)
			initable.initUnchecked(this);
		return overlay;
	}
	public <T extends Drawable & Element> T setOverlay(T overlay) {
		return setOverlay(overlay, 0);
	}
	public <T extends Screen> T setOverlayScreen(T overlay, double z) {
		this.overlay = overlay;
		this.overlayScreen = overlay;
		this.overlayZ = z;
		overlay.init(client, width, height);
		if (overlay instanceof InitializableOverlay<?> initable)
			initable.initUnchecked(this);
		return overlay;
	}
	public <T extends Screen> T setOverlayScreen(T overlay) {
		return setOverlayScreen(overlay, 0);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Drawable & Element> T getOverlay() {
		return (T) overlay;
	}
	public Screen getOverlayScreen() {
		return overlayScreen;
	}
	public double getOverlayZ() {
		return overlayZ;
	}
	
	@Override
	protected void init() {
		if (overlayScreen != null)
			overlayScreen.init(client, width, height);
		if (overlay instanceof InitializableOverlay<?> initable)
			initable.initUnchecked(this);
	}
	
	@Override
	public final void render(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		int bgMouseX = (overlay == null ? mouseX : -314);
		int bgMouseY = (overlay == null ? mouseY : -314);
		renderMain(matrices, bgMouseX, bgMouseY, delta);
		if (overlay != null) {
			boolean translated = (overlayZ != 0);
			if (translated) {
				matrices.pushMatrix();
				matrices.translate(0.0f, 0.0f);
			}
			((Drawable) overlay).render(matrices, mouseX, mouseY, delta);
			if (translated)
				matrices.popMatrix();
		}
	}
	protected void renderMain(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);
	}
	
	@Override
	public void tick() {
		if (overlay != null) {
			if (overlay instanceof Tickable tickable)
				tickable.tick();
		} else
			super.tick();
	}
	
	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		if (overlay != null)
			return overlay.mouseClicked(click, doubled);
		return super.mouseClicked(click, doubled);
	}
	
	@Override
	public boolean mouseReleased(Click click) {
		if (overlay != null)
			return overlay.mouseReleased(click);
		return super.mouseReleased(click);
	}
	
	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		if (overlay != null)
			overlay.mouseMoved(mouseX, mouseY);
		else
			super.mouseMoved(mouseX, mouseY);
	}
	
	@Override
	public boolean mouseDragged(Click click, double deltaX, double deltaY) {
		if (overlay != null)
			return overlay.mouseDragged(click, deltaX, deltaY);
		return super.mouseDragged(click, deltaX, deltaY);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double xAmount, double yAmount) {
		if (overlay != null)
			return overlay.mouseScrolled(mouseX, mouseY, xAmount, yAmount);
		return super.mouseScrolled(mouseX, mouseY, xAmount, yAmount);
	}
	
	@Override
	public boolean keyPressed(KeyInput keyInput) {
		if (overlay != null)
			return overlay.keyPressed(keyInput);
		return super.keyPressed(keyInput);
	}
	
	@Override
	public boolean keyReleased(KeyInput keyInput) {
		if (overlay != null)
			return overlay.keyReleased(keyInput);
		return super.keyReleased(keyInput);
	}
	
	@Override
	public boolean charTyped(CharInput charInput) {
		if (overlay != null)
			return overlay.charTyped(charInput);
		return super.charTyped(charInput);
	}
	
}
