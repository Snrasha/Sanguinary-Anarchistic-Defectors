//By Nicke535, tracks damage bonuses for the Benediction
package src.data.scripts.weapons;

import com.fs.starfarer.api.combat.*;
import java.awt.Color;
import java.util.Iterator;

public class SAD_AtanWeaponScript implements EveryFrameWeaponEffectPlugin {

    private boolean init = false;
    private WeaponAPI bleft;
    private WeaponAPI bright;
    private WeaponAPI tleft;
    private WeaponAPI tright;
    private ShipSystemAPI system;
    private ShipAPI ship;
    public static final Object KEY_JITTER = new Object();

    private float powerAcc = 0;
    private float angleAcc = 0;
    private float powerStraf = 0;
    

    private int sensAcc = 1;
    private int sensStraf = 1;
    private float effectLevel = 0;
    private boolean unapply = false;

    public static final float ENGINE_MULT = 0.3f;
    public static final float INCOMING_DAMAGE_MULT = 0.3f;
    public static final float MAXSPEEDBONUS = 50F;

    public static final Color JITTERCOLOR = new Color(255, 165, 90, 155);
    public static final Color JITTERUNDERCOLOR = new Color(255, 165, 90, 55);

    public void init(ShipAPI ship) {
        this.ship = ship;
        this.system = ship.getSystem();
        Iterator<WeaponAPI> iter = ship.getAllWeapons().iterator();
        WeaponAPI weapon;
        while (iter.hasNext()) {
            weapon = iter.next();
            switch (weapon.getSlot().getId()) {
                case "BLEFT":
                    bleft = weapon;
                    break;
                case "BRIGHT":
                    bright = weapon;
                    break;
                case "TLEFT":
                    tleft = weapon;
                    break;
                case "TRIGHT":
                    tright = weapon;
                    break;
            }
        }
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused() || weapon == null || weapon.getShip() == null) {
            return;
        }
        if (!init) {
            init = true;
            init(weapon.getShip());
        }
        ShipEngineControllerAPI engines = ship.getEngineController();

        float amountbuff = amount*10;
        if (ship.getSystem().isActive()) {
            amountbuff *= 4;
        }
        if (engines.isStrafingLeft()) {
            if (sensStraf == 1) {
                sensStraf = -1;
                powerStraf = 0;
            }
            increaseStraf(amountbuff);
        } else if (engines.isStrafingRight()) {
            if (sensStraf == -1) {
                sensStraf = 1;
                powerStraf = 0;
            }
            increaseStraf(amountbuff);
        }
        if (engines.isAccelerating()) {
            if (sensAcc == -1) {
                sensAcc = 1;
                powerAcc = 0;
            }
            increaseAcc(amountbuff);

            angleAcc += amount * 20;
            if (angleAcc > 10) {
                angleAcc = 10;
            }

        } else if (engines.isAcceleratingBackwards()) {
            if (sensAcc == 1) {
                sensAcc = -1;
                powerAcc = 0;
            }
            increaseAcc(amountbuff);
            angleAcc -= amount * 20;
            if (angleAcc < -10) {
                angleAcc = -10;
            }

        }
        if (engines.isDecelerating()) {
            angleAcc += sensAcc * amount * 20;
            if (angleAcc > 10) {
                angleAcc = 10;
            }
            if (angleAcc < -10) {
                angleAcc = -10;
            }
            increaseStraf(-8 * amountbuff);
            increaseAcc(-8 * amountbuff);

        }
        if (ship.getFluxTracker().isOverloaded() || ship.getFluxTracker().isVenting()) {
            if (effectLevel < 1) {
                effectLevel += amount * 4;
                if (effectLevel > 1) {
                    effectLevel = 1;
                }

            }
        } else {
            if (effectLevel > 0) {
                effectLevel -= amount * 2;
                if (effectLevel < 0) {
                    effectLevel = 0;
                }

            }

        }

        String id = system.getId();
        String idSub = "sub_" + id;

        MutableShipStatsAPI stats = ship.getMutableStats();

        
        this.advanceSubSystem(stats, idSub, effectLevel);

        float maxsp = Math.max(Math.abs(powerStraf), Math.abs(powerAcc));

        stats.getMaxSpeed().modifyPercent(id, maxsp * 2*(1f-effectLevel));
        
