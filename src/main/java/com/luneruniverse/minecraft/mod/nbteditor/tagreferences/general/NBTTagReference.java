package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general;

import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.util.AbstractList;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;

import net.minecraft.nbt.*;

import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

public class NBTTagReference<T> implements TagReference<T, CompoundTag> {
	
	private static Object deserialize(Tag element, Class<?> target) {
		if (target.isArray()) {
			if (!(element instanceof CollectionTag list))
				return Array.newInstance(target.componentType(), 0);
			
			Object output = Array.newInstance(target.componentType(), list.size());
			for (int i = 0; i < list.size(); i++)
				Array.set(output, i, deserialize(list.get(i), target.componentType()));
			return output;
		}
		
		if (target.isAssignableFrom(Tag.class))
			return element.copy();
		if (target.isAssignableFrom(CompoundTag.class))
			return (element instanceof CompoundTag compound ? compound.copy() : new CompoundTag());
		if (target.isAssignableFrom(ListTag.class))
			return (element instanceof ListTag list ? list.copy() : new ListTag());
		
		Class<?> primitiveTarget = (target.isPrimitive() ? target : MethodType.methodType(target).unwrap().returnType());
		if (primitiveTarget.isPrimitive()) {
			if (primitiveTarget == boolean.class)
				return (element instanceof NumericTag num ? num.byteValue() != 0 : false);
			if (primitiveTarget == byte.class)
				return (element instanceof NumericTag num ? num.byteValue() : (byte) 0);
			if (primitiveTarget == short.class)
				return (element instanceof NumericTag num ? num.shortValue() : (short) 0);
			if (primitiveTarget == char.class)
				return (element instanceof NumericTag num ? (char) num.shortValue() : (char) 0);
			if (primitiveTarget == int.class)
				return (element instanceof NumericTag num ? num.intValue() : (int) 0);
			if (primitiveTarget == long.class)
				return (element instanceof NumericTag num ? num.longValue() : (long) 0);
			if (primitiveTarget == float.class)
				return (element instanceof NumericTag num ? num.floatValue() : (float) 0);
			if (primitiveTarget == double.class)
				return (element instanceof NumericTag num ? num.doubleValue() : (double) 0);
			throw new IllegalArgumentException("Unknown primitive type " + primitiveTarget.getName());
		}
		
		if (target.isAssignableFrom(String.class))
			return (element instanceof StringTag str ? MVMisc.value(str) : "");
		
		if (target.isAssignableFrom(Component.class)) {
			try {
				Component output = TextInst.fromMinecraft(element);
				if (output == null)
					return TextInst.of("");
				return output;
			} catch (IllegalArgumentException e) {
				return TextInst.of("");
			}
		}
		
		throw new IllegalArgumentException("Cannot get " + target.getName() + " from nbt!");
	}
	
	private static Tag serialize(Object value) {
		if (value == null)
			throw new IllegalArgumentException("Cannot convert null to nbt!");
		
		Class<?> valueType = value.getClass();
		
		if (valueType.isArray()) {
			Class<?> compType = valueType.componentType();
			if (compType.isPrimitive()) {
				if (compType == byte.class)
					return new ByteArrayTag((byte[]) value);
				if (compType == int.class)
					return new IntArrayTag((int[]) value);
				if (compType == long.class)
					return new LongArrayTag((long[]) value);
			}
			
			ListTag output = new ListTag();
			int length = Array.getLength(value);
			for (int i = 0; i < length; i++)
				output.add(serialize(Array.get(value, i)));
			return output;
		}
		
		if (Tag.class.isAssignableFrom(valueType))
			return ((Tag) value).copy();
		
		Class<?> primitiveValueType = (valueType.isPrimitive() ? valueType : MethodType.methodType(valueType).unwrap().returnType());
		if (primitiveValueType.isPrimitive()) {
			if (primitiveValueType == boolean.class)
				return ByteTag.valueOf((boolean) value);
			if (primitiveValueType == byte.class)
				return ByteTag.valueOf((byte) value);
			if (primitiveValueType == short.class)
				return ShortTag.valueOf((short) value);
			if (primitiveValueType == char.class)
				return ShortTag.valueOf((short) (char) value);
			if (primitiveValueType == int.class)
				return IntTag.valueOf((int) value);
			if (primitiveValueType == long.class)
				return LongTag.valueOf((long) value);
			if (primitiveValueType == float.class)
				return FloatTag.valueOf((float) value);
			if (primitiveValueType == double.class)
				return DoubleTag.valueOf((double) value);
			throw new IllegalArgumentException("Unknown primitive type " + primitiveValueType.getName());
		}
		
		if (CharSequence.class.isAssignableFrom(valueType))
			return StringTag.valueOf(((CharSequence) value).toString());
		
		if (Component.class.isAssignableFrom(valueType))
			return TextInst.toMinecraft((Component) value);
		
		throw new IllegalArgumentException("Cannot convert " + valueType.getName() + " to nbt!");
	}
	
	private static Tag manageNbt(CompoundTag nbt, String[] path, boolean write, Tag toWrite) {
		for (int i = 0; i < path.length - 1; i++) {
			Tag element = nbt.get(path[i]);
			if (element instanceof CompoundTag compound)
				nbt = compound;
			else if (write) {
				CompoundTag compound = new CompoundTag();
				nbt.put(path[i], compound);
				nbt = compound;
			} else
				return null;
		}
		String finalKey = path[path.length - 1];
		if (write) {
			if (toWrite == null)
				nbt.remove(finalKey);
			else
				nbt.put(finalKey, toWrite);
			return null;
		}
		return nbt.get(finalKey);
	}
	private static Tag getFromNbt(CompoundTag nbt, String[] path) {
		return manageNbt(nbt, path, false, null);
	}
	private static void setToNbt(CompoundTag nbt, String[] path, Tag value) {
		manageNbt(nbt, path, true, value);
	}
	private static void removeFromNbt(CompoundTag nbt, String[] path) {
		manageNbt(nbt, path, true, null);
	}
	
	private final Class<T> clazz;
	private final String[] path;
	
	public NBTTagReference(Class<T> clazz, String path) {
		this.clazz = clazz;
		this.path = path.split("/");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T get(CompoundTag object) {
		return (T) deserialize(object == null ? null : getFromNbt(object, path), clazz);
	}

	@Override
	public void set(CompoundTag object, T value) {
		if (value == null) {
			removeFromNbt(object, path);
			return;
		}
		setToNbt(object, path, serialize(value));
	}
	
}
