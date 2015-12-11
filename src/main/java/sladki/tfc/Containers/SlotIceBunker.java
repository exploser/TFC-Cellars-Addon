package sladki.tfc.Containers;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import ru.exsdev.Utility.Coolants;

public class SlotIceBunker extends Slot {
	
	public SlotIceBunker(IInventory inventory, int i, int j, int k) {
		super(inventory, i, j, k);
	}

	@Override
	public boolean isItemValid(ItemStack itemStack) {
		Item item = itemStack.getItem();
		return Coolants.isCoolant(item);
	}
	
}
