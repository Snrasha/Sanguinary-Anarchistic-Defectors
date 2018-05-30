package src.data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import org.lwjgl.util.vector.Vector2f;

public class SAD_transform_AI implements com.fs.starfarer.api.combat.ShipSystemAIScript {

    private ShipSystemAPI system;
    private ShipAPI ship;
    private ShipwideAIFlags flags;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, com.fs.starfarer.api.combat.CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
        this.flags = flags;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        int enable = 0;

        if (flags.hasFlag(AIFlags.BACKING_OFF)) {
            enable += 2;
        }
        if (flags.hasFlag(AIFlags.DO_NOT_USE_SHIELDS)) {
            enable += 2;
        }
        if (flags.hasFlag(AIFlags.HAS_INCOMING_DAMAGE) || missileDangerDir!=null) {
            enable -= 4;
        }
        if (flags.hasFlag(AIFlags.RUN_QUICKLY)) {
            enable += 2;
        }

        if (enable > 0) {
            activateSystem();
        } else {
            deactivateSystem();
        }
    }

    private void deactivateSystem() {
        if (system.isOn()) {
            ship.useSystem();
        }
    }

    private void activateSystem() {
        if (!system.isOn()) {
            ship.useSystem();
        }
    }
}
