package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin;

import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import net.minecraft.core.component.DataComponents;
import org.spongepowered.asm.mixin.Mixin;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVComponentType;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.MVItemStackParent;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.IntegratedNBTManager;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.NBTManagers;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

@Mixin(ItemStack.class)
public class ItemStackMixin implements IntegratedNBTManager, MVItemStackParent {
	@Override
	public CompoundTag nbte$serialize(boolean requireSuccess) {
		return NBTManagers.ITEM.serialize((ItemStack) (Object) this, requireSuccess);
	}
	
	@Override
	public boolean nbte$hasNbt() {
		return NBTManagers.ITEM.hasNbt((ItemStack) (Object) this);
	}
	@Override
	public CompoundTag nbte$getNbt() {
		return NBTManagers.ITEM.getNbt((ItemStack) (Object) this);
	}
	@Override
	public CompoundTag nbte$getOrCreateNbt() {
		return NBTManagers.ITEM.getOrCreateNbt((ItemStack) (Object) this);
	}
	@Override
	public void nbte$setNbt(CompoundTag nbt) {
		NBTManagers.ITEM.setNbt((ItemStack) (Object) this, nbt);
	}
	
	
	private static final Supplier<Reflection.MethodInvoker> ItemStack_hasCustomName =
			Reflection.getOptionalMethod(ItemStack.class, "method_7938", MethodType.methodType(boolean.class));
	@Override
	public boolean nbte$hasCustomName() {
		if (NBTManagers.COMPONENTS_EXIST)
			return ((ItemStack) (Object) this).has(DataComponents.CUSTOM_NAME);
		else
			return ItemStack_hasCustomName.get().invoke(this);
	}
	private static final Supplier<Reflection.MethodInvoker> ItemStack_setCustomName =
			Reflection.getOptionalMethod(ItemStack.class, "method_7977", MethodType.methodType(ItemStack.class, Component.class));
	@Override
	public ItemStack nbte$setCustomName(Component name) {
		if (NBTManagers.COMPONENTS_EXIST)
			((ItemStack) (Object) this).set(DataComponents.CUSTOM_NAME, name);
		else
			ItemStack_setCustomName.get().invoke(this, name);
		return (ItemStack) (Object) this;
	}
}
