package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class HideFlagsCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "hideflags";
	}
	
	@Override
	public String getExtremeAlias() {
		return "hf";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		builder.executes(context -> {
			MinecraftClient.getInstance().player.sendMessage(Text.literal("this command has been disabled."),false);
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
