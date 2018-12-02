package src.data.scripts.campaign.raid.sub;

import java.awt.Color;
import java.util.List;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import src.data.scripts.campaign.raid.SAD_RouteManager;
import src.data.scripts.campaign.raid.SAD_RouteManager.RouteData;
import src.data.scripts.campaign.raid.SAD_RouteManager.RouteSegment;
import src.data.scripts.campaign.raid.SAD_raidIntel;
import src.data.scripts.campaign.raid.SAD_raidIntel.RaidStageStatus;

public class SAD_TravelStage extends SAD_BaseRaidStage {

	protected SectorEntityToken from;
	protected SectorEntityToken to;
	protected boolean requireNearTarget;


	public SAD_TravelStage(SAD_raidIntel raid, SectorEntityToken from, SectorEntityToken to, boolean requireNearTarget) {
		super(raid);
		this.from = from;
		this.to = to;
		this.requireNearTarget = requireNearTarget;
	}

	@Override
	public void notifyStarted() {
		updateRoutes();
	}


	protected void updateRoutes() {
		resetRoutes();
		
		List<RouteData> routes = SAD_RouteManager.getInstance().getRoutesForSource(intel.getRouteSourceId());
		for (RouteData route : routes) {
			float travelDays = RouteLocationCalculator.getTravelDays(from, to);

			
			route.addSegment(new RouteSegment(travelDays, from, to));
			route.addSegment(new RouteSegment(1000f, to, SAD_AssembleStage.WAIT_STAGE));
			
			maxDays = Math.max(maxDays, travelDays);
		}
	}
	
	protected void updateStatus() {
		abortIfNeededBasedOnFP(true);
		updateStatusBasedOnReaching(to, true, requireNearTarget);
	}
	
	
	public void showStageInfo(TooltipMakerAPI info) {
		int curr = intel.getCurrentStage();
		int index = intel.getStageIndex(this);
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		
		if (status == RaidStageStatus.FAILURE) {
			info.addPara("The \"Peace and Love\" force has failed to successfully reach the " +
					intel.getSystem().getNameWithLowercaseType() + ".", opad);
		} else if (curr == index) {
			info.addPara("The \"Peace and Love\" force is currently travelling to the " + 
					intel.getSystem().getNameWithLowercaseType() + ".", opad);
		}
	}
}



