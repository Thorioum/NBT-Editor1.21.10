package com.luneruniverse.minecraft.mod.nbteditor.screens;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVScreen;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

public class TickableSupportingScreen extends MVScreen {
	
	protected TickableSupportingScreen(Component title) {
		super(title);
	}
	
	@Override
	public void tick() {
		for (GuiEventListener element : children()) {
			if (element instanceof Tickable tickable)
				tickable.tick();
		}
	}
	
}
