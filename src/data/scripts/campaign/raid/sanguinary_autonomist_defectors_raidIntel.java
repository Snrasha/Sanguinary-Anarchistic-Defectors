package src.data.scripts.campaign.raid;

import java.awt.Color;
import java.util.Random;
import java.util.Set;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.command.WarSimScript;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import static com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin.getDaysString;
import static com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin.getSoundColonyThreat;
import static com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin.getSoundMajorPosting;
import static com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin.getSoundStandardUpdate;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import src.data.scripts.campaign.raid.sanguinary_autonomist_defectors_RouteManager.RouteData;
import src.data.scripts.campaign.raid.sanguinary_autonomist_defectors_RouteManager.sanguinary_autonomist_defectors_OptionalFleetData;
import src.data.scripts.campaign.raid.sanguinary_autonomist_defectors_RouteManager.sanguinary_autonomist_defectors_RouteFleetSpawner;
import src.data.scripts.campaign.raid.sanguinary_autonomist_defectors_raidManager.PunExGoal;
import src.data.scripts.campaign.raid.sanguinary_autonomist_defectors_raidManager.PunExReason;
import src.data.scripts.campaign.raid.sanguinary_autonomist_defectors_raidManager.PunExType;
import src.data.utils.sanguinary_autonomist_defectors_Tags;
import src.data.scripts.campaign.raid.sub.*;

public class sanguinary_autonomist_defectors_raidIntel extends BaseIntelPlugin implements sanguinary_autonomist_defectors_RouteFleetSpawner {

    public static final Logger log = Global.getLogger(sanguinary_autonomist_defectors_raidIntel.class);

    public static final String BUTTON_AVERT = "BUTTON_CHANGE_ORDERS";

    public static enum PunExOutcome {
        TASK_FORCE_DEFEATED,
        COLONY_NO_LONGER_EXISTS,
        SUCCESS,
        BOMBARD_FAIL,
        RAID_FAIL,
        AVERTED,
    }

    public static final Object ENTERED_SYSTEM_UPDATE = new Object();
    public static final Object OUTCOME_UPDATE = new Object();

    protected sanguinary_autonomist_defectors_ActionStage action;
    public PunExGoal goal;
    protected MarketAPI target;
    protected CampaignFleetAPI from;
    protected PunExOutcome outcome;

    protected Random random = new Random();

    protected PunExReason bestReason;
    public Industry targetIndustry;
    protected FactionAPI targetFaction;

    protected int currentStage = 0;
    protected int failStage = -1;
    protected List<sanguinary_autonomist_defectors_RaidStage> stages = new ArrayList<>();

    protected String id = Misc.genUID();
    protected String sid = "raid_" + id;

    protected float extraDays = 60f;
    protected StarSystemAPI system;
    protected FactionAPI faction;
    protected float defenderStr = 0f;

    public sanguinary_autonomist_defectors_raidIntel(FactionAPI faction, CampaignFleetAPI from, MarketAPI target,
            float expeditionFP, float organizeDuration,
            PunExGoal goal, Industry targetIndustry, PunExReason bestReason) {

        this.system = from.getStarSystem();
        this.faction = faction;
 
        Global.getSector().addScript(this);
        defenderStr = WarSimScript.getEnemyStrength(getFaction(), system);
        this.goal = goal;
        this.targetIndustry = targetIndustry;
        this.bestReason = bestReason;
        this.from = from;
        this.target = target;
        targetFaction = target.getFaction();

        SectorEntityToken gather = from;//target.getPrimaryEntity();

        float orgDur = organizeDuration;
        if (DebugFlags.PUNITIVE_EXPEDITION_DEBUG) {
            orgDur = 0.5f;
        }

        addStage(new sanguinary_autonomist_defectors_OrganizeStage(this, from, orgDur));

        float successMult = 0.5f;
        sanguinary_autonomist_defectors_AssembleStage assemble = new sanguinary_autonomist_defectors_AssembleStage(this, gather);
        assemble.setSpawnFP(expeditionFP);
        assemble.setAbortFP(expeditionFP * successMult);
        addStage(assemble);

        SectorEntityToken raidJump = RouteLocationCalculator.findJumpPointToUse(getFactionForUIColors(), target.getPrimaryEntity());
        sanguinary_autonomist_defectors_TravelStage travel = new sanguinary_autonomist_defectors_TravelStage(this, gather, raidJump, false);
        travel.setAbortFP(expeditionFP * successMult);
        addStage(travel);

        action = new sanguinary_autonomist_defectors_ActionStage(this, target);
        action.setAbortFP(expeditionFP * successMult);
        addStage(action);

        addStage(new sanguinary_autonomist_defectors_ReturnStage(this));

        Global.getSector().getIntelManager().addIntel(this);
    }

