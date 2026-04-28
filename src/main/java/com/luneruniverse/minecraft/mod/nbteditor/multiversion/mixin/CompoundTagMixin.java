package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin;

import java.lang.invoke.MethodType;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.spongepowered.asm.mixin.Mixin;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.MVNbtCompoundParent;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.UUIDUtil;

@Mixin(CompoundTag.class)
public class CompoundTagMixin implements MVNbtCompoundParent {
	
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_contains =
			Reflection.getOptionalMethod(CompoundTag.class, "method_10573", MethodType.methodType(boolean.class, String.class, int.class));
	public boolean nbte$contains(String key, byte type) {
		if (NBT_CODE_REFACTORED) {
			Tag value = ((CompoundTag) (Object) this).get(key);
			return value != null && (value.getId() == type || type == NUMBER_TYPE &&
					value.getId() >= Tag.TAG_BYTE && value.getId() <= Tag.TAG_DOUBLE);
		}
		return NbtCompound_contains.get().invoke(this, key, type);
	}
	
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_containsUuid =
			Reflection.getOptionalMethod(CompoundTag.class, "method_25928", MethodType.methodType(boolean.class, String.class));
	public boolean nbte$containsUuid(String key) {
		if (NBT_CODE_REFACTORED)
			return ((CompoundTag) (Object) this).read(key, UUIDUtil.CODEC).isPresent();
		return NbtCompound_containsUuid.get().invoke(this, key);
	}
	
