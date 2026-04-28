package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;

public class MVScreen extends Screen implements OldEventBehavior, IgnoreCloseScreenPacket {
	
	protected MVScreen(Component title) {
		super(title);
	}


	public void render(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		super.extractRenderState(MVDrawableHelper.getDrawContext(matrices),mouseX,mouseY,delta);
		//MVDrawableHelper.super_render(MVScreen.class, this, matrices, mouseX, mouseY, delta);
	}

	@Override
	public void extractRenderState(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		render(MVDrawableHelper.getMatrices(context), mouseX, mouseY, delta);
	}
	
	public void setInitialFocus(@NonNull GuiEventListener element) {
		MVMisc.setInitialFocus(this, element, super::setInitialFocus);
	}
	@Override
	protected void setInitialFocus() {}
	
}
