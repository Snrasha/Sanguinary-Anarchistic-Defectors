package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FluxTrackerAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_fighterFlareAI implements ShipSystemAIScript
{
  private ShipAPI ship;
  private ShipSystemAPI system;
  private final IntervalUtil tracker = new IntervalUtil(0.1F, 0.2F);
  
  public MS_fighterFlareAI() {}
  
  public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) { this.ship = ship;
    this.system = system;
  }
  
  public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target)
  {
    if (Global.getCombatEngine().isPaused()) {
      return;
    }
    
    tracker.advance(amount);
    
    if (tracker.intervalElapsed()) {
      if ((system.isActive()) || (system.getCooldownRemaining() > 0.0F) || (system.getAmmo() <= 0) || (ship.getFluxTracker().isOverloadedOrVenting())) {
        return;
      }
      
      float missilesNearby = 0.0F;
      
      List<MissileAPI> missiles = CombatUtils.getMissilesWithinRange(ship.getLocation(), 450.0F);
      for (MissileAPI missile : missiles) {
        if ((missile.getOwner() != ship.getOwner()) && (!missile.isFlare()))
        {


          WeaponAPI weapon = missile.getWeapon();
          if (weapon == null) {
            if (MathUtils.getShortestRotation(missile.getFacing(), Vector2f.angle(missile.getLocation(), ship.getLocation())) <= 135.0F)
            {


              missilesNearby += 1.5F * missile.getDamageAmount() / 100.0F;
            }
            
          }
          else if ((weapon.hasAIHint(WeaponAPI.AIHints.ANTI_FTR)) || (weapon.hasAIHint(WeaponAPI.AIHints.HEATSEEKER))) {
            missilesNearby += 2.0F * missile.getDamageAmount() / 100.0F;
          } else if (weapon.hasAIHint(WeaponAPI.AIHints.DO_NOT_AIM)) {
            if (MathUtils.getShortestRotation(missile.getFacing(), Vector2f.angle(missile.getLocation(), ship.getLocation())) <= 135.0F)
            {


              missilesNearby += 1.5F * missile.getDamageAmount() / 100.0F; }
          } else if (weapon.hasAIHint(WeaponAPI.AIHints.GUIDED_POOR)) {
            if (MathUtils.getShortestRotation(missile.getFacing(), Vector2f.angle(missile.getLocation(), ship.getLocation())) <= 90.0F)
            {


              missilesNearby += missile.getDamageAmount() / 100.0F; }
          }
        }
      }
      if (missilesNearby >= 30.0F) {
        ship.useSystem();
      }
    }
  }
}
