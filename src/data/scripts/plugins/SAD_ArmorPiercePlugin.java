package src.data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class SAD_ArmorPiercePlugin
        extends BaseEveryFrameCombatPlugin {

    private static CombatEngineAPI engine;
    private static final Map<String, CollisionClass> originalCollisionClasses = new HashMap();

    private static final String PIERCE_SOUND = "explosion_missile";

    private static final String PROJ_IDS = "SAD_rhpcblast";

    private static final Color COLOR1 = new Color(165, 215, 145, 150);
    private static final Color COLOR2 = new Color(155, 255, 155, 150);

    private static final Vector2f ZERO = new Vector2f();

  //  private static final Set<String> PROJ_IDS = new HashSet();

    private float MAX_DAMAGE;

    private float DAMAGE_PER_TICK;
    private float EMP_PER_TICK;
    private float DAMAGE_TOTAL;
    private final IntervalUtil interval = new IntervalUtil(0.05F, 0.05F);
    private boolean runOnce = false;
/*
    static {
        PROJ_IDS.add("ms_rhpcblast");
    }*/

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine != Global.getCombatEngine()) {
            engine = Global.getCombatEngine();
            originalCollisionClasses.clear();
        }

        if (engine.isPaused()) {
            return;
        }

        interval.advance(amount);
        DamagingProjectileAPI proj;
        String spec;

        for (Iterator localIterator1 = engine.getProjectiles().iterator(); localIterator1.hasNext();) {
            proj = (DamagingProjectileAPI) localIterator1.next();
            spec = proj.getProjectileSpecId();

            if (PROJ_IDS.equals(spec)) {

                if (!runOnce) {
                    runOnce = true;

                    MAX_DAMAGE = proj.getDamageAmount();
                    DAMAGE_PER_TICK = (MAX_DAMAGE * 0.2F);
                    EMP_PER_TICK = (proj.getEmpAmount() * 0.2F);
                }

                if (!originalCollisionClasses.containsKey(spec)) {
                    originalCollisionClasses.put(spec, proj.getCollisionClass());
                }

                proj.setCollisionClass(CollisionClass.NONE);

                Vector2f point = new Vector2f(-50.0F, 0.0F);
                VectorUtils.rotate(point, proj.getFacing(), point);
                Vector2f.add(point, proj.getLocation(), point);

                Vector2f spawn = MathUtils.getRandomPointInCircle(proj.getLocation(), proj.getCollisionRadius());
                float size = MathUtils.getRandomNumberInRange(20.0F, 10.0F);
                float sharpDur = MathUtils.getRandomNumberInRange(0.2F, 0.6F);
                float smoothDur = MathUtils.getRandomNumberInRange(0.1F, 0.4F);

                if ((Math.random() > 0.08D) && (!engine.isPaused())) {
                    engine.addHitParticle(spawn, ZERO, size, MathUtils.getRandomNumberInRange(1.0F, 2.0F), sharpDur, COLOR2);
                }
                if ((Math.random() > 0.04D) && (!engine.isPaused())) {
                    engine.addSmoothParticle(spawn, ZERO, size * 2.0F, MathUtils.getRandomNumberInRange(0.5F, 1.0F), smoothDur, COLOR1);
                }
                if ((Math.random() > 0.15D) && (!engine.isPaused())) {
                    engine.spawnEmpArc(proj.getSource(), spawn, null, new SimpleEntity(point), DamageType.ENERGY, 0.0F, 0.0F, 10000.0F, null, 10.0F, COLOR1, COLOR1);
                }

                List<CombatEntityAPI> toCheck = new ArrayList();
                toCheck.addAll(CombatUtils.getShipsWithinRange(proj.getLocation(), proj.getCollisionRadius() + 5.0F));
                toCheck.addAll(CombatUtils.getMissilesWithinRange(proj.getLocation(), proj.getCollisionRadius() + 5.0F));
                toCheck.addAll(CombatUtils.getAsteroidsWithinRange(proj.getLocation(), proj.getCollisionRadius() + 5.0F));

                toCheck.remove(proj.getSource());
                for (CombatEntityAPI entity : toCheck) {
                    if (entity.getCollisionClass() != CollisionClass.NONE) {

                        if ((entity.getShield() != null) && (entity.getShield().isOn()) && (entity.getShield().isWithinArc(proj.getLocation()))) {
                            proj.setCollisionClass((CollisionClass) originalCollisionClasses.get(spec));
                        } else if (CollisionUtils.isPointWithinBounds(proj.getLocation(), entity)) {
                            float speed = proj.getVelocity().length();

                            if (interval.intervalElapsed()) {
                                DAMAGE_TOTAL += DAMAGE_PER_TICK;
                                float DAMAGE_REMAINING = MAX_DAMAGE - DAMAGE_TOTAL;

                                engine.applyDamage(entity, proj.getLocation(), DAMAGE_PER_TICK, proj.getDamageType(), EMP_PER_TICK, true, true, proj.getSource());
                                proj.setDamageAmount(DAMAGE_REMAINING);

                                engine.spawnExplosion(proj.getLocation(), entity.getVelocity(), COLOR1, speed * amount * 2.0F, 0.5F);

                                Global.getSoundPlayer().playLoop(PIERCE_SOUND, proj, 1.0F, 1.0F, proj.getLocation(), entity.getVelocity());

                                if (DAMAGE_REMAINING <= 0.0F) {
                                    DAMAGE_PER_TICK = 0.0F;
                                    proj.setCollisionClass((CollisionClass) originalCollisionClasses.get(spec));
                                    proj.isFading();
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    @Override
    public void renderInWorldCoords(ViewportAPI viewport) {
        if (engine == null) {
            return;
        }

        float x,y;
        for (DamagingProjectileAPI proj : engine.getProjectiles()) {
            String spec = proj.getProjectileSpecId();

            if (PROJ_IDS.equals(spec)) {

                
                x = proj.getLocation().getX();
                y =  proj.getLocation().getY();

                SpriteAPI sprite = Global.getSettings().getSprite("flare", "nidhoggr_ALF");

                if (!engine.isPaused()) {
                    sprite.setAlphaMult(MathUtils.getRandomNumberInRange(0.9F, 1.0F));
                } else {
                    float tAlf = sprite.getAlphaMult();
                    sprite.setAlphaMult(tAlf);
                }
                sprite.setSize(800.0F, 100.0F);
                sprite.setAdditiveBlend();
                sprite.renderAtCenter(x, y);
            }
        }
    }


}
