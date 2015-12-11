package sladki.tfc;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ModConfig {

	public static boolean isDebugging;
	public static float coolantConsumptionMultiplier;
	public static float cellarTemperature;
	public static float iceHouseTemperature;

	public static void loadConfig(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());

		config.load();

		isDebugging = config.get(Configuration.CATEGORY_GENERAL, "Debug", false).getBoolean(false);
		cellarTemperature = (float)config.get(Configuration.CATEGORY_GENERAL, "TemperatureCellar", 5.0).getDouble(5.0);
		iceHouseTemperature = (float)config.get(Configuration.CATEGORY_GENERAL, "TemperatureIceHouse", 0.2).getDouble(0.2);
		
		Property coolantConsumptionMultiplierProperty = config.get(Configuration.CATEGORY_GENERAL,
				"CoolantConsumptionMultiplier", 100);
		coolantConsumptionMultiplierProperty.comment = "The multiplier 100 is 1.0, 123 is 1.23";
		coolantConsumptionMultiplier = (float) (0.01 * coolantConsumptionMultiplierProperty.getInt());

		config.save();
	}

}