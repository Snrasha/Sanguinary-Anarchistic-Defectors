package src.data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import org.lazywizard.lazylib.MathUtils;

import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class sanguinary_autonomist_defectors_siegemodeAI implements com.fs.starfarer.api.combat.ShipSystemAIScript {

    private ShipSystemAPI system;
    private ShipAPI ship;
    private ShipwideAIFlags flags;

    public sanguinary_autonomist_defectors_siegemodeAI() {
    }

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, com.fs.starfarer.api.combat.CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
        this.flags = flags;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        boolean shieldExtensionNeeded = false;

        if (missileDangerDir != null && MathUtils.getDistance(ship, missileDangerDir)<200) {
            
            shieldExtensionNeeded = true;

        }
        if (!shieldExtensionNeeded) {
            for (ShipAPI shp : AIUtils.getNearbyEnemies(ship, ship.getCollisionRadius()*6f)) {
                if (((!shp.getFluxTracker().isOverloaded()) || (shp.getFluxTracker().getOverloadTimeRemaining() <= 1.8F)) && ((!shp.getFluxTracker().isVenting()) || (shp.getFluxTracker().getTimeToVent() <= 1.8F))) {
                    shieldExtensionNeeded = true;

                }
            }
        }
        if ((shieldExtensionNeeded) || (flags.hasFlag(AIFlags.TURN_QUICKLY))) {
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
