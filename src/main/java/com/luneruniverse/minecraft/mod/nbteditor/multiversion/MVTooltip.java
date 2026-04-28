package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.joml.Matrix3x2fStack;
import org.lwjgl.opengl.GL20;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.util.TextUtil;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;

public class MVTooltip {
	
	public static final MVTooltip EMPTY = new MVTooltip(new Component[0]);
	private static boolean oneTooltip = false;
	private static boolean lastTooltip = false;
	private static MVTooltip theOneTooltip;
	
	public static MVTooltip setOneTooltip(boolean oneTooltip, boolean lastTooltip) {
		MVTooltip.oneTooltip = oneTooltip;
		MVTooltip.lastTooltip = lastTooltip;
		MVTooltip output = theOneTooltip;
		theOneTooltip = null;
		return output;
	}
	public static boolean isOneTooltip() {
		return oneTooltip;
	}
	public static boolean isLastTooltip() {
		return lastTooltip;
	}
	public static MVTooltip getTheOneTooltip() {
		return theOneTooltip;
	}
	public static boolean setExternalOneTooltip(List<FormattedCharSequence> tooltip) {
		if (isOneTooltip()) {
			if (lastTooltip || theOneTooltip == null)
				theOneTooltip = new MVTooltip(tooltip, null);
			return true;
		}
		return false;
	}
	public static boolean renderOneTooltip(Matrix3x2fStack matrices, int mouseX, int mouseY) {
		MVTooltip tooltip = setOneTooltip(false, false);
		if (tooltip == null)
			return false;
		tooltip.render(matrices, mouseX, mouseY);
		return true;
	}
	
	private static Component combine(List<Component> lines) {
		EditableText combined = TextInst.literal("");
		for (int i = 0; i < lines.size(); i++) {
			if (i > 0)
				combined = combined.append(" ");
			combined = combined.append(lines.get(i));
		}
		return combined;
	}
	
	private final List<FormattedCharSequence> lines;
	private final Component combined;
	
	private MVTooltip(List<FormattedCharSequence> lines, Component combined) {
		this.lines = lines;
		this.combined = combined;
	}
	public MVTooltip(List<Component> lines) {
		this(lines.stream().map(Component::getVisualOrderText).collect(Collectors.toList()), combine(lines));
	}
	public MVTooltip(Component... lines) {
		this(Arrays.stream(lines).flatMap(line -> TextUtil.splitText(line).stream()).toList());
	}
	public MVTooltip(String... keys) {
		this(Arrays.asList(keys).stream().map(TextInst::translatable).toList().toArray(new EditableText[0]));
	}
	
	public List<FormattedCharSequence> getLines() {
		return lines;
	}
	
	public Component getCombined() {
		return combined;
	}
	
	public boolean isEmpty() {
		return this == EMPTY || lines.isEmpty();
	}
	
	Tooltip toNewTooltip() {
		if (isEmpty())
			return null;
		
		Tooltip output = Tooltip.create(combined);
		Reflection.getField(Tooltip.class, "cachedTooltip", "Ljava/util/List;").set(output, lines);
		MixinLink.NEW_TOOLTIPS.put(output, true);
		return output;
	}
	Object toOldTooltip() {
		if (isEmpty())
			return Reflection.getField(Button.class, "field_25035", "Lnet/minecraft/class_4185$class_5316;").get(null); // ButtonWidget.EMPTY
		
		return Proxy.newProxyInstance(MVMisc.class.getClassLoader(),
				new Class<?>[] {Reflection.getClass("net.minecraft.class_4185$class_5316")}, (obj, method, args) -> {
			if (args.length == 1) // supply
				return null;
			if (args.length != 4) // onTooltip
				throw new RuntimeException("Unexpected method call: " + method.getName());
			render((Matrix3x2fStack) args[1], (int) args[2], (int) args[3]);
			return null;
		});
	}
	
	public void render(Matrix3x2fStack matrices, int mouseX, int mouseY) {
		if (oneTooltip) {
			if (lastTooltip || theOneTooltip == null)
				theOneTooltip = this;
			return;
		}
		
		// Undo translations and render at actual position
		// This allows Screen#renderTooltip to adjust for window height
		matrices.pushMatrix();
		boolean scissor = MVGlStateManager.isScissorEnabled();
		if (scissor)
			GL20.glDisable(GL20.GL_SCISSOR_TEST);
		
		MVDrawableHelper.renderTooltip(matrices, lines, mouseX, mouseY);
		
		if (scissor)
			GL20.glEnable(GL20.GL_SCISSOR_TEST);
		matrices.popMatrix();
	}
	
}
