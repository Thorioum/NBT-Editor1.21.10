package tsp.headdb.ported.inventory;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerInput;

public class InventoryClickEvent {
	private final Slot slot;
	private final int slotId;
	private final int button;
	private final ContainerInput actionType;
	private final ClickTypeMod ContainerInput;
	
	public InventoryClickEvent(Slot slot, int slotId, int button, ContainerInput actionType, ClickTypeMod ContainerInput) {
		this.slot = slot;
		this.slotId = slotId;
		this.button = button;
		this.actionType = actionType;
		this.ContainerInput = ContainerInput;
	}
	
	public Slot getSlot() {
		return slot;
	}
	public int getSlotId() {
		return slotId;
	}
	public int getButton() {
		return button;
	}
	public ContainerInput getActionType() {
		return actionType;
	}
	public ClickTypeMod getContainerInput() {
		return ContainerInput;
	}
}
