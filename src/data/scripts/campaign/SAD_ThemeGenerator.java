package src.data.scripts.campaign;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI.JumpDestination;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.BaseFIDDelegate;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfig;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfigGen;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.NameAssigner;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.SpecialCreationContext;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import static com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.computeSystemData;
import static com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.convertOrbitWithSpin;
import static com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.pickCommonLocation;
import static com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.setEntityLocation;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.procgen.themes.ThemeGenContext;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity;
import java.util.Random;
import org.apache.log4j.Logger;
import src.data.utils.SAD_Tags;
import src.data.utils.SAD_themes;

public class SAD_ThemeGenerator extends BaseThemeGenerator {
    public static final Logger log = Global.getLogger(SAD_ThemeGenerator.class);

    public static enum SAD_SystemType {

        SUPPRESSED(SAD_Tags.THEME_SAD_SUPPRESSED, "$sadSuppressed"),
        RESURGENT(SAD_Tags.THEME_SAD_RESURGENT, "$sadResurgent"),;

        private String tag;
        private String beaconFlag;

        private SAD_SystemType(String tag, String beaconFlag) {
            this.tag = tag;
            this.beaconFlag = beaconFlag;
        }

        public String getTag() {
            return tag;
        }

        public String getBeaconFlag() {
            return beaconFlag;
        }
    }

    public static final int MIN_CONSTELLATIONS_WITH_SAD = 4;//15
    public static final int MAX_CONSTELLATIONS_WITH_SAD = 6;//25


    @Override
    public String getThemeId() {
        return SAD_themes.SAD;
    }

