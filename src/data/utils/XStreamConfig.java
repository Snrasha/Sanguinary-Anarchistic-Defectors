package src.data.utils;

import src.data.scripts.campaign.sanguinary_autonomist_defectors_AssignmentAI;
import src.data.scripts.campaign.sanguinary_autonomist_defectors_SeededFleetManager;
import src.data.scripts.campaign.sanguinary_autonomist_defectors_StationFleetManager;
import src.data.scripts.campaign.sanguinary_autonomist_defectors_ThemeGenerator;
import src.data.scripts.campaign.sanguinary_autonomist_defectors_WarningBeaconEntityPlugin;
import src.data.scripts.campaign.intels.sanguinary_autonomist_defectors_DiscoverEntityListener;

public class XStreamConfig {

    public static void configureXStream(com.thoughtworks.xstream.XStream x) {
        x.alias("sanguinary_autonomist_defectors_AssignmentAI", sanguinary_autonomist_defectors_AssignmentAI.class);
        x.aliasAttribute(sanguinary_autonomist_defectors_AssignmentAI.class, "homeSystem", "h");
        x.aliasAttribute(sanguinary_autonomist_defectors_AssignmentAI.class, "fleet", "f");
        x.aliasAttribute(sanguinary_autonomist_defectors_AssignmentAI.class, "source", "s");

        x.alias("sanguinary_autonomist_defectors_SeededFleetManager", sanguinary_autonomist_defectors_SeededFleetManager.class);
        x.aliasAttribute(sanguinary_autonomist_defectors_SeededFleetManager.class, "minPts", "i");
        x.aliasAttribute(sanguinary_autonomist_defectors_SeededFleetManager.class, "maxPts", "a");
        x.aliasAttribute(sanguinary_autonomist_defectors_SeededFleetManager.class, "activeChance", "c");

        x.alias("sanguinary_autonomist_defectors_StationFleetManager", sanguinary_autonomist_defectors_StationFleetManager.class);
        x.aliasAttribute(sanguinary_autonomist_defectors_StationFleetManager.class, "minPts", "iA");
        x.aliasAttribute(sanguinary_autonomist_defectors_StationFleetManager.class, "maxPts", "aA");
        x.aliasAttribute(sanguinary_autonomist_defectors_StationFleetManager.class, "totalLost", "tL");

        x.alias("sanguinary_autonomist_defectors_RSICGen", sanguinary_autonomist_defectors_ThemeGenerator.sanguinary_autonomist_defectors_StationInteractionConfigGen.class);
        x.alias("sanguinary_autonomist_defectors_RFICGen", sanguinary_autonomist_defectors_SeededFleetManager.sanguinary_autonomist_defectors_FleetInteractionConfigGen.class);

        x.alias("sanguinary_autonomist_defectors_WarningBeaconEntityPlugin", sanguinary_autonomist_defectors_WarningBeaconEntityPlugin.class);
        x.aliasAttribute(sanguinary_autonomist_defectors_WarningBeaconEntityPlugin.class, "phase", "p");
        x.aliasAttribute(sanguinary_autonomist_defectors_WarningBeaconEntityPlugin.class, "freqMult", "f");
        x.aliasAttribute(sanguinary_autonomist_defectors_WarningBeaconEntityPlugin.class, "sincePing", "s");

	x.alias("sanguinary_autonomist_defectors_DiscoverEntityListener", sanguinary_autonomist_defectors_DiscoverEntityListener.class);
    }
}
