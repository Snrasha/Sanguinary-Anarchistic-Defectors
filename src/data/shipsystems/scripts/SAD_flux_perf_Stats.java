package src.data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.EnumSet;

public class SAD_flux_perf_Stats implements ShipSystemStatsScript {

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
        /*if ((effectLevel > 0f || effectLevel < 0.9f)) {

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
                        weapon.getSprite().setHeight(hapyheight - (effectLevel * 4));
                        weapon.getSprite().setCenter(widthS - (0.10f * widthS * effectLevel), heightS + (0.50f * heightS * effectLevel));
                        break;
                    case "LEFT":
                        widthS = weapon.getSprite().getWidth() / 2;
                        heightS = weapon.getSprite().getHeight() / 2;
                        weapon.getSprite().setHeight(hapyheight - (effectLevel * 4));
                        weapon.getSprite().setCenter(widthS + (0.10f * widthS * effectLevel), heightS + (0.50f * heightS * effectLevel));
                        break;
                }

            }
        }*/

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
            return new StatusData("engine power redirected", false);
        }
        return null;
    }
}
