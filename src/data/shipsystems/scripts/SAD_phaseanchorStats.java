package src.data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class SAD_phaseanchorStats extends BaseShipSystemScript {

    protected Object STATUSKEY2 = new Object();

    private void maintainStatus(ShipAPI playerShip, ShipSystemStatsScript.State state, float effectLevel) {
        float level = effectLevel;
        float f = 0.0F;

        ShipSystemAPI cloak = playerShip.getPhaseCloak();
        if (cloak == null) {
            cloak = playerShip.getSystem();
        }
        if (cloak == null) {
            return;
        }
        if (level > f) {

            Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY2, cloak
                    .getSpecAPI().getIconSpriteName(), cloak.getDisplayName(), "time flow altered", false);
        }
    }

    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
        ShipAPI ship = null;
        boolean player = false;
        if ((stats.getEntity() instanceof ShipAPI)) {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
            id = id + "_" + ship.getId();
        } else {
            return;
        }

        if (player) {
            maintainStatus(ship, state, effectLevel);
        }

        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        if ((state == ShipSystemStatsScript.State.COOLDOWN) || (state == ShipSystemStatsScript.State.IDLE)) {
            unapply(stats, id);
            return;
        }

        float level = effectLevel;

        float levelForAlpha = level;

        ShipSystemAPI cloak = ship.getPhaseCloak();
        if (cloak == null) {
        }
        if ((state == ShipSystemStatsScript.State.IN) || (state == ShipSystemStatsScript.State.ACTIVE)) {
            ship.setPhased(true);
            levelForAlpha = level;

            stats.getAcceleration().modifyFlat(id, 60f * effectLevel);
            stats.getDeceleration().modifyFlat(id, 60f * effectLevel);

            stats.getTurnAcceleration().modifyPercent(id, 180.0F * effectLevel);
            stats.getMaxTurnRate().modifyPercent(id, 140.0F);

            stats.getMaxSpeed().modifyFlat(id, 60f * effectLevel);
        } else if (state == ShipSystemStatsScript.State.OUT) {
            ship.setPhased(true);
            levelForAlpha = level;

            stats.getMaxSpeed().unmodify(id);
            stats.getMaxTurnRate().unmodify(id);
        }

        ship.setExtraAlphaMult(1.0F - 0.75F * levelForAlpha);
        ship.setApplyExtraAlphaToEngines(true);

        float shipTimeMult = 1.0F + 2.0F * levelForAlpha;
        stats.getTimeMult().modifyMult(id, shipTimeMult);
        if (player) {
            Global.getCombatEngine().getTimeMult().modifyMult(id, 1.0F / shipTimeMult);
        } else {
            Global.getCombatEngine().getTimeMult().unmodify(id);
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship;

        if ((stats.getEntity() instanceof ShipAPI)) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        Global.getCombatEngine().getTimeMult().unmodify(id);
        stats.getTimeMult().unmodify(id);

        ship.setPhased(false);
        ship.setExtraAlphaMult(1.0F);

        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
    }

    @Override
    public StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {
        return null;
    }

}
