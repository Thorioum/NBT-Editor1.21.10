package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import java.util.function.BiFunction;

import com.mojang.blaze3d.shaders.ShaderSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.shaders.MVShader3;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.GpuDevice;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.resources.Identifier;

@Mixin(GameRenderer.class)
public class GameRendererMixin_1_21_5 {
	@Inject(method = "preloadUiShader", at = @At("RETURN"))
	private void preloadPrograms(ResourceProvider factory, CallbackInfo info, @Local GpuDevice gpuDevice, @Local ShaderSource sourceRetriever) {
		for (RenderPipeline pipeline : MVShader3.RENDER_PIPELINES)
			gpuDevice.precompilePipeline(pipeline, sourceRetriever);
	}
}
