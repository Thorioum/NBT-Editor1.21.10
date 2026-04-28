package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import java.util.OptionalInt;

import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVServerNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ContainerScreenS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMixinLink;

import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.server.level.ServerPlayer;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
	@Inject(method = "openMenu", at = @At("HEAD"))
	private void openHandledScreen(MenuProvider factory, CallbackInfoReturnable<OptionalInt> info) {
		if (factory instanceof BaseContainerBlockEntity ||
				ServerMainUtil.getRootEnclosingClass(factory.getClass()) == ChestBlock.class || // Double chests
				ServerMVMisc.isInstanceOfVehicleInventory(factory))
			MVServerNetworking.send((ServerPlayer) (Object) this, new ContainerScreenS2CPacket());
	}
	@ModifyVariable(method = "openMenu", at = @At("STORE"), ordinal = 0)
	private AbstractContainerMenu openHandledScreen_screenHandler(AbstractContainerMenu screenHandler) {
		ServerPlayer source = (ServerPlayer) (Object) this;
		if (screenHandler instanceof ChestMenu generic && generic.getContainer() == source.getEnderChestInventory())
			MVServerNetworking.send(source, new ContainerScreenS2CPacket());
		return screenHandler;
	}
	@Inject(method = "openHorseInventory", at = @At("HEAD"))
	private void openHorseInventory(AbstractHorse horse, Container inventory, CallbackInfo info) {
		MVServerNetworking.send((ServerPlayer) (Object) this, new ContainerScreenS2CPacket());
	}
	
	@Inject(method = "initMenu", at = @At("HEAD"))
	private void onScreenHandlerOpened(AbstractContainerMenu screenHandler, CallbackInfo info) {
		for (Slot slot : screenHandler.slots)
			ServerMixinLink.SLOT_OWNER.put(slot, (ServerPlayer) (Object) this);
	}
}
