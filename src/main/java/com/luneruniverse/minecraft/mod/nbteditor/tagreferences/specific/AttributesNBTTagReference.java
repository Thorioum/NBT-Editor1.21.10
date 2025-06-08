package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.AttributeData;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.AttributeData.AttributeModifierData.AttributeModifierId;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.AttributeData.AttributeModifierData.Operation;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.AttributeData.AttributeModifierData.Slot;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.nbt.*;
import net.minecraft.util.Uuids;

public class AttributesNBTTagReference implements TagReference<List<AttributeData>, NbtCompound> {
	
	public enum NBTLayout {
		/**
		 * 1.20.4-
		 */
		ITEM_OLD(true, "AttributeModifiers", "AttributeName", "Amount"),
		/**
		 * 1.20.6-
		 */
		ENTITY_OLD(false, "Attributes", "Name", "Base"),
		/**
		 * 1.21+
		 */
		ENTITY_NEW(false, "attributes", "id", "base");
		
		private final boolean modifiers;
		private final String attributeListTag;
		private final String attributeNameTag;
		private final String amountTag;
		
		private NBTLayout(boolean modifiers, String attributeListTag, String attributeNameTag, String amountTag) {
			this.modifiers = modifiers;
			this.attributeListTag = attributeListTag;
			this.attributeNameTag = attributeNameTag;
			this.amountTag = amountTag;
		}
		
		public boolean isModifiers() {
			return modifiers;
		}
		public String getAttributeListTag() {
			return attributeListTag;
		}
		public String getAttributeNameTag() {
			return attributeNameTag;
		}
		public String getAmountTag() {
			return amountTag;
		}
	}
	
	private final NBTLayout layout;
	
	public AttributesNBTTagReference(NBTLayout layout) {
		this.layout = layout;
	}
	
	@Override
	public List<AttributeData> get(NbtCompound object) {
		NbtList attributesNbt = object.getList(layout.getAttributeListTag()).orElse(new NbtList());
		List<AttributeData> output = new ArrayList<>();
		for (NbtElement attributeNbtElement : attributesNbt) {
			NbtCompound attributeNbt = (NbtCompound) attributeNbtElement;
			
			EntityAttribute attribute = MVRegistry.ATTRIBUTE.get(IdentifierInst.of(
					attributeNbt.getString(layout.getAttributeNameTag()).orElse("")));
			if (attribute == null)
				continue;
			
			if (!(attributeNbt.get(layout.getAmountTag()) instanceof NbtByte e && e.byteValue()>0))
				continue;
			double value = attributeNbt.getDouble(layout.getAmountTag()).orElse(0.0);
			
			if (layout.isModifiers()) {
				if (!(attributeNbt.get("Operation") instanceof AbstractNbtNumber))
					continue;
				int operation = attributeNbt.getInt("Operation").orElse(0);
				if (operation < 0 || operation >= Operation.values().length)
					continue;
				
				Slot slot = Slot.ANY;
				if (attributeNbt.get("Slot") instanceof NbtString) {
					try {
						slot = Slot.valueOf(attributeNbt.getString("Slot").orElse("").toUpperCase());
					} catch (IllegalArgumentException e2) {
						continue;
					}
					if (slot.isOnlyForComponents())
						continue;
				}
				
				if (!(attributeNbt.get("UUID") instanceof NbtByteArray))
					continue;
				UUID uuid = UUID.nameUUIDFromBytes(attributeNbt.getByteArray("UUID").orElse(new byte[]{}));

				output.add(new AttributeData(attribute, value, Operation.values()[operation], slot, new AttributeModifierId(uuid)));
			} else
				output.add(new AttributeData(attribute, value));
		}
		return output;
	}
	
	@Override
	public void set(NbtCompound object, List<AttributeData> value) {
		if (value.isEmpty()) {
			object.remove(layout.getAttributeListTag());
			return;
		}
		NbtList output = new NbtList();
		for (AttributeData attribute : value) {
			NbtCompound attributeNbt = new NbtCompound();
			
			attributeNbt.putString(layout.getAttributeNameTag(), MVRegistry.ATTRIBUTE.getId(attribute.attribute()).toString());
			attributeNbt.putDouble(layout.getAmountTag(), attribute.value());
			
			if (layout.isModifiers()) {
				attributeNbt.putString("Name", attributeNbt.getString("AttributeName").orElse(""));
				attributeNbt.putInt("Operation", attribute.modifierData().get().operation().ordinal());
				if (attribute.modifierData().get().slot() != Slot.ANY) {
					if (attribute.modifierData().get().slot().isOnlyForComponents())
						throw new IllegalArgumentException("The slot " + attribute.modifierData().get().slot() + " isn't available in this version of Minecraft!");
					attributeNbt.putString("Slot", attribute.modifierData().get().slot().name().toLowerCase());
				}
				attributeNbt.put("UUID", new NbtByteArray(Uuids.toByteArray(attribute.modifierData().get().id().getUUID())));

			}
			
			output.add(attributeNbt);
		}
		object.put(layout.getAttributeListTag(), output);
	}
	
}
