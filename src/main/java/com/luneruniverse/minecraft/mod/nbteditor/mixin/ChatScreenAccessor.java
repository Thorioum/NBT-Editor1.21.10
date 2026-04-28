package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.components.EditBox;

@Mixin(ChatScreen.class)
public interface ChatScreenAccessor {
	@Accessor
	public EditBox getInput();
}
