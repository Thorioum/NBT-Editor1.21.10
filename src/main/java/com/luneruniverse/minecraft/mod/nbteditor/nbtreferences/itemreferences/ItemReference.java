package com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences;

import java.lang.reflect.Proxy;
import java.util.function.Predicate;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItem;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItemStack;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.NBTManagers;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.Slot;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.Identifier;

public interface ItemReference extends NBTReference<LocalItem> {
	public static ItemReference getHeldItem(Predicate<ItemStack> isAllowed, Component failText) throws CommandSyntaxException {
		ItemStack item = MainUtil.client.player.getMainHandItem();
		InteractionHand hand = InteractionHand.MAIN_HAND;
		if (item == null || item.isEmpty() || !isAllowed.test(item)) {
			item = MainUtil.client.player.getOffhandItem();
			hand = InteractionHand.OFF_HAND;
		}
		if (item == null || item.isEmpty() || !isAllowed.test(item))
			throw new SimpleCommandExceptionType(failText).create();
		
		return new HandItemReference(hand);
	}
	public static ItemReference getHeldItem() throws CommandSyntaxException {
		return getHeldItem(item -> true, TextInst.translatable("nbteditor.no_hand.no_item.to_edit"));
	}
	public static ItemReference getHeldItemAirable() {
		try {
			return getHeldItem();
		} catch (CommandSyntaxException e) {
			return new HandItemReference(InteractionHand.MAIN_HAND);
		}
	}
	public static ItemReference getHeldAir() throws CommandSyntaxException {
		if (MainUtil.client.player.getMainHandItem().isEmpty())
			return new HandItemReference(InteractionHand.MAIN_HAND);
		if (MainUtil.client.player.getOffhandItem().isEmpty())
			return new HandItemReference(InteractionHand.OFF_HAND);
		throw new SimpleCommandExceptionType(TextInst.translatable("nbteditor.no_hand.all_item")).create();
	}
	
	public static ItemReference getContainerItem(AbstractContainerScreen<?> screen, Slot slot) {
		if (slot.container == MainUtil.client.player.getInventory()) {
			return new InventoryItemReference(slot.getContainerSlot()).setParent(
					() -> NBTEditorClient.CURSOR_MANAGER.showBranch(screen));
		}
		return new ServerItemReference(screen, slot.index);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends LocalNBT> NBTReference<T> toItemStackRef(NBTReference<T> ref) {
		if (ref instanceof ItemReference itemRef)
			return (NBTReference<T>) itemRef.toStackRef();
		return ref;
	}
	/**
	 * @see #toPartsRef() for deprecation details
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	public static <T extends LocalNBT> NBTReference<T> toItemPartsRef(NBTReference<T> ref) {
		if (ref instanceof ItemReference itemRef)
			return (NBTReference<T>) itemRef.toPartsRef();
		return ref;
	}
	
	public default ItemReference toStackRef() {
		return this;
	}
	/**
	 * Make sure to call {@link #toStackRef()} before passing this to any code not designed for a parts ref!<br>
	 * Also, make sure to never call {@link LocalItem#getEditableItem()}!
	 */
	@Deprecated
	public default ItemReference toPartsRef() {
		ItemReference stackRef = this;
		return (ItemReference) Proxy.newProxyInstance(ItemReference.class.getClassLoader(),
				new Class<?>[] {ItemReference.class}, (obj, method, args) -> {
			if (method.getName().equals("toStackRef")) {
				return stackRef;
			}
			if (method.getName().equals("toPartsRef")) {
				return obj;
			}
			
			Object output = method.invoke(stackRef, args);
			if (output instanceof LocalItem localItem)
				return localItem.toParts();
			return output;
		});
	}
	
	@Override
	public default LocalItem getLocalNBT() {
		return new LocalItemStack(getItem());
	}
	@Override
	public default void saveLocalNBT(LocalItem nbt, Runnable onFinished) {
		saveItem(nbt.getReadableItem(), onFinished);
	}
	
	public ItemStack getItem();
	public void saveItem(ItemStack toSave, Runnable onFinished);
	public default void saveItem(ItemStack toSave, Component msg) {
		saveItem(toSave, () -> MainUtil.client.player.sendSystemMessage(msg));
	}
	public default void saveItem(ItemStack toSave) {
		saveItem(toSave, () -> {});
	}
	
	public boolean isLocked();
	public boolean isLockable();
	
	/**
	 * Prevents a slot from being clicked or swapped while open in a container screen
	 * @return The slot to block (format: inv) or -1 if no slot should be blocked
	 */
	public int getBlockedSlot();
	
	@Override
	public default Identifier getId() {
		return MVRegistry.ITEM.getId(getItem().getItem());
	}
	@Override
	public default CompoundTag getNBT() {
		CompoundTag nbt = NBTManagers.ITEM.getNbt(getItem());
		if (nbt != null)
			return nbt;
		return new CompoundTag();
	}
	@Override
	public default void saveNBT(Identifier id, CompoundTag toSave, Runnable onFinished) {
		ItemStack item = getItem();
		if (!MVRegistry.ITEM.getId(item.getItem()).equals(id))
			item = MainUtil.setType(MVRegistry.ITEM.get(id), item);
		NBTManagers.ITEM.setNbt(item,toSave);
		saveItem(item, onFinished);
	}
	
	@Override
	public void showParent();
}
