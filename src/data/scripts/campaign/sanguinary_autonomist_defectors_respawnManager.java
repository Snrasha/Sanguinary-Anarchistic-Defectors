package src.data.scripts.campaign;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import static com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.convertOrbitWithSpin;
import static com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.pickCommonLocation;
import static com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.setEntityLocation;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lazywizard.lazylib.MathUtils;
import static src.data.scripts.campaign.sanguinary_autonomist_defectors_ThemeGenerator.addSADStationInteractionConfig;
import src.data.utils.sanguinary_autonomist_defectors_Tags;

public class sanguinary_autonomist_defectors_respawnManager implements EveryFrameScript {

    public static final String KEY = "$sanguinary_autonomist_defectors_respawnManager";

    public static sanguinary_autonomist_defectors_respawnManager getInstance() {
        Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
        return (sanguinary_autonomist_defectors_respawnManager) test;
    }

    public sanguinary_autonomist_defectors_respawnManager() {
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
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
            if (days < 240f) {
                continue;
            }

            float weight = 0f;
            weight = 1000f;
            if (!system.hasTag(sanguinary_autonomist_defectors_Tags.THEME_SAD) && !system.hasTag(sanguinary_autonomist_defectors_Tags.THEME_SAD_MAIN)) {
                continue;
            }
            if (!system.hasTag(sanguinary_autonomist_defectors_Tags.THEME_SAD_RESURGENT)) {
                continue;
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

    public List<CampaignFleetAPI> addBattlestations(StarSystemAPI system, float chanceToAddAny, int min, int max, WeightedRandomPicker<String> stationTypes,int id) {
        List<CampaignFleetAPI> result = new ArrayList<>();
        if (random.nextFloat() >= chanceToAddAny) {
            return result;
        }

        int num = min + random.nextInt(max - min + 1);
        for (int i = 0; i < num; i++) {

            BaseThemeGenerator.EntityLocation loc = pickCommonLocation(random, system, 200f, true, null);

            String type = stationTypes.pick();
            if (loc != null) {

                CampaignFleetAPI fleet = FleetFactoryV3.createEmptyFleet(sanguinary_autonomist_defectors_Tags.SAD_FACTION, FleetTypes.BATTLESTATION, null);

                FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, type);
                fleet.getFleetData().addFleetMember(member);

                //fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
                fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
                fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_JUMP, true);
                fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE, true);

                fleet.setStationMode(true);
                fleet.addTag(sanguinary_autonomist_defectors_Tags.SAD_STATION);

                addSADStationInteractionConfig(fleet,id);

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
                        Global.getSector().getFaction(sanguinary_autonomist_defectors_Tags.SAD_FACTION), level, true);
                if (!damaged) {
                    commander.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 3);
                }
                FleetFactoryV3.addCommanderSkills(commander, fleet, random);
                fleet.setCommander(commander);
                fleet.getFlagship().setCaptain(commander);

                member.getRepairTracker().setCR(member.getRepairTracker().getMaxCR());
                result.add(fleet);

            }
        }
        for (CampaignFleetAPI station : result) {
            int maxFleets = 4 + random.nextInt(3);
            sanguinary_autonomist_defectors_StationFleetManager activeFleets = new sanguinary_autonomist_defectors_StationFleetManager(
                    station, 1f, 0, maxFleets, 20f, 6, 20);
            system.addScript(activeFleets);
        }
        return result;
    }

    public CampaignFleetAPI getStation() {
        StarSystemAPI system = pickSADSystem(true);

        if (system == null) {
            return null;
        }

        List<CampaignFleetAPI> fleets = system.getFleets();

        for (CampaignFleetAPI fleet : fleets) {
            if (fleet.hasTag(sanguinary_autonomist_defectors_Tags.SAD_STATION)) {
                return fleet;
            }
        }
        if (0 == MathUtils.getRandom().nextInt(10)) {
            fleets = addBattlestations(system, 1f, 1, 1, createStringPicker("sanguinary_autonomist_defectors_MotherShip_2_Standard", 10f),2);
        } else {
            fleets = addBattlestations(system, 1f, 1, 1, createStringPicker("sanguinary_autonomist_defectors_MotherShip_Standard", 10f),1);
        }
        for (CampaignFleetAPI fleet : fleets) {
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
        compt -= days;
        if (compt < 0) {
            compt = 240;
            CampaignFleetAPI sta = getStation();
        }
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

}
