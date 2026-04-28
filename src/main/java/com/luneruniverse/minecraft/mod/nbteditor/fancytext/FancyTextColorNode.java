package com.luneruniverse.minecraft.mod.nbteditor.fancytext;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public record FancyTextColorNode(TextColor color) implements FancyTextNode {
	
	@Override
	public Style modifyStyle(Style style) {
		return style.withColor(color);
	}
	
	@Override
	public int getNumberOfTextNodes() {
		return 0;
	}
	
}
