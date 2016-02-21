/**
 * 
 */
package ru.exsdev.TileEntities;

import com.bioxx.tfc.Core.TFC_Core;
import com.bioxx.tfc.TileEntities.TEFoodPrep;

/**
 * @author exploser
 *
 */
public class TEFoodPrepReworked extends TEFoodPrep
{
	private boolean inCellar = false;
	private float temperature = 0;

	@Override
	public void updateEntity()
	{
		if (inCellar)
		{
			float envDecay = TFC_Core.getEnvironmentalDecay(temperature);
			TFC_Core.handleItemTicking(this, worldObj, xCoord, yCoord, zCoord, envDecay);
		}
		else
		{
			TFC_Core.handleItemTicking(this, worldObj, xCoord, yCoord, zCoord);
		}
	}

	public void updateCellarState(boolean inCellar, float temperature)
	{
		this.inCellar = inCellar;
		this.temperature = temperature;
	}
	
	@Override
	public String getInventoryName()
	{
		return "Food Prep";
	}

}
