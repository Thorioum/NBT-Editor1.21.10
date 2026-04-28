package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import java.util.List;

import org.joml.Matrix3x2fStack;
import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(Screen.class)
public class ScreenMixin {
	@Inject(method = "method_32633", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4587;method_22903()V", shift = At.Shift.AFTER), remap = false)
	private void renderTooltipFromComponents(Matrix3x2fStack matrices, List<ClientTooltipComponent> tooltip, int x, int y, ClientTooltipPositioner positioner, CallbackInfo info) {
		if (!ConfigScreen.isTooltipOverflowFix())
			return;
		
		int[] size = MixinLink.getTooltipSize(tooltip);
		Vector2ic pos = MVMisc.getPosition(positioner, MainUtil.client.screen, x, y, size[0], size[1]);
		int screenWidth = MainUtil.client.getWindow().getGuiScaledWidth();
		int screenHeight = MainUtil.client.getWindow().getGuiScaledHeight();
		
		MixinLink.renderTooltipFromComponents(matrices, pos.x(), pos.y(), size[0], size[1], screenWidth, screenHeight);
	}
}
