package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import net.minecraft.client.input.KeyEvent;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.clientchest.ClientChest;
import com.luneruniverse.minecraft.mod.nbteditor.clientchest.ClientChestHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.DataVersionStatus;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.EditableText;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientChestScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.util.FancyConfirmScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.NamedTextFieldWidget;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.components.Button;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class ClientChestDataVersionScreen extends TickableSupportingScreen {
	
	private final DataVersionStatus dataVersionStatus;
	private final Component msg;
	private NamedTextFieldWidget dataVersion;
	private Button updatePageBtn;
	
	public ClientChestDataVersionScreen(Optional<Integer> dataVersion) {
		super(TextInst.of("Client Chest DataVersion"));
		DataVersionStatus dataVersionStatus = DataVersionStatus.of(dataVersion);
		
		this.dataVersionStatus = dataVersionStatus;
		this.msg = TextInst.translatable(
				"nbteditor.client_chest.data_version." + switch (dataVersionStatus) {
					case UNKNOWN -> "unknown";
					case OUTDATED -> "old";
					case TOO_UPDATED -> "new";
					default -> throw new IllegalArgumentException("Unexpected DataVersionStatus: " + dataVersionStatus);
				},
				ClientChestScreen.PAGE + 1,
				dataVersion.flatMap(Version::getMCVersion).or(() -> dataVersion.map(value -> value.toString())).orElse(""));
	}
	
	@Override
	protected void init() {
		EditableText prevKeybind = TextInst.translatable("nbteditor.keybind.page.down");
		EditableText nextKeybind = TextInst.translatable("nbteditor.keybind.page.up");
		if (ConfigScreen.isInvertedPageKeybinds()) {
			EditableText temp = prevKeybind;
			prevKeybind = nextKeybind;
			nextKeybind = temp;
		}
		
		int dontUpdatePageX = width / 2 + (dataVersionStatus == DataVersionStatus.TOO_UPDATED ? -50 : 58);
		
		addRenderableWidget(MVMisc.newButton(dontUpdatePageX, height / 2 - 34, 52, 20,
				TextInst.translatable("nbteditor.client_chest.data_version.dont_update_page"), btn -> onClose()));
		addRenderableWidget(MVMisc.newButton(dontUpdatePageX + 56, height / 2 - 34, 20, 20, TextInst.of("<"), btn -> prevPage(),
				ConfigScreen.isKeybindsHidden() ? null : new MVTooltip(TextInst.literal("")
						.append(prevKeybind).append(TextInst.translatable("nbteditor.keybind.page.prev")))))
				.active = ClientChestScreen.PAGE > 0;
		addRenderableWidget(MVMisc.newButton(dontUpdatePageX + 80, height / 2 - 34, 20, 20, TextInst.of(">"), btn -> nextPage(),
				ConfigScreen.isKeybindsHidden() ? null : new MVTooltip(TextInst.literal("")
						.append(nextKeybind).append(TextInst.translatable("nbteditor.keybind.page.next")))))
				.active = ClientChestScreen.PAGE < NBTEditorClient.CLIENT_CHEST.getPageCount() - 1;
		
		addRenderableWidget(MVMisc.newButton(dontUpdatePageX, height / 2 - 10, 100, 20,
				TextInst.translatable("nbteditor.client_chest.reload_page"), btn -> {
			LoadingScreen.show(ClientChestHelper.reloadPage(ClientChestScreen.PAGE), pageData -> ClientChestScreen.show());
		}));
		addRenderableWidget(MVMisc.newButton(dontUpdatePageX, height / 2 + 14, 100, 20,
				TextInst.translatable("nbteditor.client_chest.clear_page"), btn -> {
			minecraft.setScreen(new FancyConfirmScreen(value -> {
				if (value) {
					LoadingScreen.show(ClientChestHelper.discardPage(ClientChestScreen.PAGE), success -> ClientChestScreen.show());
					return;
				}
				
				minecraft.setScreen(this);
			}, TextInst.translatable("nbteditor.client_chest.clear_page.title"), TextInst.translatable("nbteditor.client_chest.clear_page.desc"),
					TextInst.translatable("nbteditor.client_chest.clear_page.yes"), TextInst.translatable("nbteditor.client_chest.clear_page.no")));
		}));
		
		if (dataVersionStatus == DataVersionStatus.TOO_UPDATED)
			return;
		
		addRenderableWidget(MVMisc.newButton(width / 2 - 158, height / 2 - 10, 100, 20,
				TextInst.translatable("nbteditor.client_chest.data_version.import_page"), btn -> {
					LoadingScreen.show(
							addSuccessMessage(ClientChestHelper.importPage(ClientChestScreen.PAGE), false),
							success -> ClientChestScreen.show());
				}, new MVTooltip("nbteditor.client_chest.data_version.import_page.desc")))
				.active = (dataVersionStatus == DataVersionStatus.UNKNOWN);
		addRenderableWidget(MVMisc.newButton(width / 2 - 158, height / 2 + 14, 100, 20,
				TextInst.translatable("nbteditor.client_chest.data_version.import_all_pages"), btn -> {
					LoadingScreen.show(
							addSuccessMessage(ClientChestHelper.importAllPages(), true),
							success -> ClientChestScreen.show());
				}, new MVTooltip("nbteditor.client_chest.data_version.import_all_pages.desc")));
		
		dataVersion = addRenderableWidget(
				new NamedTextFieldWidget(width / 2 - 50, height / 2 - 32, 100, 16, dataVersion)
				.name(TextInst.translatable("nbteditor.nbt.import.data_version"))
				.tooltip(new MVTooltip("nbteditor.nbt.import.data_version.desc")));
		updatePageBtn = addRenderableWidget(MVMisc.newButton(width / 2 - 50, height / 2 - 10, 100, 20,
				TextInst.translatable("nbteditor.client_chest.data_version.update_page"), btn -> {
					Optional<Integer> dataVersionValue = Version.getDataVersion(dataVersion.getValue())
							.filter(value -> value < Version.getDataVersion());
					if (dataVersionStatus == DataVersionStatus.UNKNOWN && dataVersionValue.isEmpty())
						return;
					
					updateWithWarning(() -> {
						LoadingScreen.show(
								addSuccessMessage(ClientChestHelper.updatePage(ClientChestScreen.PAGE, dataVersionValue), false),
								success -> ClientChestScreen.show());
					});
				}, new MVTooltip("nbteditor.client_chest.data_version.update_page.desc." + (dataVersionStatus == DataVersionStatus.UNKNOWN ? "unknown" : "old"))));
		addRenderableWidget(MVMisc.newButton(width / 2 - 50, height / 2 + 14, 100, 20,
				TextInst.translatable("nbteditor.client_chest.data_version.update_all_pages"), btn -> {
					Optional<Integer> dataVersionValue = Version.getDataVersion(dataVersion.getValue())
							.filter(value -> value < Version.getDataVersion());
					
					updateWithWarning(() -> {
						LoadingScreen.show(
								addSuccessMessage(ClientChestHelper.updateAllPages(dataVersionValue), true),
								success -> ClientChestScreen.show());
					});
				}, new MVTooltip("nbteditor.client_chest.data_version.update_all_pages.desc")));
	}
	
	@Override
	public void render(Matrix3x2fStack matrices, int mouseX, int mouseY, float delta) {
		boolean fullButtons = (dataVersionStatus != DataVersionStatus.TOO_UPDATED);
		
		if (fullButtons) {
			dataVersion.setValid((dataVersionStatus == DataVersionStatus.OUTDATED && dataVersion.getValue().isEmpty()) ||
					Version.getDataVersion(dataVersion.getValue()).filter(value -> value < Version.getDataVersion()).isPresent());
			updatePageBtn.active = (dataVersionStatus == DataVersionStatus.UNKNOWN ? dataVersion.isValid() : dataVersion.getValue().isEmpty());
		}
		
		MVTooltip.setOneTooltip(true, false);
		
		this.extractBackground(MVDrawableHelper.getDrawContext(matrices), mouseX, mouseY, delta);
		super.render(matrices, mouseX, mouseY, delta);
		MVDrawableHelper.drawCenteredTextWithShadow(matrices, font,
				msg, width / 2, height / 2 - 44 - font.lineHeight / 2, -1);
		if (fullButtons) {
			MVDrawableHelper.fill(matrices, width / 2 - 55, height / 2 - 34, width / 2 - 53, height / 2 + 34, 0xFFAAAAAA);
			MVDrawableHelper.fill(matrices, width / 2 + 53, height / 2 - 34, width / 2 + 55, height / 2 + 34, 0xFFAAAAAA);
			MVDrawableHelper.drawCenteredTextWithShadow(matrices, font,
					TextInst.translatable("nbteditor.client_chest.data_version.import", Version.getReleaseTarget()),
					width / 2 - 108, height / 2 - 24 - font.lineHeight / 2, -1);
		}
		MainUtil.renderLogo(matrices);
		
		MVTooltip.renderOneTooltip(matrices, mouseX, mouseY);
	}
	
	@Override
	public boolean keyPressed(KeyEvent keyInput) {
		int keyCode = keyInput.key();
		if (keyCode == GLFW.GLFW_KEY_PAGE_UP || keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
			boolean prev = (keyCode == GLFW.GLFW_KEY_PAGE_DOWN);
			if (ConfigScreen.isInvertedPageKeybinds())
				prev = !prev;
			if (prev)
				prevPage();
			else
				nextPage();
		}
		
		return super.keyPressed(keyInput);
	}
	
	private void prevPage() {
		if (ClientChestScreen.PAGE > 0) {
			ClientChestScreen.PAGE--;
			ClientChestScreen.show();
		}
	}
	private void nextPage() {
		if (ClientChestScreen.PAGE < NBTEditorClient.CLIENT_CHEST.getPageCount() - 1) {
			ClientChestScreen.PAGE++;
			ClientChestScreen.show();
		}
	}
	
	private CompletableFuture<Boolean> addSuccessMessage(CompletableFuture<Boolean> future, boolean all) {
		future.thenAccept(success -> {
			if (success) {
				EditableText msg;
				if (all) {
					msg = TextInst.translatable("nbteditor.client_chest.data_version.update_all_pages_success");
				} else {
					msg = TextInst.translatable("nbteditor.client_chest.data_version.update_page_success",
							TextInst.literal(ClientChestScreen.PAGE + 1 + "").formatted(ChatFormatting.GREEN));
				}
				MainUtil.client.player.sendSystemMessage(ClientChest.attachShowFolder(msg));
			}
		});
		return future;
	}
	
	private void updateWithWarning(Runnable callback) {
		if (Version.<Boolean>newSwitch()
				.range("1.21", null, true)
				.range("1.20.5", "1.20.6", false)
				.range(null, "1.20.4", true)
				.get()) {
			callback.run();
			return;
		}
		
		minecraft.setScreen(new FancyConfirmScreen(value -> {
			if (value)
				callback.run();
			else
				minecraft.setScreen(this);
		}, TextInst.translatable("nbteditor.client_chest.data_version.update_page_confirm_1.20.5_1.20.6.title"),
				TextInst.translatable("nbteditor.client_chest.data_version.update_page_confirm_1.20.5_1.20.6.desc")));
	}
	
}