	public Optional<Byte> nbte$getByte(String key) {
		if (NBT_CODE_REFACTORED)
			return ((CompoundTag) (Object) this).getByte(key);
		if (nbte$contains(key, NUMBER_TYPE))
			return Optional.of(nbte$getByteOrDefault(key));
		return Optional.empty();
	}
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getByte =
			Reflection.getOptionalMethod(CompoundTag.class, "method_10571", MethodType.methodType(byte.class, String.class));
	public byte nbte$getByteOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((CompoundTag) (Object) this).getByteOr(key, (byte) 0);
		return NbtCompound_getByte.get().invoke(this, key);
	}
	
	public Optional<Short> nbte$getShort(String key) {
		if (NBT_CODE_REFACTORED)
			return ((CompoundTag) (Object) this).getShort(key);
		if (nbte$contains(key, NUMBER_TYPE))
			return Optional.of(nbte$getShortOrDefault(key));
		return Optional.empty();
	}
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getShort =
			Reflection.getOptionalMethod(CompoundTag.class, "method_10568", MethodType.methodType(short.class, String.class));
	public short nbte$getShortOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((CompoundTag) (Object) this).getShortOr(key, (short) 0);
		return NbtCompound_getShort.get().invoke(this, key);
	}
	
	public Optional<Integer> nbte$getInt(String key) {
		if (NBT_CODE_REFACTORED)
			return ((CompoundTag) (Object) this).getInt(key);
		if (nbte$contains(key, NUMBER_TYPE))
			return Optional.of(nbte$getIntOrDefault(key));
		return Optional.empty();
	}
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getInt =
			Reflection.getOptionalMethod(CompoundTag.class, "method_10550", MethodType.methodType(int.class, String.class));
	public int nbte$getIntOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((CompoundTag) (Object) this).getIntOr(key, 0);
		return NbtCompound_getInt.get().invoke(this, key);
	}
	
	public Optional<Long> nbte$getLong(String key) {
		if (NBT_CODE_REFACTORED)
			return ((CompoundTag) (Object) this).getLong(key);
		if (nbte$contains(key, NUMBER_TYPE))
			return Optional.of(nbte$getLongOrDefault(key));
		return Optional.empty();
	}
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getLong =
			Reflection.getOptionalMethod(CompoundTag.class, "method_10537", MethodType.methodType(long.class, String.class));
	public long nbte$getLongOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((CompoundTag) (Object) this).getLongOr(key, 0);
		return NbtCompound_getLong.get().invoke(this, key);
	}
	
	public Optional<Float> nbte$getFloat(String key) {
		if (NBT_CODE_REFACTORED)
			return ((CompoundTag) (Object) this).getFloat(key);
		if (nbte$contains(key, NUMBER_TYPE))
			return Optional.of(nbte$getFloatOrDefault(key));
		return Optional.empty();
	}
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getFloat =
			Reflection.getOptionalMethod(CompoundTag.class, "method_10583", MethodType.methodType(float.class, String.class));
	public float nbte$getFloatOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((CompoundTag) (Object) this).getFloatOr(key, 0);
		return NbtCompound_getFloat.get().invoke(this, key);
	}
	
	public Optional<Double> nbte$getDouble(String key) {
		if (NBT_CODE_REFACTORED)
			return ((CompoundTag) (Object) this).getDouble(key);
		if (nbte$contains(key, NUMBER_TYPE))
			return Optional.of(nbte$getDoubleOrDefault(key));
		return Optional.empty();
	}
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getDouble =
			Reflection.getOptionalMethod(CompoundTag.class, "method_10574", MethodType.methodType(double.class, String.class));
	public double nbte$getDoubleOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((CompoundTag) (Object) this).getDoubleOr(key, 0);
		return NbtCompound_getDouble.get().invoke(this, key);
	}
	
	public Optional<String> nbte$getString(String key) {
		if (NBT_CODE_REFACTORED)
			return ((CompoundTag) (Object) this).getString(key);
		if (nbte$contains(key, Tag.TAG_STRING))
			return Optional.of(nbte$getStringOrDefault(key));
		return Optional.empty();
	}
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getString =
			Reflection.getOptionalMethod(CompoundTag.class, "method_10558", MethodType.methodType(String.class, String.class));
	public String nbte$getStringOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((CompoundTag) (Object) this).getStringOr(key, "");
		return NbtCompound_getString.get().invoke(this, key);
	}
	
	public Optional<byte[]> nbte$getByteArray(String key) {
		if (NBT_CODE_REFACTORED)
			return ((CompoundTag) (Object) this).getByteArray(key);
		if (nbte$contains(key, Tag.TAG_BYTE_ARRAY))
			return Optional.of(nbte$getByteArrayOrDefault(key));
		return Optional.empty();
	}
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getByteArray =
			Reflection.getOptionalMethod(CompoundTag.class, "method_10547", MethodType.methodType(byte[].class, String.class));
	public byte[] nbte$getByteArrayOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((CompoundTag) (Object) this).getByteArray(key).orElseGet(() -> new byte[0]);
		return NbtCompound_getByteArray.get().invoke(this, key);
	}
	
	public Optional<int[]> nbte$getIntArray(String key) {
		if (NBT_CODE_REFACTORED)
			return ((CompoundTag) (Object) this).getIntArray(key);
		if (nbte$contains(key, Tag.TAG_INT_ARRAY))
			return Optional.of(nbte$getIntArrayOrDefault(key));
		return Optional.empty();
	}
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getIntArray =
			Reflection.getOptionalMethod(CompoundTag.class, "method_10561", MethodType.methodType(int[].class, String.class));
	public int[] nbte$getIntArrayOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((CompoundTag) (Object) this).getIntArray(key).orElseGet(() -> new int[0]);
		return NbtCompound_getIntArray.get().invoke(this, key);
	}
	
	public Optional<long[]> nbte$getLongArray(String key) {
		if (NBT_CODE_REFACTORED)
			return ((CompoundTag) (Object) this).getLongArray(key);
		if (nbte$contains(key, Tag.TAG_LONG_ARRAY))
			return Optional.of(nbte$getLongArrayOrDefault(key));
		return Optional.empty();
	}
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getLongArray =
			Reflection.getOptionalMethod(CompoundTag.class, "method_10565", MethodType.methodType(long[].class, String.class));
	public long[] nbte$getLongArrayOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((CompoundTag) (Object) this).getLongArray(key).orElseGet(() -> new long[0]);
		return NbtCompound_getLongArray.get().invoke(this, key);
	}
	
	public Optional<CompoundTag> nbte$getCompound(String key) {
		if (NBT_CODE_REFACTORED)
			return ((CompoundTag) (Object) this).getCompound(key);
		if (nbte$contains(key, Tag.TAG_COMPOUND))
			return Optional.of(nbte$getCompoundOrEmpty(key));
		return Optional.empty();
	}
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getCompound =
			Reflection.getOptionalMethod(CompoundTag.class, "method_10562", MethodType.methodType(CompoundTag.class, String.class));
	public CompoundTag nbte$getCompoundOrEmpty(String key) {
		if (NBT_CODE_REFACTORED)
			return ((CompoundTag) (Object) this).getCompoundOrEmpty(key);
		return NbtCompound_getCompound.get().invoke(this, key);
	}
	
	public Optional<ListTag> nbte$getList(String key) {
		if (NBT_CODE_REFACTORED)
			return ((CompoundTag) (Object) this).getList(key);
		if (nbte$contains(key, Tag.TAG_LIST))
			return Optional.of(nbte$getListOrDefault(key));
		return Optional.empty();
	}
	public ListTag nbte$getListOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((CompoundTag) (Object) this).getListOrEmpty(key);
		if (((CompoundTag) (Object) this).get(key) instanceof ListTag list)
			return list;
		return new ListTag();
	}
	public Optional<ListTag> nbte$getList(String key, byte type) {
		if (NBT_CODE_REFACTORED)
			return ((CompoundTag) (Object) this).getList(key).filter(list -> list.stream().allMatch(element -> element.getId() == type));
		if (nbte$contains(key, Tag.TAG_LIST)) {
			ListTag list = nbte$getListOrDefault(key);
			byte ht = (byte) 0;
			for (Tag element : list) {
				if (ht == 0)
					ht = element.getId();
				else if (ht != element.getId()){}
			}
			if (list.isEmpty() || ht == type)
				return Optional.of(list);
		}
		return Optional.empty();
	}
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getList =
			Reflection.getOptionalMethod(CompoundTag.class, "method_10554", MethodType.methodType(ListTag.class, String.class, int.class));
	public ListTag nbte$getListOrDefault(String key, byte type) {
		if (NBT_CODE_REFACTORED)
			return nbte$getList(key, type).orElseGet(ListTag::new);
		return NbtCompound_getList.get().invoke(this, key, type);
	}
	public Optional<ListTag> nbte$getPartialList(String key, byte type) {
		if (NBT_CODE_REFACTORED) {
			return ((CompoundTag) (Object) this).getList(key).map(list -> list.stream().filter(element -> element.getId() == type)
					.collect(Collectors.toCollection(ListTag::new)));
		}
		return nbte$getList(key, type);
	}
	public ListTag nbte$getPartialListOrDefault(String key, byte type) {
		if (NBT_CODE_REFACTORED)
			return nbte$getPartialList(key, type).orElseGet(ListTag::new);
		return nbte$getListOrDefault(key, type);
	}
	
	public Optional<Boolean> nbte$getBoolean(String key) {
		return nbte$getByte(key).map(b -> b != 0);
	}
	public boolean nbte$getBooleanOrDefault(String key) {
		return nbte$getByteOrDefault(key) != 0;
	}
	
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getUuid =
			Reflection.getOptionalMethod(CompoundTag.class, "method_25926", MethodType.methodType(UUID.class, String.class));
	public Optional<UUID> nbte$getUuid(String key) {
		if (NBT_CODE_REFACTORED)
			return ((CompoundTag) (Object) this).read(key, UUIDUtil.CODEC);
		if (nbte$containsUuid(key))
			return Optional.of(NbtCompound_getUuid.get().invoke(this, key));
		return Optional.empty();
	}
	
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_putUuid =
			Reflection.getOptionalMethod(CompoundTag.class, "method_25927", MethodType.methodType(void.class, String.class, UUID.class));
	public void nbte$putUuid(String key, UUID uuid) {
		if (NBT_CODE_REFACTORED)
			((CompoundTag) (Object) this).store(key, UUIDUtil.CODEC, uuid);
		else
			NbtCompound_putUuid.get().invoke(this, key, uuid);
	}
	
}
