package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.util.Optional;
import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItem;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;
import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.MVNbtCompoundParent;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.NBTManagers;
import com.luneruniverse.minecraft.mod.nbteditor.screens.nbtfolder.NBTFolder;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.List2D;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.StringJsonWriterQuoted;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.client.input.MouseButtonEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

public class NBTValue extends List2D.List2DValue {
	
	private static final Identifier BACK = IdentifierInst.of("nbteditor", "textures/nbt/back.png");
	private static final Identifier BYTE = IdentifierInst.of("nbteditor", "textures/nbt/byte.png");
	private static final Identifier SHORT = IdentifierInst.of("nbteditor", "textures/nbt/short.png");
	private static final Identifier INT = IdentifierInst.of("nbteditor", "textures/nbt/int.png");
	private static final Identifier LONG = IdentifierInst.of("nbteditor", "textures/nbt/long.png");
	private static final Identifier FLOAT = IdentifierInst.of("nbteditor", "textures/nbt/float.png");
	private static final Identifier DOUBLE = IdentifierInst.of("nbteditor", "textures/nbt/double.png");
	private static final Identifier NUMBER = IdentifierInst.of("nbteditor", "textures/nbt/number.png");
	private static final Identifier STRING = IdentifierInst.of("nbteditor", "textures/nbt/string.png");
	private static final Identifier LIST = IdentifierInst.of("nbteditor", "textures/nbt/list.png");
	private static final Identifier BYTE_ARRAY = IdentifierInst.of("nbteditor", "textures/nbt/byte_array.png");
	private static final Identifier INT_ARRAY = IdentifierInst.of("nbteditor", "textures/nbt/int_array.png");
	private static final Identifier LONG_ARRAY = IdentifierInst.of("nbteditor", "textures/nbt/long_array.png");
	private static final Identifier COMPOUND = IdentifierInst.of("nbteditor", "textures/nbt/compound.png");
	
	private final NBTEditorScreen<?> screen;
	private final String key;
	private Tag value;
	private CollectionTag parentList;
	
	private boolean selected;
	private boolean unsafe;
	private boolean invalidComponent;
	
	public NBTValue(NBTEditorScreen<?> screen, String key, Tag value, CollectionTag parentList) {
		this.screen = screen;
		this.key = key;
		this.value = value;
		this.parentList = parentList;
	}
	public NBTValue(NBTEditorScreen<?> screen, String key, Tag value) {
		this(screen, key, value, null);
	}
	
	@Override
	public void extractRenderState(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		Identifier icon;
		if (key == null) {
			icon = BACK;
		} else {
			icon = switch (value.getId()) {
				case Tag.TAG_BYTE -> BYTE;
				case Tag.TAG_SHORT -> SHORT;
				case Tag.TAG_INT -> INT;
				case Tag.TAG_LONG -> LONG;
				case Tag.TAG_FLOAT -> FLOAT;
				case Tag.TAG_DOUBLE -> DOUBLE;
				case MVNbtCompoundParent.NUMBER_TYPE -> NUMBER;
				case Tag.TAG_STRING -> STRING;
				case Tag.TAG_LIST -> LIST;
				case Tag.TAG_BYTE_ARRAY -> BYTE_ARRAY;
				case Tag.TAG_INT_ARRAY -> INT_ARRAY;
				case Tag.TAG_LONG_ARRAY -> LONG_ARRAY;
				case Tag.TAG_COMPOUND -> COMPOUND;
				default -> null;
			};
		}
		if (icon != null)
			MVDrawableHelper.drawTexture(matrices, icon, 0, 0, 0, 0, 32, 32, 32, 32);
		
		int color = -1;
		String tooltip = null;

		byte heldType = (byte) 0;
		for (Tag element : parentList) {
			if (heldType == 0)
				heldType = element.getId();
			else if (heldType != element.getId()){}
		}

		byte ht = (byte) 0;
		for (Tag element : parentList) {
			if (ht == 0)
				ht = element.getId();
			else if (ht != element.getId()){}
		}

		if (unsafe && selected || parentList != null &&
				!MVNbtCompoundParent.NBT_CODE_REFACTORED && ht != value.getId()) {
			color = 0xFFFFAA33;
			tooltip = "nbteditor.nbt.marker.unsafe";
		} else if (invalidComponent) {
			color = 0xFF550000;
			tooltip = "nbteditor.nbt.marker.invalid_component";
		} else if (selected)
			color = 0xFFDF4949;
		else if (isHovering(mouseX, mouseY))
			color = 0xFF257789;
		if (color != -1) {
			MVDrawableHelper.fill(matrices, -4, -4, 36, 0, color);
			MVDrawableHelper.fill(matrices, -4, -4, 0, 36, color);
			MVDrawableHelper.fill(matrices, -4, 32, 36, 36, color);
			MVDrawableHelper.fill(matrices, 32, -4, 36, 36, color);
		}
		if (tooltip != null && isHovering(mouseX, mouseY))
			new MVTooltip(tooltip).render(matrices, mouseX, mouseY);
		
		if (key == null)
			return;
		
		matrices.pushMatrix();
		matrices.scale((float) ConfigScreen.getKeyTextSize(), (float) ConfigScreen.getKeyTextSize());
		double scale = 1 / ConfigScreen.getKeyTextSize();
		MainUtil.drawWrappingString(matrices, textRenderer, key, (int) (16 * scale), (int) (24 * scale), (int) (32 * scale), -1, true, true);
		matrices.popMatrix();
	}
	
	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean d) {
		if (isHovering((int) click.x(), (int) click.y())) {
			if (key == null) {
				screen.selectNbt(null, true);
				return true;
			}
			
			NBTFolder<?> folder = NBTFolder.get(value);
			screen.selectNbt(this, selected && folder != null && !folder.hasEmptyKey());
			selected = !selected;
			return selected;
		}
		
		selected = false;
		return false;
	}
	
	private boolean isHovering(int mouseX, int mouseY) {
		return isInsideList() && mouseX >= 0 && mouseY >= 0 && mouseX <= 32 && mouseY <= 32;
	}
	
	public void valueChanged(String str, Consumer<Tag> onChange) {
		try {
			value = MixinLink.parseSpecialElement(new StringReader(str));
			onChange.accept(value);
		} catch (CommandSyntaxException e) {}
	}
	
	public String getKey() {
		return key;
	}
	public String getValueText(boolean json) {
		return json ? new StringJsonWriterQuoted().apply(value) : value.toString();
	}
	
	public void setUnsafe(boolean unsafe) {
		this.unsafe = unsafe;
	}
	/**
	 * @return Returns if this value has been manually set as unsafe; doesn't take into account list types
	 */
	public boolean isUnsafe() {
		return unsafe;
	}
	
	public void setInvalidComponent(boolean invalidComponent) {
		this.invalidComponent = invalidComponent;
	}
	public void updateInvalidComponent(LocalNBT localNBT, String component) {
		if (!NBTManagers.COMPONENTS_EXIST)
			return;
		if (localNBT instanceof LocalItem localItem) {
			CompoundTag nbtOutput = NBTManagers.ITEM.getNbt(localItem.getReadableItem());
			if (component == null)
				component = this.key;
			this.invalidComponent = (nbtOutput == null || !nbtOutput.contains(MainUtil.addNamespace(component)));
		}
	}
	public boolean isInvalidComponent() {
		return invalidComponent;
	}
	
}
