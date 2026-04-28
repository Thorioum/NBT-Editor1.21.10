package com.luneruniverse.minecraft.mod.nbteditor.tagreferences;

import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.ArraySplitTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.NBTTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

public class SignSideTagReferences {
	
	public static final TagReference<Boolean, CompoundTag> GLOWING = Version.<TagReference<Boolean, CompoundTag>>newSwitch()
			.range("1.20.0", null, () -> new NBTTagReference<>(Boolean.class, "has_glowing_text"))
			.range(null, "1.19.4", () -> new NBTTagReference<>(Boolean.class, "GlowingText"))
			.get();
	
	public static final TagReference<String, CompoundTag> COLOR = Version.<TagReference<String, CompoundTag>>newSwitch()
			.range("1.20.0", null, () -> new NBTTagReference<>(String.class, "color"))
			.range(null, "1.19.4", () -> new NBTTagReference<>(String.class, "Color"))
			.get();
	
	public static final TagReference<List<Component>, CompoundTag> TEXT = Version.<TagReference<List<Component>, CompoundTag>>newSwitch()
			.range("1.20.0", null, () -> TagReference.forLists(Component.class, new NBTTagReference<>(Component[].class, "messages")))
			.get();
	
}
