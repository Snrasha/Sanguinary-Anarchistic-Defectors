package src.data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class SAD_wadjet_transform_AI implements ShipSystemAIScript {

    private ShipSystemAPI system;
    private ShipAPI ship;
    private float compt = 0;
    private final float comptmax = 1;
    private final float grange = 450;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, com.fs.starfarer.api.combat.CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        compt += amount;

        if (compt < comptmax) {
            return;
        }

        compt = 0;

        boolean usesystem = false;

        ShipAPI target2 = target;
        if (target == null) {
            if (ship.getWing() != null && ship.getWing().getSourceShip() != null) {
                target2 = ship.getWing().getSourceShip().getShipTarget();
            }
        }

        if (target2 != null) {

            float range = grange;

            float distance = MathUtils.getDistance(ship, target2);
            if (distance < range) {
                usesystem = true;
            }

        }
        if (usesystem) {
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
