package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data;

import java.lang.invoke.MethodType;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.AttributeData.AttributeModifierData.AttributeModifierId;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.AttributeData.AttributeModifierData.Operation;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.AttributeData.AttributeModifierData.Slot;

import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public record AttributeData(Attribute attribute, double value, Optional<AttributeModifierData> modifierData) {
	
	public static record AttributeModifierData(Operation operation, Slot slot, AttributeModifierId id) {
		
		public enum Operation {
			ADD("nbteditor.attributes.operation.add"),
			ADD_MULTIPLIED_BASE("nbteditor.attributes.operation.add_multiplied_base"),
			ADD_MULTIPLIED_TOTAL("nbteditor.attributes.operation.add_multiplied_total");
			
			public static Operation fromMinecraft(net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation operation) {
				return switch (operation) {
					case ADD_VALUE -> ADD;
					case ADD_MULTIPLIED_BASE -> ADD_MULTIPLIED_BASE;
					case ADD_MULTIPLIED_TOTAL -> ADD_MULTIPLIED_TOTAL;
				};
			}
			
			private final Component name;
			private Operation(String key) {
				this.name = TextInst.translatable(key);
			}
			public net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation toMinecraft() {
				return switch (this) {
					case ADD -> net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE;
					case ADD_MULTIPLIED_BASE -> net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
					case ADD_MULTIPLIED_TOTAL -> net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
				};
			}
			@Override
			public String toString() {
				return name.getString();
			}
		}
		
		public enum Slot {
			ANY("nbteditor.attributes.slot.any"),
			HAND("nbteditor.attributes.slot.hand", "1.20.5", null),
			MAINHAND("nbteditor.attributes.slot.mainhand"),
			OFFHAND("nbteditor.attributes.slot.offhand"),
			ARMOR("nbteditor.attributes.slot.armor", "1.20.5", null),
			HEAD("nbteditor.attributes.slot.head"),
			CHEST("nbteditor.attributes.slot.chest"),
			LEGS("nbteditor.attributes.slot.legs"),
			FEET("nbteditor.attributes.slot.feet"),
			BODY("nbteditor.attributes.slot.body", "1.20.5", null),
			SADDLE("nbteditor.attributes.slot.saddle", "1.21.5", null);
			
			public static Slot fromMinecraft(Object slot) {
				return switch ((EquipmentSlotGroup) slot) {
					case ANY -> ANY;
					case HAND -> HAND;
					case MAINHAND -> MAINHAND;
					case OFFHAND -> OFFHAND;
					case ARMOR -> ARMOR;
					case HEAD -> HEAD;
					case CHEST -> CHEST;
					case LEGS -> LEGS;
					case FEET -> FEET;
					case BODY -> BODY;
					case SADDLE -> SADDLE;
				};
			}
			
			private final Component name;
			private final boolean inThisVersion;
			private Slot(String key, String minVersion, String maxVersion) {
				this.name = TextInst.translatable(key);
				this.inThisVersion = Version.<Boolean>newSwitch()
						.range(minVersion, maxVersion, true)
						.getOptionally().orElse(false);
			}
			private Slot(String key) {
				this(key, null, null);
			}
			public Object toMinecraft() {
				return switch (this) {
					case ANY -> EquipmentSlotGroup.ANY;
					case HAND -> EquipmentSlotGroup.HAND;
					case MAINHAND -> EquipmentSlotGroup.MAINHAND;
					case OFFHAND -> EquipmentSlotGroup.OFFHAND;
					case ARMOR -> EquipmentSlotGroup.ARMOR;
					case HEAD -> EquipmentSlotGroup.HEAD;
					case CHEST -> EquipmentSlotGroup.CHEST;
					case LEGS -> EquipmentSlotGroup.LEGS;
					case FEET -> EquipmentSlotGroup.FEET;
					case BODY -> EquipmentSlotGroup.BODY;
					case SADDLE -> EquipmentSlotGroup.SADDLE;
				};
			}
			public boolean isInThisVersion() {
				return inThisVersion;
			}
			@Override
			public String toString() {
				return name.getString();
			}
		}
		
		public static class AttributeModifierId {
			
			public static final boolean ID_IS_IDENTIFIER = Version.<Boolean>newSwitch()
					.range("1.21.0", null, true)
					.range(null, "1.20.6", false)
					.get();
			
			public static AttributeModifierId randomUUID() {
				return new AttributeModifierId(UUID.randomUUID());
			}
			
			private static final Supplier<Reflection.MethodInvoker> EntityAttributeModifier_uuid =
					Reflection.getOptionalMethod(AttributeModifier.class, "comp_2447", MethodType.methodType(UUID.class));
			public static AttributeModifierId fromMinecraft(AttributeModifier modifier) {
				if (ID_IS_IDENTIFIER)
					return new AttributeModifierId(modifier.id());
				return new AttributeModifierId((UUID) EntityAttributeModifier_uuid.get().invoke(modifier));
			}
			
			private final Object id;
			
			public AttributeModifierId(UUID id) {
				this.id = id;
			}
			public AttributeModifierId(Identifier id) {
				if (!ID_IS_IDENTIFIER)
					throw new IllegalArgumentException("Attribute IDs are UUIDs in this version!");
				this.id = id;
			}
			
			public UUID getUUID() {
				return (UUID) id;
			}
			
			public Identifier getIdentifier() {
				if (id instanceof UUID uuid)
					return IdentifierInst.of("minecraft", uuid.toString());
				return (Identifier) id;
			}
			
			public AttributeModifier toMinecraft(String name, double value, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation operation) {
				if (ID_IS_IDENTIFIER)
					return new AttributeModifier(getIdentifier(), value, operation);
				
				return Reflection.newInstance(
						AttributeModifier.class,
						new Class<?>[] {UUID.class, String.class, double.class, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.class},
						getUUID(), name, value, operation);
			}
			
		}
		
		public static AttributeModifierData fromMinecraft(AttributeModifier modifier, EquipmentSlotGroup slot) {
			return new AttributeModifierData(
					Operation.fromMinecraft(modifier.operation()),
					Slot.fromMinecraft(slot),
					AttributeModifierId.fromMinecraft(modifier));
		}
		
		public AttributeModifier toMinecraft(String name, double value) {
			return id.toMinecraft(name, value, operation.toMinecraft());
		}
		
	}
	
	public static AttributeData fromComponentEntry(ItemAttributeModifiers.Entry entry) {
		return new AttributeData(
				entry.attribute().value(),
				entry.modifier().amount(),
				Optional.of(AttributeModifierData.fromMinecraft(entry.modifier(), entry.slot())));
	}
	
	public AttributeData(Attribute attribute, double value) {
		this(attribute, value, Optional.empty());
	}
	public AttributeData(Attribute attribute, double value, Operation operation, Slot slot, AttributeModifierId id) {
		this(attribute, value, Optional.of(new AttributeModifierData(operation, slot, id)));
	}
	
	public ItemAttributeModifiers.Entry toComponentEntry() {
		return new ItemAttributeModifiers.Entry(
				BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute),
				modifierData.get().toMinecraft(BuiltInRegistries.ATTRIBUTE.getKey(attribute).toString(), value),
				(EquipmentSlotGroup) modifierData.get().slot().toMinecraft());
	}
	
}
