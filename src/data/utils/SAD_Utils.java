package src.data.utils;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class SAD_Utils {

    static final float SAFE_DISTANCE = 600.0F;
    static final float DEFAULT_DAMAGE_WINDOW = 3.0F;

    public SAD_Utils() {
    }

    public static float estimateIncomingDamage(ShipAPI ship) {
        return estimateIncomingDamage(ship, 3.0F);
    }

    public static float estimateIncomingDamage(ShipAPI ship, float damageWindowSeconds) {
        float accumulator = 0.0F;

        accumulator += estimateIncomingBeamDamage(ship, damageWindowSeconds);

        for (DamagingProjectileAPI proj : Global.getCombatEngine().getProjectiles()) {
            if (proj.getOwner() != ship.getOwner()) {
                Vector2f endPoint = new Vector2f(proj.getVelocity());
                endPoint.scale(damageWindowSeconds);
                Vector2f.add(endPoint, proj.getLocation(), endPoint);

                if (((ship.getShield() == null) || (!ship.getShield().isWithinArc(proj.getLocation())))
                        && (org.lazywizard.lazylib.CollisionUtils.getCollides(proj.getLocation(), endPoint, new Vector2f(ship
                                .getLocation()), ship.getCollisionRadius()))) {

                    accumulator += proj.getDamageAmount() + proj.getEmpAmount();
                }
            }
        }
        return accumulator;
    }

    public static float estimateIncomingBeamDamage(ShipAPI ship, float damageWindowSeconds) {
        float accumulator = 0.0F;

        for (BeamAPI beam : Global.getCombatEngine().getBeams()) {
            if (beam.getDamageTarget() == ship) {
                float dps = beam.getWeapon().getDerivedStats().getDamageOver30Sec() / 30.0F;
                float emp = beam.getWeapon().getDerivedStats().getEmpPerSecond();

                accumulator += (dps + emp) * damageWindowSeconds;
            }
        }
        return accumulator;
    }

    public static float estimateIncomingMissileDamage(ShipAPI ship) {
        float accumulator = 0.0F;

        for (Iterator iter = Global.getCombatEngine().getMissiles().iterator(); iter.hasNext();) {
            DamagingProjectileAPI missile = (DamagingProjectileAPI) iter.next();

            if (missile.getOwner() != ship.getOwner()) {
                float safeDistance = 600.0F + ship.getCollisionRadius();
                float threat = missile.getDamageAmount() + missile.getEmpAmount();

                if ((ship.getShield() == null) || (!ship.getShield().isWithinArc(missile.getLocation()))) {

                    accumulator = (float) (accumulator + threat * Math.max(0.0D, Math.min(1.0D, Math.pow(1.0F - MathUtils.getDistance(missile, ship) / safeDistance, 2.0D))));
                }
            }
        }
        return accumulator;
    }

    public static float getActualDistance(Vector2f from, CombatEntityAPI target, boolean considerShield) {
        if ((considerShield) && ((target instanceof ShipAPI))) {
            ShipAPI ship = (ShipAPI) target;
            ShieldAPI shield = ship.getShield();
            if ((shield != null) && (shield.isOn()) && (shield.isWithinArc(from))) {
                return MathUtils.getDistance(from, shield.getLocation()) - shield.getRadius();
            }
        }
        return MathUtils.getDistance(from, target.getLocation()) - com.fs.starfarer.api.util.Misc.getTargetingRadius(from, target, false);
    }

    public static List<ShipAPI> getSortedAreaList(Vector2f loc, List<ShipAPI> list) {
        List<ShipAPI> out;

        out = new ArrayList(list);
        Collections.sort(out, new SortShipsByDistance(loc));
        return out;
    }

    private static class SortShipsByDistance implements java.util.Comparator<ShipAPI> {

        private final Vector2f loc;

        SortShipsByDistance(Vector2f loc) {
            this.loc = loc;
        }

        public int compare(ShipAPI s1, ShipAPI s2) {
            float dist1;
            if ((s1.getShield() != null) && (s1.getShield().isOn()) && (s1.getShield().isWithinArc(loc))) {
                dist1 = MathUtils.getDistance(s1.getLocation(), loc) - s1.getShield().getRadius();
                dist1 *= dist1;
            } else {
                dist1 = MathUtils.getDistanceSquared(s1.getLocation(), loc);
            }
            float dist2;
            if ((s2.getShield() != null) && (s2.getShield().isOn()) && (s2.getShield().isWithinArc(loc))) {
                dist2 = MathUtils.getDistance(s2.getLocation(), loc) - s2.getShield().getRadius();
                dist2 *= dist1;
            } else {
                dist2 = MathUtils.getDistanceSquared(s2.getLocation(), loc);
            }
            return Float.compare(dist1, dist2);
        }
    }

    public static boolean isWithinEmpRange(Vector2f loc, float dist, ShipAPI ship) {
        float distSq = dist * dist;
        if ((ship.getShield() != null) && (ship.getShield().isOn()) && (ship.getShield().isWithinArc(loc))) {
            if (MathUtils.getDistance(ship.getLocation(), loc) - ship.getShield().getRadius() <= dist) {
                return true;
            }
        }

        for (WeaponAPI weapon : ship.getAllWeapons()) {
            if ((!weapon.getSlot().isHidden()) && (weapon.getSlot().getWeaponType() != WeaponAPI.WeaponType.DECORATIVE) && (weapon.getSlot().getWeaponType() != WeaponAPI.WeaponType.LAUNCH_BAY)) {
                if (weapon.getSlot().getWeaponType() != WeaponAPI.WeaponType.SYSTEM) {
                    if (MathUtils.getDistanceSquared(weapon.getLocation(), loc) <= distSq) {
                        return true;
                    }
                }
            }
        }
        for (ShipEngineControllerAPI.ShipEngineAPI engine : ship.getEngineController().getShipEngines()) {
            if (!engine.isSystemActivated()) {
                if (MathUtils.getDistanceSquared(engine.getLocation(), loc) <= distSq) {
                    return true;
                }
            }
        }

        return false;
    }
}
