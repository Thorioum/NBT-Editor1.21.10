package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import java.util.ArrayList;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.Click;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

public abstract class ConfigGroupingHorizontal<K, T extends ConfigGroupingHorizontal<K, T>> extends ConfigGrouping<K, T> {
	
	protected ConfigGroupingHorizontal(Text name, Constructor<K, T> cloneImpl) {
		super(name, cloneImpl);
	}
	
	protected int getNameWidth() {
		return name == null ? 0 : MainUtil.client.textRenderer.getWidth(name) + PADDING;
	}
	
	@Override
	public void render(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		int xOffset = 0;
		Text fullName = getFullName();
		if (fullName != null) {
			MVDrawableHelper.drawTextWithShadow(matrices, MainUtil.client.textRenderer, fullName, PADDING * 2, 0, 0xFFFFFFFF);
			xOffset += getNameWidth();
		}
		
		for (ConfigPath path : new ArrayList<>(paths.values())) {
			matrices.pushMatrix();
			matrices.translate((float) xOffset, 0.0f);
			path.render(matrices, mouseX - xOffset, mouseY, delta);
			matrices.popMatrix();
			
			xOffset += path.getSpacingWidth() + PADDING;
		}
	}
	
	@Override
	public int getSpacingWidth() {
		return getNameWidth() + paths.values().stream().mapToInt(Configurable::getSpacingWidth).reduce((a, b) -> a + PADDING + b).orElse(0);
	}
	
	@Override
	public int getSpacingHeight() {
		int output = 0;
		for (ConfigPath path : paths.values()) {
			int height = path.getSpacingHeight();
			if (height > output)
				output = height;
		}
		return output;
	}
	
	@Override
	public int getRenderWidth() {
		int output = getNameWidth();
		int xOffset = output;
		for (ConfigPath path : paths.values()) {
			int rightX = xOffset + path.getRenderWidth();
			if (rightX > output)
				output = rightX;
			xOffset += path.getSpacingWidth() + PADDING;
		}
		return output;
	}
	
	@Override
	public int getRenderHeight() {
		int output = 0;
		for (ConfigPath path : paths.values()) {
			int height = path.getRenderHeight();
			if (height > output)
				output = height;
		}
		return output;
	}
	
	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		int xOffset = getNameWidth();
		
		for (ConfigPath path : new ArrayList<>(paths.values())) {
			if (path.mouseClicked(new Click(click.x() - xOffset, click.y(), click.buttonInfo()),doubled))
				return true;
			xOffset += path.getSpacingWidth() + PADDING;
		}
		return false;
	}
	@Override
	public boolean mouseReleased(Click click) {
		int xOffset = getNameWidth();
		
		for (ConfigPath path : new ArrayList<>(paths.values())) {
			if (path.mouseReleased(new Click(click.x() - xOffset, click.y(), click.buttonInfo())))
				return true;
			xOffset += path.getSpacingWidth() + PADDING;
		}
		return false;
	}
	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		int xOffset = getNameWidth();
		
		for (ConfigPath path : new ArrayList<>(paths.values())) {
			path.mouseMoved(mouseX - xOffset, mouseY);
			xOffset += path.getSpacingWidth() + PADDING;
		}
	}
	@Override
	public boolean mouseDragged(Click click, double deltaX, double deltaY) {
		int xOffset = getNameWidth();
		
		for (ConfigPath path : new ArrayList<>(paths.values())) {
			if (path.mouseDragged(new Click(click.x() - xOffset, click.y(), click.buttonInfo()), deltaX, deltaY))
				return true;
			xOffset += path.getSpacingWidth() + PADDING;
		}
		return false;
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double xAmount, double yAmount) {
		int xOffset = getNameWidth();
		
		for (ConfigPath path : new ArrayList<>(paths.values())) {
			if (path.mouseScrolled(mouseX - xOffset, mouseY, xAmount, yAmount))
				return true;
			xOffset += path.getSpacingWidth() + PADDING;
		}
		return false;
	}
	
}
