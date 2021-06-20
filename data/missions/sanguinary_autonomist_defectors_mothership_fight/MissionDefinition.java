package data.missions.sanguinary_autonomist_defectors_mothership_fight;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

    @Override
    public void defineMission(MissionDefinitionAPI api) {

        // Set up the fleets
        api.initFleet(FleetSide.PLAYER, "HSS", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "ISS", FleetGoal.ATTACK, true);

        // Set a blurb for each fleet
        api.setFleetTagline(FleetSide.PLAYER, Factions.INDEPENDENT);
        api.setFleetTagline(FleetSide.ENEMY, "Sanguinary Anarchistic Defectors");

        // These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Win.");

        api.addToFleet(FleetSide.PLAYER, "astral_Attack", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "astral_Strike", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "paragon_Elite", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "paragon_Raider", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "legion_Assault", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "legion_FS", FleetMemberType.SHIP, true);

        api.addToFleet(FleetSide.ENEMY, "sanguinary_autonomist_defectors_MotherShip_Standard", FleetMemberType.SHIP, false);

        // Set up the map.
        // Set up the map.
        float width = 24000f;
        float height = 18000f;
        api.initMap((float) -width / 2f, (float) width / 2f, (float) -height / 2f, (float) height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

        // All the addXXX methods take a pair of coordinates followed by data for
        // whatever object is being added.
        // Add two big nebula clouds
        api.addNebula(minX + width * 0.66f, minY + height * 0.5f, 2000);
        api.addNebula(minX + width * 0.25f, minY + height * 0.6f, 1000);
        api.addNebula(minX + width * 0.25f, minY + height * 0.4f, 1000);

        // And a few random ones to spice up the playing field.
        for (int i = 0; i < 5; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 2;
            float radius = 100f + (float) Math.random() * 400f;
            api.addNebula(x, y, radius);
        }

        // add objectives
        api.addObjective(minX + width * 0.25f + 2000f, minY + height * 0.5f,
                "sensor_array");
        api.addObjective(minX + width * 0.75f - 2000f, minY + height * 0.5f,
                "comm_relay");
        api.addObjective(minX + width * 0.33f + 2000f, minY + height * 0.4f,
                "nav_buoy");
        api.addObjective(minX + width * 0.66f - 2000f, minY + height * 0.6f,
                "nav_buoy");

        api.addAsteroidField(-(minY + height), minY + height, -45, 2000f,
                20f, 70f, 100);

        api.addPlanet(0, 0, 400f, "barren", 200f, true);
        api.addRingAsteroids(0, 0, 30, 32, 32, 48, 200);
    }

}
