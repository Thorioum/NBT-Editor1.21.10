package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.IndexedIterable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Optional;

@Mixin(IndexedIterable.class)
public interface IndexedIterableMixin<T> {

    @Shadow
    int getRawId(T var1);

    //me and mega had a discussion about this kind of implementation a while ago
    //he didnt like it, i found no problem with it, bite me
    @SuppressWarnings("unchecked")
    @ModifyVariable(method = "getRawIdOrThrow", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private T injected(T value) {
        if(getRawId(value) != -1 || MainUtil.client.getNetworkHandler() == null) return value;
        if(value instanceof RegistryEntry.Reference<?> e) {
            Optional<? extends RegistryKey<?>> regKey = e.getKey();
            if(regKey.isPresent()) {
                Identifier id = regKey.get().getValue();
                RegistryKey<? extends Registry<?>> registryRefRegKey = regKey.get().getRegistryRef();
                Optional<Registry<Object>> reg = MainUtil.client.getNetworkHandler().getRegistryManager().getOptional(registryRefRegKey);
                if(reg.isPresent()) {
                    Optional<RegistryEntry.Reference<Object>> newRef = reg.get().getEntry(id);
                    if(newRef.isPresent()) {
                        return (T) newRef.get();
                    }
                }
            }
        }
        return value;
    }
}
