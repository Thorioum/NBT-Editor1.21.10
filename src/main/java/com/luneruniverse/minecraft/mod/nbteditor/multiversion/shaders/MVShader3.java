package com.luneruniverse.minecraft.mod.nbteditor.multiversion.shaders;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public class MVShader3 extends MVShader {
	
	public static final List<RenderPipeline> RENDER_PIPELINES = new ArrayList<>();
	
	private final RenderType layer;
	
	public MVShader3(MVShader.Builder builder) {
		RenderPipeline.Builder pipelineBuilder = RenderPipeline.builder(builder.getSnippets().stream()
				.map(snippet -> (RenderPipeline.Snippet) snippet).toArray(RenderPipeline.Snippet[]::new))
				.withLocation("pipeline/" + builder.getLayerName())
				.withVertexShader("core/" + builder.getShaderName())
				.withFragmentShader("core/" + builder.getShaderName())
				.withVertexFormat((VertexFormat) builder.getVertexFormat().getInternalValue(),
						(VertexFormat.Mode) builder.getDrawMode().getInternalValue());
		
		if (builder.isTranslucentBlendFunc())
			pipelineBuilder.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT));

		RenderPipeline pipeline = RenderPipelines.register(pipelineBuilder.build());
		
		layer = RenderType.create(
				builder.getLayerName(),
				RenderSetup.builder(pipeline).affectsCrumbling().bufferSize(builder.getExpectedBufferSize()).createRenderSetup());
		
		RENDER_PIPELINES.add(pipeline);
	}
	
	@Override
	public RenderType getLayer() {
		return layer;
	}
	
}
