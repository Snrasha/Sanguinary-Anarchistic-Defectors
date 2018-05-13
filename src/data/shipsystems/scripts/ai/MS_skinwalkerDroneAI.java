package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lwjgl.util.vector.Vector2f;



public class MS_skinwalkerDroneAI
{
  private ShipAPI ship;
  private IntervalUtil tracker = new IntervalUtil(0.1F, 0.5F);
  
  public MS_skinwalkerDroneAI() {}
  
  public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
    this.ship = ship;
  }
  
  public void advance(float amount, Vector2f position, Vector2f collisionDanger, ShipAPI target)
  {
    tracker.advance(amount);
    
    if (tracker.intervalElapsed()) {}
  }
}
