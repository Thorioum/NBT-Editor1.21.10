package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.literal;

import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReferenceFilter;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.factories.AttributesScreen;
import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.ItemTagReferences;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.AttributeData;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.AttributeData.AttributeModifierData.AttributeModifierId;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

public class AttributesCommand extends ClientCommand {
	
	public static final NBTReferenceFilter ATTRIBUTES_FILTER = NBTReferenceFilter.create(
			ref -> true,
			null,
			ref -> ServerMVMisc.createEntity(ref.getEntityType(), MainUtil.client.level) instanceof Mob,
			TextInst.translatable("nbteditor.no_ref.attributes"),
			TextInst.translatable("nbteditor.no_hand.no_item.to_edit"));
	
	@Override
	public String getName() {
		return "attributes";
	}
	
	@Override
	public String getExtremeAlias() {
		return "a";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		builder.then(literal("newuuids").executes(context -> {
			ItemReference ref = ItemReference.getHeldItem();
			ItemStack item = ref.getItem();
			List<AttributeData> attributes = ItemTagReferences.ATTRIBUTES.get(item);
			if (attributes.isEmpty())
				MainUtil.client.player.sendSystemMessage(TextInst.translatable("nbteditor.attributes.new_uuids.no_attributes"));
			else {
				attributes.replaceAll(attribute -> new AttributeData(attribute.attribute(), attribute.value(),
						attribute.modifierData().get().operation(), attribute.modifierData().get().slot(), AttributeModifierId.randomUUID()));
				ItemTagReferences.ATTRIBUTES.set(item, attributes);
				ref.saveItem(item, () -> MainUtil.client.player.sendSystemMessage(TextInst.translatable("nbteditor.attributes.new_uuids.success")));
			}
			return Command.SINGLE_SUCCESS;
		})).executes(context -> {
			NBTReference.getReference(ATTRIBUTES_FILTER, false, ref -> MainUtil.client.setScreen(new AttributesScreen<>(ref)));
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
