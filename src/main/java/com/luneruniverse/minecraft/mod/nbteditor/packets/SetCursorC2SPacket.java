package com.luneruniverse.minecraft.mod.nbteditor.packets;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacket;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

public class SetCursorC2SPacket implements MVPacket {
	
	public static final Identifier ID = IdentifierInst.of("nbteditor", "set_cursor");
	
	private final ItemStack item;
	
	public SetCursorC2SPacket(ItemStack item) {
		this.item = item;
	}
	public SetCursorC2SPacket(FriendlyByteBuf payload) {
		this.item = ItemStack.validatedStreamCodec(ItemStack.OPTIONAL_UNTRUSTED_STREAM_CODEC).decode((RegistryFriendlyByteBuf) payload);
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	@Override
	public void write(FriendlyByteBuf payload) {
		ItemStack.validatedStreamCodec(ItemStack.OPTIONAL_UNTRUSTED_STREAM_CODEC).encode((RegistryFriendlyByteBuf) payload,item);
	}
	
	@Override
	public Identifier getPacketId() {
		return ID;
	}
	
}
