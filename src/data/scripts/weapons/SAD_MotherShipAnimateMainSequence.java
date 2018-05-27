package src.data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.awt.Color;
import java.util.Iterator;
import org.lazywizard.lazylib.MathUtils;

public class SAD_MotherShipAnimateMainSequence implements EveryFrameWeaponEffectPlugin {

    private WeaponAPI theWeapon;
    private AnimationAPI theFlash;
    private ShipAPI ship;
    private final IntervalUtil anim = new IntervalUtil(0.03F, 0.03F);
    private int frameM = 0;
    private int maxFrameM = 0;
    private float charge;
    private float lastCharge = 0.0F;

    private boolean runOnce = false;
    private boolean fired = false;
    private boolean firing = false;
    private boolean chargeUp = false;
    private boolean chargeDown = false;
    private boolean reset = true;

    public SAD_MotherShipAnimateMainSequence() {
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }
        WeaponAPI w;
        if ((!runOnce) || (ship == null)) {
            runOnce = true;
            ship = weapon.getShip();

            for (Iterator localIterator = ship.getAllWeapons().iterator(); localIterator.hasNext();) {
                w = (WeaponAPI) localIterator.next();
                switch (w.getSlot().getId()) {
                    case "MAIN":
                        theWeapon = w;
                        break;
                    case "MUZZLE":
                        theFlash = w.getAnimation();
                        maxFrameM = theFlash.getNumFrames();
                }
            }
            return;
        }

        float newCharge = weapon.getChargeLevel();

        if ((newCharge > 0.0F) || (firing)) {
            anim.advance(amount);

            if (newCharge > lastCharge) {
                firing = true;
                chargeUp = true;
                chargeDown = false;
                if (reset) {
                    reset = false;
                    charge = 1.0F;
                }
                
            } else {
                chargeUp = false;
                chargeDown = true;
                reset = true;
            }
            lastCharge = newCharge;

            if ((newCharge == 0.0F) && (charge == 0.0F)) {
                firing = false;
            }

            if ((chargeDown) && (charge > 0.0F)) {
                charge = Math.min(1.0F,
                        Math.max(0.0F, charge - charge * amount * 10.0F
                                + ((float) Math.random() - 0.5F) / 20.0F));
            }

            if ((chargeUp) && (anim.intervalElapsed())) {
                charge = 1.0F;
            }

           
            if ((newCharge == 1.0F) && (theWeapon.getAmmo() != 3)) {
           //if( theWeapon.isFiring()){
                fired = true;
                engine.addSmoothParticle(theWeapon
                        .getLocation(), ship
                                .getVelocity(),
                        MathUtils.getRandomNumberInRange(125, 150), 0.25F, 1.0F, new Color(50, 255, 100));

                engine.addHitParticle(theWeapon
                        .getLocation(), ship
                                .getVelocity(),
                        MathUtils.getRandomNumberInRange(50, 75), 1.0F, 0.1F, new Color(200, 255, 200));

                /*

        float pFacing=ship.getFacing();
        for (DamagingProjectileAPI p : engine.getProjectiles()) {
          if (p.getWeapon() == weapon) {
            pFacing = p.getFacing();
            engine.removeEntity(p);
            break;
          }
        }
        Vector2f muzzle_location = new Vector2f(theWeapon.getLocation());
        Vector2f ship_velocity = new Vector2f(theWeapon.getShip().getVelocity());
        


        engine.spawnProjectile(ship, weapon, "SAD_rhpbc_replacement", muzzle_location, pFacing, ship_velocity);
                 */
            }

            if (((fired) || (frameM != 0)) && (anim.intervalElapsed())) {
                fired = false;
                frameM += 1;
                if (frameM == maxFrameM) {
                    frameM = 0;
                }
            }

            theFlash.setFrame(frameM);
        }
    }
}
