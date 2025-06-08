package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ClickSlotC2SPacketParent;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;

import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;

@Mixin(ClickSlotC2SPacket.class)
public class ClickSlotC2SPacketMixin implements ClickSlotC2SPacketParent {
	@Shadow @Final private byte button;
	@Unique
	private static final int NO_SLOT_RESTRICTIONS_FLAG = 64;

	
	@ModifyVariable(method = "<init>", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	@Group(name = "<init>", min = 1)
	private static byte init_new(byte button) {
		if (ConfigScreen.isNoSlotRestrictions() && NBTEditorClient.SERVER_CONN.isEditingExpanded())
			return (byte) (button | NO_SLOT_RESTRICTIONS_FLAG);
		return button;
	}
	
	@Inject(method = "button", at = @At("RETURN"), cancellable = true)
	private void getButton(CallbackInfoReturnable<Integer> info) {
		info.setReturnValue(info.getReturnValue() & ~NO_SLOT_RESTRICTIONS_FLAG);
	}
	@Override
	public boolean isNoSlotRestrictions() {
		return (button & NO_SLOT_RESTRICTIONS_FLAG) != 0;
	}
}
