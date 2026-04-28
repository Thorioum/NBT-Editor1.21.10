package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.screens.Tickable;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.NamedTextFieldWidget;

import net.minecraft.client.gui.components.EditBox;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.resources.Identifier;

@Mixin(EditBox.class)
public abstract class EditBoxMixin implements Tickable {
	private static final Identifier TEXT_FIELD_INVALID = IdentifierInst.of("nbteditor", "widget/text_field_invalid");

    @ModifyArg(method = "extractWidgetRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 0))
	@Group(name = "renderButton", min = 1)
	private Identifier drawGuiTexture2(Identifier texture) {
		EditBox source = (EditBox) (Object) this;
		if (source instanceof NamedTextFieldWidget named && !named.isValid())
			return TEXT_FIELD_INVALID;
		return texture;
	}

    @ModifyArg(method = "extractWidgetRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/TextCursorUtils;extractAppendCursor(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIIZ)V", ordinal = 0), index = 4, remap = false)
	@Group(name = "renderButton", min = 1)
	private int fillDrawContext(int color) {
		EditBox source = (EditBox) (Object) this;
		if (source instanceof NamedTextFieldWidget named && !named.isValid())
			return 0xFFDF4949;
		return color;
	}
	
	private static final Supplier<Reflection.FieldReference> TextFieldWidget_focusedTicks =
			Reflection.getOptionalField(EditBox.class, "field_2107", "I");
	@Override
	public void tick() {
		EditBox source = (EditBox) (Object) this;
		Version.newSwitch()
				.range("1.20.2", null, () -> {})
				.range(null, "1.20.1", () -> TextFieldWidget_focusedTicks.get().set(source, (int) TextFieldWidget_focusedTicks.get().get(source) + 1))
				.run();
	}
}
