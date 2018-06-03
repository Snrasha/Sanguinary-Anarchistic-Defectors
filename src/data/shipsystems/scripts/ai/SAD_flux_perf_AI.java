package src.data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.util.Iterator;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class SAD_flux_perf_AI implements ShipSystemAIScript {

    private ShipSystemAPI system;
    private ShipAPI ship;
    private float rangeWeapon;
    private float compt = 0;
    private final float comptmax = 1;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, com.fs.starfarer.api.combat.CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
       Iterator<WeaponAPI> iter = ship.getAllWeapons().iterator();
            WeaponAPI weapon;
            float inc=0;
            while (iter.hasNext()) {
                weapon = iter.next();
                if(weapon.getSlot().isHardpoint()){
                    inc++;
                    rangeWeapon+=weapon.getRange();
                    
                }
            }
            rangeWeapon/=inc;
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
            float distance = MathUtils.getDistance(ship, target);
            if (distance < rangeWeapon) {
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
