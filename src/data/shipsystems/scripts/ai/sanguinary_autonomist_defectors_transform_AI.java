package src.data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class sanguinary_autonomist_defectors_transform_AI implements ShipSystemAIScript {

    private ShipSystemAPI system;
    private ShipAPI ship;

    private float compt = 0;
    private final float comptmax = 1;

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

        if (target != null) {

            float range = 0;
            for (WeaponAPI weapon : ship.getAllWeapons()) {
                if (weapon.getSlot().getId().equals("LARGE")) {
                    range = weapon.getRange();
                    break;
                }
            }
            if (!system.isOn()) {
                range = range * 1.5f;
            }
            float tan = VectorUtils.getAngle(ship.getLocation(), target.getLocation());
            
            float distance=MathUtils.getDistance(ship, target) ;
            float shortan= Math.abs(MathUtils.getShortestRotation(ship.getFacing(), tan));
            if (distance< range && shortan< 80) {
                usesystem = true;
            }

            if (target.getFluxTracker().isOverloadedOrVenting()
                    && (target.getFluxTracker().getOverloadTimeRemaining() > 5f
                    || target.getFluxTracker().getTimeToVent() > 5f)) {
                usesystem = true;
            }

        }
        /*if (ship.getVelocity().length() < (ship.getMaxSpeedWithoutBoost() * 0.75f)) {
            usesystem=true;
        }*/

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
