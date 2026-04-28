package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.server.NBTEditorServer;
import com.luneruniverse.minecraft.mod.nbteditor.util.CompletableFutureCache;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.PacketListener;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.RegistryLayer;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagLoader;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;

public class DynamicRegistryManagerHolder {
	
	private static final CompletableFutureCache<RegistryAccess> defaultManagerCache =
			new CompletableFutureCache<>(DynamicRegistryManagerHolder::loadDefaultManagerImpl);
	private static final Set<Thread> defaultManagerForced = ConcurrentHashMap.newKeySet();
	private static volatile RegistryCache defaultManagerRegistryCache;
	
	private static volatile RegistryAccess clientManager;
	private static volatile RegistryAccess serverManager;
	
	private static final Supplier<Reflection.MethodInvoker> RegistryLoader_loadFromResource =
			Reflection.getOptionalMethod(RegistryDataLoader.class, "method_56515", MethodType.methodType(RegistryAccess.Frozen.class, ResourceManager.class, RegistryAccess.class, List.class));
	private static CompletableFuture<RegistryAccess> loadDefaultManagerImpl() {
		CompletableFuture<RegistryAccess> future = new CompletableFuture<>();
		MixinLink.executeCrashableTask(() -> {
			if (MainUtil.client.getResourcePackRepository().getSelectedPacks().isEmpty())
				MainUtil.client.getResourcePackRepository().reload();
			
			// Based on https://github.com/MineLittlePony/HDSkins/blob/f9c6b8e570cae03908598eb629bf92e2f4faf5b3/src/main/java/com/minelittlepony/hdskins/client/gui/player/DummyNetworkHandler.java#L49
			// and https://github.com/MineLittlePony/HDSkins/blob/a19fe3b0d7d98019bafc814a8782b7a263d090b9/src/main/java/com/minelittlepony/hdskins/client/gui/player/DummyNetworkHandler.java#L41
			
			LayeredRegistryAccess<RegistryLayer> combinedRegistries =
					RegistryLayer.createRegistryAccess();
			ResourceManager resourceManager = new MultiPackResourceManager(
					PackType.SERVER_DATA, MainUtil.client.getResourcePackRepository().openAllSelected());
			
			List<RegistryDataLoader.RegistryData<?>> entries = new ArrayList<>();
			entries.addAll(RegistryDataLoader.WORLDGEN_REGISTRIES);
			entries.addAll(RegistryDataLoader.DIMENSION_REGISTRIES);


			RegistryAccess.Frozen dynamicRegistries = Version.<RegistryAccess.Frozen>newSwitch()
					.range("1.21.2", null, () -> {
						List<Registry.PendingTags<?>> tags = TagLoader.loadTagsForExistingRegistries(resourceManager, combinedRegistries.getLayer(RegistryLayer.STATIC));
						RegistryAccess.Frozen preceding = combinedRegistries.getAccessForLoading(RegistryLayer.RELOADABLE);
						List<HolderLookup.RegistryLookup<?>> loadedRegistries = TagLoader.buildUpdatedLookups(preceding, tags);

                        try {
                            return RegistryDataLoader.load(resourceManager, loadedRegistries, entries, Util.backgroundExecutor()).get();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    })
					.range("1.20.5", "1.21.1", () -> RegistryLoader_loadFromResource.get().invoke(null, resourceManager, combinedRegistries.compositeAccess(), entries))
					.get();
			
			future.complete(combinedRegistries.replaceFrom(RegistryLayer.RELOADABLE, dynamicRegistries).compositeAccess());
		});
		return future;
	}
	public static ReloadInstance loadDefaultManager() {
		CompletableFuture<RegistryAccess> future = defaultManagerCache.get();
		
		return new ReloadInstance() {
			@Override
			public CompletableFuture<?> done() {
				return future;
			}
			@Override
			public float getActualProgress() {
				return future.isDone() ? 1 : 0;
			}
		};
	}
	public static void onDefaultManagerLoad(Runnable callback) {
			defaultManagerCache.get().whenComplete((manager, e) -> MixinLink.executeCrashableTask(callback));
	}
	
	public static RegistryAccess getManager() {
		if (NBTEditorServer.isOnServerThread()) {
			if (serverManager == null)
				throw new IllegalStateException("The server manager hasn't been set yet!");
			return serverManager;
		}
		
		if (hasClientManager())
			return clientManager;
		
		if (MixinLink.isOnMainThread() && defaultManagerCache.getStatus() != CompletableFutureCache.Status.LOADED)
			throw new RuntimeException("Cannot synchronously load the default manager on the main thread");
		return defaultManagerCache.get().join();
	}
	public static HolderLookup.Provider get() {
		return getManager();
	}
	
	public static void setClientManager(PacketListener listener) {
		clientManager = (listener == null ? null : ((ClientPacketListener) listener).registryAccess());
	}
	public static void setServerManager(MinecraftServer server) {
		serverManager = server.registryAccess();
	}
	
	public static boolean hasClientManager() {
		return !defaultManagerForced.contains(Thread.currentThread()) && clientManager != null;
	}
	
	public static <T> T withDefaultManager(Supplier<T> callback) {
		if (NBTEditorServer.isOnServerThread())
			throw new IllegalStateException("Cannot use withDefaultManager on the server!");
		
		defaultManagerForced.add(Thread.currentThread());
		try {
			return callback.get();
		} finally {
			defaultManagerForced.remove(Thread.currentThread());
		}
	}
	public static void withDefaultManager(Runnable callback) {
		withDefaultManager(() -> {
			callback.run();
			return null;
		});
	}
	
	private static final boolean getReadOnlyWrapperExists = Version.<Boolean>newSwitch()
			.range("1.21.2", null, false)
			.range(null, "1.21.1", true)
			.get();
	private static final Supplier<Reflection.MethodInvoker> Registry_getReadOnlyWrapper =
			Reflection.getOptionalMethod(Registry.class, "method_46771", MethodType.methodType(HolderLookup.RegistryLookup.class));
	public static <T> boolean isOwnedByDefaultManager(Holder.Reference<T> entry) {
		if (NBTEditorServer.isOnServerThread() || defaultManagerCache.getStatus() != CompletableFutureCache.Status.LOADED)
			return false;
		
		if (defaultManagerRegistryCache == null)
			defaultManagerRegistryCache = new RegistryCache(defaultManagerCache.get().join());
		
		@SuppressWarnings("unchecked")
		Registry<T> registry = (Registry<T>) defaultManagerRegistryCache.getRegistry(entry.key().registry()).orElse(null);
		if (registry == null)
			return false;
		
		// Attempting to convert references in static registries to the current registry manager
		// causes a stack overflow as the reference isn't changed
		if (RegistryCache.isRegistryStatic(registry))
			return false;
		
		return entry.owner.canSerializeIn(getReadOnlyWrapperExists ? Registry_getReadOnlyWrapper.get().invoke(registry) : registry);
	}
	
}