    @Override
    public void generateForSector(ThemeGenContext context, float allowedUnusedFraction) {

        float total = (float) (context.constellations.size() - context.majorThemes.size()) * allowedUnusedFraction;
        if (total <= 0) {
            return;
        }

        int num = (int) StarSystemGenerator.getNormalRandom(MIN_CONSTELLATIONS_WITH_SAD, MAX_CONSTELLATIONS_WITH_SAD);
        //num = 30;
        if (num > total) {
            num = (int) total;
        }
        int numSuppressed = (int) (num * (0.23f + 0.1f * random.nextFloat()));
        if (numSuppressed < 1) {
            numSuppressed = 1;
        }

        float suppressedStationMult = 0.5f;
        int suppressedStations = (int) Math.ceil(numSuppressed * suppressedStationMult);

        WeightedRandomPicker<Boolean> addSuppressedStation = new WeightedRandomPicker<>(random);
        for (int i = 0; i < numSuppressed; i++) {
            if (i < suppressedStations) {
                addSuppressedStation.add(true, 1f);
            } else {
                addSuppressedStation.add(false, 1f);
            }
        }

        List<Constellation> constellations = getSortedAvailableConstellations(context, false, new Vector2f(), null);
        Collections.reverse(constellations);

        List<StarSystemData> sadSystems = new ArrayList<>();

        log.info("\n\n\n");
        log.info("Generating sad systems");
        

        int numUsed = 0;
        for (int i = 0; i < num && i < constellations.size(); i++) {
            Constellation c = constellations.get(i);

            List<StarSystemData> systems = new ArrayList<>();
            for (StarSystemAPI system : c.getSystems()) {
                StarSystemData data = computeSystemData(system);
                if(!data.system.hasTag(Tags.THEME_REMNANT))
                systems.add(data);
            }

            List<StarSystemData> mainCandidates = getSortedSystemsSuitedToBePopulated(systems);

            int numMain = 1 + random.nextInt(2);
            if (numMain > mainCandidates.size()) {
                numMain = mainCandidates.size();
            }
            if (numMain <= 0) {
                log.info("Skipping constellation " + c.getName() + ", no suitable main candidates");
                
                continue;
            }

            SAD_SystemType type = SAD_SystemType.RESURGENT;
            if (numUsed < numSuppressed) {
                type = SAD_SystemType.SUPPRESSED;
            }

            context.majorThemes.put(c, getThemeId());
            numUsed++;

            log.info("Generating " + numMain + " main systems in " + c.getName());
            
            for (int j = 0; j < numMain; j++) {
                StarSystemData data = mainCandidates.get(j);
                populateMain(data, type);

                data.system.addTag(SAD_Tags.THEME_SAD);
                data.system.addTag(SAD_Tags.THEME_SAD_MAIN);
                data.system.addTag(type.getTag());
                sadSystems.add(data);

                if (!NameAssigner.isNameSpecial(data.system)) {
                    NameAssigner.assignSpecialNames(data.system);
                }

                if (type == SAD_SystemType.SUPPRESSED) {
                    SAD_SeededFleetManager fleets = new SAD_SeededFleetManager(data.system, 7, 12, 4, 12, 0.25f);
                    data.system.addScript(fleets);

                    Boolean addStation = random.nextFloat() < suppressedStationMult;
                    if (j == 0 && !addSuppressedStation.isEmpty()) {
                        addSuppressedStation.pickAndRemove();
                    }
                    if (addStation) {
                            List<CampaignFleetAPI> stations = addBattlestations(data, 1f, 1, 1, createStringPicker("SAD_MotherShip_Standard", 10f),1);
                        for (CampaignFleetAPI station : stations) {
                            int maxFleets = 4 + random.nextInt(3);
                            SAD_StationFleetManager activeFleets = new SAD_StationFleetManager(
                                    station, 1f, 0, maxFleets, 20f, 6, 20);
                            data.system.addScript(activeFleets);
                        }

                    }
                } else if (type == SAD_SystemType.RESURGENT) {
                    List<CampaignFleetAPI> stations = addBattlestations(data, 1f, 1, 1, createStringPicker("SAD_MotherShip_2_Standard", 10f),2);
                    for (CampaignFleetAPI station : stations) {
                        int maxFleets = 4 + random.nextInt(5);
                        SAD_StationFleetManager activeFleets = new SAD_StationFleetManager(
                                station, 1f, 0, maxFleets, 10f, 8, 24);
                        data.system.addScript(activeFleets);
                    }
                }
            }

            for (StarSystemData data : systems) {
                int index = mainCandidates.indexOf(data);
                if (index >= 0 && index < numMain) {
                    continue;
                }

                populateNonMain(data);

                data.system.addTag(SAD_Tags.THEME_SAD);
                data.system.addTag(SAD_Tags.THEME_SAD_MAIN);
                data.system.addTag(type.getTag());
                sadSystems.add(data);

                SAD_SeededFleetManager fleets = new SAD_SeededFleetManager(data.system, 1, 3, 1, 2, 0.05f);
                data.system.addScript(fleets);
            }
        }

        SpecialCreationContext specialContext = new SpecialCreationContext();
        specialContext.themeId = getThemeId();
        SalvageSpecialAssigner.assignSpecials(sadSystems, specialContext);
        log.info("Finished generating sad systems\n\n\n\n\n");
        

    }

    public void populateNonMain(StarSystemData data) {
        log.info(" Generating secondary sad system in " + data.system.getName());
        
            addResearchStations(data, 0.75f, 1, 1, createStringPicker(Entities.STATION_RESEARCH, 10f));

        if (random.nextFloat() < 0.5f) {
            return;
        }
        addShipGraveyard(data, 0.05f, 1, 1,
                createStringPicker(Factions.TRITACHYON, 10f, Factions.HEGEMONY, 7f, Factions.INDEPENDENT, 3f));
        addDebrisFields(data, 0.25f, 1, 2);

        addDerelictShips(data, 0.5f, 0, 3,
                createStringPicker(Factions.TRITACHYON, 10f, Factions.HEGEMONY, 7f, Factions.INDEPENDENT, 3f));

    }

