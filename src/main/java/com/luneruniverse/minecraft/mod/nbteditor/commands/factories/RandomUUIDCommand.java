package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.literal;

import java.util.UUID;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.ItemTagReferences;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.ChatFormatting;

public class RandomUUIDCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "randomuuid";
	}
	
	@Override
	public String getExtremeAlias() {
		return "ru";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		Command<FabricClientCommandSource> add = context -> {
			ItemReference ref = ItemReference.getHeldItem();
			ItemStack item = ref.getItem();
			CompoundTag nbt = ItemTagReferences.CUSTOM_DATA.get(item);
			UUID uuid = UUID.randomUUID();
			nbt.putIntArray("UUID", UUIDUtil.uuidToIntArray(uuid));
			ItemTagReferences.CUSTOM_DATA.set(item, nbt);
			ref.saveItem(item, TextInst.translatable("nbteditor.random_uuid.added",
					TextInst.literal(uuid.toString()).formatted(ChatFormatting.GOLD)));
			return Command.SINGLE_SUCCESS;
		};
		Command<FabricClientCommandSource> remove = context -> {
			ItemReference ref = ItemReference.getHeldItem();
			ItemStack item = ref.getItem();
			CompoundTag nbt = ItemTagReferences.CUSTOM_DATA.get(item);
			if (!(nbt.get("UUID") instanceof IntArrayTag)) {
				MainUtil.client.player.sendSystemMessage(TextInst.translatable("nbteditor.random_uuid.already_removed"));
				return Command.SINGLE_SUCCESS;
			}
			nbt.remove("UUID");
			ItemTagReferences.CUSTOM_DATA.set(item, nbt);
			ref.saveItem(item, TextInst.translatable("nbteditor.random_uuid.removed"));
			return Command.SINGLE_SUCCESS;
		};
		
		builder
				.then(literal("add").executes(add))
				.then(literal("remove").executes(remove))
				.executes(add);
	}
	
}
