package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.components;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Attempt;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.DynamicRegistryManagerHolder;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.NBTManager;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.util.ProblemReporter.PathElement;
import net.minecraft.util.ProblemReporter.Problem;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.util.ProblemReporter;

public class ComponentBlockEntityNBTManager implements NBTManager<BlockEntity> {
	
	private static final Codec<DataComponentMap> BlockEntity_Components_CODEC = Version.<Codec<DataComponentMap>>newSwitch()
			.range("1.21.5", null, () -> DataComponentMap.CODEC)
			.get();

	private static class sErrorReporter implements ProblemReporter {
		public Problem lastError = null;
		@Override
		public ProblemReporter forChild(PathElement context) {
			return null;
		}

		@Override
		public void report(Problem error) {
			lastError = error;
		}
	}
	@Override
	public Attempt<CompoundTag> trySerialize(BlockEntity subject) {
		// Based on BlockEntity#createNbtWithId
		
		HolderLookup.Provider registryLookup = (MainUtil.client.getConnection() == null ? VanillaRegistries.createLookup() : MainUtil.client.getConnection().registryAccess());
		
		CompoundTag output = new CompoundTag();
		sErrorReporter errorReporter = new sErrorReporter();
		subject.saveWithId(new TagValueOutput(errorReporter,NbtOps.INSTANCE,output));
		return new Attempt<>(output, errorReporter.lastError == null ? null : errorReporter.lastError.description());
	}
	
	@Override
	public boolean hasNbt(BlockEntity subject) {
		return true;
	}
	@Override
	public CompoundTag getNbt(BlockEntity subject) {
		return subject.saveWithoutMetadata((MainUtil.client.getConnection() == null ? VanillaRegistries.createLookup() : MainUtil.client.getConnection().registryAccess()));
	}
	@Override
	public CompoundTag getOrCreateNbt(BlockEntity subject) {
		return getNbt(subject);
	}
	@Override
	public void setNbt(BlockEntity subject, CompoundTag nbt) {
		subject.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,(MainUtil.client.getConnection() == null ? VanillaRegistries.createLookup() : MainUtil.client.getConnection().registryAccess()),nbt));
	}
	
}
