package com.luneruniverse.minecraft.mod.nbteditor.tagreferences;

import java.util.Optional;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalBlock;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.NBTComponentTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.GameProfileNBTTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.GameProfileNameNBTTagReference;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;

import net.minecraft.world.item.component.ResolvableProfile;

public class BlockTagReferences {
	
	public static final TagReference<Optional<String>, LocalBlock> PROFILE_NAME = Version.<TagReference<Optional<String>, LocalBlock>>newSwitch()
			.range("1.20.5", null, () -> TagReference.forLocalNBT(Optional::empty,
					new NBTComponentTagReference<>("profile", ResolvableProfile.CODEC, Optional::empty,
							ResolvableProfile::name,
							name -> ResolvableProfile.createUnresolved(name.orElse("")))))
			.range(null, "1.20.4", () -> TagReference.forLocalNBT(Optional::empty, new GameProfileNameNBTTagReference()))
			.get();
	public static final TagReference<Optional<GameProfile>, LocalBlock> PROFILE = Version.<TagReference<Optional<GameProfile>, LocalBlock>>newSwitch()
			.range("1.20.5", null, () -> TagReference.forLocalNBT(Optional::empty,
					new NBTComponentTagReference<>("profile", ResolvableProfile.CODEC, Optional::empty,
							profile -> Optional.of(profile.partialProfile()),
							profile -> profile.map(ResolvableProfile::createResolved).orElse(null))))
			.range(null, "1.20.4", () -> TagReference.forLocalNBT(Optional::empty, new GameProfileNBTTagReference()))
			.get();
	
}
