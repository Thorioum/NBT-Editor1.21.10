package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.util.FormattedCharSequence;

@Mixin(Tooltip.class)
public class TooltipMixin {
	
	@Shadow
	private List<FormattedCharSequence> cachedTooltip;
	
	@Inject(method = "toCharSequence", at = @At("HEAD"), cancellable = true)
	private void getLines(Minecraft client, CallbackInfoReturnable<List<FormattedCharSequence>> info) {
		if (MixinLink.NEW_TOOLTIPS.containsKey((Tooltip) (Object) this))
			info.setReturnValue(cachedTooltip);
	}
	
}
