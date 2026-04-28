package com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

/**
 * Used internally in multiversion.networking; DO NOT USE
 */
@Deprecated
public class MVPacketCustomPayload implements CustomPacketPayload {
	
	/**
	 * Hides the {@link CustomPacketPayload} in {@link ServerboundCustomPayloadPacket#CustomPayloadC2SPacket(CustomPacketPayload)}
	 */
	public static ServerboundCustomPayloadPacket wrapC2S(MVPacket packet) {
		return new ServerboundCustomPayloadPacket(new MVPacketCustomPayload(packet));
	}
	/**
	 * Hides the {@link CustomPacketPayload} in {@link ClientboundCustomPayloadPacket#CustomPayloadS2CPacket(CustomPacketPayload)}
	 */
	public static ClientboundCustomPayloadPacket wrapS2C(MVPacket packet) {
		return new ClientboundCustomPayloadPacket(new MVPacketCustomPayload(packet));
	}
	
	/**
	 * Hides the {@link CustomPacketPayload} in {@link ServerboundCustomPayloadPacket#payload()}
	 */
	public static MVPacket unwrapC2S(ServerboundCustomPayloadPacket packet) {
		if (packet.payload() instanceof MVPacketCustomPayload mvPacket)
			return mvPacket.getPacket();
		return null;
	}
	/**
	 * Hides the {@link CustomPacketPayload} in {@link ClientboundCustomPayloadPacket#payload()}
	 */
	public static MVPacket unwrapS2C(ClientboundCustomPayloadPacket packet) {
		if (packet.payload() instanceof MVPacketCustomPayload mvPacket)
			return mvPacket.getPacket();
		return null;
	}
	
	private final MVPacket packet;
	
	public MVPacketCustomPayload(MVPacket packet) {
		this.packet = packet;
	}
	
	public MVPacket getPacket() {
		return packet;
	}
	
	@Override
	public Type<MVPacketCustomPayload> type() {
		return new Type<>(packet.getPacketId());
	}
	
	// write
	public void method_53028(FriendlyByteBuf payload) {
		packet.write(payload);
	}
	
	// id
	public Identifier comp_1678() {
		return packet.getPacketId();
	}
	
}
