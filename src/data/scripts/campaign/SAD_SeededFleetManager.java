package src.data.scripts.campaign;

import java.util.Random;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfig;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfigGen;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDDelegate;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.SeededFleetManager;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity;
import com.fs.starfarer.api.util.Misc;
import java.util.ArrayList;
import java.util.List;

public class SAD_SeededFleetManager extends SeededFleetManager {

    public static class SAD_FleetInteractionConfigGen implements FIDConfigGen {

        @Override
        public FIDConfig createConfig() {
            FIDConfig config = new FIDConfig();
            config.showTransponderStatus = false;
            config.delegate = new FIDDelegate() {
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

                    List<DropData> dropRandom = new ArrayList<DropData>();

                    String group = "blueprints_guaranteed";
                    int[] counts = new int[3];
                    String[] groups = new String[]{"survey_data1", "survey_data2", "survey_data3"};
                    //for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
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

                        DropData d = new DropData();
                        //d.group = groups[i];
                        d.group = group;
                        d.chances = (int) Math.ceil(count * 0.05f);
                        dropRandom.add(d);
                    }

                    Random salvageRandom = new Random(Misc.getSalvageSeed(fleet));
                    CargoAPI extra = SalvageEntity.generateSalvage(salvageRandom, 1f, 1f, 1f, 1f, null, dropRandom);
                    for (CargoStackAPI stack : extra.getStacksCopy()) {
                        salvage.addFromStack(stack);
                    }
                }

                public void notifyLeave(InteractionDialogAPI dialog) {
                }

                public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {
                    //bcc.aiRetreatAllowed = false;
                    //bcc.objectivesAllowed = false;
                }
            };
            return config;
        }
    }

    protected int minPts;
    protected int maxPts;
    protected float activeChance;

    public SAD_SeededFleetManager(StarSystemAPI system, int minFleets, int maxFleets, int minPts, int maxPts, float activeChance) {
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
        int supportPoints = 0;
        if (combatPoints > 8) {
            type = FleetTypes.PATROL_MEDIUM;
            supportPoints = 2;
        }
        if (combatPoints > 16) {
            type = FleetTypes.PATROL_LARGE;
            supportPoints = 2;
        }

        combatPoints *= 8f; // 8 is fp cost of remnant frigate

        FleetParamsV3 params = new FleetParamsV3(
                system.getLocation(),
                "sad",
                1f,
                type,
                combatPoints, // combatPts
                supportPoints, // freighterPts 
                supportPoints, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                0f // qualityMod
        );
        params.withOfficers = false;
        params.random = random;

        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
        if (fleet == null) {
            return null;
        }

        system.addEntity(fleet);
        fleet.setFacing(random.nextFloat() * 360f);

        int numActive = 0;
        for (SeededFleet f : fleets) {
            if (f.fleet != null) {
                numActive++;
            }
        }
        initSADFleetProperties(random, fleet);

        fleet.addScript(new SAD_AssignmentAI(fleet, system, null));

        return fleet;
    }

    public static void initSADFleetProperties(Random random, CampaignFleetAPI fleet) {
        if (random == null) {
            random = new Random();
        }

        fleet.removeAbility(Abilities.EMERGENCY_BURN);
        fleet.removeAbility(Abilities.SENSOR_BURST);
        fleet.removeAbility(Abilities.GO_DARK);

        // to make sure they attack the player on sight when player's transponder is off
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, true);

        // to make dormant fleets not try to retreat and get harried repeatedly for CR loss 
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_JUMP, true);

        addSADInteractionConfig(fleet);
        //addRemnantAICoreDrops(random, fleet, 1f);

        long salvageSeed = random.nextLong();
        fleet.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, salvageSeed);
    }

    public static void addSADInteractionConfig(CampaignFleetAPI fleet) {
        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN,
                new SAD_FleetInteractionConfigGen());
    }

}
