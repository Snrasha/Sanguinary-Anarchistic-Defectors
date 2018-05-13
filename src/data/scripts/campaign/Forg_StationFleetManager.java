package src.data.scripts.campaign;

import java.util.Random;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetOrStubAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV2;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParams;
import com.fs.starfarer.api.impl.campaign.fleets.SourceBasedFleetManager;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;

public class Forg_StationFleetManager extends SourceBasedFleetManager {

	protected int minPts;
	protected int maxPts;
	protected int totalLost;

	public Forg_StationFleetManager(SectorEntityToken source, float thresholdLY, int minFleets, int maxFleets, float respawnDelay, 
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
		if (bonus > maxPts) bonus = maxPts;
		
		combatPoints += bonus;
		
		String type = FleetTypes.PATROL_SMALL;
		if (combatPoints > 8) type = FleetTypes.PATROL_MEDIUM;
		if (combatPoints > 16) type = FleetTypes.PATROL_LARGE;
		
		FleetParams params = new FleetParams(
				source.getLocationInHyperspace(),
				source.getMarket(), 
				"forgotten",
				null, // fleet's faction, if different from above, which is also used for source market picking
				type,
				combatPoints, // combatPts
				0f, // freighterPts 
				0f, // tankerPts
				0f, // transportPts
				0f, // linerPts
				0f, // civilianPts 
				0f, // utilityPts
				0f, // qualityBonus
				1f, // qualityOverride
				1f, // officer num mult
				0 // officer level bonus
				);
		//params.withOfficers = false;
		params.random = random;
		
		CampaignFleetAPI fleet = FleetFactoryV2.createFleet(params);
		if (fleet == null) return null;;
		
		LocationAPI location = source.getContainingLocation();
		location.addEntity(fleet);
		
		Forg_SeededFleetManager.initForgottenFleetProperties(random, fleet);
		
		fleet.setLocation(source.getLocation().x, source.getLocation().y);
		fleet.setFacing(random.nextFloat() * 360f);
		
		fleet.addScript(new Forg_AssignmentAI(fleet, (StarSystemAPI) source.getContainingLocation(), source));
		
		return fleet;
	}

	
	@Override
	public void reportFleetDespawnedToListener(FleetOrStubAPI fleet, FleetDespawnReason reason, Object param) {
		super.reportFleetDespawnedToListener(fleet, reason, param);
		if (reason == FleetDespawnReason.DESTROYED_BY_BATTLE) {
			totalLost++;
		}
	}

	
}
