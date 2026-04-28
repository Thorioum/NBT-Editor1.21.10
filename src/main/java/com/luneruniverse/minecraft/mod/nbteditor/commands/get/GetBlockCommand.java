package com.luneruniverse.minecraft.mod.nbteditor.commands.get;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalBlock;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.util.BlockStateProperties;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;

public class GetBlockCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "block";
	}
	
	@Override
	public String getExtremeAlias() {
		return "b";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		Command<FabricClientCommandSource> getBlock = context -> {
			Coordinates posArg = getDefaultArg(context, "pos", null, Coordinates.class);
			BlockPos pos = (posArg == null ? null : posArg.getBlockPos(MVMisc.getCommandSource(context.getSource().getPlayer())));
			if (pos != null && !MainUtil.client.level.isInWorldBounds(pos))
				throw BlockPosArgument.ERROR_OUT_OF_WORLD.create();
			BlockInput blockArg = context.getArgument("block", BlockInput.class);
			CompoundTag nbt = blockArg.tag;
			if (nbt == null)
				nbt = new CompoundTag();
			LocalBlock block = new LocalBlock(blockArg.getState().getBlock(), new BlockStateProperties(blockArg.getState()), nbt);
			
			if (pos == null) {
				block.toItem(false).ifPresentOrElse(MainUtil::getWithMessage,
						() -> MainUtil.client.player.sendSystemMessage(TextInst.translatable("nbteditor.nbt.export.item.error")));
			} else if (NBTEditorClient.SERVER_CONN.isEditingExpanded())
				block.place(pos);
			else
				MainUtil.client.player.sendSystemMessage(TextInst.translatable("nbteditor.requires_server"));
			
			return Command.SINGLE_SUCCESS;
		};
		
		builder.then(argument("block", MVMisc.getBlockStateArg()).executes(getBlock))
				.then(argument("pos", BlockPosArgument.blockPos()).then(argument("block", MVMisc.getBlockStateArg()).executes(getBlock)));
	}
	
}
