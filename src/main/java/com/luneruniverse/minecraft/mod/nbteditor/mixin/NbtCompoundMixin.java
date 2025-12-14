package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import net.minecraft.nbt.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Optional;

@Mixin(NbtCompound.class)
public abstract class NbtCompoundMixin  {
    @Shadow
    @Final
    private Map<String, NbtElement> entries;

    @Inject(method = "put(Ljava/lang/String;Lnet/minecraft/nbt/NbtElement;)Lnet/minecraft/nbt/NbtElement;", at = @At("HEAD"), cancellable = true)
    public void injPut(String key, NbtElement element, CallbackInfoReturnable<NbtElement> cir) {
        if (element instanceof NbtString e) {
            Optional<String> n = e.asString();
            if(n.isEmpty()) return;
            switch (n.get()) {
                case "NaNd" -> cir.setReturnValue(this.entries.put(key, NbtDouble.of(Double.NaN)));
                case "NaNf" -> cir.setReturnValue(this.entries.put(key, NbtFloat.of(Float.NaN)));
                case "-NaNd" -> cir.setReturnValue(this.entries.put(key, NbtDouble.of(-1*Double.NaN)));
                case "-NaNf" -> cir.setReturnValue(this.entries.put(key, NbtFloat.of(-1*Float.NaN)));
                case "Infinityd" -> cir.setReturnValue(this.entries.put(key, NbtDouble.of(Double.POSITIVE_INFINITY)));
                case "Infinityf" -> cir.setReturnValue(this.entries.put(key, NbtFloat.of(Float.POSITIVE_INFINITY)));
                case "-Infinityd" -> cir.setReturnValue(this.entries.put(key, NbtDouble.of(Double.NEGATIVE_INFINITY)));
                case "-Infinityf" -> cir.setReturnValue(this.entries.put(key, NbtFloat.of(Float.NEGATIVE_INFINITY)));
            }
        }
    }
}
