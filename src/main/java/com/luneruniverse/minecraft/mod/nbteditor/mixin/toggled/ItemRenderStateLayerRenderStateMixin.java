package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.luneruniverse.minecraft.mod.nbteditor.MC_1_17_Link.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.MC_1_17_Link.MixinLink;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.ItemRenderState.Glint;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

@Mixin(ItemRenderState.LayerRenderState.class)
public class ItemRenderStateLayerRenderStateMixin {

	
}
