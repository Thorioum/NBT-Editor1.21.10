package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.async.UpdateCheckerThread;
import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	
	@Inject(method = "setOverlay", at = @At("HEAD"))
	private void setOverlay(Overlay overlay, CallbackInfo info) {
		if (((Minecraft) (Object) this).getOverlay() instanceof LoadingOverlay && overlay == null && !MixinLink.CLIENT_LOADED) {
			MixinLink.CLIENT_LOADED = true;
			new UpdateCheckerThread().start();
		}
	}
	
	@Inject(method = "setScreen", at = @At("HEAD"))
	private void setScreen(Screen screen, CallbackInfo info) {
		if (screen == null) {
			NBTEditorClient.CURSOR_MANAGER.onNoScreenSet();
		} else if (screen instanceof AbstractContainerScreen<?> handledScreen) {
			for (Slot slot : handledScreen.getMenu().slots)
				ServerMixinLink.SLOT_OWNER.put(slot, MainUtil.client.player);
			NBTEditorClient.CURSOR_MANAGER.onHandledScreenSet(handledScreen);
		}
	}
	
	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/thread/ReentrantBlockableEventLoop;<init>(Ljava/lang/String;Z)V", shift = At.Shift.AFTER))
	private void init(GameConfig args, CallbackInfo info) {
		MixinLink.MAIN_THREAD = Thread.currentThread();
	}
	
	@Inject(method = "destroy", at = @At("HEAD"))
	private void stop(CallbackInfo info) {
		if (NBTEditorClient.CLIENT_CHEST != null)
			NBTEditorClient.CLIENT_CHEST.stop();
	}
	
}
