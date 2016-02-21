package ru.exsdev;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import ru.exsdev.Blocks.BlockFoodPrepReworked;
import ru.exsdev.TileEntities.TEFoodPrepReworked;

public class ModManagerReworked
{
	public static Block BlockFoodPrepReworked;
	
	public static void loadBlocks()
	{
		BlockFoodPrepReworked = new BlockFoodPrepReworked().setBlockName("FoodPrepReworked").setHardness(2);
	}

	public static void registerBlocks()
	{
		GameRegistry.registerBlock(BlockFoodPrepReworked, "FoodPrepReworked");
	}

	public static void registerTileEntities()
	{
		GameRegistry.registerTileEntity(TEFoodPrepReworked.class, "FoodPrepReworked");
		
	}
}
