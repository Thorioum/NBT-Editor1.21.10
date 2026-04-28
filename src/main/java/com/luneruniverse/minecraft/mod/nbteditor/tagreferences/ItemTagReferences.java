package com.luneruniverse.minecraft.mod.nbteditor.tagreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVComponentType;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.ComponentTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.NBTTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.AttributesNBTTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.CustomDataNBTTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.CustomPotionContentsNBTTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.EnchantsTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.GameProfileNBTTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.GameProfileNameNBTTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.AttributeData;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.CustomPotionContents;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.Enchants;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.hideflags.HideFlag;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.hideflags.HideFlagsNBTTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.hideflags.HideFlagsTooltipDisplayComponentTagReference;
import com.mojang.authlib.GameProfile;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.network.Filterable;
import net.minecraft.network.chat.Component;

public class ItemTagReferences {
	
	private static TagReference<CompoundTag, ItemStack> getComponentTagRefOfNBT(MVComponentType<CustomData> component) {
		return new ComponentTagReference<>(component,
				null,
				componentValue -> componentValue == null ? new CompoundTag() : componentValue.copyTag(),
				r-> CustomData.of(r));
	}
	private static TagReference<TypedEntityData<EntityType<?>>, ItemStack> getComponentTagRefOfEntityData() {
		return new ComponentTagReference<>(MVComponentType.ENTITY_DATA,
				null,
				componentValue -> componentValue == null ? TypedEntityData.of(EntityType.BAT,new CompoundTag()) : componentValue,
				r-> r);
	}
	private static TagReference<TypedEntityData<BlockEntityType<?>>, ItemStack> getComponentTagRefOfBlockEntityData() {
		return new ComponentTagReference<>(MVComponentType.BLOCK_ENTITY_DATA,
				null,
				componentValue -> componentValue == null ? TypedEntityData.of(BlockEntityType.COMMAND_BLOCK,new CompoundTag()) : componentValue,
				r-> r);
	}

	public static final TagReference<CustomPotionContents, ItemStack> CUSTOM_POTION_CONTENTS = Version.<TagReference<CustomPotionContents, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> new ComponentTagReference<>(MVComponentType.POTION_CONTENTS,
					() -> MVMisc.newPotionContents(Optional.empty(), Optional.empty(), List.of()),
					contents -> new CustomPotionContents(contents.customColor(), contents.customEffects()),
					contents -> MVMisc.newPotionContents(Optional.empty(), contents.color(), contents.effects())))
			.range(null, "1.20.4", CustomPotionContentsNBTTagReference::new)
			.get();
	
	public static final TagReference<Optional<String>, ItemStack> PROFILE_NAME = Version.<TagReference<Optional<String>, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> new ComponentTagReference<>(MVComponentType.PROFILE,
					null,
					component -> component == null ? Optional.empty() : component.name(),
					name -> ResolvableProfile.createUnresolved(name.orElse(""))))
			.range(null, "1.20.4", () -> TagReference.forItems(Optional::empty, new GameProfileNameNBTTagReference()))
			.get();
	public static final TagReference<Optional<GameProfile>, ItemStack> PROFILE = Version.<TagReference<Optional<GameProfile>, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> new ComponentTagReference<>(MVComponentType.PROFILE,
					null,
					profile -> Optional.ofNullable(profile).map(ResolvableProfile::partialProfile),
					profile -> profile.map(ResolvableProfile::createResolved).orElse(null)))
			.range(null, "1.20.4", () -> TagReference.forItems(Optional::empty, new GameProfileNBTTagReference()))
			.get();
	
	public static final TagReference<List<AttributeData>, ItemStack> ATTRIBUTES = Version.<TagReference<List<AttributeData>, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> new ComponentTagReference<>(MVComponentType.ATTRIBUTE_MODIFIERS,
					null,
					component -> component == null ? new ArrayList<>() :
						component.modifiers().stream().map(AttributeData::fromComponentEntry).collect(Collectors.toList()),
					(component, list) -> (ItemAttributeModifiers) MVMisc.withAttributes(component,
							list.stream().map(AttributeData::toComponentEntry).toList())))
			.range(null, "1.20.4", () -> TagReference.forItems(ArrayList::new, new AttributesNBTTagReference(AttributesNBTTagReference.NBTLayout.ITEM_OLD)))
			.get();
	
	public static final TagReference<List<String>, ItemStack> WRITABLE_BOOK_PAGES = Version.<TagReference<List<String>, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> new ComponentTagReference<>(MVComponentType.WRITABLE_BOOK_CONTENT,
					() -> new WritableBookContent(List.of()),
					content -> content.pages().stream().map(Filterable::raw).collect(Collectors.toList()),
					pages -> new WritableBookContent(pages.stream().map(Filterable::passThrough).toList())))
			.get();
	
	public static final TagReference<Boolean, ItemStack> UNBREAKABLE = Version.<TagReference<Boolean, ItemStack>>newSwitch()
			.range("1.21.5", null, () -> ComponentTagReference.forExistance(MVComponentType.UNBREAKABLE_1_21_5))
			.range("1.20.5", "1.21.4", () -> ComponentTagReference.forExistance(MVComponentType.UNBREAKABLE_1_20_5_1_21_4, () -> Reflection.newInstance("net.minecraft.class_9300", new Class<?>[] {boolean.class}, true)))
			.get();
	
	public static final TagReference<CompoundTag, ItemStack> CUSTOM_DATA = Version.<TagReference<CompoundTag, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> getComponentTagRefOfNBT(MVComponentType.CUSTOM_DATA))
			.range(null, "1.20.4", () -> new CustomDataNBTTagReference())
			.get();
	
	public static final TagReference<Map<String, String>, ItemStack> BLOCK_STATE = Version.<TagReference<Map<String, String>, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> new ComponentTagReference<>(MVComponentType.BLOCK_STATE,
					null,
					component -> component == null ? new HashMap<>() : new HashMap<>(component.properties()),
					BlockItemStateProperties::new))
			.get();
	
	public static final TagReference<TypedEntityData<BlockEntityType<?>>, ItemStack> BLOCK_ENTITY_DATA = getComponentTagRefOfBlockEntityData();
	
	public static final TagReference<TypedEntityData<EntityType<?>>, ItemStack> ENTITY_DATA = getComponentTagRefOfEntityData();
	
	public static final TagReference<Enchants, ItemStack> ENCHANTMENTS = new EnchantsTagReference();
	
	public static final TagReference<List<Component>, ItemStack> LORE = Version.<TagReference<List<Component>, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> new ComponentTagReference<>(MVComponentType.LORE,
					() -> ItemLore.EMPTY,
					component -> new ArrayList<>(component.lines()),
					lore -> new ItemLore(lore.stream().limit(256).toList())))
			.get();
	
	public static final TagReference<Map<HideFlag, Boolean>, ItemStack> HIDE_FLAGS = Version.<TagReference<Map<HideFlag, Boolean>, ItemStack>>newSwitch()
			.range("1.21.5", null, () -> new HideFlagsTooltipDisplayComponentTagReference())
			.get();
	
}
