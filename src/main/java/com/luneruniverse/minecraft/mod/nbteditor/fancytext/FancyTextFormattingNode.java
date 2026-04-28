package com.luneruniverse.minecraft.mod.nbteditor.fancytext;

import com.luneruniverse.minecraft.mod.nbteditor.util.StyleUtil;

import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;

public record FancyTextFormattingNode(ChatFormatting formatting) implements FancyTextNode {
	
	@Override
	public Style modifyStyle(Style style) {
		if (formatting == ChatFormatting.RESET)
			return StyleUtil.RESET_STYLE.applyTo(style);
		return style.applyFormat(formatting);
	}
	
	@Override
	public int getNumberOfTextNodes() {
		return 0;
	}
	
}
