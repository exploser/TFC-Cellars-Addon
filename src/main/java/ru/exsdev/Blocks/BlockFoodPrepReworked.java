package ru.exsdev.Blocks;

import com.bioxx.tfc.Blocks.Devices.BlockFoodPrep;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import ru.exsdev.TileEntities.TEFoodPrepReworked;

public class BlockFoodPrepReworked extends BlockFoodPrep
{
	
	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_)
	{
		return new TEFoodPrepReworked();
	}

}
