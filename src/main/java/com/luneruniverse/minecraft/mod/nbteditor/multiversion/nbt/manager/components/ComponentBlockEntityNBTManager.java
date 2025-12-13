package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.components;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Attempt;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.DynamicRegistryManagerHolder;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.NBTManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.ComponentMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ErrorReporter;

public class ComponentBlockEntityNBTManager implements NBTManager<BlockEntity> {
	
	private static final Codec<ComponentMap> BlockEntity_Components_CODEC = Version.<Codec<ComponentMap>>newSwitch()
			.range("1.21.5", null, () -> ComponentMap.CODEC)
			.get();

	private static class sErrorReporter implements ErrorReporter {
		public Error lastError = null;
		@Override
		public ErrorReporter makeChild(Context context) {
			return null;
		}

		@Override
		public void report(Error error) {
			lastError = error;
		}
	}
	@Override
	public Attempt<NbtCompound> trySerialize(BlockEntity subject) {
		// Based on BlockEntity#createNbtWithId
		
		RegistryWrapper.WrapperLookup registryLookup = DynamicRegistryManagerHolder.get();
		
		NbtCompound output = new NbtCompound();
		sErrorReporter errorReporter = new sErrorReporter();
		subject.writeDataWithId(new NbtWriteView(errorReporter,NbtOps.INSTANCE,output));
		return new Attempt<>(output, errorReporter.lastError == null ? null : errorReporter.lastError.getMessage());
	}
	
	@Override
	public boolean hasNbt(BlockEntity subject) {
		return true;
	}
	@Override
	public NbtCompound getNbt(BlockEntity subject) {
		return subject.createNbt(DynamicRegistryManagerHolder.get());
	}
	@Override
	public NbtCompound getOrCreateNbt(BlockEntity subject) {
		return getNbt(subject);
	}
	@Override
	public void setNbt(BlockEntity subject, NbtCompound nbt) {
		subject.read(NbtReadView.create(ErrorReporter.EMPTY,DynamicRegistryManagerHolder.get(),nbt));
	}
	
}
