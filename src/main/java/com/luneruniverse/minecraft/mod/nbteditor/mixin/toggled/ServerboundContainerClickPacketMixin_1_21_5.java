package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ClickSlotC2SPacketParent;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;

import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;

@Mixin(ServerboundContainerClickPacket.class)
public class ServerboundContainerClickPacketMixin_1_21_5 implements ClickSlotC2SPacketParent {
	private static final byte NO_SLOT_RESTRICTIONS_FLAG = 0b01000000;
	
	@Shadow
	private byte buttonNum;
	
	@ModifyVariable(method = "<init>(IISBLnet/minecraft/world/inventory/ContainerInput;Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;Lnet/minecraft/network/HashedStack;)V", at = @At("HEAD"))
	private static byte init(byte button) {
		if (ConfigScreen.isNoSlotRestrictions() && NBTEditorClient.SERVER_CONN.isEditingExpanded())
			return (byte) (button | NO_SLOT_RESTRICTIONS_FLAG);
		return button;
	}
	
	@Inject(method = "buttonNum", at = @At("RETURN"), cancellable = true)
	private void button(CallbackInfoReturnable<Byte> info) {
		info.setReturnValue((byte) (info.getReturnValue() & ~NO_SLOT_RESTRICTIONS_FLAG));
	}
	@Override
	public boolean isNoSlotRestrictions() {
		return (buttonNum & NO_SLOT_RESTRICTIONS_FLAG) != 0;
	}
}
