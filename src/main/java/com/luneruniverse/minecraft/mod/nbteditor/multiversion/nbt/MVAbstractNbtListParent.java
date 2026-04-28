package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt;

import java.lang.invoke.MethodType;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;

import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.Tag;

public interface MVAbstractNbtListParent {
	
	static final Supplier<Reflection.MethodInvoker> AbstractNbtList_getHeldType =
			Reflection.getOptionalMethod(CollectionTag.class, "method_10601", MethodType.methodType(byte.class));
	public default Optional<Byte> nbte$getHeldType() {
		throw new RuntimeException("Missing implementation for MVAbstractNbtListParent#nbte$getHeldType");
	}
	
	public default int size() {
		throw new RuntimeException("Missing implementation for MVAbstractNbtListParent#size");
	}
	
	public default boolean nbte$isEmpty() {
		throw new RuntimeException("Missing implementation for MVAbstractNbtListParent#nbte$isEmpty");
	}
	
	public default Iterable<Tag> nbte$iterable() {
		throw new RuntimeException("Missing implementation for MVAbstractNbtListParent#nbte$iterable");
	}
	
	public default Stream<Tag> nbte$stream() {
		throw new RuntimeException("Missing implementation for MVAbstractNbtListParent#nbte$stream");
	}
	
	public default Tag nbte$get(int index) {
		throw new RuntimeException("Missing implementation for MVAbstractNbtListParent#nbte$get");
	}
	
	public default void nbte$add(int index, Tag element) {
		throw new RuntimeException("Missing implementation for MVAbstractNbtListParent#nbte$add");
	}
	public default void nbte$add(Tag element) {
		throw new RuntimeException("Missing implementation for MVAbstractNbtListParent#nbte$add");
	}
	
	public default void nbte$set(int index, Tag element) {
		throw new RuntimeException("Missing implementation for MVAbstractNbtListParent#nbte$set");
	}
	
	public default Tag nbte$remove(int index) {
		throw new RuntimeException("Missing implementation for MVAbstractNbtListParent#nbte$remove");
	}
	
	public default void nbte$clear() {
		throw new RuntimeException("Missing implementation for MVAbstractNbtListParent#nbte$clear");
	}
	
}
