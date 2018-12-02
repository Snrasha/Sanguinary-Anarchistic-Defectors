package src.data.scripts.campaign.raid.sub;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.MilitaryResponseScript;
import com.fs.starfarer.api.impl.campaign.command.WarSimScript;
import com.fs.starfarer.api.impl.campaign.econ.impl.OrbitalStation;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseAssignmentAI;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import src.data.scripts.campaign.raid.SAD_RouteManager;
import src.data.scripts.campaign.raid.SAD_raidManager;
import src.data.scripts.campaign.raid.SAD_raidIntel;


public class SAD_ActionStage extends SAD_BaseRaidStage implements BaseAssignmentAI.FleetActionDelegate {
	public SAD_ActionStage(SAD_raidIntel raid, MarketAPI target) {
		super(raid);
		this.target = target;
		playerTargeted = target.isPlayerOwned();
		
		untilAutoresolve = 15f + 5f * (float) Math.random();
	}

	@Override
	public void notifyStarted() {
		updateRoutes();
	}

        	
	protected MarketAPI target;
	protected boolean playerTargeted = false;
	protected List<MilitaryResponseScript> scripts = new ArrayList<MilitaryResponseScript>();
	protected boolean gaveOrders = true; // will be set to false in updateRoutes()
	protected float untilAutoresolve = 30f;

	

	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		float days = Misc.getDays(amount);
		untilAutoresolve -= days;
		if (DebugFlags.PUNITIVE_EXPEDITION_DEBUG) {
			untilAutoresolve -= days * 100f;
		}
		