        ship.getEngineController().extendFlame(id, 1 - 0.5f * effectLevel+maxsp/MAXSPEEDBONUS, 1 - 0.5f * effectLevel+maxsp/MAXSPEEDBONUS, 1 - 0.5f * effectLevel+maxsp/MAXSPEEDBONUS);

        
        bleft.setCurrAngle(ship.getFacing() +angleAcc * 1.5f*(1f-effectLevel)+45*effectLevel);
        tleft.setCurrAngle(ship.getFacing() + angleAcc*(1f-effectLevel)+45*effectLevel);

        bright.setCurrAngle(ship.getFacing() - angleAcc * 1.5f*(1f-effectLevel)-45*effectLevel);
        tright.setCurrAngle(ship.getFacing() - angleAcc*(1f-effectLevel)-45*effectLevel);
        
        
       /* flapflap(amount);
        bleft.setCurrAngle(ship.getFacing() -flapflap*(1f-effectLevel) +angleAcc * 1.5f*(1f-effectLevel)+45*effectLevel);
        tleft.setCurrAngle(ship.getFacing() +flapflap*(1f-effectLevel)+ angleAcc*(1f-effectLevel)+45*effectLevel);

        bright.setCurrAngle(ship.getFacing()+flapflap*(1f-effectLevel) - angleAcc * 1.5f*(1f-effectLevel)-45*effectLevel);
        tright.setCurrAngle(ship.getFacing()-flapflap*(1f-effectLevel) - angleAcc*(1f-effectLevel)-45*effectLevel);
        */
        
        float widthS;
        float heightS;
        widthS = bleft.getSprite().getWidth() / 2;
        heightS = bleft.getSprite().getHeight() / 2;

        float heighm = angleAcc / 10f;
        bleft.getSprite().setCenter(widthS, heightS + heighm*(1f-effectLevel));
        bright.getSprite().setCenter(widthS, heightS + heighm*(1f-effectLevel));

        widthS = tleft.getSprite().getWidth() / 2;
        heightS = tleft.getSprite().getHeight() / 2;

        tleft.getSprite().setCenter(widthS, heightS + heighm*(1f-effectLevel));
        tright.getSprite().setCenter(widthS, heightS + heighm*(1f-effectLevel));
        
    }

    private void increaseStraf(float amount) {
        powerStraf += sensStraf * amount * 10;
        if (powerStraf > MAXSPEEDBONUS) {
            powerStraf = MAXSPEEDBONUS;
        }

        if (powerStraf < -MAXSPEEDBONUS) {
            powerStraf = -MAXSPEEDBONUS;
        }
    }
    /*
    private float flapflap=-10;
    private int flip=1;
    private void flapflap(float amount){
        this.flapflap+=flip*amount*320;
        if(flapflap>10){
            flip=-1;
        }
        if(flapflap<-10){
            flip=1;
        }
    }
*/
    private void increaseAcc(float amount) {
        powerAcc += sensAcc * amount;
        if (powerAcc > MAXSPEEDBONUS) {
            powerAcc = MAXSPEEDBONUS;
        }
        if (powerAcc < -MAXSPEEDBONUS) {
            powerAcc = -MAXSPEEDBONUS;
        }
    }

    private void advanceSubSystem(MutableShipStatsAPI stats, String id, float effectLevel) {

        if (effectLevel > 0) {
            stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, 0f);
            stats.getMaxSpeed().modifyMult(id, 1f - ENGINE_MULT * effectLevel);
            stats.getHullDamageTakenMult().modifyMult(id, 1f - (1f - INCOMING_DAMAGE_MULT) * effectLevel);
            stats.getArmorDamageTakenMult().modifyMult(id, 1f - (1f - INCOMING_DAMAGE_MULT) * effectLevel);
            stats.getEmpDamageTakenMult().modifyMult(id, 1f - (1f - INCOMING_DAMAGE_MULT) * effectLevel);
            ship.setJitter(KEY_JITTER, JITTERCOLOR, effectLevel, 3, 2);
            ship.setJitterUnder(KEY_JITTER, JITTERUNDERCOLOR, effectLevel, 1, 4);
            unapply = false;
            
        } else if (!unapply) {
            stats.getMaxSpeed().unmodify(id);
            stats.getZeroFluxMinimumFluxLevel().unmodify(id);
            stats.getHullDamageTakenMult().unmodify(id);
            stats.getArmorDamageTakenMult().unmodify(id);
            stats.getEmpDamageTakenMult().unmodify(id);
            unapply = true;
        }
    }

}
