package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.core.IdMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Optional;

@Mixin(IdMap.class)
public interface IdMapMixin<T> {

    @Shadow int getId(T var1);

    @SuppressWarnings("unchecked")
    @ModifyVariable(method = "getIdOrThrow", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private T injected(T value) {
        if(getId(value) != -1 || MainUtil.client.getConnection() == null) return value;
        if(value instanceof Holder.Reference<?> e) {
            Optional<? extends ResourceKey<?>> regKey = e.unwrapKey();
            if(regKey.isPresent()) {
                Identifier id = regKey.get().identifier();
                ResourceKey<? extends Registry<?>> registryRefRegKey = regKey.get().registryKey();
                Optional<Registry<Object>> reg = MainUtil.client.getConnection().registryAccess().lookup(registryRefRegKey);
                if(reg.isPresent()) {
                    Optional<Holder.Reference<Object>> newRef = reg.get().get(id);
                    if(newRef.isPresent()) {
                        return (T) newRef.get();
                    }
                }
            }
        }
        return value;
    }
}
