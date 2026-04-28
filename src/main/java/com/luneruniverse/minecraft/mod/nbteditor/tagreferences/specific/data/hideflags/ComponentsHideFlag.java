package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.hideflags;

import java.lang.invoke.MethodType;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVComponentType;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;

public class ComponentsHideFlag extends HideFlag {
	
	public static final Map<DataComponentType<?>, HideFlag> FLAGS = new LinkedHashMap<>();
	
	private static HideFlag register(String name, DataComponentType<?> component,
                                     Predicate<ItemStack> getShowInTooltip, BiConsumer<ItemStack, Boolean> setShowInTooltip) {
		HideFlag flag = new ComponentsHideFlag(
				TextInst.translatable("nbteditor.hide_flags." + name), component, getShowInTooltip, setShowInTooltip);
		FLAGS.put(component, flag);
		return flag;
	}
	@SuppressWarnings("unchecked")
	private static HideFlag register(String name, DataComponentType<?> component,
                                     Predicate<Object> getShowInTooltip, BiFunction<Object, Boolean, Object> setShowInTooltip) {
		return register(name, component, item -> getShowInTooltip.test(item.get(component)),
				(item, showInTooltip) -> {
					Object value = item.get(component);
					if (value == null)
						return;
					item.set((DataComponentType<Object>) component, setShowInTooltip.apply(value, showInTooltip));
				});
	}
	private static HideFlag register(String name, DataComponentType<?> component,
                                     Predicate<Object> getShowInTooltip, Class<?> componentClass, String setterMethodName) {
		return register(name, component, getShowInTooltip, Reflection.getMethod(
				componentClass, setterMethodName, MethodType.methodType(componentClass, boolean.class))::invoke);
	}
	private static HideFlag registerFieldGetter(String name, DataComponentType<?> component,
                                                Class<?> componentClass, String fieldName, String setterMethodName) {
		return register(name, component,
				Reflection.getField(componentClass, fieldName, "Z")::get, componentClass, setterMethodName);
	}
	private static HideFlag registerMethodGetter(String name, DataComponentType<?> component,
                                                 Class<?> componentClass, String getterMethodName, String setterMethodName) {
		return register(name, component,
				Reflection.getMethod(componentClass, getterMethodName, MethodType.methodType(boolean.class))::invoke,
				componentClass, setterMethodName);
	}
	
	public static final HideFlag ENCHANTMENTS = registerFieldGetter("enchantments",
			DataComponents.ENCHANTMENTS, ItemEnchantments.class, "field_49390", "method_58449");
	public static final HideFlag ATTRIBUTE_MODIFIERS = registerMethodGetter("attribute_modifiers",
			DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.class, "comp_2394", "method_58423");
	public static final HideFlag UNBREAKABLE = registerMethodGetter("unbreakable",
			DataComponents.UNBREAKABLE, Reflection.getClass("net.minecraft.class_9300"), "comp_2417", "method_58435");
	public static final HideFlag MISC = register("misc",
			(DataComponentType<?>) MVComponentType.HIDE_ADDITIONAL_TOOLTIP_1_20_5_1_21_4.getInternalValue(),
			item -> item.has(DataComponents.TOOLTIP_DISPLAY),
			(item, showInTooltip) -> {
				if (showInTooltip && item.has(DataComponents.TOOLTIP_DISPLAY))
					item.set(DataComponents.TOOLTIP_DISPLAY,new TooltipDisplay(item.get(DataComponents.TOOLTIP_DISPLAY).hideTooltip(),item.get(DataComponents.TOOLTIP_DISPLAY).hiddenComponents()));
				else
					item.set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(true,new LinkedHashSet<DataComponentType<?>>()));
			});
	public static final HideFlag DYED_COLOR = registerMethodGetter("dyed_color",
			DataComponents.DYED_COLOR, DyedItemColor.class, "comp_2385", "method_58422");
	
	// Was previously covered by MISC
	public static final HideFlag STORED_ENCHANTMENTS = registerFieldGetter("stored_enchantments",
			DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.class, "field_49390", "method_58449");
	public static final HideFlag TRIM = Version.<HideFlag>newSwitch()
			.range("1.21.0", null, () -> registerMethodGetter("trim",
					DataComponents.TRIM, ArmorTrim.class, "comp_3181", "method_58421"))
			.range("1.20.5", "1.20.6", () -> registerFieldGetter("trim",
					DataComponents.TRIM, ArmorTrim.class, "field_49279", "method_58421"))
			.get();
	
	public static final HideFlag JUKEBOX_PLAYABLE = Version.<HideFlag>newSwitch()
			.range("1.21.0", "1.21.4", () -> registerMethodGetter("jukebox_playable",
					DataComponents.JUKEBOX_PLAYABLE, JukeboxPlayable.class, "comp_2834", "method_60749"))
			.range("1.20.5", "1.20.6", () -> null)
			.get();
	
	private final Component name;
	private final DataComponentType<?> component;
	private final Predicate<ItemStack> getShowInTooltip;
	private final BiConsumer<ItemStack, Boolean> setShowInTooltip;
	
	private ComponentsHideFlag(Component name, DataComponentType<?> component,
                               Predicate<ItemStack> getShowInTooltip, BiConsumer<ItemStack, Boolean> setShowInTooltip) {
		this.name = name;
		this.component = component;
		this.getShowInTooltip = getShowInTooltip;
		this.setShowInTooltip = setShowInTooltip;
	}
	
	@Override
	public Component getName() {
		return name;
	}
	
	public DataComponentType<?> getComponent() {
		return component;
	}
	
	public boolean getShowInTooltip(ItemStack item) {
		return getShowInTooltip.test(item);
	}
	
	public void setShowInTooltip(ItemStack item, boolean showInTooltip) {
		setShowInTooltip.accept(item, showInTooltip);
	}
	
}
