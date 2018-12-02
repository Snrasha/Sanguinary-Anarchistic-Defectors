package src.data.scripts.campaign.raid.sub;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import src.data.scripts.campaign.raid.*;
import src.data.scripts.campaign.raid.SAD_RouteManager.RouteData;
import src.data.scripts.campaign.raid.SAD_RouteManager.RouteSegment;
import src.data.scripts.campaign.raid.SAD_RouteManager.SAD_OptionalFleetData;
import src.data.scripts.campaign.raid.SAD_raidIntel.RaidStageStatus;

public class SAD_AssembleStage extends SAD_BaseRaidStage {

    public static final String PREP_STAGE = "prep_stage";
    public static final String WAIT_STAGE = "wait_stage";

    protected IntervalUtil interval = new IntervalUtil(0.1f, 0.2f);

    public SectorEntityToken gatheringPoint;
    protected float spawnFP = 0;
    protected float origSpawnFP = 0;

    protected float minDays = 3f;

    
    public SAD_raidIntel raid;

    protected int currSource = 0;
    protected String prevType = null;

    public static float FP_SMALL = 20;
    public static float FP_MEDIUM = 45;
    public static float FP_LARGE = 85;


    public SAD_AssembleStage(SAD_raidIntel raid, SectorEntityToken gatheringPoint) {
        super(raid);
        this.intel = raid;
        this.gatheringPoint = gatheringPoint;
        interval.forceIntervalElapsed();
        this.raid = raid;
    }

    @Override
    public void showStageInfo(TooltipMakerAPI info) {
        int curr = intel.getCurrentStage();
        int index = intel.getStageIndex(this);

        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 3f;
        float opad = 10f;

        if (status == RaidStageStatus.FAILURE) {
            info.addPara("The \"Peace and Love\" forces have failed to successfully assemble at the rendezvous point. The raid is now over.", opad);
        } else if (curr == index) {
            info.addPara("The \"Peace and Love\" force is currently assembling in the " + gatheringPoint.getContainingLocation().getNameWithLowercaseType() + ".", opad);
        }
    }

    protected void addRoutesAsNeeded(float amount) {
        if (spawnFP <= 0) {
            return;
        }

        float days = Misc.getDays(amount);

        interval.advance(days);
        if (!interval.intervalElapsed()) {
            return;
        }

        currSource = 1;

        SAD_OptionalFleetData extra = new SAD_OptionalFleetData();
        extra.quality = 5f;
        extra.factionId = "sad";

        String sid = intel.getRouteSourceId();
        RouteData route = SAD_RouteManager.getInstance().addRoute(sid, gatheringPoint, Misc.genRandomSeed(), extra, raid, null);

        extra.fleetType = pickNextType();
        float fp = getFP(extra.fleetType);

        //extra.fp = Misc.getAdjustedFP(fp, market);
        extra.fp = fp;
        extra.strength = (float) (Global.getSector().getPlayerStats().getLevel()) / 10;
        float prepDays = 3f + 3f * (float) Math.random();
        float travelDays = RouteLocationCalculator.getTravelDays(gatheringPoint, gatheringPoint);

        route.addSegment(new RouteSegment(prepDays, gatheringPoint, PREP_STAGE));
        route.addSegment(new RouteSegment(travelDays, gatheringPoint, gatheringPoint));
        route.addSegment(new RouteSegment(1000f, gatheringPoint, WAIT_STAGE));

        maxDays = Math.max(maxDays, prepDays + travelDays);
        //maxDays = 6f;

    }

    protected String pickNextType() {
        return FleetTypes.TASK_FORCE;
    }

    protected float getFP(String type) {
        float base = 100f;
        if (spawnFP < base * 1.5f) {
            base = spawnFP;
        }
        if (base > spawnFP) {
            base = spawnFP;
        }

        spawnFP -= base;
        return base;
    }

    public boolean isSourceKnown() {
        return true;
    }

    public void setSpawnFP(float spawnFP) {
        this.spawnFP = spawnFP;
        this.origSpawnFP = spawnFP;
    }

    public float getOrigSpawnFP() {
        return origSpawnFP;
    }

    public float getSpawnFP() {
        return spawnFP;
    }

