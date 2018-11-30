package src.data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import java.util.Iterator;
import org.lazywizard.lazylib.MathUtils;

public class SAD_transform_Stats extends BaseShipSystemScript {

    private CombatEngineAPI engine;

    private final float standardarcM = 340;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        if (engine == null) {
            engine = Global.getCombatEngine();

        }
        if (engine.isPaused()) {
            return;
        }

        ShipAPI ship;
        if ((stats.getEntity() instanceof ShipAPI)) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        stats.getMaxSpeed().modifyPercent(id, -50f * effectLevel);
        stats.getAcceleration().modifyPercent(id, -50f * effectLevel);
        stats.getDeceleration().modifyPercent(id, -50f * effectLevel);
        stats.getTurnAcceleration().modifyPercent(id, -50f * effectLevel);
        stats.getMaxTurnRate().modifyPercent(id, -50f * effectLevel);
        stats.getShieldUpkeepMult().modifyPercent(id, -50f * effectLevel);
        stats.getSensorStrength().modifyPercent(id, 25f * effectLevel);
        stats.getEnergyWeaponRangeBonus().modifyPercent(id, 50f * effectLevel);
        stats.getEnergyRoFMult().modifyPercent(id, 50f * effectLevel);
        stats.getEnergyWeaponFluxCostMod().modifyPercent(id, -50f * effectLevel);

        if ((effectLevel > 0f || effectLevel < 0.9f)) {
            boolean effect50 = effectLevel < 0.5f;

            Iterator<WeaponAPI> iter = ship.getAllWeapons().iterator();
            WeaponAPI weapon;
            float widthS;
            float heightS;
            while (iter.hasNext()) {
                weapon = iter.next();
                if (effect50) {
                    switch (weapon.getSlot().getId()) {
                        case "LEFT":
                            weapon.setCurrAngle(ship.getFacing() - effectLevel * 15f);
                            break;
                        case "RIGHT":
                            weapon.setCurrAngle(ship.getFacing() + effectLevel * 15f);
                            break;
                        case "LEFT2":
                            weapon.setCurrAngle(ship.getFacing() - effectLevel * 15f);
                            break;
                        case "RIGHT2":
                            weapon.setCurrAngle(ship.getFacing() + effectLevel * 15f);
                            break;

                    }
                } else {
                    float effectLevel2 = effectLevel - 0.5f;
                    switch (weapon.getSlot().getId()) {
                        case "LEFT":
                            widthS = weapon.getSprite().getWidth() / 2;
                            heightS = weapon.getSprite().getHeight() / 2;
                            weapon.getSprite().setCenter(widthS - (0.55f * widthS * effectLevel2), heightS - (0.30f * heightS * effectLevel2));
                            break;
                        case "RIGHT":
                            widthS = weapon.getSprite().getWidth() / 2;
                            heightS = weapon.getSprite().getHeight() / 2;
                            weapon.getSprite().setCenter(widthS + (0.55f * widthS * effectLevel2), heightS - (0.30f * heightS * effectLevel2));
                            break;
                        case "LEFT2":
                            widthS = weapon.getSprite().getWidth() / 2;
                            heightS = weapon.getSprite().getHeight() / 2;
                            weapon.getSprite().setCenter(widthS + (0.5f * widthS * effectLevel2), heightS + (0.5f * heightS * effectLevel2));
                            break;
                        case "RIGHT2":
                            widthS = weapon.getSprite().getWidth() / 2;
                            heightS = weapon.getSprite().getHeight() / 2;
                            weapon.getSprite().setCenter(widthS - (0.5f * widthS * effectLevel2), heightS + (0.5f * heightS * effectLevel2));
                            break;
                        /*  case "ZJOINTL":
                            weapon.setCurrAngle(-90 + ship.getFacing() + effectLevel2 * 25f);
                            break;
                        case "ZJOINTR":
                            weapon.setCurrAngle(90 + ship.getFacing() - effectLevel2 * 25f);
                            break;*/
                    }
                }

                switch (weapon.getSlot().getId()) {
                    case "MIDDLE":

                        weapon.getSlot().setArc(standardarcM - 0.9f * effectLevel * standardarcM);
                        float angl = weapon.getSlot().getArc() / 2;
                        float fac = (ship.getFacing());
                        float currang = (weapon.getCurrAngle());

                        float minfac = -angl + fac;
                        float maxfac = angl + fac;
                        float dist1 = MathUtils.getShortestRotation(fac, currang);
                        if (Math.abs(dist1) <= angl) {

                        } else {

                            if (dist1 > 0) {
                                weapon.setCurrAngle(maxfac);
                            } else {
                                weapon.setCurrAngle(minfac);
                            }
                        }
                        break;

                }

            }
        }
        if (effectLevel == 1f) {
            Iterator<WeaponAPI> iter = ship.getAllWeapons().iterator();
            WeaponAPI weapon;
            while (iter.hasNext()) {
                weapon = iter.next();
                switch (weapon.getSlot().getId()) {
                    case "MIDDLE":
                        float angl = weapon.getSlot().getArc() / 2;
                        float fac = (ship.getFacing());
                        float currang = (weapon.getCurrAngle());

                        float minfac = -angl + fac;
                        float maxfac = angl + fac;
                        float dist1 = MathUtils.getShortestRotation(fac, currang);
                        if (Math.abs(dist1) <= angl) {

                        } else {

                            if (dist1 > 0) {
                                weapon.setCurrAngle(maxfac);
                            } else {
                                weapon.setCurrAngle(minfac);
                            }
                        }

                        break;
                }
            }

        }

    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id
    ) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getShieldUpkeepMult().unmodify(id);
        stats.getSensorStrength().unmodify(id);
        stats.getEnergyWeaponRangeBonus().unmodify(id);
        stats.getEnergyRoFMult().unmodify(id);
        stats.getEnergyWeaponFluxCostMod().unmodify(id);
        
        
        ShipAPI ship;
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
                    weapon.getSlot().setArc(340);
                    break;
            }
        }
    }

    @Override
    public StatusData getStatusData(int index, State state,
            float effectLevel
    ) {
        if (index == 0) {
            return new StatusData("engine power redirected", false);
        }
        return null;
    }
}
