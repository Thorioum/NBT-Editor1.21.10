package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.nbt;

import java.lang.invoke.MethodType;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Attempt;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.DeserializableNBTManager;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public class NBTItemNBTManager implements DeserializableNBTManager<ItemStack> {
	
	private static final Reflection.MethodInvoker ItemStack_writeNbt =
			Reflection.getMethod(ItemStack.class, "method_7953", MethodType.methodType(CompoundTag.class, CompoundTag.class));
	@Override
	public Attempt<CompoundTag> trySerialize(ItemStack subject) {
		return new Attempt<>(ItemStack_writeNbt.invoke(subject, new CompoundTag()));
	}
	private static final Reflection.MethodInvoker ItemStack_fromNbt =
			Reflection.getMethod(ItemStack.class, "method_7915", MethodType.methodType(ItemStack.class, CompoundTag.class));
	@Override
	public Attempt<ItemStack> tryDeserialize(CompoundTag nbt) {
		return new Attempt<>(ItemStack_fromNbt.invoke(null, nbt.copy()));
	}
	
	private static final Reflection.MethodInvoker ItemStack_hasNbt =
			Reflection.getMethod(ItemStack.class, "method_7985", MethodType.methodType(boolean.class));
	@Override
	public boolean hasNbt(ItemStack subject) {
		return ItemStack_hasNbt.invoke(subject);
	}
	private static final Reflection.MethodInvoker ItemStack_getNbt =
			Reflection.getMethod(ItemStack.class, "method_7969", MethodType.methodType(CompoundTag.class));
	@Override
	public CompoundTag getNbt(ItemStack subject) {
		CompoundTag nbt = ItemStack_getNbt.invoke(subject);
		if (nbt == null)
			return null;
		return nbt.copy();
	}
	private static final Reflection.MethodInvoker ItemStack_getOrCreateNbt =
			Reflection.getMethod(ItemStack.class, "method_7948", MethodType.methodType(CompoundTag.class));
	@Override
	public CompoundTag getOrCreateNbt(ItemStack subject) {
		return ((CompoundTag) ItemStack_getOrCreateNbt.invoke(subject)).copy();
	}
	private static final Reflection.MethodInvoker ItemStack_setNbt =
			Reflection.getMethod(ItemStack.class, "method_7980", MethodType.methodType(void.class, CompoundTag.class));
	@Override
	public void setNbt(ItemStack subject, CompoundTag nbt) {
		ItemStack_setNbt.invoke(subject, nbt == null ? null : nbt.copy());
	}
	
}
