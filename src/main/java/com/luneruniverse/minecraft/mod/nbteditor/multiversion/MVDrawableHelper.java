package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderPipelines;
import com.mojang.blaze3d.opengl.GlProgram;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fStack;

public class MVDrawableHelper {
	
	private static final Cache<Matrix3x2fStack, GuiGraphicsExtractor> drawContexts = CacheBuilder.newBuilder().weakKeys().weakValues().build();
	public static Matrix3x2fStack getMatrices(GuiGraphicsExtractor context) {
		Matrix3x2fStack matrices = context.pose();
		drawContexts.put(matrices, context);
		return matrices;
	}
	public static GuiGraphicsExtractor getDrawContext(Matrix3x2fStack matrices) {
		return drawContexts.getIfPresent(matrices);
	}

	public static void render(Renderable caller, Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		Version.newSwitch()
				.range("1.20.0", null, () -> caller.extractRenderState(MVDrawableHelper.getDrawContext(matrices), mouseX, mouseY, delta))
				.run();
	}
	
	public static MultiBufferSource.BufferSource getVertexConsumerProvider() {
		return MainUtil.client.gameRenderer.renderBuffers.bufferSource();
	}
	
	
	private static final Cache<String, Reflection.MethodInvoker> methodCache = CacheBuilder.newBuilder().build();
	@SuppressWarnings("unchecked")
	private static <R> R call(String method, Class<?> rtype, Class<?>[] ptypes, Matrix3x2fStack matrices, Object... args) {
		try {
			GuiGraphicsExtractor context;
			MethodType type;
			context = MVDrawableHelper.getDrawContext(matrices);
			type = MethodType.methodType(rtype, ptypes);
			return (R) methodCache.get(method, () -> Reflection.getMethod(GuiGraphicsExtractor.class, method, type)).invoke(context, args);
		} catch (ExecutionException | UncheckedExecutionException e) {
			throw new RuntimeException("Error invoking method", e);
		}
	}
	
	
	public static void fill(Matrix3x2fStack matrices, int x1, int y1, int x2, int y2, int color) {
		MVDrawableHelper.getDrawContext(matrices).fill(x1, y1, x2, y2, color);
	}
	
	public static void drawText(Matrix3x2fStack matrices, Font textRenderer, Component text, int x, int y, int color, boolean shadow) {
		if (shadow)
			drawTextWithShadow(matrices, textRenderer, text, x, y, color);
		else
			drawTextWithoutShadow(matrices, textRenderer, text, x, y, color);
	}
	
	private static final Supplier<Reflection.MethodInvoker> TextRenderer_draw =
			Reflection.getOptionalMethod(Font.class, "method_30883", MethodType.methodType(int.class, PoseStack.class, Component.class, float.class, float.class, int.class));
	public static void drawTextWithoutShadow(Matrix3x2fStack matrices, Font textRenderer, Component text, int x, int y, int color) {
		Version.newSwitch()
				.range("1.20.0", null, () -> getDrawContext(matrices).text(textRenderer, text, x, y, color, false))
				.range(null, "1.19.4", () -> TextRenderer_draw.get().invoke(textRenderer, matrices, text, x, y, color))
				.run();
	}
	
	public static void drawTextWithShadow(Matrix3x2fStack matrices, Font textRenderer, Component text, int x, int y, int color) {
		getDrawContext(matrices).text(textRenderer, text, x, y, color);
	}
	
	public static void drawCenteredTextWithShadow(Matrix3x2fStack matrices, Font textRenderer, Component text, int x, int y, int color) {
		MVDrawableHelper.getDrawContext(matrices).centeredText(textRenderer, text, x, y, color);
	}
	
