package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableParent;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(Drawable.class)
public interface DrawableMixin extends MVDrawableParent {

}
