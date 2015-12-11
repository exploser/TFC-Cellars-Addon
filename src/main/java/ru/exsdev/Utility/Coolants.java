package ru.exsdev.Utility;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import sladki.tfc.ModManager;

public class Coolants {

	private static final Map<Object, Integer> coolantValues = new HashMap<Object, Integer>();

	static {
		coolantValues.put(Blocks.snow, 40);
		coolantValues.put(ModManager.IceBlock, 120);
		coolantValues.put(Items.snowball, 10);
	}

	public static int getCoolantFromItem(Item item) {
		Block tmpblock = Block.getBlockFromItem(item);

		if (coolantValues.containsKey(item)) {
			return coolantValues.get(item);
		} else {
			if (coolantValues.containsKey(tmpblock))
				return coolantValues.get(tmpblock);
			return 0;
		}
	}

	public static boolean isCoolant(Item item) {
		return coolantValues.containsKey(item) || coolantValues.containsKey(Block.getBlockFromItem(item));
	}
}
