package com.luneruniverse.minecraft.mod.nbteditor.packets;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacket;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

public class SetSlotC2SPacket implements MVPacket {
	
	public static final Identifier ID = IdentifierInst.of("nbteditor", "set_slot");
	
	private final int slot;
	private final ItemStack item;
	
	public SetSlotC2SPacket(int slot, ItemStack item) {
		this.slot = slot;
		this.item = item;
	}
	public SetSlotC2SPacket(FriendlyByteBuf payload) {
		this.slot = payload.readVarInt();
		this.item = ItemStack.validatedStreamCodec(ItemStack.OPTIONAL_UNTRUSTED_STREAM_CODEC).decode((RegistryFriendlyByteBuf) payload);
	}
	
	public int getSlot() {
		return slot;
	}
	public ItemStack getItem() {
		return item;
	}
	
	@Override
	public void write(FriendlyByteBuf payload) {
		payload.writeVarInt(slot);
		ItemStack.validatedStreamCodec(ItemStack.OPTIONAL_UNTRUSTED_STREAM_CODEC).encode((RegistryFriendlyByteBuf) payload,item);
	}
	
	@Override
	public Identifier getPacketId() {
		return ID;
	}
	
}
