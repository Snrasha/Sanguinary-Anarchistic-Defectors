package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.BattleObjectiveAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.List;
import org.lazywizard.lazylib.CollectionUtils;
import org.lazywizard.lazylib.CollectionUtils.CollectionFilter;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;


public class MS_afterburnerAI
  implements ShipSystemAIScript
{
  private ShipAPI ship;
  private IntervalUtil tracker = new IntervalUtil(0.1F, 0.5F);
  private float range = 800.0F;
  private final CollectionUtils.CollectionFilter filterGoals = new CollectionUtils.CollectionFilter()
  {
    public boolean accept(Object t) {
      CombatEntityAPI entity = (CombatEntityAPI)t;
      

      if (((entity instanceof DamagingProjectileAPI)) || ((entity instanceof MissileAPI))) {
        return false;
      }
      

      return (entity == ship.getShipTarget()) || ((entity instanceof BattleObjectiveAPI));
    }
  };
  
  public MS_afterburnerAI() {}
  
  public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
    this.ship = ship;
  }
  

  public void advance(float amount, Vector2f position, Vector2f collisionDanger, ShipAPI target)
  {
    tracker.advance(amount);
    Vector2f shipLoc = ship.getLocation();
    
    if (tracker.intervalElapsed())
    {
      if (!AIUtils.canUseSystemThisFrame(ship)) {
        return;
      }
      
      boolean shouldUseSystem = true;
      
      List<CombatEntityAPI> goalThings = CombatUtils.getEntitiesWithinRange(shipLoc, range);
      goalThings = CollectionUtils.filter(goalThings, filterGoals);
      goalThings.addAll(AIUtils.getEnemiesOnMap(ship));
      
      if (!goalThings.isEmpty()) {
        shouldUseSystem = false;
      }
      


      if (shouldUseSystem) {
        ship.useSystem();
      }
    }
  }
}
