package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.luneruniverse.minecraft.mod.nbteditor.misc.ResetableDataTracker;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.syncher.SynchedEntityData;

@Mixin(SynchedEntityData.class)
public class SynchedEntityDataMixin implements ResetableDataTracker {
	@Shadow
	private boolean isDirty;
	private static final Supplier<Reflection.FieldReference> DataTracker_entries_array =
			Reflection.getOptionalField(SynchedEntityData.class, "field_13331", "[Lnet/minecraft/class_2945$class_2946;");
	private static final Supplier<Reflection.FieldReference> DataTracker_entries_Int2ObjectMap =
			Reflection.getOptionalField(SynchedEntityData.class, "field_13331", "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;");
	private static final Supplier<Reflection.FieldReference> DataTracker_lock =
			Reflection.getOptionalField(SynchedEntityData.class, "field_13335", "Ljava/util/concurrent/locks/ReadWriteLock;");
	@Override
	public void reset() {
		if (Version.<Boolean>newSwitch()
				.range("1.19.3", null, false)
				.range(null, "1.19.2", true)
				.get())
			return; // DataTracker$Entry#initialValue doesn't exist
		ReadWriteLock lock = Version.<ReadWriteLock>newSwitch()
				.range("1.20.5", null, () -> null)
				.range(null, "1.20.4", () -> DataTracker_lock.get().get(this))
				.get();
		if (lock != null)
			lock.writeLock().lock();
		try {
			@SuppressWarnings("unchecked")
			SynchedEntityData.DataItem<?>[] entries = Version.<SynchedEntityData.DataItem<?>[]>newSwitch()
					.range("1.20.5", null, () -> DataTracker_entries_array.get().get(this))
					.range(null, "1.20.4", () -> ((Int2ObjectMap<SynchedEntityData.DataItem<?>>) DataTracker_entries_Int2ObjectMap.get().get(this)).values().toArray(SynchedEntityData.DataItem[]::new))
					.get();
			for (SynchedEntityData.DataItem<?> entry : entries) {
				resetEntry(entry);
				entry.setDirty(true);
			}
			isDirty = true;
		} finally {
			if (lock != null)
				lock.writeLock().unlock();
		}
	}
	private <T> void resetEntry(SynchedEntityData.DataItem<T> entry) {
		entry.setValue(entry.initialValue);
	}
}
