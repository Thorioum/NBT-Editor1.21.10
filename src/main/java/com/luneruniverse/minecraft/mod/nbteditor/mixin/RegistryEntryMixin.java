package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(Holder.Reference.class)
public abstract class RegistryEntryMixin<T> {

    @Shadow private @Nullable Set<TagKey<T>> tags;

    @Shadow public abstract Optional<ResourceKey<T>> unwrapKey();

    @Inject(method = "canSerializeIn(Lnet/minecraft/core/HolderOwner;)Z", at = @At("RETURN"), cancellable = true)
    private void onOwnerEquals(HolderOwner<T> owner, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Inject(method = "boundTags", at = @At("HEAD"), cancellable = true)
    private void onGetTags(CallbackInfoReturnable<Set<TagKey<Object>>> cir) {
        if(tags == null && MainUtil.client.getConnection() != null) {

            Optional<? extends ResourceKey<?>> regKey = this.unwrapKey();
            if(regKey.isPresent()) {
                Identifier id = regKey.get().identifier();
                ResourceKey<? extends Registry<?>> registryRefRegKey = regKey.get().registryKey();
                Optional<Registry<Object>> reg = MainUtil.client.getConnection().registryAccess().lookup(registryRefRegKey);
                if(reg.isPresent()) {
                    Optional<Holder.Reference<Object>> newRef = reg.get().get(id);
                    newRef.ifPresent(ref -> {
                        Set<TagKey<Object>> tags = ref.boundTags();
                        cir.setReturnValue(tags);
                    });
                }
            }


        }
    }
}
