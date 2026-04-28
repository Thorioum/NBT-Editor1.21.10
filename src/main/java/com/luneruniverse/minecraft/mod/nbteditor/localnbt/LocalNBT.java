package com.luneruniverse.minecraft.mod.nbteditor.localnbt;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVQuaternionf;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fStack;

public interface LocalNBT {
	public static Optional<LocalNBT> deserialize(CompoundTag nbt, int defaultDataVersion) {
		return Optional.ofNullable(switch (nbt.getString("type").orElse("item")) {
			case "item" -> LocalItemStack.deserialize(nbt, defaultDataVersion);
			case "block" -> LocalBlock.deserialize(nbt, defaultDataVersion);
			case "entity" -> LocalEntity.deserialize(nbt, defaultDataVersion);
			default -> null;
		});
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends LocalNBT> T copy(T localNBT) {
		return (T) localNBT.copy();
	}

	
	public default boolean isEmpty() {
		return isEmpty(getId());
	}
	public boolean isEmpty(Identifier id);
	
	public Component getName();
	public void setName(Component name);
	public String getDefaultName();
	
	public Identifier getId();
	public void setId(Identifier id);
	public Set<Identifier> getIdOptions();
	
	public CompoundTag getNBT();
	public void setNBT(CompoundTag nbt);
	public default CompoundTag getOrCreateNBT() {
		CompoundTag nbt = getNBT();
		if (nbt == null) {
			nbt = new CompoundTag();
			setNBT(nbt);
		}
		return nbt;
	}
	public default void modifyNBT(Consumer<CompoundTag> modifier) {
		CompoundTag nbt = getNBT();
		if (nbt == null)
			nbt = new CompoundTag();
		modifier.accept(nbt);
		setNBT(nbt);
	}
	
	public void renderIcon(Matrix3x2fStack matrices, int x, int y, float tickDelta);
	
	public Optional<ItemStack> toItem(boolean cleanup);
	public CompoundTag serialize();
	public Component toHoverableText();
	
	public LocalNBT copy();
	@Override
	public boolean equals(Object nbt);
}
