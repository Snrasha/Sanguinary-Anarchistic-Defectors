package src.data.scripts.campaign.raid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import static com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.convertOrbitWithSpin;
import static com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.pickCommonLocation;
import static com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.setEntityLocation;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lazywizard.lazylib.MathUtils;
import static src.data.scripts.campaign.sanguinary_autonomist_defectors_ThemeGenerator.addSADStationInteractionConfig;
import src.data.utils.sanguinary_autonomist_defectors_Tags;
import org.apache.log4j.Logger;
import src.data.scripts.campaign.sanguinary_autonomist_defectors_StationFleetManager;

public class sanguinary_autonomist_defectors_raidManager implements EveryFrameScript {

    public static final Logger log = Global.getLogger(sanguinary_autonomist_defectors_raidManager.class);

    public static final String KEY = "$sanguinary_autonomist_defectors_raidManager";

    public static sanguinary_autonomist_defectors_raidManager getInstance() {
        Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
        return (sanguinary_autonomist_defectors_raidManager) test;
    }

    public static int MAX_CONCURRENT = 2;

    // if more factions send non-territorial expeditions, longer timeout
    public static float TARGET_NUMBER_FOR_FREQUENCY = 5f;

    public static final float MAX_THRESHOLD = 1000f;

    public static enum PunExType {
        ANTI_COMPETITION,
        ANTI_FREE_PORT,
        TERRITORIAL,
    }

    public static enum PunExGoal {
        RAID_PRODUCTION,
        RAID_SPACEPORT,
        BOMBARD,
        //EVACUATE,
    }

    public static class PunExReason {

        public PunExType type;
        public String commodityId;
        public String marketId;
        public float weight;

        public PunExReason(PunExType type) {
            this.type = type;
        }
    }

    public static class PunExData {

        public FactionAPI faction;
        public IntervalUtil tracker = new IntervalUtil(10f, 20f);
        public float anger = 0f;
        public float threshold = 100f;
        public float timeout = 0f;

        public sanguinary_autonomist_defectors_raidIntel intel;
        public Random random = new Random();

        public int numSuccesses = 0;
        public int numAttempts = 0;
    }
    protected FactionAPI dataSAD;
    protected PunExData dataPun;

    public sanguinary_autonomist_defectors_raidManager() {
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
        dataSAD = Global.getSector().getFaction(sanguinary_autonomist_defectors_Tags.sanguinary_autonomist_defectors_FACTION);
        PunExData curr = new PunExData();
        dataPun = curr;
        dataPun.anger = 500;

    }

    protected Object readResolve() {
        return this;
    }
    protected Random random = MathUtils.getRandom();

    protected StarSystemAPI pickSADSystem(boolean logEnable) {
        WeightedRandomPicker<StarSystemAPI> far = new WeightedRandomPicker<>(random);
        WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<>(random);

        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            float days = Global.getSector().getClock().getElapsedDaysSince(system.getLastPlayerVisitTimestamp());
            if (days < 45f) {
                continue;
            }
            if (logEnable) {
                for (String str : system.getTags()) {
                    log.info(system.getNameWithLowercaseType() + " has TAGs: " + str);
                }
            }

            float weight = 0f;
            weight = 1000f;
            if (!system.hasTag(sanguinary_autonomist_defectors_Tags.THEME_SAD) && !system.hasTag(sanguinary_autonomist_defectors_Tags.THEME_sanguinary_autonomist_defectors_MAIN)) {
                continue;
            }
            if (logEnable) {
                log.info("    Picker add " + system.getNameWithLowercaseType());
            }

            float dist = system.getLocation().length();

            float distMult = 1f;

            if (dist > 36000f) {
                far.add(system, weight * distMult);
            } else {
                picker.add(system, weight * distMult);
            }
        }

        if (picker.isEmpty()) {
            picker.addAll(far);
        }

