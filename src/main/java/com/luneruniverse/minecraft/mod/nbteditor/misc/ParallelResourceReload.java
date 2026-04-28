package com.luneruniverse.minecraft.mod.nbteditor.misc;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import net.minecraft.server.packs.resources.ReloadInstance;

public class ParallelResourceReload implements ReloadInstance {
	
	private final ReloadInstance[] monitors;
	private final CompletableFuture<?> future;
	
	public ParallelResourceReload(ReloadInstance mainMonitor, ReloadInstance... additionalMonitors) {
		monitors = new ReloadInstance[1 + additionalMonitors.length];
		monitors[0] = mainMonitor;
		System.arraycopy(additionalMonitors, 0, monitors, 1, additionalMonitors.length);
		
		future = CompletableFuture.allOf(
				Arrays.stream(monitors).map(ReloadInstance::done).toArray(CompletableFuture<?>[]::new))
				.thenApply(voidResult -> mainMonitor.done().join());
	}
	
	@Override
	public CompletableFuture<?> done() {
		return future;
	}
	
	@Override
	public float getActualProgress() {
		return (float) Arrays.stream(monitors).mapToDouble(ReloadInstance::getActualProgress).average().getAsDouble();
	}
	
}
