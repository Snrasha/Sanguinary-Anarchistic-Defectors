package src.data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class SAD_MurtiSoundPlugin implements EveryFrameWeaponEffectPlugin {

    private static final Color CHARGEUP_PARTICLE_COLOR = new Color(130, 190, 160, 100);

    private static final float CHARGEUP_PARTICLE_DISTANCE_MIN = 4.0F;
    private static final float CHARGEUP_PARTICLE_DISTANCE_MAX = 60.0F;
    private static final float CHARGEUP_PARTICLE_SIZE_MIN = 1.0F;
    private static final float CHARGEUP_PARTICLE_SIZE_MAX = 4.0F;
    private static final float CHARGEUP_PARTICLE_ANGLE_SPREAD = 180.0F;
    private static final float CHARGEUP_PARTICLE_DURATION = 0.2F;
    private static final float CHARGEUP_PARTICLE_COUNT_FACTOR = 5.0F;
    private static final float CHARGEUP_PARTICLE_BRIGHTNESS = 0.7F;
    private static final Vector2f ZERO = new Vector2f();

    public static final float MAX_OFFSET = 5.0F;
    public static final float SWEEP_INTERVAL = 0.95F;

    protected float timer = 0.0F;
    protected int dir = 1;

    private float last_charge_level = 0.0F;
    private boolean charging = false;
    private boolean firing = false;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }

        Vector2f point1 = new Vector2f(weapon.getLocation());

        float charge_level = weapon.getChargeLevel();

        if (charging) {
            if ((firing) && (weapon.getCooldownRemaining() <= 0.0F) && (weapon.getChargeLevel() < CHARGEUP_PARTICLE_SIZE_MIN)) {
                charging = false;
                firing = false;
            } else if ((weapon.getChargeLevel() < CHARGEUP_PARTICLE_SIZE_MIN) && (weapon.getCooldownRemaining() <= 0.0F)) {
                if ((charge_level > last_charge_level)
                        && (charge_level > last_charge_level) && (weapon.isFiring())) {
                    int particle_count = (int) (CHARGEUP_PARTICLE_COUNT_FACTOR * charge_level);

                    for (int i = 0; i < particle_count; i++) {
                        float distance = MathUtils.getRandomNumberInRange(CHARGEUP_PARTICLE_DISTANCE_MIN, CHARGEUP_PARTICLE_DISTANCE_MAX);

                        float size = MathUtils.getRandomNumberInRange(CHARGEUP_PARTICLE_SIZE_MIN, CHARGEUP_PARTICLE_SIZE_MAX);

                        float angle = MathUtils.getRandomNumberInRange(-90.0F, 90.0F);

                        Vector2f spawn_location1 = MathUtils.getPointOnCircumference(point1, distance, angle + weapon
                                .getCurrAngle());

                        float speed = distance / CHARGEUP_PARTICLE_DURATION;
                        Vector2f particle_velocity = MathUtils.getPointOnCircumference(weapon
                                .getShip().getVelocity(), speed, CHARGEUP_PARTICLE_ANGLE_SPREAD + angle + weapon
                                        .getCurrAngle());

                        engine.addHitParticle(spawn_location1, particle_velocity, size, CHARGEUP_PARTICLE_BRIGHTNESS, CHARGEUP_PARTICLE_DURATION, CHARGEUP_PARTICLE_COLOR);

                    }

                }

            } else {

                firing = true;

                if (weapon.isFiring()) {
                    Global.getCombatEngine().addHitParticle(point1, ZERO, (float) Math.random() * 8.0F + 8.0F * weapon.getChargeLevel(), weapon.getChargeLevel() * 0.3F, CHARGEUP_PARTICLE_DURATION, new Color(
                            MathUtils.getRandomNumberInRange(200, 255), MathUtils.getRandomNumberInRange(150, 200),
                            MathUtils.getRandomNumberInRange(50, 150), 255));
                }
            }
        } else if ((weapon.getChargeLevel() > 0.0F) && (weapon.getCooldownRemaining() <= 0.0F)) {
            charging = true;
            //Global.getSoundPlayer().playSound("SAD_slowBeamM_charge", 0.5F, 0.5F, point1, weapon.getShip().getVelocity());
        }

        last_charge_level = charge_level;
    }
}
