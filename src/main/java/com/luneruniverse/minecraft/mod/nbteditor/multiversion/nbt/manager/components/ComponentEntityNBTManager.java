package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.components;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Attempt;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.DynamicRegistryManagerHolder;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.NBTManager;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.util.ErrorReporter;

public class ComponentEntityNBTManager implements NBTManager<Entity> {
	
	@Override
	public Attempt<NbtCompound> trySerialize(Entity subject) {
		NbtWriteView view = new NbtWriteView(ErrorReporter.EMPTY, NbtOps.INSTANCE,new NbtCompound());
		view.putString("id", EntityType.getId(subject.getType()).toString());
		subject.writeData(view);
		return new Attempt<>(view.getNbt());
	}
	
	@Override
	public boolean hasNbt(Entity subject) {
		return true;
	}
	@Override
	public NbtCompound getNbt(Entity subject) {
		NbtWriteView v = new NbtWriteView(ErrorReporter.EMPTY,NbtOps.INSTANCE,new NbtCompound());
		subject.writeData(v);
		return v.getNbt();
	}
	@Override
	public NbtCompound getOrCreateNbt(Entity subject) {
		return getNbt(subject);
	}
	@Override
	public void setNbt(Entity subject, NbtCompound nbt) {
		subject.readData(NbtReadView.create(ErrorReporter.EMPTY,DynamicRegistryManagerHolder.get(),nbt));
	}
	
}
