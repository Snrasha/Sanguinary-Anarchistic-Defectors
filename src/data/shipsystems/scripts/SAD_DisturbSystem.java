package src.data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import data.scripts.plugins.MagicRenderPlugin;
import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import src.data.scripts.plugins.SAD_DisruptSystemEffect;

public class SAD_DisturbSystem extends BaseShipSystemScript {

    private CombatEngineAPI engine;

    public static final float RANGE_BONUS = 1000f;
    private SpriteAPI sprite = null;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
        if (engine == null) {
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
        final ViewportAPI view = Global.getCombatEngine().getViewport();
        
        if (effectLevel == 1f) {
            List<ShipAPI> list = AIUtils.getNearbyEnemies(ship, RANGE_BONUS );

            for (ShipAPI target : list) {
                if ((!target.isAlive()) || target.isFighter()) {
                    continue;
                }
                SAD_DisruptSystemEffect.putTELEMETRY(target);
            }
        }
        if (effectLevel > 0) {
            if (sprite == null) {
                sprite = Global.getSettings().getSprite("ping", "SAD_tagPing");
                if(sprite==null)return;
            }
            if (view.isNearViewport(ship.getLocation(), RANGE_BONUS + ship.getCollisionRadius())) {
                if (sprite != null) {
                    
                    sprite.setAlphaMult(effectLevel*0.25f);
                    //sprite.setAdditiveBlend();
                    float raduis=(RANGE_BONUS+ ship.getCollisionRadius())*2;
                    if(state.equals(State.IN)){
                        raduis*=effectLevel;
                    }
                    sprite.setSize(raduis,raduis);
                    MagicRenderPlugin.addSingleframe(sprite, ship.getLocation(), CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
              //      MagicRenderPlugin.addSingleframe(sprite, ship.getLocation());
                }
            }
        }
    }

}
