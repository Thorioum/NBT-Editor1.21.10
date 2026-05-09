package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.nbt;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Attempt;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.DynamicRegistryManagerHolder;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.NBTManager;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.util.ProblemReporter;

public class NBTEntityNBTManager implements NBTManager<Entity> {
	
	@Override
	public Attempt<CompoundTag> trySerialize(Entity subject) {
		CompoundTag nbt = new CompoundTag();
		nbt.putString("id", EntityType.getKey(subject.getType()).toString());
		subject.saveWithoutId(new TagValueOutput(ProblemReporter.DISCARDING, NbtOps.INSTANCE,nbt));
		return new Attempt<>(nbt);
	}
	
	@Override
	public boolean hasNbt(Entity subject) {
		return true;
	}
	@Override
	public CompoundTag getNbt(Entity subject) {
		TagValueOutput v = new TagValueOutput(ProblemReporter.DISCARDING, NbtOps.INSTANCE,new CompoundTag());
		subject.saveWithoutId(v);
		return v.buildResult();
	}
	@Override
	public CompoundTag getOrCreateNbt(Entity subject) {
		return getNbt(subject);
	}
	@Override
	public void setNbt(Entity subject, CompoundTag nbt) {
		subject.load(TagValueInput.create(ProblemReporter.DISCARDING, (MainUtil.client.getConnection() == null ? VanillaRegistries.createLookup() : MainUtil.client.getConnection().registryAccess()),nbt));
	}
	
}
