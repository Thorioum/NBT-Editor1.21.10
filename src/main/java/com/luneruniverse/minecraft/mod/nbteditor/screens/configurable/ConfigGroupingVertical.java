package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.Click;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

public abstract class ConfigGroupingVertical<K, T extends ConfigGroupingVertical<K, T>> extends ConfigGrouping<K, T> {
	
	protected ConfigGroupingVertical(Text name, Constructor<K, T> cloneImpl) {
		super(name, cloneImpl);
	}
	
	protected int getNameHeight() {
		return name == null ? 0 : MainUtil.client.textRenderer.fontHeight + PADDING;
	}
	
	@Override
	public void render(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		MVDrawableHelper.fill(matrices, 0, 0, PADDING, getSpacingHeight(), isValueValid() ? 0xFFAAAAAA : 0xFFDF4949);
		
		int yOffset = 0;
		Text fullName = getFullName();
		if (fullName != null) {
			MVDrawableHelper.drawTextWithShadow(matrices, MainUtil.client.textRenderer, fullName, PADDING * 2, 0, 0xFFFFFFFF);
			yOffset += getNameHeight();
		}
		
		// Render in reverse order to allow dropdowns to display over below components
		List<ConfigPath> paths = new ArrayList<>(this.paths.values());
		Collections.reverse(paths);
		for (ConfigPath path : paths)
			yOffset += path.getSpacingHeight() + PADDING;
		
		for (ConfigPath path : paths) {
			yOffset -= path.getSpacingHeight() + PADDING;
			
			matrices.pushMatrix();
			matrices.translate(PADDING * 2, yOffset);
			path.render(matrices, mouseX - PADDING * 2, mouseY - yOffset, delta);
			matrices.popMatrix();
		}
	}
	
	@Override
	public int getSpacingWidth() {
		int output = 0;
		for (ConfigPath path : paths.values()) {
			int width = path.getSpacingWidth();
			if (width > output)
				output = width;
		}
		return output;
	}
	
	@Override
	public int getSpacingHeight() {
		return getNameHeight() + paths.values().stream().mapToInt(Configurable::getSpacingHeight).reduce((a, b) -> a + PADDING + b).orElse(0);
	}
	
	@Override
	public int getRenderWidth() {
		int output = 0;
		for (ConfigPath path : paths.values()) {
			int width = path.getRenderWidth();
			if (width > output)
				output = width;
		}
		return output;
	}
	
	@Override
	public int getRenderHeight() {
		int output = getNameHeight();
		int yOffset = output;
		for (ConfigPath path : paths.values()) {
			int bottomY = yOffset + path.getRenderHeight();
			if (bottomY > output)
				output = bottomY;
			yOffset += path.getSpacingHeight() + PADDING;
		}
		return output;
	}
	
	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		int yOffset = getNameHeight();
		
		for (ConfigPath path : new ArrayList<>(paths.values())) {
			if (path.mouseClicked(new Click(click.x() - PADDING * 2, click.y() - yOffset, click.buttonInfo()),doubled))
				return true;
			yOffset += path.getSpacingHeight() + PADDING;
		}
		return false;
	}
	@Override
	public boolean mouseReleased(Click click) {
		int yOffset = getNameHeight();
		
		for (ConfigPath path : new ArrayList<>(paths.values())) {
			if (path.mouseReleased(new Click(click.x() - PADDING * 2, click.y() - yOffset, click.buttonInfo())))
				return true;
			yOffset += path.getSpacingHeight() + PADDING;
		}
		return false;
	}
	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		int yOffset = getNameHeight();
		
		for (ConfigPath path : new ArrayList<>(paths.values())) {
			path.mouseMoved(mouseX - PADDING * 2, mouseY - yOffset);
			yOffset += path.getSpacingHeight() + PADDING;
		}
	}
	@Override
	public boolean mouseDragged(Click click, double deltaX, double deltaY) {
		int yOffset = getNameHeight();
		
		for (ConfigPath path : new ArrayList<>(paths.values())) {
			// Buttons return true by default, causing problems with returning early
			path.mouseDragged(new Click(click.x() - PADDING * 2, click.y() - yOffset, click.buttonInfo()), deltaX, deltaY);
			yOffset += path.getSpacingHeight() + PADDING;
		}
		return false;
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double xAmount, double yAmount) {
		int yOffset = getNameHeight();
		
		for (ConfigPath path : new ArrayList<>(paths.values())) {
			if (path.mouseScrolled(mouseX - PADDING * 2, mouseY - yOffset, xAmount, yAmount))
				return true;
			yOffset += path.getSpacingHeight() + PADDING;
		}
		return false;
	}
	
}
