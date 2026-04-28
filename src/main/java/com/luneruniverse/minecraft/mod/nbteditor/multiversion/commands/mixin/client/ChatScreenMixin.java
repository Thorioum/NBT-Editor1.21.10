package com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.mixin.client;

import net.minecraft.client.input.KeyEvent;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandInternals;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.components.EditBox;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
	@Shadow
	protected EditBox input;
	
	// TODO(Ravel): remapper for org.spongepowered.asm.mixin.injection.Group is not implemented
// TODO(Ravel): remapper for org.spongepowered.asm.mixin.injection.Group is not implemented
    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/ChatScreen;handleChatInput(Ljava/lang/String;Z)V"), cancellable = true)
	@Group(name = "keyPressed", min = 1)
	private void enterPressed_new(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
		enterPressed_impl(cir);
	}
	// TODO(Ravel): remapper for org.spongepowered.asm.mixin.injection.Group is not implemented
// TODO(Ravel): remapper for org.spongepowered.asm.mixin.injection.Group is not implemented
    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_408;method_44056(Ljava/lang/String;Z)Z"), cancellable = true, remap = false)
	@Group(name = "keyPressed", min = 1)
	@SuppressWarnings("target")
	private void enterPressed_mid(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
		enterPressed_impl(cir);
	}
	// TODO(Ravel): remapper for org.spongepowered.asm.mixin.injection.Group is not implemented
// TODO(Ravel): target method method_25404 with the signature not found
// TODO(Ravel): remapper for org.spongepowered.asm.mixin.injection.Group is not implemented
// TODO(Ravel): target method method_25404 with the signature not found
    @Inject(method = "method_25404(III)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_408;method_25427(Ljava/lang/String;)V"), cancellable = true, remap = false)
	@Group(name = "keyPressed", min = 1)
	@SuppressWarnings("target")
	private void enterPressed_old(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> info) {
		enterPressed_impl(info);
	}
	private void enterPressed_impl(CallbackInfoReturnable<Boolean> info) {
		String text = StringUtils.normalizeSpace(input.getValue().trim());
		if (text.length() <= 256)
			return;
		if (text.charAt(0) == '/' && ClientCommandInternals.executeCommand(text.substring(1))) {
			MainUtil.client.gui.getChat().addRecentChat(text);
			if (MainUtil.client.screen instanceof ChatScreen)
				MainUtil.client.setScreen(null);
			info.setReturnValue(true);
		} else
			input.value = text.substring(0, 256);
	}
}