		if (!gaveOrders) {
			gaveOrders = true;
		
			removeMilScripts();

			// getMaxDays() is always 1 here
			// scripts get removed anyway so we don't care about when they expire naturally
			// just make sure they're around for long enough
			float duration = 100f;
			
			MilitaryResponseScript.MilitaryResponseParams params = new MilitaryResponseScript.MilitaryResponseParams(CampaignFleetAIAPI.ActionType.HOSTILE, 
					"PE_" + Misc.genUID() + target.getId(), 
					intel.getFaction(),
					target.getPrimaryEntity(),
					1f,
					duration);
			MilitaryResponseScript script = new MilitaryResponseScript(params);
			target.getContainingLocation().addScript(script);
			scripts.add(script);
			
			MilitaryResponseScript.MilitaryResponseParams defParams = new MilitaryResponseScript.MilitaryResponseParams(CampaignFleetAIAPI.ActionType.HOSTILE, 
					"defPE_" + Misc.genUID() + target.getId(), 
					target.getFaction(),
					target.getPrimaryEntity(),
					1f,
					duration);
			MilitaryResponseScript defScript = new MilitaryResponseScript(defParams);
			target.getContainingLocation().addScript(defScript);
			scripts.add(defScript);
		}
	}

	protected void removeMilScripts() {
		if (scripts != null) {
			for (MilitaryResponseScript s : scripts) {
				s.forceDone();
			}
		}
	}
	
	@Override
	protected void updateStatus() {
//		if (true) {
//			status = RaidStageStatus.SUCCESS;
//			return;
//		}
		
		abortIfNeededBasedOnFP(true);
		if (status != SAD_raidIntel.RaidStageStatus.ONGOING) return;
		
		boolean inSpawnRange = SAD_RouteManager.isPlayerInSpawnRange(target.getPrimaryEntity());
		if (!inSpawnRange && untilAutoresolve <= 0){
			autoresolve();
			return;
		}
		
		if (!target.isInEconomy() || !target.isPlayerOwned()) {
			status = SAD_raidIntel.RaidStageStatus.FAILURE;
			removeMilScripts();
			giveReturnOrdersToStragglers(getRoutes());
			return;
		}
		
	}
	
	public String getRaidActionText(CampaignFleetAPI fleet, MarketAPI market) {
		SAD_raidIntel intel = ((SAD_raidIntel)this.intel);
		SAD_raidManager.PunExGoal goal = intel.getGoal();
		if (goal == SAD_raidManager.PunExGoal.BOMBARD) {
			return "bombarding " + market.getName();
		}
		return "raiding " + market.getName();
	}

	public String getRaidApproachText(CampaignFleetAPI fleet, MarketAPI market) {
		SAD_raidIntel intel = ((SAD_raidIntel)this.intel);
		SAD_raidManager.PunExGoal goal = intel.getGoal();
		if (goal == SAD_raidManager.PunExGoal.BOMBARD) {
			return "moving in to bombard " + market.getName();
		}
		return "moving in to raid " + market.getName();
	}

	public void performRaid(CampaignFleetAPI fleet, MarketAPI market) {
		removeMilScripts();
		
		SAD_raidIntel intel = ((SAD_raidIntel)this.intel);
		SAD_raidManager.PunExGoal goal = intel.getGoal();
		
		status = SAD_raidIntel.RaidStageStatus.SUCCESS;
		
		if (goal == SAD_raidManager.PunExGoal.BOMBARD) {
			float cost = MarketCMD.getBombardmentCost(market, fleet);
			//float maxCost = intel.getAssembleStage().getOrigSpawnFP() * Misc.FP_TO_BOMBARD_COST_APPROX_MULT;
			float maxCost = intel.getRaidFP() / intel.getNumFleets() * Misc.FP_TO_BOMBARD_COST_APPROX_MULT;
			if (fleet != null) {
				maxCost = fleet.getCargo().getMaxFuel() * 0.25f;
			}
			
			if (cost <= maxCost) {
				new MarketCMD(market.getPrimaryEntity()).doBombardment(intel.getFaction(), MarketCMD.BombardType.SATURATION);
				intel.setOutcome(SAD_raidIntel.PunExOutcome.SUCCESS);
			} else {
				intel.setOutcome(SAD_raidIntel.PunExOutcome.BOMBARD_FAIL);
				status = SAD_raidIntel.RaidStageStatus.FAILURE;
				
				Misc.setFlagWithReason(market.getMemoryWithoutUpdate(), MemFlags.RECENTLY_BOMBARDED, 
			   			   			   intel.getFaction().getId(), true, 30f);
			}
		} else {
			//float str = intel.getAssembleStage().getOrigSpawnFP() * Misc.FP_TO_GROUND_RAID_STR_APPROX_MULT;
			float str = intel.getRaidFP() / intel.getNumFleets() * Misc.FP_TO_GROUND_RAID_STR_APPROX_MULT;
			
			if (fleet != null) str = MarketCMD.getRaidStr(fleet);
			//float re = MarketCMD.getRaidEffectiveness(target, str);
			
			//str = 10f;
			
			float durMult = Global.getSettings().getFloat("punitiveExpeditionDisruptDurationMult");
			boolean raidSuccess = new MarketCMD(market.getPrimaryEntity()).doIndustryRaid(intel.getFaction(), str, intel.targetIndustry, durMult);
			
			if (raidSuccess) {
				intel.setOutcome(SAD_raidIntel.PunExOutcome.SUCCESS);
			} else {
				intel.setOutcome(SAD_raidIntel.PunExOutcome.RAID_FAIL);
				status = SAD_raidIntel.RaidStageStatus.FAILURE;
				
				Misc.setFlagWithReason(market.getMemoryWithoutUpdate(), MemFlags.RECENTLY_RAIDED, 
						   			   intel.getFaction().getId(), true, 30f);
			}
		}
		
//		// so it doesn't keep trying to raid/bombard
//		if (fleet != null) {
//			fleet.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_RAIDER);
//		}
		
		// when FAILURE, gets sent by RaidIntel
		if (intel.getOutcome() != null) {
			if (status == SAD_raidIntel.RaidStageStatus.SUCCESS) {
				intel.sendOutcomeUpdate();
			} else {
				removeMilScripts();
				giveReturnOrdersToStragglers(getRoutes());
			}
		}
	}

	
	protected void autoresolve() {
		float str = WarSimScript.getFactionStrength(intel.getFaction(), target.getStarSystem());
		float enemyStr = WarSimScript.getFactionStrength(target.getFaction(), target.getStarSystem());
		
		float defensiveStr = enemyStr + WarSimScript.getStationStrength(target.getFaction(), 
							 target.getStarSystem(), target.getPrimaryEntity());
		if (defensiveStr >= str) {
			status = SAD_raidIntel.RaidStageStatus.FAILURE;
			removeMilScripts();
			giveReturnOrdersToStragglers(getRoutes());
			
			// not strictly necessary, I think, but shouldn't hurt
			// otherwise would get set in PunitiveExpeditionIntel.notifyRaidEnded()
			SAD_raidIntel intel = ((SAD_raidIntel)this.intel);
			intel.setOutcome(SAD_raidIntel.PunExOutcome.TASK_FORCE_DEFEATED);
			return;
		}
		
		Industry station = Misc.getStationIndustry(target);
		if (station != null) {
			OrbitalStation.disrupt(station);
		}
		
		performRaid(null, target);
	}
	
	
	protected void updateRoutes() {
		resetRoutes();
		
		gaveOrders = false;
		
		((SAD_raidIntel)intel).sendEnteredSystemUpdate();
		
		List<SAD_RouteManager.RouteData> routes = SAD_RouteManager.getInstance().getRoutesForSource(intel.getRouteSourceId());
		for (SAD_RouteManager.RouteData route : routes) {
			if (target.getStarSystem() != null) { // so that fleet may spawn NOT at the target
				route.addSegment(new SAD_RouteManager.RouteSegment(Math.min(5f, untilAutoresolve), target.getStarSystem().getCenter()));
			}
			route.addSegment(new SAD_RouteManager.RouteSegment(1000f, target.getPrimaryEntity()));
		}
	}
	
	
	public void showStageInfo(TooltipMakerAPI info) {
		int curr = intel.getCurrentStage();
		int index = intel.getStageIndex(this);
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		
		if (curr < index) return;
		
		if (status == SAD_raidIntel.RaidStageStatus.ONGOING && curr == index) {
			info.addPara("The expedition forces are currently in-system.", opad);
			return;
		}
		
		SAD_raidIntel intel = ((SAD_raidIntel)this.intel);
		if (intel.getOutcome() != null) {
			switch (intel.getOutcome()) {
			case BOMBARD_FAIL:
				info.addPara("The ground defenses of " + target.getName() + " were sufficient to prevent bombardment.", opad);
				break;
			case RAID_FAIL:
				info.addPara("The \"Peace and Love\" forces have been repelled by the ground defenses of " + target.getName() + ".", opad);
				break;
			case SUCCESS:
				if (intel.goal == SAD_raidManager.PunExGoal.BOMBARD) {
					if (!target.isInEconomy()) {
						info.addPara("The \"Peace and Love\" force has successfully bombarded " + target.getName() + ", destroying the colony outright.", opad);
					} else {
						info.addPara("The \"Peace and Love\" force has successfully bombarded " + target.getName() + ".", opad);
					}
				} else if (intel.getTargetIndustry() != null) {
					info.addPara("The \"Peace and Love\" force has disrupted " + 
							intel.getTargetIndustry().getCurrentName() + " operations for %s days.",
							opad, h, "" + (int)Math.round(intel.getTargetIndustry().getDisruptedDays()));
				}
				break;
			case TASK_FORCE_DEFEATED:
				info.addPara("The \"Peace and Love\" force has been defeated by the defenders of " +
								target.getName() + ".", opad);
				break;
			case COLONY_NO_LONGER_EXISTS:
				info.addPara("The \"Peace and Love\" force has been aborted.", opad);
				break;
			
			}
		} else if (status == SAD_raidIntel.RaidStageStatus.SUCCESS) {			
			info.addPara("The \"Peace and Love\" force has succeeded.", opad); // shouldn't happen?
		} else {
			info.addPara("The \"Peace and Love\" force has failed.", opad); // shouldn't happen?
		}
	}

	public boolean canRaid(CampaignFleetAPI fleet, MarketAPI market) {
		SAD_raidIntel intel = ((SAD_raidIntel)this.intel);
		if (intel.getOutcome() != null) return false;
		return market == target;
	}
	
	public String getRaidPrepText(CampaignFleetAPI fleet, SectorEntityToken from) {
		return "orbiting " + from.getName();
	}
	
	public String getRaidInSystemText(CampaignFleetAPI fleet) {
		return "traveling";
	}
	
	public String getRaidDefaultText(CampaignFleetAPI fleet) {
		return "traveling";		
	}
	
	public boolean isPlayerTargeted() {
		return playerTargeted;
	}
        
}
