package com.luneruniverse.minecraft.mod.nbteditor.localnbt;

import java.util.Optional;
import java.util.Set;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.NBTManagers;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.components.ComponentItemNBTManager;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fStack;

public class LocalItemStack extends LocalItem {
	
	public static LocalItemStack deserialize(CompoundTag nbt, int defaultDataVersion) {
		return new LocalItemStack(NBTManagers.ITEM.deserialize(
				MainUtil.updateDynamic(References.ITEM_STACK, nbt, defaultDataVersion), true));
	}
	
	private ItemStack item;
	
	public LocalItemStack(ItemStack item) {
		this.item = item;
	}
	
	@Override
	public LocalItemStack toStack() {
		return this;
	}
	@Override
	public LocalItemParts toParts() {
		return new LocalItemParts(item);
	}
	
	@Override
	public ItemStack getEditableItem() {
		return item;
	}
	@Override
	public ItemStack getReadableItem() {
		return item;
	}
	
	@Override
	public boolean isEmpty() {
		return item.isEmpty();
	}
	@Override
	public boolean isEmpty(Identifier id) {
		return MVRegistry.ITEM.get(id) == Items.AIR;
	}
	
	@Override
	public Component getName() {
		return MainUtil.getCustomItemNameSafely(item);
	}
	@Override
	public void setName(Component name) {
		item.set(DataComponents.CUSTOM_NAME,name);
	}
	@Override
	public String getDefaultName() {
		return MainUtil.getBaseItemNameSafely(item).getString();
	}
	
	@Override
	public Item getItemType() {
		return item.getItem();
	}
	@Override
	public Identifier getId() {
		return MVRegistry.ITEM.getId(item.getItem());
	}
	@Override
	public void setId(Identifier id) {
		item = MainUtil.setType(MVRegistry.ITEM.get(id), item);
	}
	@Override
	public Set<Identifier> getIdOptions() {
		return MVRegistry.ITEM.getIds();
	}
	
	@Override
	public int getCount() {
		return item.getCount();
	}
	@Override
	public void setCount(int count) {
		item = MainUtil.setType(item.getItem(), item, count);
	}
	
	@Override
	public CompoundTag getNBT() {
		return NBTManagers.ITEM.serialize(item,true);
	}
	@Override
	public void setNBT(CompoundTag nbt) {
		NBTManagers.ITEM.setNbt(item,nbt);
	}
	@Override
	public CompoundTag getOrCreateNBT() {
		return NBTManagers.ITEM.getOrCreateNbt(item);
	}
	
	@Override
	public void renderIcon(Matrix3x2fStack matrices, int x, int y, float tickDelta) {
		MVDrawableHelper.renderItem(matrices, 200.0F, true, item, x, y);
	}
	
	@Override
	public Optional<ItemStack> toItem(boolean cleanup) {
		return Optional.of(item.copy());
	}
	@Override
	public CompoundTag serialize() {
		CompoundTag output = NBTManagers.ITEM.serialize(item,true);
		output.putString("type", "item");
		return output;
	}
	@Override
	public Component toHoverableText() {
		return item.getDisplayName();
	}
	
	@Override
	public LocalItemStack copy() {
		return new LocalItemStack(MainUtil.copyAirable(item));
	}
	
}
