package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.datafixers.util.Pair;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
}
