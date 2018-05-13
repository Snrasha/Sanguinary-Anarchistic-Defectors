package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.Iterator;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_telemetryAI implements ShipSystemAIScript
{
  private ShipAPI ship;
  private CombatEngineAPI engine;
  private ShipwideAIFlags flags;
  private ShipSystemAPI system;
  private float nominalRange = 0.0F;
  private float activeRange = 0.0F;
  
  private final IntervalUtil tracker = new IntervalUtil(0.35F, 0.6F);
  
  private boolean runOnce = false;
  
  public MS_telemetryAI() {}
  
  public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) { this.ship = ship;
    this.flags = flags;
    this.engine = engine;
    this.system = system;
  }
  
  public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target)
  {
    if ((engine.isPaused()) || (ship.getShipAI() == null)) {
      return;
    }
    WeaponAPI w;
    if (!runOnce) {
      runOnce = true;
      List<WeaponAPI> weapons = ship.getAllWeapons();
      int i = 0;
      for (Iterator localIterator1 = weapons.iterator(); localIterator1.hasNext();) { w = (WeaponAPI)localIterator1.next();
        if (((w.getType() == WeaponAPI.WeaponType.ENERGY) || (w.getType() == WeaponAPI.WeaponType.BALLISTIC) || (w.getType() != WeaponAPI.WeaponType.MISSILE)) && (w.getRange() > 200.0F) && (!w.hasAIHint(WeaponAPI.AIHints.PD))) {
          nominalRange += w.getRange();
          i++;
        }
      }
      nominalRange /= i;
      activeRange = (nominalRange * 1.33F);
    }
    
    tracker.advance(amount);
    Vector2f shipLoc = new Vector2f(ship.getLocation());
    
    if (tracker.intervalElapsed()) {
      if (!AIUtils.canUseSystemThisFrame(ship)) {
        return;
      }
      
      if (target == null) { return;
      }
      boolean shouldUseSystem = false;
      
      Object nearbyShips = org.lazywizard.lazylib.combat.CombatUtils.getShipsWithinRange(shipLoc, activeRange);
      for (ShipAPI tracking : (List)nearbyShips) {
        float closestDistance = Float.MAX_VALUE;
        for (ShipAPI tmp : AIUtils.getNearbyEnemies(ship, activeRange))
        {
          float distance = MathUtils.getDistance(tmp, ship.getLocation());
          if (distance < closestDistance)
          {
            closestDistance = distance;
          }
        }
        


        if ((tracking.getOwner() != ship.getOwner()) && (!tracking.isHulk()) && (!tracking.isFighter()) && 
          ((((List)nearbyShips).isEmpty()) || (closestDistance >= nominalRange)) && (!((List)nearbyShips).isEmpty()))
        {


          if ((!((List)nearbyShips).isEmpty()) && (closestDistance > nominalRange) && (closestDistance < activeRange)) {
            shouldUseSystem = true;
          }
        }
      }
      if ((ship.getSystem().isActive() ^ shouldUseSystem)) {
        ship.useSystem();
      }
    }
  }
}
