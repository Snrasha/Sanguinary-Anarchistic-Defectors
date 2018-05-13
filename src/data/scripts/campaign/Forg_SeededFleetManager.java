package src.data.scripts.campaign;

import java.util.Random;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfig;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfigGen;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDDelegate;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV2;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParams;
import com.fs.starfarer.api.impl.campaign.fleets.SeededFleetManager;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class Forg_SeededFleetManager extends SeededFleetManager {

    public static class Forg_FleetInteractionConfigGen implements FIDConfigGen {

        @Override
        public FIDConfig createConfig() {
            FIDConfig config = new FIDConfig();
            config.showTransponderStatus = false;
            config.delegate = new FIDDelegate() {
                @Override
                public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context, CargoAPI salvage) {
                }

                @Override
                public void notifyLeave(InteractionDialogAPI dialog) {
                }

                @Override
                public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {
                    bcc.aiRetreatAllowed = false;
                    bcc.objectivesAllowed = false;
                }
            };
            return config;
        }
    }

    protected int minPts;
    protected int maxPts;
    protected float activeChance;

    public Forg_SeededFleetManager(StarSystemAPI system, int minFleets, int maxFleets, int minPts, int maxPts, float activeChance) {
        super(system, 1f);
        this.minPts = minPts;
        this.maxPts = maxPts;
        this.activeChance = activeChance;

        int num = minFleets + StarSystemGenerator.random.nextInt(maxFleets - minFleets + 1);
        for (int i = 0; i < num; i++) {
            long seed = StarSystemGenerator.random.nextLong();
            addSeed(seed);
        }
    }

    @Override
    protected CampaignFleetAPI spawnFleet(long seed) {
        Random random = new Random(seed);

        int combatPoints = minPts + random.nextInt(maxPts - minPts + 1);

        String type = FleetTypes.PATROL_SMALL;
        if (combatPoints > 8) {
            type = FleetTypes.PATROL_MEDIUM;
        }
        if (combatPoints > 16) {
            type = FleetTypes.PATROL_LARGE;
        }

        FleetParams params = new FleetParams(
                system.getLocation(),
                null,
                "forgotten",
                null, // fleet's faction, if different from above, which is also used for source market picking
                type,
                combatPoints, // combatPts
                0f, // freighterPts 
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // civilianPts 
                0f, // utilityPts
                0f, // qualityBonus
                1f, // qualityOverride
                0f, // officer num mult
                0 // officer level bonus
        );
        params.withOfficers = false;
        params.random = random;

        CampaignFleetAPI fleet = FleetFactoryV2.createFleet(params);
        if (fleet == null) {
            return null;
        }

        system.addEntity(fleet);
        fleet.setFacing(random.nextFloat() * 360f);

    
        initForgottenFleetProperties(random, fleet);

        fleet.addScript(new Forg_AssignmentAI(fleet, system, null));

        return fleet;
    }

    public static SectorEntityToken pickEntityToGuard(Random random, StarSystemAPI system, CampaignFleetAPI fleet) {
        WeightedRandomPicker<SectorEntityToken> picker = new WeightedRandomPicker<>(random);

        for (SectorEntityToken entity : system.getEntitiesWithTag(Tags.SALVAGEABLE)) {
            float w = 1f;
            if (entity.hasTag(Tags.NEUTRINO_HIGH)) {
                w = 3f;
            }
            if (entity.hasTag(Tags.NEUTRINO_LOW)) {
                w = 0.33f;
            }
            picker.add(entity, w);
        }

        for (SectorEntityToken entity : system.getJumpPoints()) {
            picker.add(entity, 1f);
        }

        return picker.pick();
    }

    public static void initForgottenFleetProperties(Random random, CampaignFleetAPI fleet) {
        if (random == null) {
            random = new Random();
        }

        fleet.removeAbility(Abilities.EMERGENCY_BURN);
        fleet.removeAbility(Abilities.SENSOR_BURST);
        fleet.removeAbility(Abilities.GO_DARK);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_JUMP, true);

        addForgottenInteractionConfig(fleet);
        addForgottenSurveyDataDrops(random, fleet, 1f);
    }

    public static void addForgottenInteractionConfig(CampaignFleetAPI fleet) {
        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN,
                new Forg_FleetInteractionConfigGen());
    }

    public static void addForgottenSurveyDataDrops(Random random, CampaignFleetAPI fleet, float mult) {
        if (random == null) {
            random = new Random();
        }
        long salvageSeed = random.nextLong();
        fleet.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, salvageSeed);

        int[] counts = new int[3];
        String[] groups = new String[]{"survey_data1", "survey_data2", "survey_data3"};
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
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

        if (fleet.isStationMode()) {
            counts[2] += 10;
        }

        for (int i = 0; i < counts.length; i++) {
            int count = counts[i];
            if (count <= 0) {
                continue;
            }

            DropData d = new DropData();
            d.group = groups[i];
            d.chances = (int) Math.ceil(count * mult);
            fleet.addDropRandom(d);
        }

    }

}
