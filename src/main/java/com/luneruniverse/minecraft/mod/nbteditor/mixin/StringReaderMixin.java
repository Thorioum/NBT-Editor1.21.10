package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import com.mojang.brigadier.StringReader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StringReader.class)
public abstract class StringReaderMixin {

    @Shadow private int cursor;

    @Shadow @Final private String string;

    @Shadow public abstract boolean canRead();

    @Inject(method = "readDouble", at = @At("HEAD"), cancellable = true,remap = false)
    private void onParseDouble(CallbackInfoReturnable<Double> cir) {
        final int start = this.cursor;
        if(!canRead()) return;
        String s = string.substring(start);
        if(s.startsWith("NaNd")) {
            cir.setReturnValue(Double.NaN);
            cursor+=4;
        }
        if(s.startsWith("-NaNd")) {
            cir.setReturnValue(-1*Double.NaN);
            cursor+=5;
        }
        if(s.startsWith("Infinityd")) {
            cir.setReturnValue(Double.MAX_VALUE);
            cursor+=9;
        }
        if(s.startsWith("-Infinityd")) {
            cir.setReturnValue(Double.MIN_VALUE);
            cursor+=10;
        }
    }
    @Inject(method = "readFloat", at = @At("HEAD"), cancellable = true,remap = false)
    private void onParseFloat(CallbackInfoReturnable<Float> cir) {
        final int start = this.cursor;
        if(!canRead()) return;
        String s = string.substring(start);
        if(s.startsWith("NaNf")) {
            cir.setReturnValue(Float.NaN);
            cursor+=4;
        }
        if(s.startsWith("-NaNf")) {
            cir.setReturnValue(-1*Float.NaN);
            cursor+=5;
        }
        if(s.startsWith("Infinityf")) {
            cir.setReturnValue(Float.MAX_VALUE);
            cursor+=9;
        }
        if(s.startsWith("-Infinityf")) {
            cir.setReturnValue(Float.MIN_VALUE);
            cursor+=10;
        }
    }
}
