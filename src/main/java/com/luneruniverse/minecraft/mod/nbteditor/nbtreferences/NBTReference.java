package com.luneruniverse.minecraft.mod.nbteditor.nbtreferences;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.HandItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public interface NBTReference<T extends LocalNBT> {
	public static CompletableFuture<? extends Optional<? extends NBTReference<?>>> getReference(NBTReferenceFilter filter, boolean airable) {
		HitResult target = MainUtil.client.hitResult;
		if (target instanceof EntityHitResult entity && filter.isEntityAllowed()) {
			return EntityReference.getEntity(entity.getEntity().level().dimension(), entity.getEntity().getUUID())
					.thenApply(ref -> ref.<NBTReference<?>>map(UnaryOperator.identity())
							.filter(filter).or(() -> getClientReference(target, filter, airable)));
		}
		if (target instanceof BlockHitResult block && filter.isBlockAllowed()) {
			return BlockReference.getBlock(MainUtil.client.level.dimension(), block.getBlockPos())
					.thenApply(ref -> ref.<NBTReference<?>>map(UnaryOperator.identity())
							.filter(filter).or(() -> getClientReference(target, filter, airable)));
		}
		return CompletableFuture.completedFuture(getClientReference(target, filter, airable));
	}
	private static Optional<? extends NBTReference<?>> getClientReference(HitResult target, NBTReferenceFilter filter, boolean airable) {
		boolean heldItemDisallowed = false;
		if (filter.isItemAllowed()) {
			try {
				ItemReference ref = ItemReference.getHeldItem();
				if (filter.test(ref))
					return Optional.of(ref);
				heldItemDisallowed = true;
			} catch (CommandSyntaxException e) {}
		}
		
		if (filter.isBlockAllowed() && NBTEditorClient.SERVER_CONN.isEditingExpanded()) {
			if (target instanceof BlockHitResult block && block.getType() != HitResult.Type.MISS) {
				BlockReference ref = BlockReference.getBlockWithoutNBT(block.getBlockPos());
				if (filter.test(ref))
					return Optional.of(ref);
			}
		}
		
		if (airable && !heldItemDisallowed && filter.isItemAllowed())
			return Optional.of(new HandItemReference(InteractionHand.MAIN_HAND));
		
		return Optional.empty();
	}
	
	public static void getReference(NBTReferenceFilter filter, boolean airable, Consumer<NBTReference<?>> consumer) {
		NBTReference.getReference(filter, airable).thenAccept(ref -> MainUtil.client.execute(() -> {
			ref.ifPresentOrElse(consumer, () -> {
				if (MainUtil.client.player != null)
					MainUtil.client.player.sendSystemMessage(filter.getFailMessage());
			});
		}));
	}
	
	public boolean exists();
	
	T getLocalNBT();
	public default void saveLocalNBT(T nbt, Runnable onFinished) {
		saveNBT(nbt.getId(), nbt.getNBT(), onFinished);
	}
	public default void saveLocalNBT(T nbt, Component msg) {
		saveLocalNBT(nbt, () -> MainUtil.client.player.sendSystemMessage(msg));
	}
	public default void saveLocalNBT(T nbt) {
		saveLocalNBT(nbt, () -> {});
	}
	public default void modifyLocalNBT(Consumer<T> nbtConsumer, Runnable onFinished) {
		T nbt = getLocalNBT();
		nbtConsumer.accept(nbt);
		saveLocalNBT(nbt, onFinished);
	}
	public default void modifyLocalNBT(Consumer<T> nbtConsumer, Component msg) {
		modifyLocalNBT(nbtConsumer, () -> MainUtil.client.player.sendSystemMessage(msg));
	}
	public default void modifyLocalNBT(Consumer<T> nbtConsumer) {
		modifyLocalNBT(nbtConsumer, () -> {});
	}
	
	public Identifier getId();
	public CompoundTag getNBT();
	public void saveNBT(Identifier id, CompoundTag toSave, Runnable onFinished);
	public default void saveNBT(Identifier id, CompoundTag toSave, Component msg) {
		saveNBT(id, toSave, () -> MainUtil.client.player.sendSystemMessage(msg));
	}
	public default void saveNBT(Identifier id, CompoundTag toSave) {
		saveNBT(id, toSave, () -> {});
	}
	
	public default void showParent() {
		escapeParent();
	}
	public default void escapeParent() {
		NBTEditorClient.CURSOR_MANAGER.closeRoot();
	}
}
