package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodType;
import java.nio.FloatBuffer;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import org.joml.*;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

public class MVMatrix4f {
	
	public static final Class<?> Matrix4f_class = Version.<Class<?>>newSwitch()
			.range("1.19.3", null, () -> Reflection.getClass("org.joml.Matrix4f"))
			.range(null, "1.19.2", () -> Reflection.getClass("net.minecraft.class_1159"))
			.get();
	public static final Class<?> Matrix4fc_class = Version.<Class<?>>newSwitch()
			.range("1.19.3", null, () -> Reflection.getClass("org.joml.Matrix4fc"))
			.range(null, "1.19.2", Matrix4f_class)
			.get();

	
}
