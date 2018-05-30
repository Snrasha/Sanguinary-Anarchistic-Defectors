package src.data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import java.awt.Color;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import src.data.scripts.plugins.SAD_DisruptSystemEffect;

public class SAD_DisturbSystem extends BaseShipSystemScript {

    private CombatEngineAPI engine;
    private static final Color COLOR1 = new Color(210, 125, 105, 155);
    private static final Color COLOR2 = new Color(105, 125, 210, 155);

    public static final float RANGE_BONUS = 1000f;
    private static final Vector2f ZERO = new Vector2f();


    @Override
    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
        if (engine ==null) {
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
            jitterRangeBonus = jitterLevel * RANGE_BONUS;

            if (jitterLevel > 0.0F) {
                for (int i = 0; i < 30; i++) {
                    float size = MathUtils.getRandomNumberInRange(8.0F, 2.0F);
                    Vector2f spawn = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius() + jitterRangeBonus);

                    //if (Math.random() > 0.9) {
                    engine.addSmoothParticle(spawn, ZERO, size, (float) Math.random() * 1.0F, 1.0F, COLOR1);
                    // }
                }

            }
        }
        if (effectLevel == 1f) {
            List<ShipAPI> list = AIUtils.getNearbyEnemies(ship, RANGE_BONUS + ship.getCollisionRadius());

            for (ShipAPI target : list) {
                if ((!target.isAlive()) || target.isFighter()) {
                    continue;
                }
                SAD_DisruptSystemEffect.putTELEMETRY(target);
            }
            for (int i = 0; i < 30; i++) {
                    float size = MathUtils.getRandomNumberInRange(10.0F, 6.0F);
                    Vector2f spawn = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius() + RANGE_BONUS);

                    engine.addSmoothParticle(spawn, ZERO, size, (float) Math.random() * 1.0f, 1.0F, COLOR2);
                }

        }
    }

}
