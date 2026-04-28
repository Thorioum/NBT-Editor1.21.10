package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;
import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.literal;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.ChatFormatting;

public class MaxStackSizeCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "maxstacksize";
	}
	
	@Override
	public String getExtremeAlias() {
		return "mss";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		builder.then(literal("default").executes(context -> {
			ItemReference ref = ItemReference.getHeldItem();
			ItemStack item = ref.getItem();
			if (item.getComponentsPatch().get(item.getComponents(),DataComponents.MAX_STACK_SIZE) == null) {
				MainUtil.client.player.sendSystemMessage(TextInst.translatable("nbteditor.max_stack_size.already_removed"));
			} else if (item.has(DataComponents.MAX_DAMAGE) &&
					item.getPrototype().getOrDefault(DataComponents.MAX_STACK_SIZE, 1) > 1) {
				MainUtil.client.player.sendSystemMessage(TextInst.translatable("nbteditor.max_stack_size.invalid_state"));
			} else {
				int size = item.getPrototype().get(DataComponents.MAX_STACK_SIZE);
				if (item.getCount() > size)
					item.setCount(size);
				item.set(DataComponents.MAX_STACK_SIZE, size);
				ref.saveItem(item, TextInst.translatable("nbteditor.max_stack_size.removed"));
			}
			return Command.SINGLE_SUCCESS;
		})).then(argument("size", IntegerArgumentType.integer(1, 99)).executes(context -> {
			int size = context.getArgument("size", Integer.class);
			ItemReference ref = ItemReference.getHeldItem();
			ItemStack item = ref.getItem();
			if (item.has(DataComponents.MAX_DAMAGE) && size > 1)
				MainUtil.client.player.sendSystemMessage(TextInst.translatable("nbteditor.max_stack_size.invalid_state"));
			else {
				if (item.getCount() > size)
					item.setCount(size);
				item.set(DataComponents.MAX_STACK_SIZE, size);
				ref.saveItem(item, TextInst.translatable("nbteditor.max_stack_size.added",
						TextInst.literal(size + "").formatted(ChatFormatting.GOLD)));
			}
			return Command.SINGLE_SUCCESS;
		}));
	}
	
}