        return picker.pick();
    }

    public List<CampaignFleetAPI> addBattlestations(StarSystemAPI system, float chanceToAddAny, int min, int max, WeightedRandomPicker<String> stationTypes) {
        List<CampaignFleetAPI> result = new ArrayList<>();
        if (random.nextFloat() >= chanceToAddAny) {
            return result;
        }

        int num = min + random.nextInt(max - min + 1);
        log.info("    Adding " + num + " battlestations");
        for (int i = 0; i < num; i++) {

            BaseThemeGenerator.EntityLocation loc = pickCommonLocation(random, system, 200f, true, null);

            String type = stationTypes.pick();
            if (loc != null) {

                CampaignFleetAPI fleet = FleetFactoryV3.createEmptyFleet("sad", FleetTypes.BATTLESTATION, null);

                FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, type);
                fleet.getFleetData().addFleetMember(member);

                //fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
                fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
                fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_JUMP, true);
                fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE, true);

                fleet.setStationMode(true);
                fleet.addTag(sanguinary_autonomist_defectors_Tags.sanguinary_autonomist_defectors_STATION);

                addSADStationInteractionConfig(fleet,2);

                system.addEntity(fleet);

                //fleet.setTransponderOn(true);
                fleet.clearAbilities();
                fleet.addAbility(Abilities.TRANSPONDER);
                fleet.getAbility(Abilities.TRANSPONDER).activate();
                fleet.getDetectedRangeMod().modifyFlat("gen", 1000f);

                fleet.setAI(null);

                setEntityLocation(fleet, loc, null);
                convertOrbitWithSpin(fleet, 5f);

                boolean damaged = type.toLowerCase().contains("damaged");
                float mult = 25f;
                int level = 20;
                if (damaged) {
                    mult = 10f;
                    level = 10;
                    fleet.getMemoryWithoutUpdate().set("$damagedStation", true);
                } //else {
                PersonAPI commander = OfficerManagerEvent.createOfficer(
                        Global.getSector().getFaction("sad"), level, true);
                if (!damaged) {
                    commander.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 3);
                }
                FleetFactoryV3.addCommanderSkills(commander, fleet, random);
                fleet.setCommander(commander);
                fleet.getFlagship().setCaptain(commander);
                //}

                member.getRepairTracker().setCR(member.getRepairTracker().getMaxCR());
                result.add(fleet);

            }
        }
        for (CampaignFleetAPI station : result) {
            int maxFleets = 2 + random.nextInt(3);
            sanguinary_autonomist_defectors_StationFleetManager activeFleets = new sanguinary_autonomist_defectors_StationFleetManager(
                    station, 1f, 0, maxFleets, 20f, 6, 12);
            system.addScript(activeFleets);
        }
        return result;
    }

    public CampaignFleetAPI getStation() {
        StarSystemAPI system = pickSADSystem(true);

        if (system == null) {
            log.info("null has be choice ");

            return null;
        }
        log.info(system.getNameWithLowercaseType() + " has be choice ");

        List<CampaignFleetAPI> fleets = system.getFleets();
        log.info("Fleet size:" + fleets.size());

        for (CampaignFleetAPI fleet : fleets) {
            for (String str : fleet.getTags()) {
                log.info(fleet.getName() + " has TAGs: " + str);
            }

            if (fleet.hasTag(sanguinary_autonomist_defectors_Tags.sanguinary_autonomist_defectors_STATION)) {
                log.info("Found sad station to " + fleet.getContainingLocation().getNameWithLowercaseType());
                return fleet;
            }
        }

        fleets = addBattlestations(system, 1f, 1, 1, createStringPicker("sanguinary_autonomist_defectors_MotherShip_Standard", 10f));
        for (CampaignFleetAPI fleet : fleets) {
            log.info("Found a build sad station to " + fleet.getContainingLocation().getNameWithLowercaseType());
            return fleet;

        }

        return null;

    }

    public WeightedRandomPicker<String> createStringPicker(Object... params) {
        return BaseThemeGenerator.createStringPicker(random, params);
    }

    float compt = 5f;

    @Override
    public void advance(float amount) {

        float days = Misc.getDays(amount);

        if (this.dataPun.intel != null) {
            if (this.dataPun.intel.isEnded()) {
                this.dataPun.timeout = 100f * this.dataPun.random.nextFloat();

                if (this.dataPun.intel instanceof sanguinary_autonomist_defectors_raidIntel) {
                    sanguinary_autonomist_defectors_raidIntel intel =this.dataPun.intel;
                    if (!intel.isTerritorial()) {
                        this.dataPun.timeout += getExtraTimeout(this.dataPun);
                    }
                }

                this.dataPun.intel = null;
            }
        } else {
            compt -= days;
            if (compt < 0) {
                compt = 5;

                CampaignFleetAPI sta = getStation();
                if (sta != null) {
                    log.info("Stations get: " + sta.getFullName());
                    log.info("Stations get: " + sta.getId());
                } else {
                    log.info("Stations get: null");
                }
            }
            this.dataPun.timeout -= days;
            if (this.dataPun.timeout <= 0) {
                this.dataPun.timeout = 0;
            }
        }

        this.dataPun.tracker.advance(days);
        //System.out.println(curr.tracker.getElapsed());
        if (this.dataPun.tracker.intervalElapsed()
                && this.dataPun.intel == null
                && this.dataPun.timeout <= 0) {
            checkExpedition(this.dataPun);

        }
    }

    public float getExtraTimeout(PunExData d) {
        float total = 0f;
        total += 3;

        return Math.min(10f, Math.max(0, total - TARGET_NUMBER_FOR_FREQUENCY)) * (20f + 20f * d.random.nextFloat());
    }

    public int getOngoing() {
        int ongoing = 0;
        PunExData d = this.dataPun;
        if (d.intel != null) {
            ongoing++;
        }

        //ongoing = 0;
        return ongoing;
    }

    protected void checkExpedition(PunExData curr) {

        List<PunExReason> reasons = getExpeditionReasons(curr);
        float total = 0f;
        for (PunExReason reason : reasons) {
            total += reason.weight*100;
        }

        curr.anger += total * (0.25f + curr.random.nextFloat() * 0.75f);
        if (curr.anger >= curr.threshold) {
            if (getOngoing() >= MAX_CONCURRENT) {
                curr.anger = 0;
            } else {
                createExpedition(curr);
            }
        }
    }

    public static float COMPETITION_PRODUCTION_MULT = 5f;
    public static float ILLEGAL_GOODS_MULT = 1f;
    public static float FREE_PORT_SIZE_MULT = 1f;
    public static float TERRITORIAL_ANGER = 3000f;

    public List<PunExReason> getExpeditionReasons(PunExData curr) {
        List<PunExReason> result = new ArrayList<>();

        for (MarketAPI market : Global.getSector().getEconomy().getMarketsInGroup(null)) {
            if (!market.isPlayerOwned()) {
                continue;
            }
            if (!market.isFreePort()) {
                continue;
            }

            if (market.isFreePort()) {
                PunExReason reason = new PunExReason(PunExType.ANTI_FREE_PORT);
                reason.weight = Math.max(1, market.getSize() - 2) * FREE_PORT_SIZE_MULT;
                reason.marketId = market.getId();
                result.add(reason);
            }
        }

        if (true) {
            int maxSize = MarketCMD.getBombardDestroyThreshold();
            for (MarketAPI market : Global.getSector().getEconomy().getMarketsInGroup(null)) {
                if (!market.isPlayerOwned()) {
                    continue;
                }

                boolean destroy = market.getSize() <= maxSize;
                if (!destroy) {
                    continue;
                }

                FactionAPI claimedBy = Misc.getClaimingFaction(market.getPrimaryEntity());
                if (claimedBy != curr.faction) {
                    continue;
                }

                PunExReason reason = new PunExReason(PunExType.TERRITORIAL);
                reason.weight = TERRITORIAL_ANGER;
                reason.marketId = market.getId();
                result.add(reason);
            }
        }

        return result;
    }

    protected void createExpedition(PunExData curr) {

        List<PunExReason> reasons = getExpeditionReasons(curr);
        if (reasons.isEmpty()) {
            return;
        }

        //for (PunExReason reason : reasons) {
        MarketAPI target = null;
        float max = 0f;
        //WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<MarketAPI>(curr.random);
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (!market.isPlayerOwned()) {
                continue;
            }

            float weight = 0f;
            weight += market.getSize();
            weight += 1000f + market.getDaysInExistence();

            if (weight > max) {
                target = market;
                max = weight;
            }
        }

        if (target == null || max <= 0) {
            return;
        }

        CampaignFleetAPI from = this.getStation();
        if (from != null) {
            log.info("Stations get: " + from.getFullName());
        } else {
            log.info("Stations get: null");
               return;
        }
        Collections.sort(reasons, new Comparator<PunExReason>() {
            @Override
            public int compare(PunExReason o1, PunExReason o2) {
                return (int) Math.signum(o2.weight - o1.weight);
            }
        });
        PunExReason bestReason = reasons.get(0);

        PunExGoal goal;
        Industry industry = null;
        if (bestReason.type == PunExType.ANTI_FREE_PORT) {
            goal = PunExGoal.RAID_SPACEPORT;
            if (curr.numSuccesses >= 2) {
                goal = PunExGoal.BOMBARD;
            }
        } else if (bestReason.type == PunExType.TERRITORIAL) {
            goal = PunExGoal.BOMBARD;
        } else {
            goal = PunExGoal.RAID_PRODUCTION;
            if (bestReason.commodityId == null || curr.numSuccesses >= 1) {
                goal = PunExGoal.RAID_SPACEPORT;
            }
            if (curr.numSuccesses >= 2) {
                goal = PunExGoal.BOMBARD;
            }
        }

        //goal = PunExGoal.BOMBARD;
        if (goal == PunExGoal.RAID_SPACEPORT) {
            for (Industry temp : target.getIndustries()) {
                if (temp.getSpec().hasTag(Industries.TAG_SPACEPORT)) {
                    industry = temp;
                    break;
                }
            }
            if (industry == null) {
                return;
            }
        } else if (goal == PunExGoal.RAID_PRODUCTION && bestReason.commodityId != null) {
            max = 0;
            for (Industry temp : target.getIndustries()) {
                int prod = temp.getSupply(bestReason.commodityId).getQuantity().getModifiedInt();
                if (prod > max) {
                    max = prod;
                    industry = temp;
                }
            }
            if (industry == null) {
                return;
            }
        }

        //float fp = from.getSize() * 20 + threshold * 0.5f;
        float fp = 50 + curr.threshold * 0.5f;
        //fp = 500;
        fp *= 5f;

        float totalAttempts = 0f;
        totalAttempts += this.dataPun.numAttempts;

        float extraMult = 0f;
        if (totalAttempts <= 2) {
            extraMult = 0f;
        } else if (totalAttempts <= 4) {
            extraMult = 1f;
        } else if (totalAttempts <= 7) {
            extraMult = 2f;
        } else if (totalAttempts <= 10) {
            extraMult = 3f;
        } else {
            extraMult = 4f;
        }

        float orgDur = 20f + extraMult * 10f + (10f + extraMult * 5f) * (float) Math.random();

 
        curr.intel = new sanguinary_autonomist_defectors_raidIntel(this.dataSAD, from, target, fp, orgDur, goal, industry, bestReason);
        if (curr.intel.isDone()) {
            curr.intel = null;
            return;
        }

        curr.numAttempts++;
        curr.anger = 0f;
        curr.threshold *= 2f;
        if (curr.threshold > MAX_THRESHOLD) {
            curr.threshold = MAX_THRESHOLD;
        }
    }

    @Override
    public boolean isDone() {
        return false;
    }

    public boolean runWhilePaused() {
        return false;
    }

}
