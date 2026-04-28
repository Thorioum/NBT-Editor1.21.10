package com.luneruniverse.minecraft.mod.nbteditor.server;

import java.lang.invoke.MethodType;
import java.util.Collection;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.MenuProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecartContainer;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.Level;

public class ServerMVMisc {
	
	private static final Supplier<Reflection.MethodInvoker> EntityTrackingListener_sendPacket =
			Reflection.getOptionalMethod(() -> Reflection.getClass("net.minecraft.class_5629"), () -> "method_14364", () -> MethodType.methodType(void.class, Packet.class));
	public static void sendS2CPacket(ServerPlayer player, Packet<?> packet) {
		Version.newSwitch()
				.range("1.20.2", null, () -> player.connection.send(packet))
				.range(null, "1.20.1", () -> EntityTrackingListener_sendPacket.get().invoke(player.connection, packet))
				.run();
	}
	
	public static boolean isInstanceOfVehicleInventory(MenuProvider factory) {
		return Version.<Boolean>newSwitch()
				.range("1.19.0", null, () -> factory instanceof ContainerEntity)
				.range(null, "1.18.2", () -> factory instanceof AbstractMinecartContainer)
				.get();
	}
	
	private static final Supplier<Reflection.MethodInvoker> PacketDecoder_decode =
			Reflection.getOptionalMethod(() -> Reflection.getClass("net.minecraft.class_9141"), () -> "decode", () -> MethodType.methodType(Object.class, Object.class));
	@SuppressWarnings("unchecked")
	public static <T> T packetCodecDecode(Object codec, Object buf) {
		return Version.<T>newSwitch()
				.range("1.20.5", null, () -> (T) PacketDecoder_decode.get().invoke(codec, buf))
				.range(null, "1.20.4", () -> { throw new IllegalStateException("Not supported in this version!"); })
				.get();
	}
	private static final Supplier<Reflection.MethodInvoker> PacketEncoder_encode =
			Reflection.getOptionalMethod(() -> Reflection.getClass("net.minecraft.class_9142"), () -> "encode", () -> MethodType.methodType(void.class, Object.class, Object.class));
	public static void packetCodecEncode(Object codec, Object buf, Object value) {
		Version.newSwitch()
				.range("1.20.5", null, () -> PacketEncoder_encode.get().invoke(codec, buf, value))
				.range(null, "1.20.4", () -> { throw new IllegalStateException("Not supported in this version!"); })
				.run();
	}
	
	private static final Supplier<Reflection.MethodInvoker> EntityType_create =
			Reflection.getOptionalMethod(EntityType.class, "method_5883", MethodType.methodType(Entity.class, Level.class));
	public static Entity createEntity(EntityType<?> entityType, Level world) {
		return Version.<Entity>newSwitch()
				.range("1.21.2", null, () -> entityType.create(world, EntitySpawnReason.COMMAND))
				.range(null, "1.21.1", () -> EntityType_create.get().invoke(entityType, world))
				.get();
	}
	
	private static final Supplier<Reflection.MethodInvoker> Entity_hasPermissionLevel =
			Reflection.getOptionalMethod(Entity.class, "method_5687", MethodType.methodType(boolean.class, int.class));
	public static boolean hasPermissionLevel(Player player, int level) {
		return Version.<Boolean>newSwitch()
				.range("1.21.2", null, () -> player.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(level))))
				.range(null, "1.21.1", () -> Entity_hasPermissionLevel.get().invoke(player, level))
				.get();
	}
	
	private static final Supplier<Reflection.MethodInvoker> Property_getValues =
			Reflection.getOptionalMethod(Property.class, "method_11898", MethodType.methodType(Collection.class));
	public static <T extends Comparable<T>> Collection<T> getValues(Property<T> property) {
		return Version.<Collection<T>>newSwitch()
				.range("1.21.2", null, () -> property.getPossibleValues())
				.range(null, "1.21.1", () -> Property_getValues.get().invoke(property))
				.get();
	}
	
}