    public Random getRandom() {
        return random;
    }

    public MarketAPI getTarget() {
        return target;
    }

    public FactionAPI getTargetFaction() {
        return targetFaction;
    }

    @Override
    public void reportAboutToBeDespawnedByRouteManager(RouteData route) {
    }

    @Override
    public boolean shouldRepeat(RouteData route) {
        return false;
    }

    @Override
    public boolean shouldCancelRouteAfterDelayCheck(RouteData route) {
        return false;
    }

    public sanguinary_autonomist_defectors_RaidAssignmentAI createAssignmentAI(CampaignFleetAPI fleet, RouteData route) {
        sanguinary_autonomist_defectors_RaidAssignmentAI raidAI = new sanguinary_autonomist_defectors_RaidAssignmentAI(fleet, route, action);
        return raidAI;
    }

    public void sendOutcomeUpdate() {
        sendUpdateIfPlayerHasIntel(OUTCOME_UPDATE, false);
    }

    public void sendEnteredSystemUpdate() {
        sendUpdateIfPlayerHasIntel(ENTERED_SYSTEM_UPDATE, false);
    }

    public String getName() {
        String base = Misc.ucFirst(faction.getPersonNamePrefix()) + " Expedition";
        if (isEnding()) {
            if (outcome == PunExOutcome.AVERTED) {
                return base + " - Averted";
            }
            if (isSendingUpdate() && isFailed()) {
                return base + " - Failed";
            }
            if (isSucceeded() || outcome == PunExOutcome.SUCCESS) {
                return base + " - Successful";
            }
            if (outcome == PunExOutcome.RAID_FAIL
                    || outcome == PunExOutcome.BOMBARD_FAIL
                    || outcome == PunExOutcome.COLONY_NO_LONGER_EXISTS
                    || outcome == PunExOutcome.TASK_FORCE_DEFEATED) {
                return base + " - Failed";
            }
        }
        return base;
    }

    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {

        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        float pad = 3f;
        float opad = 10f;

        float initPad = pad;
        if (mode == ListInfoMode.IN_DESC) {
            initPad = opad;
        }

        Color tc = getBulletColorForMode(mode);

        bullet(info);
        boolean isUpdate = getListInfoParam() != null;

        if (getListInfoParam() == OUTCOME_UPDATE) {
        }

        if (getListInfoParam() == ENTERED_SYSTEM_UPDATE) {
            FactionAPI other = target.getFaction();
            info.addPara("Target: %s", initPad, tc,
                    other.getBaseUIColor(), target.getName());
            initPad = 0f;
            info.addPara("Arrived in-system", tc, initPad);
//			info.addPara("" + faction.getDisplayName() + " forces arrive in-system", initPad, tc,
//					faction.getBaseUIColor(), faction.getDisplayName());
            return;
        }

        FactionAPI other = targetFaction;
        if (outcome != null) {
            if (outcome == PunExOutcome.TASK_FORCE_DEFEATED) {
                info.addPara("\"Peace and Love\" force defeated", tc, initPad);
            } else if (outcome == PunExOutcome.COLONY_NO_LONGER_EXISTS) {
                info.addPara("\"Peace and Love\" force aborted", tc, initPad);
            } else if (outcome == PunExOutcome.AVERTED) {
                info.addPara("\"Peace and Love\" planning disrupted", initPad, tc, other.getBaseUIColor(), target.getName());
            } else if (outcome == PunExOutcome.BOMBARD_FAIL) {
                info.addPara("Bombardment of %s failed", initPad, tc, other.getBaseUIColor(), target.getName());
            } else if (outcome == PunExOutcome.RAID_FAIL) {
                info.addPara("\"Peace and Love\" force of %s failed", initPad, tc, other.getBaseUIColor(), target.getName());
            } else if (outcome == PunExOutcome.SUCCESS) {
                if (goal == PunExGoal.BOMBARD) {
                    if (!target.isInEconomy()) {
                        info.addPara("%s destroyed by bombardment", initPad, tc, other.getBaseUIColor(), target.getName());
                    } else {
                        info.addPara("Bombardment of %s successful", initPad, tc, other.getBaseUIColor(), target.getName());
                    }
                } else if (targetIndustry != null && targetIndustry.getDisruptedDays() >= 2) {
                    info.addPara(targetIndustry.getCurrentName() + " disrupted for %s days",
                            initPad, tc, h, "" + (int) Math.round(targetIndustry.getDisruptedDays()));
                }
            }
            return;
        }

        info.addPara("Target: %s", initPad, tc,
                other.getBaseUIColor(), target.getName());
        initPad = 0f;

        if (goal == PunExGoal.BOMBARD) {
            String goalStr = "saturation bombardment";
            info.addPara("Goal: %s", initPad, tc, Misc.getNegativeHighlightColor(), goalStr);
        }

        float eta = getETA();
        if (eta > 1 && !isEnding()) {
            String days = getDaysString(eta);
            info.addPara("Estimated %s " + days + " until arrival",
                    initPad, tc, h, "" + (int) Math.round(eta));
            initPad = 0f;
        } else if (!isEnding() && action.getElapsed() > 0) {
            info.addPara("Currently in-system", tc, initPad);
            initPad = 0f;
        }

        unindent(info);
    }

