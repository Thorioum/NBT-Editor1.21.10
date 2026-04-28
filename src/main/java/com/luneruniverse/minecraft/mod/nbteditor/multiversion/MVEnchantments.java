package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.ItemTagReferences;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.Enchants;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.ChatFormatting;

public class MVEnchantments {
	
	public static final boolean DATA_PACK_ENCHANTMENTS = Version.<Boolean>newSwitch()
			.range("1.21.0", null, true)
			.range(null, "1.20.6", false)
			.get();
	
	@SuppressWarnings("unchecked")
	private static Enchantment getEnchantment(String field) {
		Object output = Reflection.getField(Enchantments.class, field,
				DATA_PACK_ENCHANTMENTS ? "Lnet/minecraft/resources/ResourceKey;" : "Lnet/minecraft/class_1887;").get(null);
		if (DATA_PACK_ENCHANTMENTS)
			return MVRegistry.getEnchantmentRegistry().get(((ResourceKey<Enchantment>) output).identifier());
		return (Enchantment) output;
	}
	
	public static final Enchantment LOYALTY = getEnchantment("LOYALTY");
	public static final Enchantment FIRE_ASPECT = getEnchantment("FIRE_ASPECT");
	
	private static final Supplier<Reflection.MethodInvoker> Enchantment_isCursed =
			Reflection.getOptionalMethod(Enchantment.class, "method_8195", MethodType.methodType(boolean.class));
	public static boolean isCursed(Enchantment enchant) {
		return Version.<Boolean>newSwitch()
				.range("1.21.0", null, () -> MVRegistry.getEnchantmentRegistry().getInternalValue().wrapAsHolder(enchant).is(EnchantmentTags.CURSE))
				.range(null, "1.20.6", () -> Enchantment_isCursed.get().invoke(enchant))
				.get();
	}
	
	public static void addEnchantment(ItemStack item, Enchantment enchant, int level) {
		Enchants enchants = ItemTagReferences.ENCHANTMENTS.get(item);
		enchants.addEnchant(enchant, level);
		ItemTagReferences.ENCHANTMENTS.set(item, enchants);
	}
	
	private static final Supplier<Reflection.MethodInvoker> Enchantment_getTranslationKey =
			Reflection.getOptionalMethod(Enchantment.class, "method_8184", MethodType.methodType(String.class));
	public static Component getEnchantmentName(Enchantment enchant) {
		ChatFormatting color = (isCursed(enchant) ? ChatFormatting.RED : ChatFormatting.GRAY);
		return Version.<Component>newSwitch()
				.range("1.21.0", null, () -> {
					MutableComponent output = enchant.description().copy();
					ComponentUtils.mergeStyles(output, Style.EMPTY.withColor(color));
					return output;
				})
				.range(null, "1.20.6", () -> {
					EditableText output = TextInst.translatable(Enchantment_getTranslationKey.get().invoke(enchant));
					output.formatted(color);
					return output;
				})
				.get();
	}
	
}
