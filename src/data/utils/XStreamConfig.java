package src.data.utils;

import src.data.scripts.campaign.SAD_AssignmentAI;
import src.data.scripts.campaign.SAD_SeededFleetManager;
import src.data.scripts.campaign.SAD_StationFleetManager;
import src.data.scripts.campaign.SAD_ThemeGenerator;
import src.data.scripts.campaign.SAD_WarningBeaconEntityPlugin;

public class XStreamConfig {
	public static void configureXStream(com.thoughtworks.xstream.XStream x)
	{
		x.alias("SAD_AssignmentAI", SAD_AssignmentAI.class);
		x.alias("SAD_SeededFleetManager", SAD_SeededFleetManager.class);
		x.alias("SAD_StationFleetManager", SAD_StationFleetManager.class);
		x.alias("SAD_ThemeGenerator", SAD_ThemeGenerator.class);
                x.alias("SAD_WarningBeaconEntityPlugin", SAD_WarningBeaconEntityPlugin.class);
                
              /*  
                x.alias("SAD_PirateBaseIntel", SAD_BaseIntel.class);
		x.aliasAttribute(SAD_BaseIntel.class, "system", "s");
		x.aliasAttribute(SAD_BaseIntel.class, "market", "m");
		x.aliasAttribute(SAD_BaseIntel.class, "entity", "e");
		x.aliasAttribute(SAD_BaseIntel.class, "elapsedDays", "eD");
		x.aliasAttribute(SAD_BaseIntel.class, "duration", "d");
		x.aliasAttribute(SAD_BaseIntel.class, "bountyData", "bD");
		x.aliasAttribute(SAD_BaseIntel.class, "tier", "t");
		x.aliasAttribute(SAD_BaseIntel.class, "matchedStationToTier", "mSTT");
		x.aliasAttribute(SAD_BaseIntel.class, "monthlyInterval", "mI");
		x.aliasAttribute(SAD_BaseIntel.class, "raidTimeoutMonths", "rTM");*/
	}
}