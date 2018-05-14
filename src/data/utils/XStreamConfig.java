package src.data.utils;

import src.data.scripts.campaign.SAD_AssignmentAI;
import src.data.scripts.campaign.SAD_SeededFleetManager;
import src.data.scripts.campaign.SAD_StationFleetManager;
import src.data.scripts.campaign.SAD_ThemeGenerator;
import src.data.scripts.campaign.SAD_WarningBeaconEntityPlugin;

public class XStreamConfig {
	public static void configureXStream(com.thoughtworks.xstream.XStream x)
	{
		x.alias("Forg_AssignmentAI", SAD_AssignmentAI.class);
		x.alias("Forg_SeededFleetManager", SAD_SeededFleetManager.class);
		x.alias("Forg_StationFleetManager", SAD_StationFleetManager.class);
		x.alias("Forg_ThemeGenerator", SAD_ThemeGenerator.class);
                x.alias("Forg_WarningBeaconEntityPlugin", SAD_WarningBeaconEntityPlugin.class);
	}
}