    public void populateMain(StarSystemData data, SAD_SystemType type) {

        log.info(" Generating sad center in " + data.system.getName());
        

        StarSystemAPI system = data.system;

        addBeacon(system, type);


        int maxHabCenters = 1 + random.nextInt(3);

        HabitationLevel level = HabitationLevel.LOW;
        if (maxHabCenters == 2) {
            level = HabitationLevel.MEDIUM;
        }
        if (maxHabCenters >= 3) {
            level = HabitationLevel.HIGH;
        }

        float probRelay = 1f;
        float probResearch = 0.25f;

        switch (level) {
            case HIGH:
                probRelay = 1f;
                break;
            case MEDIUM:
                probRelay = 0.75f;
                break;
            case LOW:
                probRelay = 0.5f;
                break;
        }
        addObjectives(data, probRelay);
        addShipGraveyard(data, 0.25f, 1, 1,
                createStringPicker(Factions.TRITACHYON, 10f, Factions.HEGEMONY, 7f, Factions.INDEPENDENT, 3f));
        addResearchStations(data, probResearch, 2, 2, createStringPicker(Entities.STATION_RESEARCH, 10f));

        addDebrisFields(data, 0.75f, 1, 5);
        addDerelictShips(data, 0.75f, 0, 7,
                createStringPicker(Factions.TRITACHYON, 10f, Factions.HEGEMONY, 7f, Factions.INDEPENDENT, 3f));


    }

    public List<StarSystemData> getSortedSystemsSuitedToBePopulated(List<StarSystemData> systems) {
        List<StarSystemData> result = new ArrayList<>();

        for (StarSystemData data : systems) {
            if (data.isBlackHole() || data.isPulsar()) {
                continue;
            }
            if(data.system.hasTag(SAD_Tags.THEME_BREAKER) || data.system.hasTag(Tags.THEME_REMNANT)){
                continue;
            }

            if (data.planets.size() >= 2) {
                result.add(data);
            }
            
        }

        Collections.sort(systems, new Comparator<StarSystemData>() {
            @Override
            public int compare(StarSystemData o1, StarSystemData o2) {
                float s1 = getMainCenterScore(o1);
                float s2 = getMainCenterScore(o2);
                return (int) Math.signum(s2 - s1);
            }
        });

        return result;
    }

    public float getMainCenterScore(StarSystemData data) {
        float total = 0f;
        total += data.planets.size() * 1f;
        total += data.habitable.size() * 2f;
        total += data.resourceRich.size() * 0.25f;
        return total;
    }

