package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.MVAbstractNbtListParent;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.MVNbtCompoundParent;

import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;

@Mixin(CollectionTag.class)
public interface CollectionTagMixin extends MVAbstractNbtListParent {
	
	public default Optional<Byte> nbte$getHeldType() {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED) {
			byte heldType = (byte) 0;
			for (Tag element : (CollectionTag) (Object) this) {
				if (heldType == 0)
					heldType = element.getId();
				else if (heldType != element.getId())
					return Optional.empty();
			}
			return Optional.of(heldType);
		}
		return Optional.of(AbstractNbtList_getHeldType.get().invoke(this));
	}
	
	public default int size() {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			return ((CollectionTag) (Object) this).size();
		return ((List<?>) this).size();
	}
	
	public default boolean nbte$isEmpty() {
		return size() == 0;
	}
	
	@SuppressWarnings("unchecked")
	public default Iterable<Tag> nbte$iterable() {
		return (Iterable<Tag>) this;
	}
	
	@SuppressWarnings("unchecked")
	public default Stream<Tag> nbte$stream() {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			return ((CollectionTag) (Object) this).stream();
		return ((List<Tag>) this).stream();
	}
	
	public default Tag nbte$get(int index) {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			return ((CollectionTag) (Object) this).get(index);
		return (Tag) ((List<?>) this).get(index);
	}
	
	@SuppressWarnings("unchecked")
	public default void nbte$add(int index, Tag element) {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			((CollectionTag) (Object) this).addTag(index, element);
		else
			((List<Tag>) this).add(cast(element));
	}
	public default void nbte$add(Tag element) {
		nbte$add(size(), element);
	}
	
	@SuppressWarnings("unchecked")
	public default void nbte$set(int index, Tag element) {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			((CollectionTag) (Object) this).setTag(index, element);
		else
			((List<Tag>) this).set(index, cast(element));
	}
	
	public default Tag nbte$remove(int index) {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			return ((CollectionTag) (Object) this).remove(index);
		return (Tag) ((List<?>) this).remove(index);
	}
	
	public default void nbte$clear() {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			((CollectionTag) (Object) this).clear();
		else
			((List<?>) this).clear();
	}
	
	private Tag cast(Tag element) {
		if ((Object) this instanceof ByteArrayTag || (Object) this instanceof IntArrayTag || (Object) this instanceof LongArrayTag) {
			if (element instanceof NumericTag)
				return element;
			throw new ClassCastException("Cannot add a " + element.getClass().getName() + " to a " + this.getClass().getName());
		}
		
		if ((Object) this instanceof ListTag) {
			int heldType = nbte$getHeldType().get();
			if (heldType == 0 || heldType == element.getId())
				return element;
			throw new ClassCastException("Cannot add a " + element.getClass().getName());
		}
		
		throw new IllegalStateException("Unknown AbstractNbtList type: " + this.getClass().getName());
	}
	
}
