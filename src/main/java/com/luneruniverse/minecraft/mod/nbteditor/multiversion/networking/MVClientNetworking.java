package com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.DynamicRegistryManagerHolder;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;

public class MVClientNetworking {
	
	public static class PlayNetworkStateEvents {
		public static interface Start {
			public static final Event<Start> EVENT = EventFactory.createArrayBacked(Start.class, listeners -> networkHandler -> {
				for (Start listener : listeners)
					listener.onPlayStart(networkHandler);
			});
			public void onPlayStart(ClientPacketListener networkHandler);
		}
		public static interface Join {
			public static final Event<Join> EVENT = EventFactory.createArrayBacked(Join.class, listeners -> () -> {
				for (Join listener : listeners)
					listener.onPlayJoin();
			});
			public void onPlayJoin();
		}
		public static interface Stop {
			public static final Event<Stop> EVENT = EventFactory.createArrayBacked(Stop.class, listeners -> () -> {
				for (Stop listener : listeners)
					listener.onPlayStop();
			});
			public void onPlayStop();
		}
	}
	
	public static void onPlayStart(ClientPacketListener networkHandler) {
		Version.newSwitch()
				.range("1.20.5", null, () -> {
					DynamicRegistryManagerHolder.setClientManager(networkHandler);
				})
				.range(null, "1.20.4", () -> {})
				.run();
		
		PlayNetworkStateEvents.Start.EVENT.invoker().onPlayStart(networkHandler);
	}
	public static void onPlayJoin() {
		PlayNetworkStateEvents.Join.EVENT.invoker().onPlayJoin();
	}
	public static void onPlayStop() {
		PlayNetworkStateEvents.Stop.EVENT.invoker().onPlayStop();
		
		Version.newSwitch()
				.range("1.20.5", null, () -> {
					DynamicRegistryManagerHolder.setClientManager(null);
				})
				.range(null, "1.20.4", () -> {})
				.run();
	}
	
	private static final Map<Identifier, List<Consumer<MVPacket>>> listeners = new HashMap<>();
	
	@SuppressWarnings("deprecation")
	public static void send(MVPacket packet) {
		MVMisc.sendC2SPacket(Version.<ServerboundCustomPayloadPacket>newSwitch()
				.range("1.20.2", null, () -> MVPacketCustomPayload.wrapC2S(packet))
				.range(null, "1.20.1", () -> {
					FriendlyByteBuf payload = new FriendlyByteBuf(Unpooled.buffer());
					packet.write(payload);
					try {
						return ServerboundCustomPayloadPacket.class.getConstructor(Identifier.class, FriendlyByteBuf.class)
								.newInstance(packet.getPacketId(), payload);
					} catch (Exception e) {
						throw new RuntimeException("Failed to create CustomPayloadC2SPacket", e);
					}
				})
				.get());
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends MVPacket> void registerListener(Identifier id, Consumer<T> listener) {
		listeners.computeIfAbsent(id, key -> new ArrayList<>()).add(packet -> listener.accept((T) packet));
	}
	
	public static void callListeners(MVPacket packet) {
		if (!MainUtil.client.isSameThread()) {
			MainUtil.client.execute(() -> callListeners(packet));
			return;
		}
		List<Consumer<MVPacket>> specificListeners = listeners.get(packet.getPacketId());
		if (specificListeners == null)
			return;
		specificListeners.forEach(listener -> listener.accept(packet));
	}
	
}
