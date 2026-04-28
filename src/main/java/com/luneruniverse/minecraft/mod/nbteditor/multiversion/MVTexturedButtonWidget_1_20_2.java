package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.Identifier;

class MVTexturedButtonWidget_1_20_2 extends Button {
	
	protected final Identifier texture;
	protected final int u;
	protected final int v;
	protected final int hoveredVOffset;
	protected final int textureWidth;
	protected final int textureHeight;
	
	public MVTexturedButtonWidget_1_20_2(int x, int y, int width, int height, int u, int v, int hoveredVOffset,
                                         Identifier texture, int textureWidth, int textureHeight, Button.OnPress pressAction) {
		super(x, y, width, height, CommonComponents.EMPTY, pressAction, DEFAULT_NARRATION);
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
		this.u = u;
		this.v = v;
		this.hoveredVOffset = hoveredVOffset;
		this.texture = texture;
	}
	
	@Override
	public void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		drawTexture(context, this.texture, getX(), getY(), this.u, this.v, this.hoveredVOffset, this.width, this.height,
				this.textureWidth, this.textureHeight);
	}

	public boolean isNarratable() {
		return this.visible && this.active;
	}
	public void drawTexture(GuiGraphicsExtractor context, Identifier texture, int x, int y, int u, int v, int hoveredVOffset,
                            int width, int height, int textureWidth, int textureHeight) {
		int i = v;
		if (!isNarratable()) {
			i += hoveredVOffset * 2;
		} else if (isHoveredOrFocused()) {
			i += hoveredVOffset;
		}
		MVGlStateManager._enableDepthTest();
		MVDrawableHelper.drawTexture(MVDrawableHelper.getMatrices(context), texture, x, y, u, i, width, height, textureWidth, textureHeight);
	}
	
}