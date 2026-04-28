package com.luneruniverse.minecraft.mod.nbteditor.commands.arguments;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.Identifier;

public class SummonableEntityArgumentType implements ArgumentType<EntityType<?>> {
	private static final Collection<String> EXAMPLES = Arrays.asList("minecraft:pig", "cow");
	
	public static SummonableEntityArgumentType summonableEntity() {
		return new SummonableEntityArgumentType();
	}
	
	public EntityType<?> parse(StringReader stringReader) throws CommandSyntaxException {
		return MVRegistry.ENTITY_TYPE.getOrEmpty(Identifier.read(stringReader)).filter(EntityType::canSummon)
				.orElseThrow(() -> CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(stringReader));
	}
	
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return SharedSuggestionProvider.suggestResource(MVRegistry.ENTITY_TYPE.getEntrySet().stream()
				.filter(entry -> entry.getValue().canSummon()).map(Map.Entry::getKey), builder);
	}
	
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}