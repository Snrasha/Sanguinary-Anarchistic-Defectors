package src.data.scripts.plugins;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.lwjgl.util.vector.Vector2f;

public class SAD_DisruptSystemEffect extends BaseEveryFrameCombatPlugin {

    private final float damageMalus = 1.5f;

    public static Map<ShipAPI, Float> telemetry = new java.util.HashMap();

    private final float maxcompt = 2f;
    private float compt = 0;
    private CombatEngineAPI engine;
  

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        SAD_DisruptSystemEffect.telemetry = new java.util.HashMap();
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }
        if (telemetry == null || engine.isPaused()) {
            return;
        }

        if (!telemetry.isEmpty()) {
            compt += amount;
            Map<ShipAPI, Float> telemetry2 = new java.util.HashMap();
            Iterator<Entry<ShipAPI, Float>> iter = telemetry.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<ShipAPI, Float> entry = iter.next();
                ShipAPI ship = (ShipAPI) entry.getKey();
                String id = ship.getFleetMemberId() + "_disrupt";
                Vector2f loc = ship.getLocation();
                Vector2f vel = ship.getVelocity();

                float remaining = (entry.getValue()) - amount;

                if (remaining < 0.0F) {
                    //telemetry.clear();

                    ship.getMutableStats().getArmorDamageTakenMult().unmodify(id);
                    ship.getMutableStats().getHullDamageTakenMult().unmodify(id);
                    ship.getMutableStats().getShieldDamageTakenMult().unmodify(id);
                } else {
                    telemetry2.put(ship, remaining);

                    if (maxcompt < compt) {
                        SAD_effectsHook.createPing(loc, vel);
                    }
                    ship.getMutableStats().getArmorDamageTakenMult().modifyMult(id, damageMalus);
                    ship.getMutableStats().getHullDamageTakenMult().modifyMult(id, damageMalus);
                    ship.getMutableStats().getShieldDamageTakenMult().modifyMult(id, damageMalus);
                }
            }
            if (maxcompt < compt) {
                compt = 0;
            }
            telemetry.clear();
            telemetry.putAll(telemetry2);
        }
    }

    public static synchronized void putTELEMETRY(ShipAPI ship) {
        telemetry.put(ship, 10.0f);
    }
}
