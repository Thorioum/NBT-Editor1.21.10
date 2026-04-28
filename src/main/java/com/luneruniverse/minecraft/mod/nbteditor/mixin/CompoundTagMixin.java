package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import net.minecraft.nbt.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Optional;

@Mixin(CompoundTag.class)
public abstract class CompoundTagMixin {
    @Shadow
    @Final
    private Map<String, Tag> tags;

    @Inject(method = "put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;", at = @At("HEAD"), cancellable = true)
    public void injPut(String key, Tag element, CallbackInfoReturnable<Tag> cir) {
        if (element instanceof StringTag e) {
            Optional<String> n = e.asString();
            if(n.isEmpty()) return;
            switch (n.get()) {
                case "NaNd" -> cir.setReturnValue(this.tags.put(key, DoubleTag.valueOf(Double.NaN)));
                case "NaNf" -> cir.setReturnValue(this.tags.put(key, FloatTag.valueOf(Float.NaN)));
                case "-NaNd" -> cir.setReturnValue(this.tags.put(key, DoubleTag.valueOf(-1*Double.NaN)));
                case "-NaNf" -> cir.setReturnValue(this.tags.put(key, FloatTag.valueOf(-1*Float.NaN)));
                case "Infinityd" -> cir.setReturnValue(this.tags.put(key, DoubleTag.valueOf(Double.POSITIVE_INFINITY)));
                case "Infinityf" -> cir.setReturnValue(this.tags.put(key, FloatTag.valueOf(Float.POSITIVE_INFINITY)));
                case "-Infinityd" -> cir.setReturnValue(this.tags.put(key, DoubleTag.valueOf(Double.NEGATIVE_INFINITY)));
                case "-Infinityf" -> cir.setReturnValue(this.tags.put(key, FloatTag.valueOf(Float.NEGATIVE_INFINITY)));
            }
        }
    }
}
