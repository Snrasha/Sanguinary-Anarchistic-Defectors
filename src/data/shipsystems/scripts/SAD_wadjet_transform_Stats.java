package src.data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import java.util.Iterator;

public class SAD_wadjet_transform_Stats implements ShipSystemStatsScript {

    private CombatEngineAPI engine;

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

        stats.getMaxSpeed().modifyPercent(id, -80f * effectLevel);
        stats.getAcceleration().modifyPercent(id, -80f * effectLevel);
        //stats.getDeceleration().modifyPercent(id, -80f * effectLevel);
        stats.getShieldDamageTakenMult().modifyPercent(id,-50f* effectLevel);
        stats.getEnergyWeaponRangeBonus().modifyPercent(id, 50f * effectLevel);
        stats.getEnergyRoFMult().modifyPercent(id, 50f * effectLevel);
        stats.getEnergyWeaponFluxCostMod().modifyPercent(id, -90f * effectLevel);

        if ((effectLevel > 0f || effectLevel < 0.9f)) {

            Iterator<WeaponAPI> iter = ship.getAllWeapons().iterator();
            WeaponAPI weapon;
            float widthS;
            float heightS;
            while (iter.hasNext()) {
                weapon = iter.next();
                switch (weapon.getSlot().getId()) {
                    case "COCKPIT":
                       
                        widthS = weapon.getSprite().getWidth() / 2;
                        heightS = weapon.getSprite().getHeight() / 2;
                        weapon.getSprite().setCenter(widthS, heightS + (1f * heightS * effectLevel));
                        break;
                    case "LEFT":
                        widthS = weapon.getSprite().getWidth() / 2;
                        heightS = weapon.getSprite().getHeight() / 2;
                        weapon.getSprite().setCenter(widthS, heightS - (1f * heightS * effectLevel));
                        break;
                    case "RIGHT":
                        widthS = weapon.getSprite().getWidth() / 2;
                        heightS = weapon.getSprite().getHeight() / 2;
                        weapon.getSprite().setCenter(widthS, heightS - (1f * heightS * effectLevel));
                        break;
                }

            }
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id
    ) {
        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
        //stats.getDeceleration().unmodify(id);
        stats.getEnergyWeaponRangeBonus().unmodify(id);
        stats.getEnergyRoFMult().unmodify(id);
        stats.getEnergyWeaponFluxCostMod().unmodify(id);
        stats.getShieldDamageTakenMult().unmodify(id);

    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        return null;
    }

}
