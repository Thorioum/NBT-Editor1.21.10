package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import java.util.List;

import org.joml.Matrix3x2fStack;
import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMatrix4f;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.Identifier;

@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorMixin {
	
	@Shadow
	public abstract Matrix3x2fStack pose();

	@Inject(method = "tooltip", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;pushMatrix()Lorg/joml/Matrix3x2fStack;", shift = At.Shift.AFTER))
	@Group(name = "renderTooltip", min = 1)
	private void drawTooltip(Font textRenderer, List<ClientTooltipComponent> tooltip, int x, int y, ClientTooltipPositioner positioner, Identifier texture, CallbackInfo info) {
		drawTooltip_impl(tooltip, x, y, positioner);
	}
	private void drawTooltip_impl(List<ClientTooltipComponent> tooltip, int x, int y, ClientTooltipPositioner positioner) {
		if (!ConfigScreen.isTooltipOverflowFix())
			return;
		
		int[] size = MixinLink.getTooltipSize(tooltip);
		Vector2ic pos = MVMisc.getPosition(positioner, MainUtil.client.screen, x, y, size[0], size[1]);
		int screenWidth = MainUtil.client.getWindow().getGuiScaledWidth();
		int screenHeight = MainUtil.client.getWindow().getGuiScaledHeight();
		
		MixinLink.renderTooltipFromComponents(pose(), pos.x(), pos.y(), size[0], size[1], screenWidth, screenHeight);
	}

	@ModifyVariable(method = "containsPointInScissor", at = @At("HEAD"), ordinal = 0, require = 0)
	private int scissorContainsX(int x) {
		float t = pose().m20;
		return (int) (x + t);
	}
	@ModifyVariable(method = "containsPointInScissor", at = @At("HEAD"), ordinal = 1, require = 0)
	private int scissorContainsY(int y) {
		float t = pose().m21;
		return (int) (y + t);
	}
	
}
