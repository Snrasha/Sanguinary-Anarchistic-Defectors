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

    private final float standardarcM = 360f;

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

        if (state == State.OUT) {
            stats.getMaxSpeed().unmodify(id);
            stats.getMaxTurnRate().unmodify(id);
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

        if ((effectLevel > 0f || effectLevel < 1f)) {// && state == state.IN) {

            Iterator<WeaponAPI> iter = ship.getAllWeapons().iterator();
            WeaponAPI weapon;
            float widthS;
            float heightS;
            while (iter.hasNext()) {
                weapon = iter.next();
                switch (weapon.getSlot().getId()) {
                    case "LEFT":
                        weapon.setCurrAngle(ship.getFacing() + effectLevel * 10f);
                        widthS = weapon.getSprite().getWidth() / 2;
                        heightS = weapon.getSprite().getHeight() / 2;

                        weapon.getSprite().setCenter(widthS+(0.5f*widthS*effectLevel), heightS+(0.5f*heightS*effectLevel));
                        break;
                    case "MIDDLE":
                        weapon.setCurrAngle(weapon.getCurrAngle() - (weapon.getCurrAngle() / ship.getFacing()));
                        weapon.getSlot().setArc(standardarcM - effectLevel * standardarcM);
                        break;
                    case "RIGHT":
                        widthS = weapon.getSprite().getWidth() / 2;
                        heightS = weapon.getSprite().getHeight() / 2;

                        weapon.setCurrAngle(ship.getFacing() - effectLevel * 10f);
                        weapon.getSprite().setCenter(widthS+(0.5f*widthS*effectLevel), heightS+(0.5f*heightS*effectLevel));

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
                        weapon.setCurrAngle(ship.getFacing());
                        break;
                }
            }

        }

    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getShieldAbsorptionMult().unmodify(id);
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
