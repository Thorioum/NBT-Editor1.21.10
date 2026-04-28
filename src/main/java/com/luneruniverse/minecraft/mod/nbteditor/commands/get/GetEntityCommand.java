package com.luneruniverse.minecraft.mod.nbteditor.commands.get;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.commands.arguments.SummonableEntityArgumentType;
import com.luneruniverse.minecraft.mod.nbteditor.integrations.NBTAutocompleteIntegration;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalEntity;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;

import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.world.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public class GetEntityCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "entity";
	}
	
	@Override
	public String getExtremeAlias() {
		return "e";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		Command<FabricClientCommandSource> getEntity = context -> {
			EntityType<?> entityType = context.getArgument("entity", EntityType.class);
			
			Coordinates posArg = getDefaultArg(context, "pos", null, Coordinates.class);
			Vec3 pos = (posArg == null ? null : posArg.getPosition(MVMisc.getCommandSource(context.getSource().getPlayer())));
			
			CompoundTag nbtArg = getDefaultArg(context, "nbt", new CompoundTag(), CompoundTag.class);
			
			LocalEntity entity = new LocalEntity(entityType, nbtArg);
			
			if (pos == null) {
				entity.toItem(false).ifPresentOrElse(MainUtil::getWithMessage,
						() -> MainUtil.client.player.sendSystemMessage(TextInst.translatable("nbteditor.nbt.export.item.error")));
			} else if (NBTEditorClient.SERVER_CONN.isEditingExpanded())
				entity.summon(MainUtil.client.level.dimension(), pos);
			else
				MainUtil.client.player.sendSystemMessage(TextInst.translatable("nbteditor.requires_server"));
			
			return Command.SINGLE_SUCCESS;
		};
		SuggestionProvider<FabricClientCommandSource> nbtSuggestions = (context, suggestionsBuilder) -> {
			if (NBTAutocompleteIntegration.INSTANCE.isEmpty())
				return Suggestions.empty();
			EntityType<?> entityType = context.getArgument("entity", EntityType.class);
			String name = "entity/" + EntityType.getKey(entityType);
			String tag = suggestionsBuilder.getRemaining();
			return NbtSuggestionManager.loadFromName(name, tag, suggestionsBuilder, false);
		};
		
		builder.then(argument("entity", SummonableEntityArgumentType.summonableEntity())
				.then(argument("pos", Vec3Argument.vec3())
						.then(argument("nbt", CompoundTagArgument.compoundTag())
								.suggests(nbtSuggestions)
								.executes(getEntity))
						.executes(getEntity))
				.then(argument("nbt", CompoundTagArgument.compoundTag())
						.suggests(nbtSuggestions)
						.executes(getEntity))
				.executes(getEntity));
	}
	
}
