package src.data.shipsystems.scripts;

import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;

public class SAD_ReserveWingStats extends BaseShipSystemScript {

    public static float EXTRA_FIGHTER_DURATION = 15;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        if (effectLevel == 1) {
            for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()) {
                if (bay.getWing() == null) {
                    continue;
                }

                bay.makeCurrentIntervalFast();
                FighterWingSpecAPI spec = bay.getWing().getSpec();

                int addForWing = getAdditionalFor(spec,stats);
                int maxTotal = spec.getNumFighters() + addForWing;
                int actualAdd = maxTotal - bay.getWing().getWingMembers().size();
                actualAdd = Math.min(spec.getNumFighters(), actualAdd);
                if (actualAdd > 0) {
                    bay.setFastReplacements(bay.getFastReplacements() + addForWing);
                    bay.setExtraDeployments(actualAdd);
                    bay.setExtraDeploymentLimit(maxTotal);
                    bay.setExtraDuration(EXTRA_FIGHTER_DURATION);
                }
            }
        }
    }

    public static int getAdditionalFor(FighterWingSpecAPI spec,MutableShipStatsAPI stats) {
        if(spec.getOpCost(stats)>=16) return 0;
        if(spec.getNumFighters()<2) return 1;
        return 2;
    }

}
