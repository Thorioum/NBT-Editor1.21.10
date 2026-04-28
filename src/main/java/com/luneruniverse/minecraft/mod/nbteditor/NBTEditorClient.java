package com.luneruniverse.minecraft.mod.nbteditor;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.luneruniverse.minecraft.mod.nbteditor.addons.NBTEditorAPI;
import com.luneruniverse.minecraft.mod.nbteditor.addons.NBTEditorAddon;
import com.luneruniverse.minecraft.mod.nbteditor.async.HeadRefreshThread;
import com.luneruniverse.minecraft.mod.nbteditor.clientchest.*;
import com.luneruniverse.minecraft.mod.nbteditor.commands.CommandHandler;
import com.luneruniverse.minecraft.mod.nbteditor.containers.ContainerIOs;
import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVEnchantments;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVClientNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.packets.OpenEnderChestC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientChestScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.CursorManager;
import com.luneruniverse.minecraft.mod.nbteditor.server.NBTEditorServer;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import tsp.headdb.ported.HeadAPI;

public class NBTEditorClient implements ClientModInitializer {
	
	public static final File SETTINGS_FOLDER = new File("nbteditor");
	public static CursorManager CURSOR_MANAGER;
	public static ClientChest CLIENT_CHEST;
	public static NBTEditorServerConn SERVER_CONN;
	
	private static final Map<String, NBTEditorAddon> addons = new HashMap<>();
	public static NBTEditorAddon getAddon(String modId) {
		return addons.get(modId);
	}
	public static Map<String, NBTEditorAddon> getAddons() {
		return Collections.unmodifiableMap(addons);
	}
	
	@Override
	public void onInitializeClient() {
		NBTEditorServer.IS_DEDICATED = false;
		
		if (!SETTINGS_FOLDER.exists())
			SETTINGS_FOLDER.mkdir();

		MVMisc.onRegistriesLoad(this::onRegistriesLoad);
		ExtraDataFixes.init();
	}
	
	private void onRegistriesLoad() {
		new Thread(() -> {


			while (MainUtil.client.level == null) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
			}

			ItemStack clientChestIcon = new ItemStack(Items.ENDER_CHEST);
			clientChestIcon.set(DataComponents.CUSTOM_NAME, TextInst.translatable("itemGroup.nbteditor.client_chest"));

			ItemStack inventoryIcon = new ItemStack(Items.CHEST);
			inventoryIcon.set(DataComponents.CUSTOM_NAME, TextInst.translatable("itemGroup.nbteditor.inventory"));

			MVEnchantments.addEnchantment(clientChestIcon, MVEnchantments.LOYALTY, 1);
			MixinLink.ENCHANT_GLINT_FIX.add(clientChestIcon);
			NBTEditorAPI.registerInventoryTab(clientChestIcon,
					ClientChestScreen::show,
					screen -> screen instanceof CreativeModeInventoryScreen || (screen instanceof InventoryScreen && SERVER_CONN.isEditingExpanded()));
			NBTEditorAPI.registerInventoryTab(inventoryIcon,
					CURSOR_MANAGER::showRoot,
					screen -> screen instanceof ClientChestScreen);
			NBTEditorAPI.registerInventoryTab(new ItemStack(Items.ENDER_CHEST),
					() -> {
						CURSOR_MANAGER.closeRoot();
						MVClientNetworking.send(new OpenEnderChestC2SPacket());
					},
					screen -> (screen instanceof CreativeModeInventoryScreen || screen instanceof InventoryScreen || screen instanceof ClientChestScreen)
							&& SERVER_CONN.isEditingExpanded());

		});

		CommandHandler.registerCommands();
		try {
			HeadAPI.loadFavorites();
		} catch (IOException e) {
			NBTEditor.LOGGER.error("Error while loading HeadDB favorites", e);
		}
		ContainerIOs.loadClass();
		new HeadRefreshThread().start();
		ConfigScreen.loadSettings();
		CURSOR_MANAGER = new CursorManager();

		CLIENT_CHEST = new ClientChest(ConfigScreen.isLargeClientChest() ? new LargeClientChestPageCache(5) : new SmallClientChestPageCache(100));
		MVClientNetworking.PlayNetworkStateEvents.Start.EVENT.register(networkHandler -> {
			ClientChestHelper.loadDefaultPages(PageLoadLevel.DYNAMIC_ITEMS);
			ClientChestHelper.loadDefaultPages(PageLoadLevel.NORMAL_ITEMS);
		});
		//MVClientNetworking.PlayNetworkStateEvents.Stop.EVENT.register(() -> ClientChestHelper.unloadAllPages(PageLoadLevel.NORMAL_ITEMS));


		SERVER_CONN = new NBTEditorServerConn();

		for (EntrypointContainer<NBTEditorAddon> container : FabricLoader.getInstance()
				.getEntrypointContainers("nbteditor", NBTEditorAddon.class)) {
			addons.put(container.getProvider().getMetadata().getId(), container.getEntrypoint());
		}
		addons.forEach((id, addon) -> addon.onInit());

	}
	
}
