package src.data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class sanguinary_autonomist_defectors_OsirisHullmod extends BaseHullMod {

    public static final float COST_REDUCTION_MEDIUM = 6;
    public static final float COST_REDUCTION_SMALL = 3;
    public static float RANGE_BONUS = 25f;
    public static float EMP_TAKEN_MULT = 50f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getDynamic().getMod(Stats.MEDIUM_MISSILE_MOD).modifyFlat(id, -COST_REDUCTION_MEDIUM);
        stats.getDynamic().getMod(Stats.SMALL_MISSILE_MOD).modifyFlat(id, -COST_REDUCTION_SMALL);

        stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
        stats.getNonBeamPDWeaponRangeBonus().modifyPercent(id, -RANGE_BONUS);
        stats.getBeamPDWeaponRangeBonus().modifyPercent(id, -RANGE_BONUS);
        stats.getEmpDamageTakenMult().modifyPercent(id, EMP_TAKEN_MULT);
    }

    /*@Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        for(WeaponAPI weapon : ship.getAllWeapons()){
            if(weapon.getSize() == WeaponSize.LARGE){
              
            }
        }
    }*/
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + (int) COST_REDUCTION_SMALL + "";
        }
        if (index == 1) {
            return "" + (int) COST_REDUCTION_MEDIUM + "";
        }
        if (index == 2) {
            return "0";
        }
        if (index == 3) {
            return "" + (int) RANGE_BONUS + "%";
        }
        if (index == 4) {
            return "" + (int) EMP_TAKEN_MULT + "%";
        }
        return null;
    }

    @Override
    public boolean affectsOPCosts() {
        return true;
    }

}
