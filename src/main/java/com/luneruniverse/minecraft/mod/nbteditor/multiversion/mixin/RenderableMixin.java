package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableParent;

import net.minecraft.client.gui.components.Renderable;
import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(Renderable.class)
public interface RenderableMixin extends MVDrawableParent {

}
