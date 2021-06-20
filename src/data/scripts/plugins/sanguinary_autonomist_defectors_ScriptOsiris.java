/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.data.scripts.plugins;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

import java.util.Iterator;

public class sanguinary_autonomist_defectors_ScriptOsiris implements EveryFrameWeaponEffectPlugin {

    private float valuefra;


    public sanguinary_autonomist_defectors_ScriptOsiris() {
        valuefra = 0;

    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }

        if (weapon == null || weapon.getShip() == null) {
            return;
        }

        ShipAPI ship = weapon.getShip();

        if (ship.isHulk() && !ship.isAlive()) {
            return;
        }

        ShipEngineControllerAPI engin = ship.getEngineController();

        boolean acc = engin.isAccelerating();
        boolean dec = engin.isDecelerating();
        boolean dacc = engin.isAcceleratingBackwards();

        if ((dacc || dec) && valuefra <= 1f) {
            valuefra += amount;
            if (valuefra > 1f) {
                valuefra = 1f;
            }
        } else if (acc) {
            valuefra = 0.4f;
        }

        Iterator<ShipEngineControllerAPI.ShipEngineAPI> iter = engin.getShipEngines().iterator();
        ShipEngineControllerAPI.ShipEngineAPI thruster;
        while (iter.hasNext()) {
            thruster = iter.next();
            if (thruster.isActive() && thruster.getStyleId().startsWith("R")) {
                if (dec) {
                    engin.setFlameLevel(thruster.getEngineSlot(), valuefra);
                } else if (acc) {
                    engin.setFlameLevel(thruster.getEngineSlot(), 0.4f);
                }
                if (dacc) {
                    engin.setFlameLevel(thruster.getEngineSlot(), valuefra);
                }
            }
        }

     
    }

}
