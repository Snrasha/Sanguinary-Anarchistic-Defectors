package src.data.scripts.campaign.raid.sub;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import src.data.scripts.campaign.raid.SAD_RouteManager;
import src.data.scripts.campaign.raid.SAD_RouteManager.RouteData;
import src.data.scripts.campaign.raid.SAD_RouteManager.RouteSegment;
import src.data.scripts.campaign.raid.SAD_raidIntel;
import src.data.scripts.campaign.raid.SAD_raidIntel.RaidStageStatus;
import src.data.scripts.campaign.raid.SAD_raidIntel.SAD_RaidStage;

public class SAD_BaseRaidStage implements SAD_RaidStage {

	public static final String STRAGGLER = "raid_straggler";
	
	protected SAD_raidIntel intel;
	
	protected IntervalUtil statusInterval = new IntervalUtil(0.1f, 0.2f);
	protected RaidStageStatus status = RaidStageStatus.ONGOING;
	protected float elapsed = 0f;
	protected float maxDays = 1f;
	
	protected float abortFP = 0;
	
	public SAD_BaseRaidStage(SAD_raidIntel raid) {
		this.intel = raid;
	}

	public float getAbortFP() {
		return abortFP;
	}

	public void setAbortFP(float successFP) {
		this.abortFP = successFP;
	}

//	protected float getTotalRouteFP() {
//		float total = 0f;
//		for (RouteData route : getRoutes()) {
//			total += route.
//		}
//	}

	public void resetRoutes() {
		List<RouteData> routes = SAD_RouteManager.getInstance().getRoutesForSource(intel.getRouteSourceId());
		for (RouteData route : routes) {
			resetRoute(route);
		}
	}
	
	public void resetRoute(RouteData route) {
		CampaignFleetAPI fleet = route.getActiveFleet();
		if (fleet != null) {
			fleet.clearAssignments();
		}
		route.getSegments().clear();
		route.setCurrent(null);
	}
	
	public List<RouteData> getRoutes() {
		List<RouteData> routes = SAD_RouteManager.getInstance().getRoutesForSource(intel.getRouteSourceId());
		List<RouteData> result = new ArrayList<RouteData>();
		for (RouteData route : routes) {
			if (!STRAGGLER.equals(route.getCustom())) {
				result.add(route);
			}
		}
		return result;
	}
	
	public void giveReturnOrdersToStragglers(List<RouteData> stragglers) {
		for (RouteData route : stragglers) {
			SectorEntityToken from1 = Global.getSector().getHyperspace().createToken(route.getInterpolatedHyperLocation());
			
			route.setCustom(STRAGGLER);
			resetRoute(route);

			float travelDays = RouteLocationCalculator.getTravelDays(from1,  route.from);
			if (DebugFlags.RAID_DEBUG) {
				travelDays *= 0.1f;
			}
			
			float orbitDays = 1f + 1f * (float) Math.random();
			route.addSegment(new RouteSegment(travelDays, from1, route.from));
			route.addSegment(new RouteSegment(orbitDays, route.from));
			
			//route.addSegment(new RouteSegment(2f + (float) Math.random() * 1f, route.getMarket().getPrimaryEntity()));
		}
	}
	
	public void advance(float amount) {
		float days = Misc.getDays(amount);
		
		elapsed += days;
		
		statusInterval.advance(days);
		if (statusInterval.intervalElapsed()) {
			updateStatus();
		}
	}

	public RaidStageStatus getStatus() {
		return status;
	}
	public void notifyStarted() {
		
	}
	
	
	protected boolean enoughMadeIt(List<RouteData> routes, List<RouteData> stragglers) {
		float madeItFP = 0;
		for (RouteData route : routes) {
			if (stragglers.contains(route)) continue;
			CampaignFleetAPI fleet = route.getActiveFleet();
			if (fleet != null) {
				float mult = 3f;
				if (mult < 1) mult = 1f;
				madeItFP += fleet.getFleetPoints() / mult;
			} else {
				madeItFP += route.getExtra().fp;
			}
		}
		return madeItFP >= abortFP;
	}
	
	protected void updateStatus() {
		abortIfNeededBasedOnFP(true);
	}
	
	protected void abortIfNeededBasedOnFP(boolean giveReturnOrders) {
		List<RouteData> routes = getRoutes();
		List<RouteData> stragglers = new ArrayList<RouteData>();
		
		boolean enoughMadeIt = enoughMadeIt(routes, stragglers);
		//enoughMadeIt = false;
		if (!enoughMadeIt) {
			status = RaidStageStatus.FAILURE;
			if (giveReturnOrders) {
				giveReturnOrdersToStragglers(routes);
			}
			return;
		}
	}
	
	protected void updateStatusBasedOnReaching(SectorEntityToken dest, boolean giveReturnOrders) {
		updateStatusBasedOnReaching(dest, giveReturnOrders, true);
	}
	protected void updateStatusBasedOnReaching(SectorEntityToken dest, boolean giveReturnOrders, boolean requireNearTarget) {
		List<RouteData> routes = getRoutes();
		float maxRange = 1000f;
		if (!requireNearTarget) {
			maxRange = 10000000f;
		}
		List<RouteData> stragglers = getStragglers(routes, dest, maxRange);
		
		boolean enoughMadeIt = enoughMadeIt(routes, stragglers);
		
		if (stragglers.isEmpty() && enoughMadeIt) {
			status = RaidStageStatus.SUCCESS;
			return;
		}
		
		if (elapsed > maxDays + intel.getExtraDays()) {
			if (enoughMadeIt) {
				status = RaidStageStatus.SUCCESS;
				if (giveReturnOrders) {
					giveReturnOrdersToStragglers(stragglers);
				}
			} else {
				status = RaidStageStatus.FAILURE;
				if (giveReturnOrders) {
					giveReturnOrdersToStragglers(routes);
				}
			}
			return;
		}
	}
	
	public float getExtraDaysUsed() {
		return Math.max(0, elapsed - maxDays);
	}
	
	public List<RouteData> getStragglers(List<RouteData> routes, SectorEntityToken dest, float maxRange) {
		List<RouteData> stragglers = new ArrayList<RouteData>();
		
		for (RouteData route : routes) {
			CampaignFleetAPI fleet = route.getActiveFleet();
			if (fleet != null) {
				if (fleet.getContainingLocation() == dest.getContainingLocation()) {
					float dist = Misc.getDistance(fleet, dest);
					if (dist > maxRange) {
						stragglers.add(route);
					}
				} else {
					stragglers.add(route);
				}
			} else if (!route.isExpired()) {
				boolean waiting = false;
				if (route.getCurrent() != null && SAD_AssembleStage.WAIT_STAGE.equals(route.getCurrent().custom)) {
					waiting = true;
				}
				if (!waiting) {
					stragglers.add(route);
				}
			}
		}
		
		return stragglers;
	}

	
	public float getElapsed() {
		return elapsed;
	}

	public float getMaxDays() {
		return maxDays;
	}

	public void showStageInfo(TooltipMakerAPI info) {
	}
	
	
	
}


