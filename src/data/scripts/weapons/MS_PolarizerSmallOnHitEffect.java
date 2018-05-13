package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundPlayerAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import data.scripts.ShadowyardsModPlugin;
import data.scripts.hullmods.TEM_LatticeShield;
import java.awt.Color;
import java.util.Collection;
import org.lwjgl.util.vector.Vector2f;

public class MS_PolarizerSmallOnHitEffect
  implements OnHitEffectPlugin
{
  public static final float empRadius = 100.0F;
  
  public MS_PolarizerSmallOnHitEffect() {}
  
  public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, CombatEngineAPI engine)
  {
    if (point == null) {
      return;
    }
    if ((target instanceof ShipAPI)) {
      ShipAPI ship = (ShipAPI)target;
      
      if ((!shieldHit) && ((!ship.getVariant().getHullMods().contains("tem_latticeshield")) || ((ShadowyardsModPlugin.templarsExist) && (TEM_LatticeShield.shieldLevel(ship) <= 0.0F) && (ship.getVariant().getHullMods().contains("tem_latticeshield")))))
      {
        engine.addSmoothParticle(point, new Vector2f(), 300.0F, 1.0F, 0.75F, new Color(100, 255, 200, 255));
        float emp = projectile.getEmpAmount() * 0.15F;
        float dam = projectile.getDamageAmount() * 0.2F;
        for (int x = 0; x < 2; x++) {
          engine.spawnEmpArc(projectile.getSource(), point, projectile.getDamageTarget(), projectile.getDamageTarget(), DamageType.ENERGY, dam, emp, 100000.0F, null, 20.0F, new Color(100, 255, 200, 255), new Color(200, 255, 255, 255));
        }
        Global.getSoundPlayer().playSound("ms_lemp_shot_impact", 1.0F, 1.0F, point, new Vector2f());
      }
    }
  }
}
