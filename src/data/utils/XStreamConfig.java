package src.data.utils;

import src.data.scripts.campaign.Forg_AssignmentAI;
import src.data.scripts.campaign.Forg_SeededFleetManager;
import src.data.scripts.campaign.Forg_StationFleetManager;
import src.data.scripts.campaign.Forg_ThemeGenerator;
import src.data.scripts.campaign.Forg_WarningBeaconEntityPlugin;

public class XStreamConfig {
	public static void configureXStream(com.thoughtworks.xstream.XStream x)
	{
		x.alias("Forg_AssignmentAI", Forg_AssignmentAI.class);
		x.alias("Forg_SeededFleetManager", Forg_SeededFleetManager.class);
		x.alias("Forg_StationFleetManager", Forg_StationFleetManager.class);
		x.alias("Forg_ThemeGenerator", Forg_ThemeGenerator.class);
                x.alias("Forg_WarningBeaconEntityPlugin", Forg_WarningBeaconEntityPlugin.class);
	}
}