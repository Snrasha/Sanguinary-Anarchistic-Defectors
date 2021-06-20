package src.data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.EnumSet;

public class sanguinary_autonomist_defectors_flux_perf_Stats extends BaseShipSystemScript {

    public static final Color JITTER_UNDER_COLOR = new Color(255, 50, 0, 125);
    private CombatEngineAPI engine;
    // private final float hapyheight = 28;

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
        stats.getEnergyWeaponRangeBonus().modifyPercent(id, 15f * effectLevel);
        stats.getEnergyWeaponFluxCostMod().modifyPercent(id, -50f * effectLevel);
        if (effectLevel > 0f) {
            ship.setWeaponGlow(effectLevel, Misc.setAlpha(JITTER_UNDER_COLOR, 255), EnumSet.allOf(WeaponAPI.WeaponType.class));
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id
    ) {
        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getEnergyWeaponRangeBonus().unmodify(id);
        stats.getEnergyWeaponFluxCostMod().unmodify(id);

    }

    @Override
    public StatusData getStatusData(int index, State state,
            float effectLevel
    ) {
        if (index == 0) {
            return new StatusData("engine power decreased of 50%", false);
        }
        if (index == 1) {
            return new StatusData("energy weapon range increased of 15%", false);
        }
        if (index == 2) {
            return new StatusData("energy weapon flux decreased of 50%", false);
        }
        return null;
    }
}
