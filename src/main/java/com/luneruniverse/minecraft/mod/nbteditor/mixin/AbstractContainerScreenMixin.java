package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.commands.get.GetLostItemCommand;
import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientHandledScreen;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerInput;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
	@Inject(method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V", at = @At("HEAD"), cancellable = true)
	private void onMouseClick(Slot slot, int slotId, int button, ContainerInput actionType, CallbackInfo info) {
		if ((AbstractContainerScreen<?>) (Object) this instanceof ClientHandledScreen)
			return;
		MixinLink.onMouseClick((AbstractContainerScreen<?>) (Object) this, slot, slotId, button, actionType, info);
	}
	@Inject(method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V", at = @At("RETURN"))
	private void onMouseClickReturn(Slot slot, int slotId, int button, ContainerInput actionType, CallbackInfo info) {
		if ((AbstractContainerScreen<?>) (Object) this instanceof ClientHandledScreen)
			return;
		ItemStack cursor = ((AbstractContainerScreen<?>) (Object) this).getMenu().getCarried();
		if (!cursor.isEmpty())
			GetLostItemCommand.addToHistory(cursor);
	}
	
	@Inject(method = "keyPressed", at = @At(value = "HEAD"), cancellable = true)
	private void keyPressed(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
		AbstractContainerScreen<?> source = (AbstractContainerScreen<?>) (Object) this;
		if (source instanceof CreativeModeInventoryScreen || source instanceof ClientHandledScreen)
			return;
		MixinLink.keyPressed(source, input.key(), input.scancode(), input.modifiers(), cir);
	}
}
