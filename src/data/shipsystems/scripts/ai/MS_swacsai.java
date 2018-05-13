package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;




public class MS_swacsai
  implements ShipSystemAIScript
{
  private ShipSystemAPI system;
  private ShipAPI ship;
  private final IntervalUtil tracker = new IntervalUtil(1.0F, 1.5F);
  
  public MS_swacsai() {}
  
  public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) { this.ship = ship;
    this.system = system;
  }
  
  public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target)
  {
    tracker.advance(amount);
    float activ_range;
    int ships_friendly;
    if (tracker.intervalElapsed())
    {
      activ_range = 5000.0F;
      
      ships_friendly = 0;
      



      for (ShipAPI shp : Global.getCombatEngine().getShips())
      {
        if ((!shp.isHulk()) || (shp.isFighter()))
        {



          if (MathUtils.getDistance(shp, shp) <= activ_range)
          {
            if (shp.getOwner() == shp.getOwner()) {
              ships_friendly++;
            }
          }
          

          if ((ships_friendly >= 1) && (!system.isActive())) {
            activateSystem();
          } else if ((ships_friendly == 0) && (system.isActive())) {
            deactivateSystem();
          } else
            return;
        }
      }
    }
  }
  
  private void deactivateSystem() {
    if (system.isOn()) {
      ship.useSystem();
    }
  }
  
  private void activateSystem() {
    if (!system.isOn()) {
      ship.useSystem();
    }
  }
}