    public static CustomCampaignEntityAPI addBeacon(StarSystemAPI system, SAD_SystemType type) {

        SectorEntityToken anchor = system.getHyperspaceAnchor();
        List<SectorEntityToken> points = Global.getSector().getHyperspace().getEntities(JumpPointAPI.class);
        float minRange = 600;

        float closestRange = Float.MAX_VALUE;
        JumpPointAPI closestPoint = null;
        for (SectorEntityToken entity : points) {
            JumpPointAPI point = (JumpPointAPI) entity;

            if (point.getDestinations().isEmpty()) {
                continue;
            }

            JumpDestination dest = point.getDestinations().get(0);
            if (dest.getDestination().getContainingLocation() != system) {
                continue;
            }

            float dist = Misc.getDistance(anchor.getLocation(), point.getLocation());
            if (dist < minRange + point.getRadius()) {
                continue;
            }

            if (dist < closestRange) {
                closestPoint = point;
                closestRange = dist;
            }
        }
        CustomCampaignEntityAPI beacon = Global.getSector().getHyperspace().addCustomEntity("SAD_warning_beacon", null, Entities.WARNING_BEACON, Factions.NEUTRAL);

        beacon.getMemoryWithoutUpdate().set(type.getBeaconFlag(), true);

        switch (type) {
            case SUPPRESSED:
                beacon.addTag(Tags.BEACON_MEDIUM);
                break;
            case RESURGENT:
                beacon.addTag(Tags.BEACON_HIGH);
                break;
        }

        if (closestPoint == null) {
            float orbitDays = minRange / (10f + StarSystemGenerator.random.nextFloat() * 5f);
            //beacon.setCircularOrbit(anchor, StarSystemGenerator.random.nextFloat() * 360f, minRange, orbitDays);
            beacon.setCircularOrbitPointingDown(anchor, StarSystemGenerator.random.nextFloat() * 360f, minRange, orbitDays);
        } else {
            float angleOffset = 20f + StarSystemGenerator.random.nextFloat() * 20f;
            float angle = Misc.getAngleInDegrees(anchor.getLocation(), closestPoint.getLocation()) + angleOffset;
            float radius = closestRange;

            if (closestPoint.getOrbit() != null) {
//				OrbitAPI orbit = Global.getFactory().createCircularOrbit(anchor, angle, radius, 
//																closestPoint.getOrbit().getOrbitalPeriod()); 
                OrbitAPI orbit = Global.getFactory().createCircularOrbitPointingDown(anchor, angle, radius,
                        closestPoint.getOrbit().getOrbitalPeriod());
                beacon.setOrbit(orbit);
            } else {
                Vector2f beaconLoc = Misc.getUnitVectorAtDegreeAngle(angle);
                beaconLoc.scale(radius);
                Vector2f.add(beaconLoc, anchor.getLocation(), beaconLoc);
                beacon.getLocation().set(beaconLoc);
            }
        }

        Color glowColor = new Color(0, 155, 255, 255);
        Color pingColor = new Color(0, 155, 255, 255);
        if (type == SAD_SystemType.RESURGENT) {
            glowColor = new Color(0, 55, 255, 255);
            pingColor = new Color(0, 125, 255, 255);
        }
        Misc.setWarningBeaconColors(beacon, glowColor, pingColor);

        return beacon;
    }


    protected List<Constellation> getSortedAvailableConstellations(ThemeGenContext context, boolean emptyOk, final Vector2f sortFrom, List<Constellation> exclude) {
        List<Constellation> constellations = new ArrayList<>();
        for (Constellation c : context.constellations) {
            if (context.majorThemes.containsKey(c)) {
                continue;
            }
            if (!emptyOk && constellationIsEmpty(c)) {
                continue;
            }

            constellations.add(c);
        }

        if (exclude != null) {
            constellations.removeAll(exclude);
        }

        Collections.sort(constellations, new Comparator<Constellation>() {
            @Override
            public int compare(Constellation o1, Constellation o2) {
                float d1 = Misc.getDistance(o1.getLocation(), sortFrom);
                float d2 = Misc.getDistance(o2.getLocation(), sortFrom);
                return (int) Math.signum(d2 - d1);
            }
        });
        return constellations;
    }

    public static boolean constellationIsEmpty(Constellation c) {
        for (StarSystemAPI s : c.getSystems()) {
            if (!systemIsEmpty(s)) {
                return false;
            }
        }
        return true;
    }

    public static boolean systemIsEmpty(StarSystemAPI system) {
        for (PlanetAPI p : system.getPlanets()) {
            if (!p.isStar()) {
                return false;
            }
        }
        //system.getTerrainCopy().isEmpty()
        return true;
    }

