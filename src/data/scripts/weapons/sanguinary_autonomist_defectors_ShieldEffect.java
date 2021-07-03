package src.data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class sanguinary_autonomist_defectors_ShieldEffect implements EveryFrameWeaponEffectPlugin {

    private boolean runOnce = false;

    // The weapon who run is the head.
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }
        if (!runOnce) {
            runOnce = true;
            ShipAPI ship = weapon.getShip();
      ShieldAPI shield = ship.getShield();
      if(shield==null)return;
            float radius = ship.getHullSpec().getShieldSpec().getRadius();
            String inner;
            String outer;
            if (radius >= 256.0f) {
                inner = "graphics/fx/sanguinary_autonomist_defectors_shields256b.png";
                outer = "graphics/fx/shields256ringd.png";
            } else if (radius >= 128.0f) {
                inner = "graphics/fx/sanguinary_autonomist_defectors_shields128b.png";
                outer = "graphics/fx/shields128ringc.png";
            } else {
                inner = "graphics/fx/sanguinary_autonomist_defectors_shields64b.png";
                outer = "graphics/fx/shields64ring.png";
            }
           
            shield.setRadius(radius, inner, outer);
            shield.setRingRotationRate(shield.getInnerRotationRate());
         
        }
    }
    
}
