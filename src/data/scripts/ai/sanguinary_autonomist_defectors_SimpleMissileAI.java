package src.data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class sanguinary_autonomist_defectors_SimpleMissileAI implements MissileAIPlugin, GuidedMissileAI {

    private CombatEngineAPI engine;
    private final MissileAPI missile;
    private CombatEntityAPI target;
    private Vector2f lead = new Vector2f(0.0F, 0.0F);
    private boolean overshoot = false;
    private boolean runOnce = false;
    private final float flightSpeed;

    public sanguinary_autonomist_defectors_SimpleMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        this.missile = missile;
        flightSpeed = missile.getMaxSpeed();
    }

    @Override
    public void advance(float amount) {
        if (engine != Global.getCombatEngine()) {
            engine = Global.getCombatEngine();
        }

        if (!runOnce) {
            setTarget(assignTarget(missile));
            runOnce = true;
        }

        if ((Global.getCombatEngine().isPaused()) || (overshoot) || (missile.isFading()) || (missile.isFizzling()) || (target == null)) {
            return;
        }

        lead = AIUtils.getBestInterceptPoint(missile.getLocation(), flightSpeed, target.getLocation(), target.getVelocity());

        if (lead == null) {
            return;
        }

        float aimAngle = MathUtils.getShortestRotation(missile.getFacing(), VectorUtils.getAngle(missile.getLocation(), lead));

        if (Math.abs(aimAngle) > 90.0F) {
            if (aimAngle < 0.0F) {
                overshoot = true;
            }
        } else {
            missile.giveCommand(ShipCommand.ACCELERATE);
            if (aimAngle < 0.0F) {
                missile.giveCommand(ShipCommand.TURN_RIGHT);
            } else {
                missile.giveCommand(ShipCommand.TURN_LEFT);
            }
        }

        if (Math.abs(aimAngle) < Math.abs(missile.getAngularVelocity()) * 0.1F) {
            missile.setAngularVelocity(aimAngle / 0.1F);
        }
    }

    public CombatEntityAPI assignTarget(MissileAPI missile) {
        ShipAPI source = missile.getSource();
        ShipAPI currentTarget = source.getShipTarget();

        if ((currentTarget != null)
                && (!currentTarget.isFighter())
                && (!currentTarget.isDrone())
                && (currentTarget.isAlive())
                && (currentTarget.getOwner() != missile.getOwner())) {
            if (MathUtils.getShortestRotation(missile.getFacing(), VectorUtils.getAngle(missile.getLocation(), currentTarget.getLocation())) < 60.0F) {
                return currentTarget;
            }
        }
        ShipAPI closest = null;
        float closestDistance = Float.MAX_VALUE;

        for (ShipAPI tmp : AIUtils.getNearbyEnemies(missile, 2000.0F)) {

            if (MathUtils.getShortestRotation(missile.getFacing(), VectorUtils.getAngle(missile.getLocation(), tmp.getLocation())) <= 60.0F) {

                float distance = MathUtils.getDistance(tmp, missile.getLocation());
                if (distance < closestDistance) {
                    closest = tmp;
                    closestDistance = distance;
                }
            }
        }
        return closest;
    }

    @Override
    public CombatEntityAPI getTarget() {
        return target;
    }

    @Override
    public void setTarget(CombatEntityAPI target) {
        this.target = target;
    }

    public void init(CombatEngineAPI engine) {
    }
}
