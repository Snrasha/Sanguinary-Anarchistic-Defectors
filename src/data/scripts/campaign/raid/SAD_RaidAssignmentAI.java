package src.data.scripts.campaign.raid;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetActionTextProvider;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseAssignmentAI;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import src.data.scripts.campaign.raid.SAD_RouteManager.RouteData;
import src.data.scripts.campaign.raid.SAD_RouteManager.RouteSegment;
import src.data.scripts.campaign.raid.sub.*;

public class SAD_RaidAssignmentAI extends BaseAssignmentAI implements FleetActionTextProvider {
       protected RouteData route;
	protected Boolean gaveReturnAssignments = null;


	public SAD_RaidAssignmentAI(CampaignFleetAPI fleet, RouteData route, FleetActionDelegate delegate) {
            		super();

            	this.fleet = fleet;
		this.route = route;
		this.delegate = delegate;
		giveInitialAssignments();
		fleet.getAI().setActionTextProvider(this);
	}
        public static enum TravelState {
		IN_SYSTEM,
		LEAVING_SYSTEM,
		IN_HYPER_TRANSIT,
		ENTERING_SYSTEM,
	}
	
	public SAD_RaidAssignmentAI(CampaignFleetAPI fleet, RouteData route) {
		super();
		this.fleet = fleet;
		this.route = route;
		giveInitialAssignments();
	}
	
	protected TravelState getTravelState(RouteSegment segment) {
		if (segment.isInSystem()) {
			return TravelState.IN_SYSTEM;
		}
		
		if (segment.hasLeaveSystemPhase() && segment.getLeaveProgress() < 1f) {
			return TravelState.LEAVING_SYSTEM;
		}
		if (segment.hasEnterSystemPhase() && segment.getEnterProgress() > 0f) {
			return TravelState.ENTERING_SYSTEM;
		}
		
		return TravelState.IN_HYPER_TRANSIT;
	}
	
	protected LocationAPI getLocationForState(RouteSegment segment, TravelState state) {
		switch (state) {
		case ENTERING_SYSTEM: {
			if (segment.to != null) {
				return segment.to.getContainingLocation();
			}
			return segment.from.getContainingLocation();
		}
		case IN_HYPER_TRANSIT: return Global.getSector().getHyperspace();
		case IN_SYSTEM: return segment.from.getContainingLocation();
		case LEAVING_SYSTEM: return segment.from.getContainingLocation();
		}
		return null;
	}
	
