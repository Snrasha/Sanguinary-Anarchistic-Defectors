package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.BattleObjectiveAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_pmineAI implements com.fs.starfarer.api.combat.ShipSystemAIScript
{
  private static final String MINE_ID = "ms_pmine";
  private static final float MINE_WITHIN_RANGE = 500.0F;
  private static final int MAX_MINES_PER_OBJECTIVE = 2;
  private ShipAPI ship;
  
  public MS_pmineAI() {}
  
  private static int getMinesAroundObjective(BattleObjectiveAPI objective, int owner)
  {
    int totalMines = 0;
    
    for (DamagingProjectileAPI tmp : org.lazywizard.lazylib.combat.CombatUtils.getProjectilesWithinRange(objective.getLocation(), 500.0F)) {
      if ((tmp.getOwner() == owner) && ("ms_pmine".equals(tmp.getProjectileSpecId()))) {
        totalMines++;
      }
    }
    
    return totalMines;
  }
  
  public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine)
  {
    this.ship = ship;
  }
  
  public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target)
  {
    if (!AIUtils.canUseSystemThisFrame(ship)) {
      return;
    }
    
    BattleObjectiveAPI nearestObjective = AIUtils.getNearestObjective(ship);
    

    if (nearestObjective == null) {
      return;
    }
    

    if (org.lazywizard.lazylib.MathUtils.getDistance(ship.getLocation(), nearestObjective.getLocation()) < 500.0F)
    {
      if (getMinesAroundObjective(nearestObjective, ship.getOwner()) < 2) {
        ship.useSystem();
      }
    }
  }
}
