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

        //x.alias("sanguinary_autonomist_defectors_ThemeGenerator", sanguinary_autonomist_defectors_ThemeGenerator.class);
        x.alias("sanguinary_autonomist_defectors_RSICGen", sanguinary_autonomist_defectors_ThemeGenerator.sanguinary_autonomist_defectors_StationInteractionConfigGen.class);
        x.alias("sanguinary_autonomist_defectors_RFICGen", sanguinary_autonomist_defectors_SeededFleetManager.sanguinary_autonomist_defectors_FleetInteractionConfigGen.class);

        x.alias("sanguinary_autonomist_defectors_WarningBeaconEntityPlugin", sanguinary_autonomist_defectors_WarningBeaconEntityPlugin.class);
        x.aliasAttribute(sanguinary_autonomist_defectors_WarningBeaconEntityPlugin.class, "phase", "p");
        x.aliasAttribute(sanguinary_autonomist_defectors_WarningBeaconEntityPlugin.class, "freqMult", "f");
        x.aliasAttribute(sanguinary_autonomist_defectors_WarningBeaconEntityPlugin.class, "sincePing", "s");

	x.alias("sanguinary_autonomist_defectors_DiscoverEntityListener", sanguinary_autonomist_defectors_DiscoverEntityListener.class);
    /*    x.alias("sanguinary_autonomist_defectors_RouteManager", sanguinary_autonomist_defectors_RouteManager.class);
        x.aliasAttribute(sanguinary_autonomist_defectors_RouteManager.class, "routes", "r");

        x.alias("sanguinary_autonomist_defectors_RouteData", RouteData.class);
        x.aliasAttribute(RouteData.class, "extra", "x");
        x.aliasAttribute(RouteData.class, "delay", "a");
        x.aliasAttribute(RouteData.class, "source", "o");
        x.aliasAttribute(RouteData.class, "from", "m");
        x.aliasAttribute(RouteData.class, "seed", "s");
        x.aliasAttribute(RouteData.class, "timestamp", "t");
        x.aliasAttribute(RouteData.class, "segments", "e");
        x.aliasAttribute(RouteData.class, "activeFleet", "f");
        x.aliasAttribute(RouteData.class, "daysSinceSeenByPlayer", "d");
        x.aliasAttribute(RouteData.class, "custom", "c");
        x.aliasAttribute(RouteData.class, "current", "r");
        x.aliasAttribute(RouteData.class, "spawner", "p");

        x.alias("sanguinary_autonomist_defectors_AssignmentAI", sanguinary_autonomist_defectors_AssignmentAI.class);
       // x.aliasAttribute(sanguinary_autonomist_defectors_AssignmentAI.class, "capTracker", "cT");
       // x.aliasAttribute(sanguinary_autonomist_defectors_AssignmentAI.class, "buildTracker", "bT");

        x.alias("sanguinary_autonomist_defectors_RaidAssignmentAI", sanguinary_autonomist_defectors_RaidAssignmentAI.class);
        x.aliasAttribute(sanguinary_autonomist_defectors_RaidAssignmentAI.class, "gaveReturnAssignments", "gRA");

        x.alias("sanguinary_autonomist_defectors_RouteFleetSpawner", sanguinary_autonomist_defectors_RouteFleetSpawner.class);

        x.alias("sanguinary_autonomist_defectors_RtSeg", RouteSegment.class);
        x.aliasAttribute(RouteSegment.class, "id", "i");
        x.aliasAttribute(RouteSegment.class, "elapsed", "e");
        x.aliasAttribute(RouteSegment.class, "daysMax", "d");
        x.aliasAttribute(RouteSegment.class, "from", "f");
        x.aliasAttribute(RouteSegment.class, "to", "t");
        x.alias("sanguinary_autonomist_defectors_OptionalFleetData", sanguinary_autonomist_defectors_OptionalFleetData.class);
        x.aliasAttribute(sanguinary_autonomist_defectors_OptionalFleetData.class, "strength", "s");
        x.aliasAttribute(sanguinary_autonomist_defectors_OptionalFleetData.class, "quality", "q");
        x.aliasAttribute(sanguinary_autonomist_defectors_OptionalFleetData.class, "factionId", "f");
        x.aliasAttribute(sanguinary_autonomist_defectors_OptionalFleetData.class, "fleetType", "t");
        x.aliasAttribute(sanguinary_autonomist_defectors_OptionalFleetData.class, "damage", "d");
*/
    }
}
