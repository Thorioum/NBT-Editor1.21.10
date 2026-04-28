package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.context.SuggestionContext;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.SuggestingTextFieldWidget;

import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.ChatFormatting;

@Mixin(CommandSuggestions.class)
public class CommandSuggestionsMixin {
	@Shadow
    EditBox input;
	
	@ModifyArgs(method = "showSuggestions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/CommandSuggestions$SuggestionsList;<init>(Lnet/minecraft/client/gui/components/CommandSuggestions;IIILjava/util/List;Z)V"))
	private void SuggestionWindow(Args args) {
		if (!(input instanceof SuggestingTextFieldWidget suggestor))
			return;
		
		if (suggestor.isDropdownOnly()) {
			Point pos = suggestor.getSpecialDropdownPos();
			args.set(1, pos.x);
			args.set(2, pos.y);
		} else
			args.set(2, input.y + input.getHeight() + 2);
	}

    @Inject(method = "fillNodeUsage", at = @At("HEAD"), cancellable = true)
	@Group(name = "showUsages", min = 1)
	private void showUsages(SuggestionContext<ClientSuggestionProvider> suggestionContext, Style usageFormat, CallbackInfoReturnable<List<FormattedCharSequence>> cir) {
		if (!(input instanceof SuggestingTextFieldWidget))
			return;
		
		cir.setReturnValue(new ArrayList<>());
	}
}
