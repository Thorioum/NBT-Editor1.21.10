package com.luneruniverse.minecraft.mod.nbteditor.localnbt;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVQuaternionf;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;

public interface LocalNBT {
	public static Optional<LocalNBT> deserialize(NbtCompound nbt, int defaultDataVersion) {
		return Optional.ofNullable(switch (nbt.nbte$getString("type").orElse("item")) {
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
	
	public Text getName();
	public void setName(Text name);
	public String getDefaultName();
	
	public Identifier getId();
	public void setId(Identifier id);
	public Set<Identifier> getIdOptions();
	
	public NbtCompound getNBT();
	public void setNBT(NbtCompound nbt);
	public default NbtCompound getOrCreateNBT() {
		NbtCompound nbt = getNBT();
		if (nbt == null) {
			nbt = new NbtCompound();
			setNBT(nbt);
		}
		return nbt;
	}
	public default void modifyNBT(Consumer<NbtCompound> modifier) {
		NbtCompound nbt = getNBT();
		if (nbt == null)
			nbt = new NbtCompound();
		modifier.accept(nbt);
		setNBT(nbt);
	}
	
	public void renderIcon(Matrix3x2fStack matrices, int x, int y, float tickDelta);
	
	public Optional<ItemStack> toItem(boolean cleanup);
	public NbtCompound serialize();
	public Text toHoverableText();
	
	public LocalNBT copy();
	@Override
	public boolean equals(Object nbt);
}
