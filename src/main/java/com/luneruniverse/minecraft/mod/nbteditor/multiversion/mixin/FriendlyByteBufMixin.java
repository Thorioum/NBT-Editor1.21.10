package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin;

import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import net.minecraft.data.registries.VanillaRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.DynamicRegistryManagerHolder;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVPacketByteBufParent;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMVMisc;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

@Mixin(FriendlyByteBuf.class)
public abstract class FriendlyByteBufMixin implements MVPacketByteBufParent {
	
	@Shadow
	private ByteBuf source;
	@Shadow
	public abstract String readUtf();
	@Shadow
	public abstract FriendlyByteBuf writeUtf(String str);
	@Shadow
	public abstract double readDouble();
	
	@Override
	public FriendlyByteBuf writeBoolean(boolean value) {
		source.writeBoolean(value);
		return (FriendlyByteBuf) (Object) this;
	}
	
	@Override
	public FriendlyByteBuf writeDouble(double value) {
		source.writeDouble(value);
		return (FriendlyByteBuf) (Object) this;
	}
	
	@Override
	public Identifier readIdentifier() {
		return IdentifierInst.of(readUtf());
	}
	@Override
	public FriendlyByteBuf writeIdentifier(Identifier id) {
		return writeUtf(id.toString());
	}
	
	@Override
	public <T> ResourceKey<T> readRegistryKey(ResourceKey<? extends Registry<T>> registryRef) {
		return ResourceKey.create(registryRef, readIdentifier());
	}
	@Override
	public void writeRegistryKey(ResourceKey<?> key) {
		writeIdentifier(key.identifier());
	}
	
	private static final Supplier<Reflection.MethodInvoker> PacketByteBuf_writeNbt =
			Reflection.getOptionalMethod(FriendlyByteBuf.class, "method_10794", MethodType.methodType(FriendlyByteBuf.class, CompoundTag.class));
	@Override
	public FriendlyByteBuf writeNbtCompound(CompoundTag element) {
		return Version.<FriendlyByteBuf>newSwitch()
				.range("1.20.2", null, () -> ((FriendlyByteBuf) (Object) this).writeNbt(element))
				.range(null, "1.20.1", () -> PacketByteBuf_writeNbt.get().invoke(this, (CompoundTag) element))
				.get();
	}
	
	@Override
	public Vec3 readVec3d() {
		return new Vec3(readDouble(), readDouble(), readDouble());
	}
	@Override
	public void writeVec3d(Vec3 vector) {
		writeDouble(vector.x());
		writeDouble(vector.y());
		writeDouble(vector.z());
	}
	
	private static final Supplier<Reflection.MethodInvoker> PacketByteBuf_readItemStack =
			Reflection.getOptionalMethod(FriendlyByteBuf.class, "method_10819", MethodType.methodType(ItemStack.class));
	@Override
	public ItemStack readItemStack() {
		return Version.<ItemStack>newSwitch()
				.range("1.20.5", null, () -> ServerMVMisc.packetCodecDecode(ItemStack.OPTIONAL_STREAM_CODEC, createRegistryByteBuf()))
				.range(null, "1.20.4", () -> PacketByteBuf_readItemStack.get().invoke(this))
				.get();
	}
	private static final Supplier<Reflection.MethodInvoker> PacketByteBuf_writeItemStack =
			Reflection.getOptionalMethod(FriendlyByteBuf.class, "method_10793", MethodType.methodType(FriendlyByteBuf.class, ItemStack.class));
	@Override
	public FriendlyByteBuf writeItemStack(ItemStack item) {
		Version.newSwitch()
				.range("1.20.5", null, () -> ServerMVMisc.packetCodecEncode(ItemStack.OPTIONAL_STREAM_CODEC, createRegistryByteBuf(), item))
				.range(null, "1.20.4", () -> PacketByteBuf_writeItemStack.get().invoke(this, item))
				.run();
		return (FriendlyByteBuf) (Object) this;
	}
	
	private Object createRegistryByteBuf() {
		return Reflection.newInstance("net.minecraft.class_9129",
				new Class<?>[] {ByteBuf.class, RegistryAccess.class},
                source, (MainUtil.client.getConnection() == null ? VanillaRegistries.createLookup() : MainUtil.client.getConnection().registryAccess()));
	}
	
}
