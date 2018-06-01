package src.data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import java.util.Iterator;

public class SAD_siegemode implements ShipSystemStatsScript {

    private CombatEngineAPI engine;
    private final float ptahwidth = 14;
    private final float maatheight = 58;

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

        stats.getTurnAcceleration().modifyFlat(id, 45.0F * effectLevel);
        stats.getMaxTurnRate().modifyFlat(id, 30.0F);

        stats.getEnergyWeaponRangeBonus().modifyFlat(id, 200.0F * effectLevel);
        stats.getEnergyWeaponFluxCostMod().modifyPercent(id, -20.0F * effectLevel);
        stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -20.0F * effectLevel);

        stats.getMaxSpeed().modifyPercent(id, -80.0F * effectLevel);
        stats.getZeroFluxSpeedBoost().modifyMult(id, 1.0F - 0.5F * effectLevel);

        stats.getShieldAbsorptionMult().modifyPercent(id, -1.0F * effectLevel * 10.0F);

        if ((effectLevel > 0f || effectLevel < 0.9f)) {
            boolean ptah = ship.getHullSpec().getBaseHullId().endsWith("h");

            Iterator<WeaponAPI> iter = ship.getAllWeapons().iterator();
            WeaponAPI weapon;
            float widthS;
            float heightS;
            while (iter.hasNext()) {
                weapon = iter.next();
                switch (weapon.getSlot().getId()) {
                    case "RIGHT":
                        widthS = weapon.getSprite().getWidth() / 2;
                        heightS = weapon.getSprite().getHeight() / 2;
                        if (ptah) {
                            weapon.getSprite().setWidth(ptahwidth - (effectLevel * 2));
                            weapon.getSprite().setCenter(widthS - (0.55f * widthS * effectLevel), heightS);
                        } else {
                            weapon.getSprite().setHeight(maatheight - (effectLevel * 4));
                            weapon.getSprite().setCenter(widthS - (0.30f * widthS * effectLevel), heightS + (0.30f * heightS * effectLevel));
                        }
                        break;
                    case "LEFT":
                        widthS = weapon.getSprite().getWidth() / 2;
                        heightS = weapon.getSprite().getHeight() / 2;
                        if (ptah) {
                            weapon.getSprite().setWidth(ptahwidth - (effectLevel * 2));
                            weapon.getSprite().setCenter(widthS + (0.55f * widthS * effectLevel), heightS);
                        } else {
                            weapon.getSprite().setHeight(maatheight - (effectLevel * 4));
                            weapon.getSprite().setCenter(widthS + (0.30f * widthS * effectLevel), heightS + (0.30f * heightS * effectLevel));
                        }
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
        stats.getEnergyWeaponRangeBonus().unmodify(id);
        stats.getEnergyWeaponFluxCostMod().unmodify(id);
        stats.getZeroFluxSpeedBoost().unmodify(id);
        stats.getShieldAbsorptionMult().unmodify(id);
    }

    @Override
    public ShipSystemStatsScript.StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("engine power redirected", false);
        }
        if (index == 1) {
            return new StatusData("energy weapon range +" + (int) (200.0F * effectLevel), false);
        }
        if (index == 2) {
            return new StatusData("energy weapon flux costs " + (int) (-20.0F * effectLevel) + "%", false);
        }
        if (index == 3) {
            return new StatusData("damage to shields reduced by " + (int) (10.0F * effectLevel) + "%", false);
        }
        return null;
    }
}
