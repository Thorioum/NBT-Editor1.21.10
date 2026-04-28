package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.NbtFormatter;

import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.TagParser;

@Mixin(TagParser.class)
public class TagParserMixin {
	// TODO(Ravel): target method method_10731 with the signature not found
// TODO(Ravel): target method method_10731 with the signature not found
    @Inject(method = "method_10731(Ljava/lang/String;)Lnet/minecraft/class_2520;", at = @At("HEAD"), cancellable = true, remap = false)
	@SuppressWarnings("target")
	private void parsePrimitive(String input, CallbackInfoReturnable<Tag> info) {
		if (ConfigScreen.isSpecialNumbers() && MixinLink.specialNumbers.contains(Thread.currentThread())) {
			Number specialNum = NbtFormatter.SPECIAL_NUMS.get(input);
			if (specialNum != null) {
				if (specialNum instanceof Double d)
					info.setReturnValue(DoubleTag.valueOf(d));
				else if (specialNum instanceof Float f)
					info.setReturnValue(FloatTag.valueOf(f));
				else
					throw new IllegalStateException("Number of invalid type: " + specialNum.getClass().getName());
			}
		}
	}
}
