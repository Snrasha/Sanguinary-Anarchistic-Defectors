package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundPlayerAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_WavebeamOnHitEffect
  implements OnHitEffectPlugin
{
  private static final String SOUND_ID = "disabled_large_crit";
  private static final DamageType[] TYPES = { DamageType.ENERGY };
  

  private static final float EXTRA_DAMAGE_CHANCE = 0.3F;
  

  private static final Color EXPLOSION_COLOR = new Color(125, 155, 115, 255);
  private static final Color PARTICLE_COLOR = new Color(125, 155, 115, 255);
  private static final int NUM_PARTICLES = 14;
  private static final float MIN_EXTRA_DAMAGE = 200.0F;
  private static final float MAX_EXTRA_DAMAGE = 300.0F;
  
  public MS_WavebeamOnHitEffect() {}
  
  public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, CombatEngineAPI engine) {
    if (((target instanceof ShipAPI)) && (!shieldHit) && (Math.random() <= 0.30000001192092896D))
    {
      engine.applyDamage(target, point, 
        MathUtils.getRandomNumberInRange(200.0F, 300.0F), TYPES[
        ((int)(Math.random() * TYPES.length))], 0.0F, false, true, projectile
        .getSource());
      

      engine.spawnExplosion(point, (Vector2f)new Vector2f(target.getVelocity()).scale(0.48F), EXPLOSION_COLOR, 39.0F, 1.0F);
      float speed = projectile.getVelocity().length();
      float facing = 400.0F;
      for (int x = 0; x < 14; x++) {
        engine.addHitParticle(point, MathUtils.getPointOnCircumference(null, 
          MathUtils.getRandomNumberInRange(speed * 0.007F, speed * 0.17F), 
          MathUtils.getRandomNumberInRange(facing - 180.0F, facing + 180.0F)), 5.0F, 1.0F, 1.6F, PARTICLE_COLOR);
      }
      


      Global.getSoundPlayer().playSound("disabled_large_crit", 1.1F, 0.5F, target.getLocation(), target.getVelocity());
    }
  }
}
