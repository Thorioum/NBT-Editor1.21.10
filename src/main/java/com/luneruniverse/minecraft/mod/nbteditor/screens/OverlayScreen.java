package com.luneruniverse.minecraft.mod.nbteditor.screens;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;

public class OverlayScreen extends OverlaySupportingScreen {
	
	public static <T extends Renderable & GuiEventListener & NarratableEntry> T setOverlayOrScreen(T overlay, double z, boolean restoreParent) {
		if (MainUtil.client.screen instanceof OverlaySupportingScreen screen)
			screen.setOverlay(overlay, z);
		else
			MainUtil.client.setScreen(new OverlayScreen(TextInst.of(overlay.getClass().getName()), overlay, z, restoreParent));
		return overlay;
	}
	public static <T extends Renderable & GuiEventListener & NarratableEntry> T setOverlayOrScreen(T overlay, boolean restoreParent) {
		return setOverlayOrScreen(overlay, 0, restoreParent);
	}
	
	private Screen parent;
	
	private <T extends Renderable & GuiEventListener & NarratableEntry> OverlayScreen(Component title, T widget, double z, boolean restoreParent) {
		super(title);
		setOverlay(widget, z);
		if (restoreParent)
			parent = MainUtil.client.screen;
	}
	
	@Override
	public <T extends Renderable & GuiEventListener> T setOverlay(T overlay, double z) {
		if (overlay == null)
			MainUtil.client.setScreen(parent);
		else
			parent = null;
		return super.setOverlay(overlay, z);
	}
	@Override
	public <T extends Screen> T setOverlayScreen(T overlay, double z) {
		if (overlay == null)
			MainUtil.client.setScreen(parent);
		else
			parent = null;
		return super.setOverlayScreen(overlay, z);
	}
	
	@Override
	protected void init() {
		if (parent != null)
			parent.init(width, height);
		super.init();
	}
	
	@Override
	protected void renderMain(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		if (parent != null)
			parent.extractRenderState(MVDrawableHelper.getDrawContext(matrices), -314, -314, delta);
	}
	
}