	private static final Supplier<Reflection.MethodInvoker> DrawContext_drawTexture =
			Reflection.getOptionalMethod(GuiGraphicsExtractor.class, "method_25290", MethodType.methodType(void.class, Identifier.class, int.class, int.class, float.class, float.class, int.class, int.class, int.class, int.class));
	private static final Supplier<Reflection.MethodInvoker> GameRenderer_getPositionTexProgram =
			Reflection.getOptionalMethod(GameRenderer.class, "method_34542", MethodType.methodType(GlProgram.class));
	private static final Supplier<Reflection.MethodInvoker> RenderSystem_setShader =
			Reflection.getOptionalMethod(RenderSystem.class, "setShader", MethodType.methodType(void.class, Supplier.class));
	private static final Supplier<Reflection.MethodInvoker> RenderSystem_setShaderTexture =
			Reflection.getOptionalMethod(RenderSystem.class, "setShaderTexture", MethodType.methodType(void.class, int.class, Identifier.class));
	public static void drawTexture(Matrix3x2fStack matrices, Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
		Version.newSwitch()
				.range("1.21.2", null, () -> getDrawContext(matrices).blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, textureWidth, textureHeight))
				.range("1.20.0", "1.21.1", () -> DrawContext_drawTexture.get().invoke(getDrawContext(matrices), texture, x, y, u, v, width, height, textureWidth, textureHeight))
				.run();
	}
	public static void drawTexture(Matrix3x2fStack matrices, Identifier texture, int x, int y, float u, float v, int width, int height) {
		drawTexture(matrices, texture, x, y, u, v, width, height, 256, 256);
	}
	
	private static final Supplier<Reflection.MethodInvoker> Screen_renderTooltip_Text =
			Reflection.getOptionalMethod(Screen.class, "method_25424", MethodType.methodType(void.class, PoseStack.class, Component.class, int.class, int.class));
	public static void renderTooltip(Matrix3x2fStack matrices, Component text, int x, int y) {
		Version.newSwitch()
				.range("1.20.0", null, () -> getDrawContext(matrices).setTooltipForNextFrame(MainUtil.client.font, text, x, y))
				.range(null, "1.19.4", () -> Screen_renderTooltip_Text.get().invoke(MainUtil.client.screen, matrices, text, x, y))
				.run();
	}
	
	private static final Supplier<Reflection.MethodInvoker> Screen_renderTooltip_List =
			Reflection.getOptionalMethod(Screen.class, "method_25417", MethodType.methodType(void.class, PoseStack.class, List.class, int.class, int.class));
	public static void renderTooltip(Matrix3x2fStack matrices, List<FormattedCharSequence> lines, int x, int y) {
		Version.newSwitch()
				.range("1.20.0", null, () -> getDrawContext(matrices).setTooltipForNextFrame(MainUtil.client.font, lines, x, y))
				.range(null, "1.19.4", () -> Screen_renderTooltip_List.get().invoke(MainUtil.client.screen, matrices, lines, x, y))
				.run();
	}

	public static void renderItem(Matrix3x2fStack matrices, float zOffset, boolean setScreenZOffset, ItemStack item, int x, int y) {
		Font textRenderer = MainUtil.client.font;
		Version.newSwitch()
				.range("1.20.0", null, () -> {
					GuiGraphicsExtractor context = getDrawContext(matrices);
					context.item(item, x, y);
					context.itemDecorations(textRenderer, item, x, y);
				})
				.run();
	}
	
	private static final Supplier<Reflection.MethodInvoker> Screen_renderBackground_MatrixStack =
			Reflection.getOptionalMethod(Screen.class, "method_25420", MethodType.methodType(void.class, PoseStack.class));
	private static final Supplier<Reflection.MethodInvoker> Screen_renderBackground_DrawContext =
			Reflection.getOptionalMethod(Screen.class, "method_25420", MethodType.methodType(void.class, GuiGraphicsExtractor.class));
	public static void renderBackground(Screen screen, Matrix3x2fStack matrices) {
		int[] mousePos = MainUtil.getMousePos();
		Version.newSwitch()
				.range("1.20.5", null, () -> {
					if (MainUtil.client.level == null)
						screen.extractBackground(getDrawContext(matrices), mousePos[0], mousePos[1], MVMisc.getTickDelta());
					else
						screen.extractTransparentBackground(getDrawContext(matrices));
				})
				.range("1.20.2", "1.20.4", () -> screen.extractBackground(getDrawContext(matrices), mousePos[0], mousePos[1], MVMisc.getTickDelta()))
				.range("1.20.0", "1.20.1", () -> Screen_renderBackground_DrawContext.get().invoke(screen, MVDrawableHelper.getDrawContext(matrices)))
				.range(null, "1.19.4", () -> Screen_renderBackground_MatrixStack.get().invoke(screen, matrices))
				.run();
	}
	
	private static final Supplier<Reflection.MethodInvoker> DrawableHelper_fillGradient =
			Reflection.getOptionalMethod(GuiGraphicsExtractor.class, "method_33284", MethodType.methodType(void.class, PoseStack.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class));
	public static void drawSlotHighlight(Matrix3x2fStack matrices, int x, int y, int color) {
		Version.newSwitch()
				.range("1.20.0", null, () -> getDrawContext(matrices).fillGradient(x, y, x + 16, y + 16, color, color))
				.range(null, "1.19.4", () -> {
					MVGlStateManager._disableDepthTest();
					MVGlStateManager._colorMask(true, true, true, false);
					DrawableHelper_fillGradient.get().invoke(null, matrices, x, y, x + 16, y + 16, color, color, 0);
					MVGlStateManager._colorMask(true, true, true, true);
					MVGlStateManager._enableDepthTest();
				})
				.run();
	}
	
	private static final Supplier<Reflection.MethodInvoker> RenderSystem_applyModelViewMatrix =
			Reflection.getOptionalMethod(RenderSystem.class, "applyModelViewMatrix", MethodType.methodType(void.class));
	public static void applyModelViewMatrix() {
		Version.newSwitch()
				.range("1.21.2", null, () -> {})
				.range(null, "1.21.1", () -> RenderSystem_applyModelViewMatrix.get().invoke(null))
				.run();
	}
	
	public static void enableScissor(Matrix3x2fStack matrices, int x, int y, int width, int height) {
		Version.newSwitch()
				.range("1.20.0", null, () -> getDrawContext(matrices).enableScissor(x, y, x + width, y + height))
				.run();
	}
	public static void disableScissor(Matrix3x2fStack matrices) {
		Version.newSwitch()
				.range("1.20.0", null, () -> getDrawContext(matrices).disableScissor())
				.run();
	}
	
}
