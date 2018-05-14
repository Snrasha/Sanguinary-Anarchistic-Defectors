package src.data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class SAD_DisturbSystem extends BaseShipSystemScript {

    private CombatEngineAPI engine;
    public static final Object KEY_JITTER = new Object();

    private static final Color COLOR1 = new Color(210, 125, 105, 155);

    public static final Color JITTER_UNDER_COLOR = new Color(255, 50, 0, 125);
    public static final Color JITTER_COLOR = new Color(255, 50, 0, 75);

    public static final float ACCURACY_BONUS = 20.0F;

    public static final float RANGE_BONUS = 10.0F;
    public static final float DAMAGE_BOOST = 33.0F;
    public static final float AGILITY_BONUS = 15.0F;
    private static final Vector2f ZERO = new Vector2f();


    public SAD_DisturbSystem() {
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
        if (engine != Global.getCombatEngine()) {
            engine = Global.getCombatEngine();
        }
        if (engine.isPaused()) {
            return;
        }

        ShipAPI ship = null;
        if ((stats.getEntity() instanceof ShipAPI)) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        float jitterLevel;
        float jitterRangeBonus;
        if (effectLevel > 0.0F) {
            jitterLevel = effectLevel;
            float maxRangeBonus = 500f;
            jitterRangeBonus = jitterLevel * maxRangeBonus;

            if (jitterLevel > 0.0F) {
                for (int i = 0; i < 30; i++) {
                    float size = MathUtils.getRandomNumberInRange(8.0F, 2.0F);
                    Vector2f spawn = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius()+jitterRangeBonus);

                    //if (Math.random() > 0.9) {
                        engine.addSmoothParticle(spawn, ZERO, size, (float) Math.random() * 1.0F, 1.0F, COLOR1);
                   // }
                }

            }
        }
    }

    public void init(CombatEngineAPI engine, ShipAPI host) {
        this.engine = engine;
    }/*
    private static final Random rng = new Random();

    public static Vector2f getRandomPointInCircle(Vector2f center, float raduismin, float radiusmax) {
        final double t = 2 * Math.PI * rng.nextDouble(),
                u = rng.nextDouble() + rng.nextDouble(),
                r = (u > 1 ? 2 - u : u);
        
        return new Vector2f((float) (r * FastTrig.cos(t)) * radiusmax + (center == null ? 0f : center.x),
                (float) (r * FastTrig.sin(t)) * radiusmax + (center == null ? 0f : center.y));
    }*/
}
