package src.data.shipsystems.scripts;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import org.lwjgl.util.vector.Vector2f;
import src.data.scripts.plugins.SAD_DisruptSystemEffect;

public class SAD_TAGOnHitEffect implements com.fs.starfarer.api.combat.OnHitEffectPlugin {

    public SAD_TAGOnHitEffect() {
    }

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, CombatEngineAPI engine) {
        ShipAPI source = projectile.getSource();

        if (((target instanceof ShipAPI)) && (!shieldHit)) {
            ShipAPI ship = (ShipAPI) target;
            String id = ship.getFleetMemberId();

            if ((!ship.isAlive()) || (ship.isAlly()) || (ship.getOwner() == source.getOwner())) {
                return;
            }

            ((SAD_DisruptSystemEffect) projectile.getWeapon().getEffectPlugin()).putTELEMETRY(ship);

        }
    }
}