    @Override
    public void advance(float amount) {
        addRoutesAsNeeded(amount);
        minDays -= Misc.getDays(amount);
        float days = Misc.getDays(amount);

        elapsed += days;

        statusInterval.advance(days);
        if (statusInterval.intervalElapsed()) {
            updateStatus();
        }
    }

    @Override
    public void resetRoute(RouteData route) {
        CampaignFleetAPI fleet = route.getActiveFleet();
        if (fleet != null) {
            fleet.clearAssignments();
        }
        route.getSegments().clear();
        route.setCurrent(null);
    }

    @Override
    protected void updateStatus() {
        if (spawnFP > 0) {
            return;
        }
        if (minDays > 0) {
            return;
        }

        abortIfNeededBasedOnFP(true);
        updateStatusBasedOnReaching(gatheringPoint, true);
    }

    @Override
    public float getAbortFP() {
        return abortFP;
    }

    @Override
    public void setAbortFP(float successFP) {
        this.abortFP = successFP;
    }

//	protected float getTotalRouteFP() {
//		float total = 0f;
//		for (RouteData route : getRoutes()) {
//			total += route.
//		}
//	}
    @Override
    public void resetRoutes() {
        List<RouteData> routes = SAD_RouteManager.getInstance().getRoutesForSource(intel.getRouteSourceId());
        for (RouteData route : routes) {
            resetRoute(route);
        }
    }

    @Override
    public List<RouteData> getRoutes() {
        List<RouteData> routes = SAD_RouteManager.getInstance().getRoutesForSource(intel.getRouteSourceId());
        List<RouteData> result = new ArrayList<>();
        for (RouteData route : routes) {
            if (!STRAGGLER.equals(route.getCustom())) {
                result.add(route);
            }
        }
        return result;
    }

    @Override
    public void giveReturnOrdersToStragglers(List<RouteData> stragglers) {
        for (RouteData route : stragglers) {
            SectorEntityToken from1 = Global.getSector().getHyperspace().createToken(route.getInterpolatedHyperLocation());

            route.setCustom(STRAGGLER);
            resetRoute(route);

            float travelDays = RouteLocationCalculator.getTravelDays(from1, route.from);
            if (DebugFlags.RAID_DEBUG) {
                travelDays *= 0.1f;
            }

            float orbitDays = 1f + 1f * (float) Math.random();
            route.addSegment(new RouteSegment(travelDays, from1, route.from));
            route.addSegment(new RouteSegment(orbitDays, route.from));

            //route.addSegment(new RouteSegment(2f + (float) Math.random() * 1f, route.getMarket().getPrimaryEntity()));
        }
    }


    @Override
    public RaidStageStatus getStatus() {
        return status;
    }

    @Override
    public void notifyStarted() {

    }

    @Override
    protected boolean enoughMadeIt(List<RouteData> routes, List<RouteData> stragglers) {
        float madeItFP = 0;
        for (RouteData route : routes) {
            if (stragglers.contains(route)) {
                continue;
            }
            CampaignFleetAPI fleet = route.getActiveFleet();
            if (fleet != null) {
                float mult = 3f;
                if (mult < 1) {
                    mult = 1f;
                }
                madeItFP += fleet.getFleetPoints() / mult;
            } else {
                madeItFP += route.getExtra().fp;
            }
        }
        return madeItFP >= abortFP;
    }


    @Override
    protected void abortIfNeededBasedOnFP(boolean giveReturnOrders) {
        List<RouteData> routes = getRoutes();
        List<RouteData> stragglers = new ArrayList<>();

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

    @Override
    protected void updateStatusBasedOnReaching(SectorEntityToken dest, boolean giveReturnOrders) {
        updateStatusBasedOnReaching(dest, giveReturnOrders, true);
    }

    @Override
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
        }
    }

    @Override
    public float getExtraDaysUsed() {
        return Math.max(0, elapsed - maxDays);
    }

    @Override
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
                if (route.getCurrent() != null && WAIT_STAGE.equals(route.getCurrent().custom)) {
                    waiting = true;
                }
                if (!waiting) {
                    stragglers.add(route);
                }
            }
        }

        return stragglers;
    }

    @Override
    public float getElapsed() {
        return elapsed;
    }

    @Override
    public float getMaxDays() {
        return maxDays;
    }

    
    


}