    public List<CampaignFleetAPI> addBattlestations(StarSystemData data, float chanceToAddAny, int min, int max,
        WeightedRandomPicker<String> stationTypes,int id) {
        List<CampaignFleetAPI> result = new ArrayList<>();
        if (random.nextFloat() >= chanceToAddAny) {
            return result;
        }

        int num = min + random.nextInt(max - min + 1);
log.info("    Adding " + num + " battlestations");
        for (int i = 0; i < num; i++) {

            EntityLocation loc = pickCommonLocation(random, data.system, 200f, true, null);

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
                fleet.addTag(SAD_Tags.SAD_STATION);

                addSADStationInteractionConfig(fleet,id);

                data.system.addEntity(fleet);

                //fleet.setTransponderOn(true);
                fleet.clearAbilities();
                fleet.addAbility(Abilities.TRANSPONDER);
                fleet.getAbility(Abilities.TRANSPONDER).activate();
                fleet.getDetectedRangeMod().modifyFlat("gen", 1000f);

                fleet.setAI(null);

                setEntityLocation(fleet, loc, null);
                convertOrbitWithSpin(fleet, 5f);

                boolean damaged = type.toLowerCase().contains("damaged");
                int level = 20;
                if (damaged) {
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

        return result;
    }

    public static void addSADStationInteractionConfig(CampaignFleetAPI fleet,int id) {
        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN,
                new SAD_StationInteractionConfigGen(id));
    }

    @Override
    public int getOrder() {
        return 1500;
    }

    public static class SAD_StationInteractionConfigGen implements FIDConfigGen {
        
        public int id=0;
        
        public SAD_StationInteractionConfigGen(int id){
            super();
            this.id=id;
        }

        @Override
        public FIDConfig createConfig() {
            FIDConfig config = new FIDConfig();

            config.alwaysAttackVsAttack = true;
            config.leaveAlwaysAvailable = true;
            config.showFleetAttitude = false;
            config.showTransponderStatus = false;
            config.showEngageText = false;

            config.delegate = new BaseFIDDelegate() {
@Override
                public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context, CargoAPI salvage) {
                    if (!(dialog.getInteractionTarget() instanceof CampaignFleetAPI)) {
                        return;
                    }

                    CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();

                    FleetEncounterContextPlugin.DataForEncounterSide data = context.getDataFor(fleet);
                    List<FleetMemberAPI> losses = new ArrayList<FleetMemberAPI>();
                    for (FleetEncounterContextPlugin.FleetMemberData fmd : data.getOwnCasualties()) {
                        losses.add(fmd.getMember());
                    }

                    List<SalvageEntityGenDataSpec.DropData> dropRandom = new ArrayList<SalvageEntityGenDataSpec.DropData>();

                    if(id==2){
                        SalvageEntityGenDataSpec.DropData d = new SalvageEntityGenDataSpec.DropData();
                        d.group = "blueprints_guaranteed";
                        d.chances = 20;
                        dropRandom.add(d);
                    }
                    if(id==1){
                        SalvageEntityGenDataSpec.DropData d = new SalvageEntityGenDataSpec.DropData();
                        d.group = "blueprints_guaranteed";
                        d.chances = 10;
                        dropRandom.add(d);
                    }
                    
                    /*int[] counts = new int[3];
                    String[] groups = new String[]{"survey_data1", "survey_data2", "survey_data3"};
                    for (FleetMemberAPI member : losses) {
                        if (member.isStation()) {
                            counts[2] += 10;
                        }

                        if (member.isCapital()) {
                            counts[2] += 2;
                        } else if (member.isCruiser()) {
                            counts[2] += 1;
                        } else if (member.isDestroyer()) {
                            counts[1] += 1;
                        } else if (member.isFrigate()) {
                            counts[0] += 1;
                        }

                    }
                    for (int i = 0; i < counts.length; i++) {
                        int count = counts[i];
                        if (count <= 0) {
                            continue;
                        }

                        SalvageEntityGenDataSpec.DropData d = new SalvageEntityGenDataSpec.DropData();
                        d.group = groups[i];
                        d.chances = (int) Math.ceil(count * 1f);
                        dropRandom.add(d);
                    }
*/
                    Random salvageRandom = new Random(Misc.getSalvageSeed(fleet));
                    CargoAPI extra = SalvageEntity.generateSalvage(salvageRandom, 1f, 1f, 1f, 1f, null, dropRandom);
                    for (CargoStackAPI stack : extra.getStacksCopy()) {
                        salvage.addFromStack(stack);
                    }
                }
                @Override
                public void notifyLeave(InteractionDialogAPI dialog) {
                }

                @Override
                public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {
                   // bcc.aiRetreatAllowed = false;
                   // bcc.objectivesAllowed = false;
                }
            };
            return config;
        }
    }

}
