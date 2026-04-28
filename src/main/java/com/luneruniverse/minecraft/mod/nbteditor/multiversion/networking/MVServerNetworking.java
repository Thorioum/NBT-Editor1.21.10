package com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMVMisc;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;

public class MVServerNetworking {
	
	public static class PlayNetworkStateEvents {
		public static interface Start {
			public static final Event<Start> EVENT = EventFactory.createArrayBacked(Start.class, listeners -> player -> {
				for (Start listener : listeners)
					listener.onPlayStart(player);
			});
			public void onPlayStart(ServerPlayer player);
		}
		public static interface Stop {
			public static final Event<Stop> EVENT = EventFactory.createArrayBacked(Stop.class, listeners -> player -> {
				for (Stop listener : listeners)
					listener.onPlayStop(player);
			});
			public void onPlayStop(ServerPlayer player);
		}
	}
	
	public static void onPlayStart(ServerPlayer player) {
		PlayNetworkStateEvents.Start.EVENT.invoker().onPlayStart(player);
	}
	public static void onPlayStop(ServerPlayer player) {
		PlayNetworkStateEvents.Stop.EVENT.invoker().onPlayStop(player);
	}
	
	private static final Map<Identifier, List<BiConsumer<MVPacket, ServerPlayer>>> listeners = new HashMap<>();
	
	@SuppressWarnings("deprecation")
	public static void send(ServerPlayer player, MVPacket packet) {
		ServerMVMisc.sendS2CPacket(player, Version.<ClientboundCustomPayloadPacket>newSwitch()
				.range("1.20.2", null, () -> MVPacketCustomPayload.wrapS2C(packet))
				.range(null, "1.20.1", () -> {
					FriendlyByteBuf payload = new FriendlyByteBuf(Unpooled.buffer());
					packet.write(payload);
					try {
						return ClientboundCustomPayloadPacket.class.getConstructor(Identifier.class, FriendlyByteBuf.class)
								.newInstance(packet.getPacketId(), payload);
					} catch (Exception e) {
						throw new RuntimeException("Failed to create CustomPayloadS2CPacket", e);
					}
				})
				.get());
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends MVPacket> void registerListener(Identifier id, BiConsumer<T, ServerPlayer> listener) {
		listeners.computeIfAbsent(id, key -> new ArrayList<>()).add((packet, player) -> listener.accept((T) packet, player));
	}
	
	public static void callListeners(MVPacket packet, ServerPlayer player) {
		if (!player.server.isSameThread()) {
			player.server.execute(() -> callListeners(packet, player));
			return;
		}
		List<BiConsumer<MVPacket, ServerPlayer>> specificListeners = listeners.get(packet.getPacketId());
		if (specificListeners == null)
			return;
		specificListeners.forEach(listener -> listener.accept(packet, player));
	}
	
}
