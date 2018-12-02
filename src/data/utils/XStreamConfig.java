package src.data.utils;

import src.data.scripts.campaign.SAD_AssignmentAI;
import src.data.scripts.campaign.SAD_SeededFleetManager;
import src.data.scripts.campaign.SAD_StationFleetManager;
import src.data.scripts.campaign.SAD_ThemeGenerator;
import src.data.scripts.campaign.SAD_WarningBeaconEntityPlugin;
import src.data.scripts.campaign.raid.SAD_RaidAssignmentAI;
import src.data.scripts.campaign.raid.SAD_RouteManager;
import src.data.scripts.campaign.raid.SAD_RouteManager.SAD_OptionalFleetData;
import src.data.scripts.campaign.raid.SAD_RouteManager.SAD_RouteFleetSpawner;
import src.data.scripts.campaign.raid.SAD_RouteManager.RouteData;
import src.data.scripts.campaign.raid.SAD_RouteManager.RouteSegment;

public class XStreamConfig {

    public static void configureXStream(com.thoughtworks.xstream.XStream x) {
        x.alias("SAD_AssignmentAI", SAD_AssignmentAI.class);
        x.aliasAttribute(SAD_AssignmentAI.class, "homeSystem", "h");
        x.aliasAttribute(SAD_AssignmentAI.class, "fleet", "f");
        x.aliasAttribute(SAD_AssignmentAI.class, "source", "s");

        x.alias("SAD_SeededFleetManager", SAD_SeededFleetManager.class);
        x.aliasAttribute(SAD_SeededFleetManager.class, "minPts", "i");
        x.aliasAttribute(SAD_SeededFleetManager.class, "maxPts", "a");
        x.aliasAttribute(SAD_SeededFleetManager.class, "activeChance", "c");

        x.alias("SAD_StationFleetManager", SAD_StationFleetManager.class);
        x.aliasAttribute(SAD_StationFleetManager.class, "minPts", "iA");
        x.aliasAttribute(SAD_StationFleetManager.class, "maxPts", "aA");
        x.aliasAttribute(SAD_StationFleetManager.class, "totalLost", "tL");

        //x.alias("SAD_ThemeGenerator", SAD_ThemeGenerator.class);
        x.alias("SAD_RSICGen", SAD_ThemeGenerator.SAD_StationInteractionConfigGen.class);
        x.alias("SAD_RFICGen", SAD_SeededFleetManager.SAD_FleetInteractionConfigGen.class);

        x.alias("SAD_WarningBeaconEntityPlugin", SAD_WarningBeaconEntityPlugin.class);
        x.aliasAttribute(SAD_WarningBeaconEntityPlugin.class, "phase", "p");
        x.aliasAttribute(SAD_WarningBeaconEntityPlugin.class, "freqMult", "f");
        x.aliasAttribute(SAD_WarningBeaconEntityPlugin.class, "sincePing", "s");

    /*    x.alias("SAD_RouteManager", SAD_RouteManager.class);
        x.aliasAttribute(SAD_RouteManager.class, "routes", "r");

        x.alias("SAD_RouteData", RouteData.class);
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

        x.alias("SAD_AssignmentAI", SAD_AssignmentAI.class);
       // x.aliasAttribute(SAD_AssignmentAI.class, "capTracker", "cT");
       // x.aliasAttribute(SAD_AssignmentAI.class, "buildTracker", "bT");

        x.alias("SAD_RaidAssignmentAI", SAD_RaidAssignmentAI.class);
        x.aliasAttribute(SAD_RaidAssignmentAI.class, "gaveReturnAssignments", "gRA");

        x.alias("SAD_RouteFleetSpawner", SAD_RouteFleetSpawner.class);

        x.alias("SAD_RtSeg", RouteSegment.class);
        x.aliasAttribute(RouteSegment.class, "id", "i");
        x.aliasAttribute(RouteSegment.class, "elapsed", "e");
        x.aliasAttribute(RouteSegment.class, "daysMax", "d");
        x.aliasAttribute(RouteSegment.class, "from", "f");
        x.aliasAttribute(RouteSegment.class, "to", "t");
        x.alias("SAD_OptionalFleetData", SAD_OptionalFleetData.class);
        x.aliasAttribute(SAD_OptionalFleetData.class, "strength", "s");
        x.aliasAttribute(SAD_OptionalFleetData.class, "quality", "q");
        x.aliasAttribute(SAD_OptionalFleetData.class, "factionId", "f");
        x.aliasAttribute(SAD_OptionalFleetData.class, "fleetType", "t");
        x.aliasAttribute(SAD_OptionalFleetData.class, "damage", "d");
*/
    }
}
