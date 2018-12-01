package src.data.scripts.campaign.raid;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import java.awt.Color;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.intel.raid.AssembleStage;
import static com.fs.starfarer.api.impl.campaign.intel.raid.AssembleStage.PREP_STAGE;
import static com.fs.starfarer.api.impl.campaign.intel.raid.AssembleStage.WAIT_STAGE;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel.RaidStageStatus;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import src.data.scripts.campaign.raid.SAD_RouteManager.SAD_OptionalFleetData;

public class SAD_AssembleStage extends AssembleStage {
       public SAD_raidIntel raid;
       
	public CampaignFleetAPI from;
	public SAD_AssembleStage(SAD_raidIntel raid, SectorEntityToken gatheringPoint, CampaignFleetAPI fleet) {
		super(raid, gatheringPoint);
               this.from=fleet;
               this.raid=raid;
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
			info.addPara("The raiding forces have failed to successfully assemble at the rendezvous point. The raid is now over.", opad);
		} else if (curr == index) {
			info.addPara("The raid is currently assembling in the " + gatheringPoint.getContainingLocation().getNameWithLowercaseType() + ".", opad);
		}
	}
        
	
        @Override
	protected void addRoutesAsNeeded(float amount) {
		if (spawnFP <= 0) return;
		
		float days = Misc.getDays(amount);
		
		interval.advance(days);
		if (!interval.intervalElapsed()) return;
			
		
		currSource = 1;
		
		SAD_OptionalFleetData extra = new SAD_RouteManager.SAD_OptionalFleetData();
                extra.quality=5f;
                extra.factionId="sad";
		
		String sid = intel.getRouteSourceId();
		SAD_RouteManager.RouteData route = SAD_RouteManager.getInstance().addRoute(sid, from, Misc.genRandomSeed(), extra, raid, null);
	//RouteManager.RouteData route = RouteManager.getInstance().addRoute(sid, null, Misc.genRandomSeed(), extra, intel, null);
		
		extra.fleetType = pickNextType();
		float fp = getFP(extra.fleetType);
		
		//extra.fp = Misc.getAdjustedFP(fp, market);
		extra.fp = fp;
		extra.strength = (float)(Global.getSector().getPlayerStats().getLevel())/10;
		float prepDays = 3f + 3f * (float) Math.random();
		float travelDays = RouteLocationCalculator.getTravelDays(from, gatheringPoint);
		
		route.addSegment(new SAD_RouteManager.RouteSegment(prepDays,from, PREP_STAGE));
		route.addSegment(new SAD_RouteManager.RouteSegment(travelDays, from, gatheringPoint));
		route.addSegment(new SAD_RouteManager.RouteSegment(1000f, gatheringPoint, WAIT_STAGE));
		
		maxDays = Math.max(maxDays, prepDays + travelDays);
		//maxDays = 6f;
		
	}

	@Override
	protected String pickNextType() {
		return FleetTypes.TASK_FORCE;
	}
	
	@Override
	protected float getFP(String type) {
		float base = 100f;
		if (spawnFP < base * 1.5f) {
			base = spawnFP;
		}
		if (base > spawnFP) base = spawnFP;
		
		spawnFP -= base;
		return base;
	}
}





