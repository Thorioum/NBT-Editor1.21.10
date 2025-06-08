package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ImportScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;

@Mixin(Screen.class)
public class ScreenMixin {
	@Inject(method = "clearAndInit", at = @At("RETURN"))
	private void clearChildren(CallbackInfo info) {
		MixinLink.addCreativeTabs((Screen) (Object) this);
	}
	@Inject(method = "init(Lnet/minecraft/client/MinecraftClient;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;init()V"), require = 0)
	private void init(MinecraftClient client, int width, int height, CallbackInfo info) {
		Version.newSwitch()
				.range("1.19.4", null, () -> MixinLink.addCreativeTabs((Screen) (Object) this))
				.range(null, "1.19.3", () -> {})
				.run();
	}
	
	@Inject(method = "onFilesDropped", at = @At("HEAD"))
	private void filesDragged(List<Path> paths, CallbackInfo info) {
		Screen source = (Screen) (Object) this;
		if (source instanceof HandledScreen || source instanceof GameMenuScreen)
			ImportScreen.importFiles(paths, Optional.empty());
	}
	
	@Inject(method = "handleTextClick", at = @At("HEAD"), cancellable = true)
	private void handleTextClick(Style style, CallbackInfoReturnable<Boolean> info) {
		if (style != null && !Screen.hasShiftDown() && style.getClickEvent() != null &&
				style.getClickEvent().getAction() == ClickEvent.Action.OPEN_FILE &&
				MixinLink.tryRunClickEvent(((ClickEvent.OpenFile)style.getClickEvent()).path())) {
			info.setReturnValue(true);
		}
	}

}
