package src.data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;


public class SAD_ConvertedHangar extends BaseHullMod {

        public static final float REFIT_TIME_PERCENT = 50f;
	

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getNumFighterBays().modifyFlat(id, 1f);
                stats.getFighterRefitTimeMult().modifyPercent(id, REFIT_TIME_PERCENT );
	}
	
	
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
                if (index == 0) return "" + (int) Math.round(REFIT_TIME_PERCENT) + "%";
                return null;
	}

}



