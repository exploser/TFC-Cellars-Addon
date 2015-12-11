package ru.exsdev.Containers;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotLighing extends Slot {

	public SlotLighing(IInventory iinv, int id, int x, int y) {
		super(iinv, id, x, y);
	}
	
	@Override
	public boolean isItemValid(ItemStack itemStack) {
		return true;
	}
}
