package com.luneruniverse.minecraft.mod.nbteditor.screens.nbtfolder;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.MVNbtCompoundParent;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTValue;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;

public class ListNBTFolder implements NBTFolder<CollectionTag> {
	
	private final Supplier<CollectionTag> get;
	private final Consumer<CollectionTag> set;
	
	public ListNBTFolder(Supplier<CollectionTag> get, Consumer<CollectionTag> set) {
		this.get = get;
		this.set = set;
	}
	
	@Override
	public CollectionTag getNBT() {
		return get.get();
	}
	
	@Override
	public void setNBT(CollectionTag value) {
		set.accept(value);
	}
	
	@Override
	public List<NBTValue> getEntries(NBTEditorScreen<?> screen) {
		CollectionTag nbt = getNBT();
		return IntStream.range(0, nbt.size())
				.mapToObj(i -> new NBTValue(screen, i + "", nbt.get(i), nbt)).collect(Collectors.toList());
	}
	
	@Override
	public boolean hasEmptyKey() {
		return false;
	}
	
	@Override
	public Tag getValue(String key) {
		CollectionTag nbt = getNBT();
		try {
			int i = Integer.parseInt(key);
			if (i < 0 || i >= nbt.size())
				return null;
			return nbt.get(i);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	@Override
	public void setValue(String key, Tag value) {
		CollectionTag nbt = getNBT();
		int i = Integer.parseInt(key);
		if (nbt.size() == 1 && i == 0 && nbt instanceof ListTag list) {
			if (MVNbtCompoundParent.NBT_CODE_REFACTORED) {
				list.setTag(0, value);
			} else {
				list.remove(0);
				list.add(value);
			}
			setNBT(nbt);
		} else {
			Tag convertedValue = convertToType(nbt, value);
			if (convertedValue != null) {
				nbt.setTag(i, convertedValue);
				setNBT(nbt);
			}
		}
	}
	
	@Override
	public void addKey(String key) {
		CollectionTag nbt = getNBT();
		nbt.addTag(Integer.parseInt(key), getDefaultValue(nbt));
		setNBT(nbt);
	}
	
	@Override
	public void removeKey(String key) {
		CollectionTag nbt = getNBT();
		try {
			int i = Integer.parseInt(key);
			if (i >= 0 && i < nbt.size()) {
				nbt.remove(i);
				setNBT(nbt);
			}
		} catch (NumberFormatException e) {}
	}
	
	@Override
	public Optional<String> getNextKey(Optional<String> pastingKey) {
		return Optional.of(getNBT().size() + "");
	}
	
	private Tag convertToType(CollectionTag nbt, Tag value) {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			return value;

		byte heldType = (byte) 0;
		for (Tag element : nbt) {
			if (heldType == 0)
				heldType = element.getId();
			else if (heldType != element.getId()){}
		}

		
		if (heldType == 0 || heldType == value.getId())
			return value;
		
		if (heldType == Tag.TAG_COMPOUND) {
			CompoundTag output = new CompoundTag();
			output.put("value", value);
			return output;
		}
		if (heldType == Tag.TAG_LIST) {
			ListTag output = new ListTag();
			output.add(value);
			return output;
		}
		if (heldType == Tag.TAG_STRING)
			return StringTag.valueOf(value.toString());
		
		if (value instanceof NumericTag num) {
			return switch (heldType) {
				case Tag.TAG_BYTE -> ByteTag.valueOf(num.byteValue());
				case Tag.TAG_SHORT -> ShortTag.valueOf(num.shortValue());
				case Tag.TAG_INT -> IntTag.valueOf(num.intValue());
				case Tag.TAG_LONG -> LongTag.valueOf(num.longValue());
				case Tag.TAG_FLOAT -> FloatTag.valueOf(num.floatValue());
				case Tag.TAG_DOUBLE -> DoubleTag.valueOf(num.doubleValue());
				case Tag.TAG_BYTE_ARRAY -> new ByteArrayTag(new byte[] {num.byteValue()});
				case Tag.TAG_INT_ARRAY -> new IntArrayTag(new int[] {num.intValue()});
				case Tag.TAG_LONG_ARRAY -> new LongArrayTag(new long[] {num.longValue()});
				default -> null;
			};
		}
		
		return null;
	}
	
	private Tag getDefaultValue(CollectionTag nbt) {
		byte heldType = (byte) 0;
		for (Tag element : (CollectionTag) nbt) {
			if (heldType == 0)
				heldType = element.getId();
			else if (heldType != element.getId()){}
		}
		return switch (heldType) {
			case Tag.TAG_BYTE -> ByteTag.ZERO;
			case Tag.TAG_SHORT -> ShortTag.valueOf((short) 0);
			case 0, Tag.TAG_INT -> IntTag.valueOf(0);
			case Tag.TAG_LONG -> LongTag.valueOf(0);
			case Tag.TAG_FLOAT -> FloatTag.ZERO;
			case Tag.TAG_DOUBLE -> DoubleTag.ZERO;
			case Tag.TAG_BYTE_ARRAY -> new ByteArrayTag(new byte[0]);
			case Tag.TAG_INT_ARRAY -> new IntArrayTag(new int[0]);
			case Tag.TAG_LONG_ARRAY -> new LongArrayTag(new long[0]);
			case Tag.TAG_LIST -> new ListTag();
			case Tag.TAG_COMPOUND -> new CompoundTag();
			case Tag.TAG_STRING -> StringTag.valueOf("");
			default -> throw new IllegalArgumentException("Unknown NBT type: " + heldType);
		};
	}
	
	@Override
	public Predicate<String> getKeyValidator(boolean renaming) {
		return MainUtil.intPredicate(() -> 0, () -> getNBT().size() + (renaming ? -1 : 0), false);
	}
	
	@Override
	public boolean handlesDuplicateKeys() {
		return true;
	}
	
}
