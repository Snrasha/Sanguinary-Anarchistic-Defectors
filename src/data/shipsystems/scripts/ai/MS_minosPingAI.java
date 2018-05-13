package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.List;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;



public class MS_minosPingAI
  implements ShipSystemAIScript
{
  private ShipAPI ship;
  private ShipSystemAPI system;
  private final IntervalUtil tracker = new IntervalUtil(0.35F, 0.6F);
  private CombatEngineAPI engine;
  
  public MS_minosPingAI() {}
  
  public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) { this.ship = ship;
    this.system = system;
  }
  
  public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target)
  {
    if (engine == null) {
      return;
    }
    
    if (engine.isPaused()) {
      return;
    }
    

    tracker.advance(amount);
    

    if (tracker.intervalElapsed()) {
      if (!AIUtils.canUseSystemThisFrame(ship)) {
        return;
      }
      
      List<ShipAPI> friends = AIUtils.getAlliesOnMap(ship);
      List<ShipAPI> enemies = AIUtils.getEnemiesOnMap(ship);
      
      float totalFriends = 0.0F;
      float allyECM = 0.0F;
      float enemyECM = 0.0F;
      
      for (ShipAPI friend : friends) {
        if ((!friend.isHulk()) || (!friend.isFighter())) {
          totalFriends += 1.0F;
          allyECM += getMutableStatsgetEccmChancebase;
        }
      }
      
      for (ShipAPI enemy : enemies) {
        if ((!enemy.isHulk()) || (!enemy.isFighter())) {
          enemyECM += getMutableStatsgetEccmChancebase;
        }
      }
      
      float sensorRange = ship.getMutableStats().getSensorStrength().base;
      List<ShipAPI> detected = AIUtils.getNearbyEnemies(ship, sensorRange);
      


      if (((detected.isEmpty()) && (!system.isActive())) || ((enemyECM > allyECM) && (allyECM + totalFriends > enemyECM) && 
        (!system.isActive()))) {
        ship.useSystem();
      }
    }
  }
}
