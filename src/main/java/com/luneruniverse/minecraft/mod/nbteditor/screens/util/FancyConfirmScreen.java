package com.luneruniverse.minecraft.mod.nbteditor.screens.util;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IgnoreCloseScreenPacket;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;

public class FancyConfirmScreen extends ConfirmScreen implements IgnoreCloseScreenPacket {
	
	private Screen parent;
	
	public FancyConfirmScreen(BooleanConsumer callback, Component title, Component message, Component yesTranslated, Component noTranslated) {
		super(callback, title, message, yesTranslated, noTranslated);
		parent = MainUtil.client.screen;
	}
	public FancyConfirmScreen(BooleanConsumer callback, Component title, Component message) {
		super(callback, title, message);
		parent = MainUtil.client.screen;
	}
	
	public FancyConfirmScreen setParent(Screen parent) {
		this.parent = parent;
		return this;
	}
	
	@Override
	protected void init() {
		if (parent != null)
			parent.init(width, height);
		super.init();
	}
	
	public void extractRenderState(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		if (parent != null)
			parent.extractRenderState(MVDrawableHelper.getDrawContext(matrices), -314, -314, delta);
		
		matrices.pushMatrix();
		matrices.translate(0.0f, 0.0f);
		super.extractRenderState(MVDrawableHelper.getDrawContext(matrices), mouseX, mouseY, delta);
		MainUtil.renderLogo(matrices);
		matrices.popMatrix();
	}

	@Override
	public final void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		extractRenderState(MVDrawableHelper.getMatrices(context), mouseX, mouseY, delta);
	}
	
	@Override
	public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		if (MainUtil.client.level == null)
			super.extractBackground(context, mouseX, mouseY, delta);
		else
			extractTransparentBackground(context);
	}
	
}
