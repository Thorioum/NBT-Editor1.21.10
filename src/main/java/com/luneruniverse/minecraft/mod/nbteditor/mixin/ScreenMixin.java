package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import net.minecraft.network.chat.ClickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTextEvents;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ImportScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.CreativeTabWidget;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Style;

@Mixin(Screen.class)
public class ScreenMixin {
	@Inject(method = "clearWidgets", at = @At("RETURN"))
	private void clearChildren(CallbackInfo info) {
		CreativeTabWidget.addCreativeTabs((Screen) (Object) this);
	}
	@Inject(method = "init(II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;init()V"), require = 0)
	private void init(int width, int height, CallbackInfo ci) {
		Version.newSwitch()
				.range("1.19.4", null, () -> CreativeTabWidget.addCreativeTabs((Screen) (Object) this))
				.range(null, "1.19.3", () -> {})
				.run();
	}
	
	@Inject(method = "onFilesDrop", at = @At("HEAD"))
	private void onFilesDropped(List<Path> paths, CallbackInfo info) {
		Screen source = (Screen) (Object) this;
		if (source instanceof AbstractContainerScreen || source instanceof PauseScreen)
			ImportScreen.importFiles(paths, Optional.empty());
	}
	
	@Inject(method = "defaultHandleGameClickEvent", at = @At("HEAD"), cancellable = true)
	private static void handleTextClick(ClickEvent event, Minecraft minecraft, Screen activeScreen, CallbackInfo ci) {
		if (event != null) {
			MVTextEvents.ClickAction<?> clickAction = MVTextEvents.ClickAction.getAction(event);
			if (clickAction == MVTextEvents.ClickAction.OPEN_FILE &&
					MixinLink.tryRunClickEvent(clickAction.getStringifiedValue(event))) {
				ci.cancel();
			}
		}
	}
}
