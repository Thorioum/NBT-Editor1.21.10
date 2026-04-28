package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.hideflags;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;

import net.minecraft.network.chat.Component;

public class TooltipHideFlag extends HideFlag {
	
	public static final HideFlag INSTANCE = new TooltipHideFlag();
	
	private static final Component NAME = TextInst.translatable("nbteditor.hide_flags.tooltip");
	
	private TooltipHideFlag() {}
	
	@Override
	public Component getName() {
		return NAME;
	}
	
}
