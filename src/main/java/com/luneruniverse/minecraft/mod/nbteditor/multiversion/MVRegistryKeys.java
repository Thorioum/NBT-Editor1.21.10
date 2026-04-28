package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class MVRegistryKeys {
	
	private static final Class<?> REGISTRY_CLASS = Reflection.getClass("net.minecraft.core.Registry");
	private static final Class<?> REGISTRY_KEYS_CLASS = Version.<Class<?>>newSwitch()
			.range("1.19.3", null, () -> Reflection.getClass("net.minecraft.core.registries.Registries"))
			.range(null, "1.19.2", () -> REGISTRY_CLASS)
			.get();
	private static <T> ResourceKey<T> getRegistryKey(String oldName, String newName) {
		return Reflection.getField(REGISTRY_KEYS_CLASS,
				Version.<String>newSwitch()
						.range("1.19.3", null, newName)
						.range(null, "1.19.2", oldName)
						.get(),
				"Lnet/minecraft/class_5321;").get(null);
	}
	
	public static final ResourceKey<Registry<Level>> WORLD = getRegistryKey("field_25298", "field_41223");
	
}
