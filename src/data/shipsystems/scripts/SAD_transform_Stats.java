package src.data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import java.util.Iterator;

public class SAD_transform_Stats implements ShipSystemStatsScript {

    private CombatEngineAPI engine;
    
    private final float standardangle=0;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        if (engine != Global.getCombatEngine()) {
            engine = Global.getCombatEngine();
        }
        if (engine.isPaused()) {
            return;
        }

        ShipAPI ship = null;
        if ((stats.getEntity() instanceof ShipAPI)) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        if (state == State.OUT) {
            stats.getMaxSpeed().modifyPercent(id, 30f * effectLevel);
            stats.getMaxTurnRate().modifyPercent(id, 30f * effectLevel);
            stats.getDeceleration().modifyPercent(id, 30f * effectLevel);
        } else {
            stats.getMaxSpeed().modifyFlat(id, 20f * effectLevel);
            stats.getMaxSpeed().modifyPercent(id, 10f * effectLevel);
            stats.getAcceleration().modifyPercent(id, 30f * effectLevel);
            stats.getDeceleration().modifyPercent(id, 20f * effectLevel);
            stats.getTurnAcceleration().modifyFlat(id, 10f * effectLevel);
            stats.getTurnAcceleration().modifyPercent(id, 10f * effectLevel);
            stats.getMaxTurnRate().modifyFlat(id, 10f * effectLevel);
            stats.getMaxTurnRate().modifyPercent(id, 10f * effectLevel);
        }
        stats.getShieldAbsorptionMult().modifyPercent(id, effectLevel * 10f);

        if (effectLevel > 0.0F) {
            Iterator<WeaponAPI> iter = ship.getAllWeapons().iterator();
            WeaponAPI weapon;
            int lr=0;
            while (iter.hasNext() && lr<2) {
                weapon = iter.next();
                switch (weapon.getSlot().getId()) {
                    case "LEFT":
                        weapon.setCurrAngle(standardangle+effectLevel*20);
                        lr++;
                        break;
                    case "RIGHT":
                        weapon.setCurrAngle(standardangle-effectLevel*20);
                        lr++;
                        break;
                }
            }

        }
        
        
        if (effectLevel == 1f) {

            Iterator<WeaponAPI> iter = ship.getAllWeapons().iterator();
            WeaponAPI weapon;
            boolean stop=false;
            while (iter.hasNext() && !stop) {
                weapon = iter.next();
                switch (weapon.getSlot().getId()) {
                    case "MIDDLE":
                        weapon.disable(true);
                        stop=true;
                        break;
                }
            }

        }

    }

    public void init(CombatEngineAPI engine, ShipAPI host) {
        this.engine = engine;
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getShieldAbsorptionMult().unmodify(id);

        ShipAPI ship = null;
        if ((stats.getEntity() instanceof ShipAPI)) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        Iterator<WeaponAPI> iter = ship.getAllWeapons().iterator();
        WeaponAPI weapon;
        while (iter.hasNext()) {
            weapon = iter.next();
            switch (weapon.getSlot().getId()) {
                case "MIDDLE":
                    weapon.disable(false);
                    weapon.repair();
                    break;
            }
        }

    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("shield power redirected", false);
        }
        if (index == 1) {
            return new StatusData("damage to shields increased by " + (int) (10.0F * effectLevel) + "%", false);
        }
        return null;
    }
}
