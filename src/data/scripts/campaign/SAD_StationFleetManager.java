package src.data.scripts.campaign;

import java.util.Random;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.SourceBasedFleetManager;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;

public class SAD_StationFleetManager extends SourceBasedFleetManager {

    protected int minPts;
    protected int maxPts;
    protected int totalLost;

    public SAD_StationFleetManager(SectorEntityToken source, float thresholdLY, int minFleets, int maxFleets, float respawnDelay,
            int minPts, int maxPts) {
        super(source, thresholdLY, minFleets, maxFleets, respawnDelay);
        this.minPts = minPts;
        this.maxPts = maxPts;
    }

    @Override
    protected CampaignFleetAPI spawnFleet() {
        Random random = new Random();

        int combatPoints = minPts + random.nextInt(maxPts - minPts + 1);

        int bonus = totalLost * 4;
        if (bonus > maxPts) {
            bonus = maxPts;
        }

        combatPoints += bonus;
        int supportPoints = 0;

        String type = FleetTypes.PATROL_SMALL;
        if (combatPoints > 8) {
            type = FleetTypes.PATROL_MEDIUM;
            supportPoints = 2;

        }
        if (combatPoints > 16) {
            type = FleetTypes.PATROL_LARGE;
            supportPoints = 2;

        }
        combatPoints *= 8f;

        FleetParamsV3 params = new FleetParamsV3(
                source.getMarket(),
                source.getLocationInHyperspace(),
                "sad",
                1f,
                type,
                combatPoints, // combatPts
                supportPoints,// freighterPts 
                supportPoints, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                0f // qualityMod
        );
        params.officerNumberBonus = 10;
        params.random = random;

        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
        if (fleet == null) {
            return null;
        };

        LocationAPI location = source.getContainingLocation();
        location.addEntity(fleet);

        SAD_SeededFleetManager.initSADFleetProperties(random, fleet);

        fleet.setLocation(source.getLocation().x, source.getLocation().y);
        fleet.setFacing(random.nextFloat() * 360f);

        fleet.addScript(new SAD_AssignmentAI(fleet, (StarSystemAPI) source.getContainingLocation(), source));

        return fleet;
    }

    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
        super.reportFleetDespawnedToListener(fleet, reason, param);
        if (reason == FleetDespawnReason.DESTROYED_BY_BATTLE) {
            totalLost++;
        }
    }

}
