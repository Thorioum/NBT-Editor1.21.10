package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalBlock;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalEntity;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItem;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.ScreenTexts;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.MVNbtCompoundParent;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.ImageToLoreWidget;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.ImportPosWidget;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.NamedTextFieldWidget;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.ItemTagReferences;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.TextUtil;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3x2fStack;

public class ImportScreen extends OverlaySupportingScreen {
	
	public static void importFiles(List<Path> paths, Optional<Integer> defaultDataVersion) {
		List<Consumer<BlockPos>> posConsumers = new ArrayList<>();
		
		for (Path path : paths) {
			File file = path.toFile();
			if (!file.isFile())
				continue;
			
			if (file.getName().endsWith(".nbt")) {
				try (FileInputStream in = new FileInputStream(file)) {
					CompoundTag nbt = MainUtil.readNBT(in);
					if (defaultDataVersion.isEmpty() && !(nbt.get("DataVersion") instanceof NumericTag))
						MainUtil.client.player.sendSystemMessage(TextUtil.parseTranslatableFormatted("nbteditor.nbt.import.data_version.unknown", file.getName()));
					if (nbt.getIntOr("DataVersion",0) > Version.getDataVersion())
						MainUtil.client.player.sendSystemMessage(TextInst.translatable("nbteditor.nbt.import.data_version.new", file.getName()));
					LocalNBT.deserialize(nbt, defaultDataVersion.orElse(Version.getDataVersion())).ifPresent(localNBT -> {
						if (localNBT instanceof LocalItem item)
							item.receive();
						else if (localNBT instanceof LocalBlock block)
							posConsumers.add(pos -> block.place(pos));
						else if (localNBT instanceof LocalEntity entity)
							posConsumers.add(pos -> entity.summon(MainUtil.client.level.dimension(), Vec3.atCenterOf(pos)));
					});
				} catch (Exception e) {
					NBTEditor.LOGGER.error("Error while importing a .nbt file", e);
					MainUtil.client.player.sendSystemMessage(TextInst.literal(e.getClass().getName() + ": " + e.getMessage()).formatted(ChatFormatting.RED));
				}
				continue;
			}
		}
		
		if (!posConsumers.isEmpty()) {
			ImportPosWidget.openImportPos(MainUtil.client.player.blockPosition(),
					pos -> posConsumers.forEach(consumer -> consumer.accept(pos)));
			return;
		}
		
		ImageToLoreWidget.openImportFiles(paths, (file, imgLore) -> {
			String name = file.getName();
			int nameDot = name.lastIndexOf('.');
			if (nameDot != -1)
				name = name.substring(0, nameDot);
			
			ItemStack painting = new ItemStack(Items.PAINTING);
			painting.set(DataComponents.CUSTOM_NAME,TextInst.literal(name).styled(style -> style.withItalic(false).withColor(ChatFormatting.GOLD)));
			ItemTagReferences.LORE.set(painting, imgLore);
			MainUtil.getWithMessage(painting);
		}, () -> {});
	}
	
	private final List<Component> msg;
	private NamedTextFieldWidget dataVersion;
	
	public ImportScreen() {
		super(TextInst.of("Import"));
		msg = TextUtil.getLongTranslatableTextLines("nbteditor.nbt.import.desc");
	}
	
	@Override
	protected void init() {
		super.init();
		dataVersion = addRenderableWidget(
				new NamedTextFieldWidget(16, 64 + font.lineHeight * msg.size() + 16, 100, 16, dataVersion)
				.name(TextInst.translatable("nbteditor.nbt.import.data_version"))
				.tooltip(new MVTooltip("nbteditor.nbt.import.data_version.desc")));
		addRenderableWidget(MVMisc.newButton(this.width - 116, this.height - 36, 100, 20, ScreenTexts.DONE, btn -> onClose()));
	}
	
	@Override
	protected void renderMain(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		dataVersion.setValid(dataVersion.getValue().isEmpty() ||
				Version.getDataVersion(dataVersion.getValue()).filter(value -> value <= Version.getDataVersion()).isPresent());
		
		this.extractBackground(MVDrawableHelper.getDrawContext(matrices), mouseX, mouseY, delta);
		super.renderMain(matrices, mouseX, mouseY, delta);
		for (int i = 0; i < msg.size(); i++)
			MVDrawableHelper.drawText(matrices, font, msg.get(i), 16, 64 + font.lineHeight * i, -1, true);
		MainUtil.renderLogo(matrices);
	}
	
	@Override
	public void onFilesDrop(List<Path> paths) {
		importFiles(paths, Version.getDataVersion(dataVersion.getValue()).filter(value -> value <= Version.getDataVersion()));
	}
	
}