    public sanguinary_autonomist_defectors_ActionStage getActionStage() {
        for (sanguinary_autonomist_defectors_RaidStage stage : stages) {
            if (stage instanceof sanguinary_autonomist_defectors_ActionStage) {
                return (sanguinary_autonomist_defectors_ActionStage) stage;
            }
        }
        return null;
        //return (PEActionStage) stages.get(2);
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color c = getTitleColor(mode);

        if (isPlayerTargeted() && false) {
            info.setParaSmallInsignia();
        } else {
            info.setParaFontDefault();
        }

        info.addPara(getName(), c, 0f);
        info.setParaFontDefault();
        addBulletPoints(info, mode);
    }

    public void addInitialDescSection(TooltipMakerAPI info, float initPad) {
        Color h = Misc.getHighlightColor();
        float opad = 10f;

        FactionAPI faction = getFaction();
        String is = faction.getDisplayNameIsOrAre();

        String goalDesc = "";
        String goalHL = "";
        Color goalColor = Misc.getTextColor();
        switch (goal) {
            case RAID_PRODUCTION:
                goalDesc = "disrupting the colony's " + targetIndustry.getCurrentName();
                break;
            case RAID_SPACEPORT:
                goalDesc = "raiding the colony's " + targetIndustry.getCurrentName() + " to disrupt its operations";
                break;
            case BOMBARD:
                goalDesc = "a saturation bombardment of the colony";
                goalHL = "saturation bombardment of the colony";
                goalColor = Misc.getNegativeHighlightColor();
                break;
        }

        String strDesc = getRaidStrDesc();

        if (outcome == null) {
            LabelAPI label = info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " " + is
                    + " targeting %s with a " + strDesc + " \"Peace and Love\" force. "
                    + "Its likely goal is " + goalDesc + ".",
                    initPad, faction.getBaseUIColor(), target.getName());
            label.setHighlight(faction.getDisplayNameWithArticleWithoutArticle(), target.getName(), strDesc, goalHL);
            label.setHighlightColors(faction.getBaseUIColor(), targetFaction.getBaseUIColor(), h, goalColor);
        } else {
            LabelAPI label = info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " " + is
                    + " targeting %s with an \"Peace and Love\" force. "
                    + "Its likely goal is " + goalDesc + ".",
                    initPad, faction.getBaseUIColor(), target.getName());
            label.setHighlight(faction.getDisplayNameWithArticleWithoutArticle(), target.getName(), goalHL);
            label.setHighlightColors(faction.getBaseUIColor(), targetFaction.getBaseUIColor(), goalColor);
        }
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {

        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 3f;
        float opad = 10f;

        info.addImage(getFactionForUIColors().getLogo(), width, 128, opad);

        FactionAPI faction = getFaction();
        String has = faction.getDisplayNameHasOrHave();
        String is = faction.getDisplayNameIsOrAre();

        addInitialDescSection(info, opad);

        info.addPara("Goal of expedition is to make sure your colony dies, preferably including you on the list of casualties. Peace and Love are concepts exclusively claimed and rightfully owned by " + faction.getDisplayNameWithArticle() + ".", opad);

        if (outcome == null) {
            addStandardStrengthComparisons(info, target, targetFaction, goal != PunExGoal.BOMBARD, goal == PunExGoal.BOMBARD,
                    "expedition", "expedition's");
        }

        info.addSectionHeading("Status",
                faction.getBaseUIColor(), faction.getDarkUIColor(), Alignment.MID, opad);

        for (sanguinary_autonomist_defectors_RaidStage stage : stages) {
            stage.showStageInfo(info);
            if (getStageIndex(stage) == failStage) {
                break;
            }
        }

        if (getCurrentStage() == 0 && !isFailed()) {
            FactionAPI pf = Global.getSector().getPlayerFaction();
            ButtonAPI button = info.addButton("Avert", BUTTON_AVERT,
                    pf.getBaseUIColor(), pf.getDarkUIColor(),
                    (int) (width), 20f, opad * 2f);
            button.setShortcut(Keyboard.KEY_T, true);
        }

        if (!from.getFaction().isHostileTo(targetFaction) && !isFailed()) {
//			LabelAPI label = info.addPara("This operation is being carried " +
//					"without an open declaration of war. Fighting the " +
//					"expeditionary force will not result in " + faction.getDisplayNameWithArticle() + 
//					" immediately becoming hostile, unless the relationship is already strained.", Misc.getGrayColor(), 
//					opad);
            LabelAPI label = info.addPara("This operation is being carried "
                    + "without an open declaration of war. Fighting the "
                    + "\"Peace and Love\" force should not result in " + faction.getDisplayNameWithArticle()
                    + " immediately becoming hostile. But they are crazy, so nobody like them.", Misc.getGrayColor(),
                    opad);
            label.setHighlight(faction.getDisplayNameWithArticleWithoutArticle());
            label.setHighlightColors(faction.getBaseUIColor());
        }
    }

    @Override
    public void sendUpdateIfPlayerHasIntel(Object listInfoParam, boolean onlyIfImportant, boolean sendIfHidden) {

        if (listInfoParam == UPDATE_RETURNING) {
            // we're using sendOutcomeUpdate() to send an end-of-event update instead
            return;
        }

        super.sendUpdateIfPlayerHasIntel(listInfoParam, onlyIfImportant, sendIfHidden);
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {

        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_MILITARY);
        tags.add(Tags.INTEL_COLONIES);
        tags.add(getFaction().getId());
        return tags;
    }

    public void notifyRaidEnded(sanguinary_autonomist_defectors_raidIntel raid, RaidStageStatus status) {
        if (outcome == null && failStage >= 0) {
            if (!target.isInEconomy() || !target.isPlayerOwned()) {
                outcome = PunExOutcome.COLONY_NO_LONGER_EXISTS;
            } else {
                outcome = PunExOutcome.TASK_FORCE_DEFEATED;
            }
        }

        sanguinary_autonomist_defectors_raidManager.PunExData data = sanguinary_autonomist_defectors_raidManager.getInstance().dataPun;
        if (data != null) {
            if (outcome == PunExOutcome.SUCCESS) {
                data.numSuccesses++;
            }
        }
    }

    public String getIcon() {
        return faction.getCrest();
    }

    public PunExGoal getGoal() {
        return goal;
    }

    public Industry getTargetIndustry() {
        return targetIndustry;
    }

    public PunExOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(PunExOutcome outcome) {
        this.outcome = outcome;
    }

    @Override
    public CampaignFleetAPI spawnFleet(RouteData route) {

        CampaignFleetAPI fleet = createFleet(sanguinary_autonomist_defectors_Tags.sanguinary_autonomist_defectors_FACTION, route, null, random);

        if (fleet == null || fleet.isEmpty()) {
            return null;
        }

        log.info("Fleet in " + from.getContainingLocation().getNameWithLowercaseType());
        from.getContainingLocation().addEntity(fleet);
        fleet.setFacing((float) Math.random() * 360f);
        // this will get overridden by the patrol assignment AI, depending on route-time elapsed etc
        fleet.setLocation(from.getLocation().x, from.getLocation().x);

        fleet.addScript(createAssignmentAI(fleet, route));
        log.info("Spawn fleet worked");
        return fleet;
    }

    public CampaignFleetAPI createFleet(String factionId, RouteData route, Vector2f locInHyper, Random random) {
        if (random == null) {
            random = this.random;
        }

        sanguinary_autonomist_defectors_OptionalFleetData extra = route.getExtra();

        float combat = extra.fp;
        float tanker = extra.fp * (0.1f + random.nextFloat() * 0.05f);
        float transport = extra.fp * (0.1f + random.nextFloat() * 0.05f);
        float freighter = 0f;

        if (goal == PunExGoal.BOMBARD) {
            tanker += transport;
        } else {
            transport += tanker / 2f;
            tanker *= 0.5f;
        }

        combat -= tanker;
        combat -= transport;

        combat *= 8f; // 8 is fp cost of remnant frigate

        FleetParamsV3 params = new FleetParamsV3(
                locInHyper,
                "sad",
                1f,
                extra.fleetType,
                combat, // combatPts
                freighter, // freighterPts 
                tanker, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                0f // qualityMod
        );

        /*
		FleetParamsV3 params = new FleetParamsV3(
				null, 
				locInHyper,
				factionId,
				route == null ? null : route.getQualityOverride(),
				extra.fleetType,
				combat, // combatPts
				freighter, // freighterPts 
				tanker, // tankerPts
				0f, // transportPts
				0f, // linerPts
				0f, // utilityPts
				0f // qualityMod, won't get used since routes mostly have quality override set
				);*/
        //params.ignoreMarketFleetSizeMult = true; // already accounted for in extra.fp
        params.timestamp = route.getTimestamp();

        params.random = random;
        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);

        if (fleet == null || fleet.isEmpty()) {
            log.info("Fleet null");
            return null;
        }
        log.info("Fleet maded: " + fleet.getFullName());

        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_WAR_FLEET, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_RAIDER, true);

        if (fleet.getFaction().getCustomBoolean(Factions.CUSTOM_PIRATE_BEHAVIOR)) {
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
        }

        String postId = Ranks.POST_PATROL_COMMANDER;
        String rankId = Ranks.SPACE_COMMANDER;

        fleet.getCommander().setPostId(postId);
        fleet.getCommander().setRankId(rankId);

        Misc.makeLowRepImpact(fleet, "punex");
        Misc.makeHostile(fleet);

        return fleet;
    }

    public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
        if (buttonId == BUTTON_AVERT) {
            ui.showDialog(null, new sanguinary_autonomist_defectors_AvertInteractionDialogPluginImpl(this, ui));
        }
    }

    public PunExReason getBestReason() {
        return bestReason;
    }

    public boolean isTerritorial() {
        return bestReason != null && bestReason.type == PunExType.TERRITORIAL;
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        if (target != null && target.isInEconomy() && target.getPrimaryEntity() != null) {
            return target.getPrimaryEntity();
        }
        return super.getMapLocation(map);
    }

    public static Object UPDATE_FAILED = new Object();
    public static Object UPDATE_RETURNING = new Object();

  

    public static enum RaidStageStatus {
        ONGOING,
        SUCCESS,
        FAILURE,
    }

    public static interface sanguinary_autonomist_defectors_RaidStage {

        RaidStageStatus getStatus();

        void advance(float amount);

        void notifyStarted();

        float getExtraDaysUsed();

        void showStageInfo(TooltipMakerAPI info);

        float getElapsed();

        float getMaxDays();
    }

    public StarSystemAPI getSystem() {
        return system;
    }

    public int getCurrentStage() {
        return currentStage;
    }

    public int getStageIndex(sanguinary_autonomist_defectors_RaidStage stage) {
        return stages.indexOf(stage);
    }

    public int getFailStage() {
        return failStage;
    }

    public sanguinary_autonomist_defectors_OrganizeStage getOrganizeStage() {
        for (sanguinary_autonomist_defectors_RaidStage stage : stages) {
            if (stage instanceof sanguinary_autonomist_defectors_OrganizeStage) {
                return (sanguinary_autonomist_defectors_OrganizeStage) stage;
            }
        }
        return null;
    }

    public sanguinary_autonomist_defectors_AssembleStage getAssembleStage() {
        for (sanguinary_autonomist_defectors_RaidStage stage : stages) {
            if (stage instanceof sanguinary_autonomist_defectors_AssembleStage) {
                return (sanguinary_autonomist_defectors_AssembleStage) stage;
            }
        }
        return null;
        //return (AssembleStage) stages.get(0);
    }

    public void addStage(sanguinary_autonomist_defectors_RaidStage stage) {
        stages.add(stage);
    }

    public String getRouteSourceId() {
        return sid;
    }

    public float getExtraDays() {
        return extraDays;
    }

    public void setExtraDays(float extraDays) {
        this.extraDays = extraDays;
    }

    @Override
    public boolean canMakeVisibleToPlayer(boolean playerInRelayRange) {
        return super.canMakeVisibleToPlayer(playerInRelayRange);
    }

    public boolean shouldSendUpdate() {
        if (DebugFlags.SEND_UPDATES_WHEN_NO_COMM || Global.getSector().getIntelManager().isPlayerInRangeOfCommRelay()) {
            return true;
        }
        if (system != null && system == Global.getSector().getCurrentLocation()) {
            return true;
        }

        return isPlayerTargeted();
    }

    public boolean isPlayerTargeted() {
        sanguinary_autonomist_defectors_ActionStage action = getActionStage();
        if (action != null && action.isPlayerTargeted()) {
            return true;
        }
        return false;
    }

    public String getCommMessageSound() {
        if (isPlayerTargeted() && !isSendingUpdate()) {
            return getSoundColonyThreat();
        }

        if (isSendingUpdate()) {
            return getSoundStandardUpdate();
        }
        return getSoundMajorPosting();
    }

    protected void advanceImpl(float amount) {
        super.advanceImpl(amount);

        if (currentStage >= stages.size()) {
            endAfterDelay();
            if (shouldSendUpdate()) {
                sendUpdateIfPlayerHasIntel(UPDATE_RETURNING, false);
            }
            return;
        }

        sanguinary_autonomist_defectors_RaidStage stage = stages.get(currentStage);

        stage.advance(amount);

        RaidStageStatus status = stage.getStatus();
        if (status == RaidStageStatus.SUCCESS) {
            currentStage++;
            setExtraDays(Math.max(0, getExtraDays() - stage.getExtraDaysUsed()));
            if (currentStage < stages.size()) {
                stages.get(currentStage).notifyStarted();
            }
            return;
        } else if (status == RaidStageStatus.FAILURE) {
            failedAtStage(stage);
            failStage = currentStage;
            endAfterDelay();
            if (shouldSendUpdate()) {
                sendUpdateIfPlayerHasIntel(UPDATE_FAILED, false);
            }
        }
    }

    public void forceFail(boolean withUpdate) {
        int index = currentStage;
        if (index >= stages.size()) {
            index = stages.size() - 1;
        }
        failedAtStage(stages.get(index));
        failStage = currentStage;
        endAfterDelay();
        if (withUpdate && shouldSendUpdate()) {
            sendUpdateIfPlayerHasIntel(UPDATE_FAILED, false);
        }
    }

    protected void failedAtStage(sanguinary_autonomist_defectors_RaidStage stage) {

    }

    protected void notifyEnded() {
        super.notifyEnded();
        Global.getSector().removeScript(this);
    }

    protected void notifyEnding() {
        super.notifyEnding();

        if (this != null) {
            RaidStageStatus status = RaidStageStatus.SUCCESS;
            if (failStage >= 0) {
                status = RaidStageStatus.FAILURE;
            }
            this.notifyRaidEnded(this, status);
        }
    }

    public float getETA() {
        int curr = getCurrentStage();
        float eta = 0f;
        for (sanguinary_autonomist_defectors_RaidStage stage : stages) {
            if (stage instanceof sanguinary_autonomist_defectors_ActionStage) {
                break;
            }
            //RouteLocationCalculator.getTravelDays(((TravelStage)stage).from, ((TravelStage)stage).to)
            int index = getStageIndex(stage);
            if (index < curr) {
                continue;
            }
            if (stage instanceof sanguinary_autonomist_defectors_OrganizeStage) {
                eta += Math.max(0f, stage.getMaxDays() - stage.getElapsed());
            } else if (stage instanceof sanguinary_autonomist_defectors_AssembleStage) {
                eta += Math.max(0f, 10f - stage.getElapsed());
            } else if (stage instanceof sanguinary_autonomist_defectors_TravelStage) {
                float travelDays = RouteLocationCalculator.getTravelDays(getAssembleStage().gatheringPoint, system.getHyperspaceAnchor());
                eta += Math.max(0f, travelDays - stage.getElapsed());
            }
        }
        return eta;
    }

    public String getSortString() {
        return "Raid";
    }

    public boolean isFailed() {
        return failStage >= 0;
    }

    public boolean isSucceeded() {
        for (sanguinary_autonomist_defectors_RaidStage stage : stages) {
            if (stage instanceof sanguinary_autonomist_defectors_ActionStage && stage.getStatus() == RaidStageStatus.SUCCESS) {
                return true;
            }
        }
        return false;
    }

    public FactionAPI getFactionForUIColors() {
        return getFaction();
    }

    public FactionAPI getFaction() {
        return faction;
    }

    public String getSmallDescriptionTitle() {
        return getName();
    }

    public List<IntelInfoPlugin.ArrowData> getArrowData(SectorMapAPI map) {
        sanguinary_autonomist_defectors_AssembleStage as = getAssembleStage();
        if (as == null || !as.isSourceKnown()) {
            return null;
        }

        SectorEntityToken from = as.gatheringPoint;
        if (system == null || system == from.getContainingLocation()) {
            return null;
        }

        List<IntelInfoPlugin.ArrowData> result = new ArrayList<IntelInfoPlugin.ArrowData>();

        SectorEntityToken entityFrom = from;
      /*  if (map != null && this instanceof IntelInfoPlugin && delegate != this) {
            SectorEntityToken iconEntity = map.getIntelIconEntity((IntelInfoPlugin) delegate);
            if (iconEntity != null) {
                entityFrom = iconEntity;
            }
        }*/

        IntelInfoPlugin.ArrowData arrow = new IntelInfoPlugin.ArrowData(entityFrom, system.getCenter());
        arrow.color = getFactionForUIColors().getBaseUIColor();
        arrow.width = 20f;
        result.add(arrow);

        return result;
    }

    protected float getRaidFPAdjusted() {
        float raidFP = getRaidFP();
        return raidFP * 3f;
    }

    public float getRaidFP() {
        sanguinary_autonomist_defectors_AssembleStage as = getAssembleStage();
        float raidStr = 0f;
        for (RouteData route : as.getRoutes()) {
            CampaignFleetAPI fleet = route.getActiveFleet();
            if (fleet != null) {
                float mult = 3f;
                if (mult < 1) {
                    mult = 1f;
                }
                raidStr += fleet.getFleetPoints() / mult;
            } else {
                raidStr += route.getExtra().fp;
            }
        }
        if (raidStr <= 0 || as.getSpawnFP() > 0) {
            raidStr = Math.max(as.getOrigSpawnFP(), raidStr);
        }
        float raidFP = raidStr;
        return raidFP;
    }

    public float getNumFleets() {
        sanguinary_autonomist_defectors_AssembleStage as = getAssembleStage();
        float num = as.getRoutes().size();
        if (as.getSpawnFP() > 0) {
            num = Math.max(num, as.getOrigSpawnFP() / 100);
        }
        if (num < 1) {
            num = 1;
        }
        return num;
    }

    public float getRaidStr() {
        float raidFP = getRaidFP();

        return raidFP * 3f;
    }

    protected String getRaidStrDesc() {
        return Misc.getStrengthDesc(getRaidStr());
    }

    public void addStandardStrengthComparisons(TooltipMakerAPI info,
            MarketAPI target, FactionAPI targetFaction,
            boolean withGround, boolean withBombard,
            String raid, String raids) {
        Color h = Misc.getHighlightColor();
        float opad = 10f;

        float raidFP = getRaidFPAdjusted() / getNumFleets();
        float raidStr = getRaidStr();

        //float defenderStr = WarSimScript.getEnemyStrength(getFaction(), system);
        float defenderStr = WarSimScript.getFactionStrength(targetFaction, system);
        float defensiveStr = defenderStr + WarSimScript.getStationStrength(targetFaction, system, target.getPrimaryEntity());

        float assumedRaidGroundStr = raidFP * Misc.FP_TO_GROUND_RAID_STR_APPROX_MULT;
        float re = MarketCMD.getRaidEffectiveness(target, assumedRaidGroundStr);

        String spaceStr = "";
        String groundStr = "";
        String outcomeDesc = null;

        if (raidStr < defensiveStr * 0.75f) {
            spaceStr = "outmatched";
            if (outcomeDesc == null) {
                outcomeDesc = "The " + raid + " is likely to be defeated in orbit";
            }
        } else if (raidStr < defensiveStr * 1.25f) {
            spaceStr = "evenly matched";
            if (outcomeDesc == null) {
                outcomeDesc = "The " + raids + " outcome is uncertain";
            }
        } else {
            spaceStr = "superior";
            if (!withGround && !withBombard) {
                if (outcomeDesc == null) {
                    outcomeDesc = "The " + raid + " is likely to be successful";
                }
            }
        }

        if (withGround) {
            if (re < 0.33f) {
                groundStr = "outmatched";
                if (outcomeDesc == null) {
                    outcomeDesc = "The " + raid + " is likely to be largely repelled by the ground defences";
                }
            } else if (re < 0.66f) {
                groundStr = "evenly matched";
                if (outcomeDesc == null) {
                    outcomeDesc = "The " + raids + " outcome is uncertain";
                }
            } else {
                groundStr = "superior";
                if (outcomeDesc == null) {
                    outcomeDesc = "The " + raid + " is likely to be successful";
                }
            }
            //info.addPara("Compared to the defenses of " + target.getName() + ", the " + raids + " space forces are %s " +
            info.addPara("Compared to the defenses, the " + raids + " space forces are %s "
                    + "and its ground forces are %s."
                    + " " + outcomeDesc + ".", opad, h, spaceStr, groundStr);
        } else if (withBombard) {
            float required = MarketCMD.getBombardmentCost(target, null);
            float available = raidFP * Misc.FP_TO_BOMBARD_COST_APPROX_MULT;

            if (required * .67 > available) {
                groundStr = "outmatched";
                if (outcomeDesc == null) {
                    outcomeDesc = "The bombardment is likely to be countered by the ground defences";
                }
            } else if (required * 1.33f > available) {
                groundStr = "evenly matched";
                if (outcomeDesc == null) {
                    outcomeDesc = "The bombardment's outcome is uncertain";
                }
            } else {
                groundStr = "superior";
                if (outcomeDesc == null) {
                    outcomeDesc = "The bombardment is likely to be successful";
                }
            }
            //info.addPara("Compared to the defenses of " + target.getName() + ", the " + raids + " space forces are %s " +
            info.addPara("Compared to the defenses, the " + raids + " space forces are %s. "
                    + "" + outcomeDesc + ".", opad, h, spaceStr, groundStr);

        } else {
            info.addPara("Compared to the defenses of " + target.getName() + ", "
                    + "the " + raids + " space forces are %s."
                    + " " + outcomeDesc + ".", opad, h, spaceStr, groundStr);
        }
    }

    public IntelInfoPlugin.IntelSortTier getSortTier() {
        if (isPlayerTargeted() && false) {
            return IntelInfoPlugin.IntelSortTier.TIER_2;
        }
        return super.getSortTier();
    }
}
