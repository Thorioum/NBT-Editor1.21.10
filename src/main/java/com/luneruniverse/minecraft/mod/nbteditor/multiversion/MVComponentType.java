package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.NBTManagers;

import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;

public class MVComponentType<T> {
	
	public static final MVComponentType<ItemAttributeModifiers> ATTRIBUTE_MODIFIERS =
			new MVComponentType<>(() -> DataComponents.ATTRIBUTE_MODIFIERS);
	public static final MVComponentType<TypedEntityData<BlockEntityType<?>>> BLOCK_ENTITY_DATA =
			new MVComponentType<>(() -> DataComponents.BLOCK_ENTITY_DATA);
	public static final MVComponentType<BlockItemStateProperties> BLOCK_STATE =
			new MVComponentType<>(() -> DataComponents.BLOCK_STATE);
	public static final MVComponentType<AdventureModePredicate> CAN_BREAK =
			new MVComponentType<>(() -> DataComponents.CAN_BREAK);
	public static final MVComponentType<AdventureModePredicate> CAN_PLACE_ON =
			new MVComponentType<>(() -> DataComponents.CAN_PLACE_ON);
	public static final MVComponentType<CustomData> CUSTOM_DATA =
			new MVComponentType<>(() -> DataComponents.CUSTOM_DATA);
	public static final MVComponentType<Component> CUSTOM_NAME =
			new MVComponentType<>(() -> DataComponents.CUSTOM_NAME);
	public static final MVComponentType<DyedItemColor> DYED_COLOR =
			new MVComponentType<>(() -> DataComponents.DYED_COLOR);
	public static final MVComponentType<ItemEnchantments> ENCHANTMENTS =
			new MVComponentType<>(() -> DataComponents.ENCHANTMENTS);
	public static final MVComponentType<TypedEntityData<EntityType<?>>> ENTITY_DATA =
			new MVComponentType<>(() -> DataComponents.ENTITY_DATA);
	public static final MVComponentType<Unit> HIDE_ADDITIONAL_TOOLTIP_1_20_5_1_21_4 =
			new MVComponentType<>("field_49638", "1.20.5", "1.21.4");
	public static final MVComponentType<Unit> HIDE_TOOLTIP_1_20_5_1_21_4 =
			new MVComponentType<>("field_50074", "1.20.5", "1.21.4");
	public static final MVComponentType<Component> ITEM_NAME =
			new MVComponentType<>(() -> DataComponents.ITEM_NAME);
	public static final MVComponentType<ItemLore> LORE =
			new MVComponentType<>(() -> DataComponents.LORE);
	public static final MVComponentType<Integer> MAX_DAMAGE =
			new MVComponentType<>(() -> DataComponents.MAX_DAMAGE);
	public static final MVComponentType<Integer> MAX_STACK_SIZE =
			new MVComponentType<>(() -> DataComponents.MAX_STACK_SIZE);
	public static final MVComponentType<PotionContents> POTION_CONTENTS =
			new MVComponentType<>(() -> DataComponents.POTION_CONTENTS);
	public static final MVComponentType<ResolvableProfile> PROFILE =
			new MVComponentType<>(() -> DataComponents.PROFILE);
	public static final MVComponentType<ItemEnchantments> STORED_ENCHANTMENTS =
			new MVComponentType<>(() -> DataComponents.STORED_ENCHANTMENTS);
	public static final MVComponentType<SuspiciousStewEffects> SUSPICIOUS_STEW_EFFECTS =
			new MVComponentType<>(() -> DataComponents.SUSPICIOUS_STEW_EFFECTS);
	public static final MVComponentType<ArmorTrim> TRIM =
			new MVComponentType<>(() -> DataComponents.TRIM);
	public static final MVComponentType<Object> UNBREAKABLE_1_20_5_1_21_4 =
			new MVComponentType<>(() -> DataComponents.UNBREAKABLE, "1.20.5", "1.21.4");
	public static final MVComponentType<Unit> UNBREAKABLE_1_21_5 =
			new MVComponentType<>(() -> DataComponents.UNBREAKABLE, "1.21.5", null);
	public static final MVComponentType<WritableBookContent> WRITABLE_BOOK_CONTENT =
			new MVComponentType<>(() -> DataComponents.WRITABLE_BOOK_CONTENT);
	public static final MVComponentType<WrittenBookContent> WRITTEN_BOOK_CONTENT =
			new MVComponentType<>(() -> DataComponents.WRITTEN_BOOK_CONTENT);
	public static final MVComponentType<JukeboxPlayable> JUKEBOX_PLAYABLE =
			new MVComponentType<>(() -> DataComponents.JUKEBOX_PLAYABLE, "1.21.0", null);
	
	private final Object component;
	
	public MVComponentType(Supplier<DataComponentType<?>> component) {
		this.component = (NBTManagers.COMPONENTS_EXIST ? component.get() : null);
	}
	public MVComponentType(Supplier<Object> component, String minVersion, String maxVersion) {
		this.component = Version.<Object>newSwitch()
				.range(minVersion, maxVersion, component)
				.getOptionally().orElse(null);
	}
	public MVComponentType(String fieldName, String minVersion, String maxVersion) {
		this(() -> Reflection.getField(DataComponents.class, fieldName, "Lnet/minecraft/class_9331;").get(null),
				minVersion, maxVersion);
	}
	
	public Object getInternalValue() {
		if (component == null)
			throw new IllegalStateException("Components aren't in this version!");
		return component;
	}
	
}
