package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data;

import java.util.List;
import java.util.Optional;

import net.minecraft.world.effect.MobEffectInstance;

public record CustomPotionContents(Optional<Integer> color, List<MobEffectInstance> effects) {
	
}
