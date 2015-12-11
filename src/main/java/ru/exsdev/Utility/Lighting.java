package ru.exsdev.Utility;

import java.util.HashMap;
import java.util.Map;

import com.bioxx.tfc.api.TFCBlocks;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import sladki.tfc.ModManager;

public class Lighting {
	private static final Map<Object, Integer> lightingValues = new HashMap<Object, Integer>();

	static {
		lightingValues.put(Blocks.glowstone, 40);
		lightingValues.put(TFCBlocks.torch, 10);
	}
	
}