	protected void giveInitialAssignments() {
		TravelState state = getTravelState(route.getCurrent());
		LocationAPI conLoc = getLocationForState(route.getCurrent(), state);
		
		if (fleet.getContainingLocation() != null) {
			fleet.getContainingLocation().removeEntity(fleet);
		}
		conLoc.addEntity(fleet);
		
//		Vector2f loc = route.getInterpolatedLocation();
//		fleet.setLocation(loc.x, loc.y);
		fleet.setFacing((float) Math.random() * 360f);
		
		pickNext(true);
	}
	
	
	protected void advance(float amount, boolean withReturnAssignments) {
		if (withReturnAssignments && route.isExpired() && gaveReturnAssignments == null) {
			RouteSegment current = route.getCurrent();
			if (current != null && current.from != null &&
					Misc.getDistance(fleet.getLocation(), current.from.getLocation()) < 1000f) {
				fleet.clearAssignments();
				fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, current.from, 1000f,
								    "returning to " + current.from.getName());
			} else {
				Misc.giveStandardReturnToSourceAssignments(fleet);
			}
			gaveReturnAssignments = true;
			return;
		}
		super.advance(amount);
	}

	protected String getTravelActionText(RouteSegment segment) {
		return "traveling";
	}


	
	protected String getEndingActionText(RouteSegment segment) {
		SectorEntityToken to = segment.to;
		if (to == null) to = segment.from;
		if (to == null) to = route.from;
		return "returning to " + to.getName();
		//return "returning to " + route.getMarket().getName());
		//return "orbiting " + route.getMarket().getName();
	}
	
	protected void pickNext() {
		pickNext(false);
	}
	
	protected void pickNext(boolean justSpawned) {
		RouteSegment current = route.getCurrent();
		if (current == null) return;
		
		List<RouteSegment> segments = route.getSegments();
		int index = route.getSegments().indexOf(route.getCurrent());
		
		
		if (index == 0 && route.from != null && !current.isTravel()) {
			if (current.getFrom() != null && (current.getFrom().isSystemCenter() || current.getFrom() != route.from)) {
				addLocalAssignment(current, justSpawned);
			} else {
				addStartingAssignment(current, justSpawned);
			}
			return;
		}
		
		if (index == segments.size() - 1 && route.from != null && !current.isTravel()
				&& (current.elapsed >= current.daysMax || current.getFrom() == route.from)) {
			addEndingAssignment(current, justSpawned);
			return;
		}
		
		// transiting from current to next; may or may not be in the same star system
		if (current.isTravel()) {
			if (index == segments.size() - 1 && 
					fleet.getContainingLocation() == current.to.getContainingLocation() && 
					current.elapsed >= current.daysMax) {
				addEndingAssignment(current, justSpawned);
			} else {
				addTravelAssignment(current, justSpawned);
			}
			return;
		}
		
		// in a system or in a hyperspace location for some time
		if (!current.isTravel()) {
			addLocalAssignment(current, justSpawned);
		}
	}
	
	protected void addStartingAssignment(final RouteSegment current, boolean justSpawned) {
		SectorEntityToken from = current.getFrom();
		if (from == null) from = route.from;
		
		if (justSpawned) {
			float progress = current.getProgress();
			RouteLocationCalculator.setLocation(fleet, progress, from, from);
		}
		fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, from, 
						    current.daysMax - current.elapsed, getStartingActionText(current),
						    goNextScript(current));
	}
	
	protected Script goNextScript(final RouteSegment current) {
		return new Script() {
			public void run() {
				route.goToAtLeastNext(current);
			}
		};
	}
	
	protected void addEndingAssignment(final RouteSegment current, boolean justSpawned) {
		if (justSpawned) {
			float progress = current.getProgress();
			RouteLocationCalculator.setLocation(fleet, progress, 
									current.getDestination(), current.getDestination());
		}

		
		SectorEntityToken to = current.to;
		if (to == null) to = current.from;
		if (to == null) to = route.from;
		
		fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, to, 1000f,
							"returning to " + to.getName());
		if (current.daysMax > current.elapsed) {
			fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, to, 
								current.daysMax - current.elapsed, "orbiting " + to.getName());
		}
		fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, to, 
				1000f, getEndingActionText(current),
				goNextScript(current));
	}
	
	protected void addLocalAssignment(final RouteSegment current, boolean justSpawned) {
		if (justSpawned) {
			float progress = current.getProgress();
			RouteLocationCalculator.setLocation(fleet, progress, 
									current.from, current.getDestination());
		}
		if (current.from != null && current.to == null && !current.isFromSystemCenter()) {
			fleet.addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, current.from, 
					current.daysMax - current.elapsed, getInSystemActionText(current),
					goNextScript(current));		
			return;
		}

		
		SectorEntityToken target = null;
		if (current.from.getContainingLocation() instanceof StarSystemAPI) {
			target = ((StarSystemAPI)current.from.getContainingLocation()).getCenter();
		} else {
			target = Global.getSector().getHyperspace().createToken(current.from.getLocation().x, current.from.getLocation().y);
		}
		
		fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, target, 
						    current.daysMax - current.elapsed, getInSystemActionText(current));
	}
	
	protected void addTravelAssignment(final RouteSegment current, boolean justSpawned) {
		if (justSpawned) {
			TravelState state = getTravelState(current);
			if (state == TravelState.LEAVING_SYSTEM) {
				float p = current.getLeaveProgress();
				JumpPointAPI jp = RouteLocationCalculator.findJumpPointToUse(fleet, current.from);
				
				RouteLocationCalculator.setLocation(fleet, p, 
						current.from, jp);
			}
			else if (state == TravelState.IN_SYSTEM) {
				float p = current.getTransitProgress();
				RouteLocationCalculator.setLocation(fleet, p, 
													current.from, current.to);
			}
			else if (state == TravelState.IN_HYPER_TRANSIT) {
				float p = current.getTransitProgress();
				SectorEntityToken t1 = Global.getSector().getHyperspace().createToken(
															   current.from.getLocationInHyperspace().x, 
															   current.from.getLocationInHyperspace().y);
				SectorEntityToken t2 = Global.getSector().getHyperspace().createToken(
															   current.to.getLocationInHyperspace().x, 
						   									   current.to.getLocationInHyperspace().y);				
				RouteLocationCalculator.setLocation(fleet, p, t1, t2);

			}

		}
		
		fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, current.to, 10000f, getTravelActionText(current), 
				goNextScript(current));

	}

	
        
	
	@Override
	public void advance(float amount) {
		advance(amount, false);
		
		RouteSegment curr = route.getCurrent();
		//if (!Misc.isBusy(fleet) && 
		if (curr != null && 
				(
					SAD_BaseRaidStage.STRAGGLER.equals(route.getCustom()) || 
					SAD_AssembleStage.WAIT_STAGE.equals(curr.custom) || 
					curr.isTravel())) {
			Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), MemFlags.FLEET_BUSY, "raid_wait", true, 1);
		}
		
		checkCapture(amount);
		//checkBuild(amount);
		
		if (fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_RAIDER)) {
			checkRaid(amount);
		}
	}

	protected String getInSystemActionText(RouteSegment segment) {
		if (SAD_AssembleStage.WAIT_STAGE.equals(segment.custom)) {
			return "waiting at rendezvous point";
		}
		String s = null;
		if (delegate != null) s = delegate.getRaidInSystemText(fleet);
		if (s == null) s = "raiding"; 
		return s;
	}



	protected String getStartingActionText(RouteSegment segment) {
		if (SAD_AssembleStage.PREP_STAGE.equals(segment.custom)) {
			String s = null;
			if (delegate != null) s = delegate.getRaidPrepText(fleet, segment.from);
			if (s == null) s = "preparing for raid"; 
			return s;
		}
		if (segment.from == route.from) {
			return "orbiting " + route.from.getName();
		}
		
		String s = null;
		if (delegate != null) s = delegate.getRaidDefaultText(fleet);
		if (s == null) s = "raiding"; 
		return s;
	}


	public String getActionText(CampaignFleetAPI fleet) {
		FleetAssignmentDataAPI curr = fleet.getCurrentAssignment();
		if (curr != null && curr.getAssignment() == FleetAssignment.PATROL_SYSTEM &&
				curr.getActionText() == null) {
			
			String s = null;
			if (delegate != null) s = delegate.getRaidDefaultText(fleet);
			if (s == null) s = "raiding"; 
			return s;
			
		}
		return null;
	}



	
	
}
