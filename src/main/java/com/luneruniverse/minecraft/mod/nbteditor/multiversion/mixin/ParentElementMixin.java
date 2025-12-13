package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin;

import net.minecraft.client.gui.Click;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.OldEventBehavior;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;

@Mixin(ParentElement.class)
public interface ParentElementMixin {
	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void mouseClicked(Click click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
		if (!(this instanceof OldEventBehavior))
			return;
		
		ParentElement source = (ParentElement) (Object) this;
		
		for (Element element : source.children()) {
			if (element.mouseClicked(click, doubled)) {
				source.setFocused(element);
				if (click.button() == 0)
					source.setDragging(true);
				cir.setReturnValue(true);
				return;
			}
		}
		cir.setReturnValue(false);
	}
}
