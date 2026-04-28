package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiRenderState.class)
public class GuiRenderStateMixin {

    @Redirect(method = "blurBeforeThisStratum", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/gui/GuiRenderState;firstStratumAfterBlur:I", opcode = Opcodes.GETFIELD))
    private int injected(GuiRenderState instance) {
        return Integer.MAX_VALUE;
    }
}
