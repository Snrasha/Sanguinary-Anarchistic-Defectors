package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FluxTrackerAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;


















public class MS_tagAI
  implements ShipSystemAIScript
{
  private static final float RANGE = 800.0F;
  private static final float RANGE_CHECK = 1000.0F;
  private static final float ANGLE_TOLERANCE = 10.0F;
  private static final float RANDOM_USE_CHANCE = 0.9F;
  private static final float FLUX_TOLERANCE = 0.9F;
  private static final float PRESENCE_CAP = 100.0F;
  private static final float PRESENCE_FLOOR = 0.0F;
  private static final float FIGHTER_WEIGHT = 1.0F;
  private static final float FRIGATE_WEIGHT = 4.0F;
  private static final float DESTROYER_WEIGHT = 8.0F;
  private static final float CRUISER_WEIGHT = 16.0F;
  private static final float CAPITAL_WEIGHT = 32.0F;
  private final IntervalUtil tracker = new IntervalUtil(0.25F, 0.5F);
  private float priority = 0.0F;
  private ShipAPI ship;
  private ShipSystemAPI system;
  
  public MS_tagAI() {}
  
  private static Vector2f intercept(Vector2f point, float speed, Vector2f target, Vector2f targetVel) { Vector2f difference = new Vector2f(x - x, y - y);
    
    float a = x * x + y * y - speed * speed;
    float b = 2.0F * (x * x + y * y);
    float c = x * x + y * y;
    
    Vector2f solutionSet = quad(a, b, c);
    
    Vector2f intercept = null;
    if (solutionSet != null) {
      float bestFit = Math.min(x, y);
      if (bestFit < 0.0F) {
        bestFit = Math.max(x, y);
      }
      if (bestFit > 0.0F) {
        intercept = new Vector2f(x + x * bestFit, y + y * bestFit);
      }
    }
    
    return intercept;
  }
  
  private static Vector2f quad(float a, float b, float c) {
    Vector2f solution = null;
    if (Float.compare(Math.abs(a), 0.0F) == 0) {
      if (Float.compare(Math.abs(b), 0.0F) == 0) {
        solution = Float.compare(Math.abs(c), 0.0F) == 0 ? new Vector2f(0.0F, 0.0F) : null;
      } else {
        solution = new Vector2f(-c / b, -c / b);
      }
    } else {
      float d = b * b - 4.0F * a * c;
      if (d >= 0.0F) {
        d = (float)Math.sqrt(d);
        float e = 2.0F * a;
        solution = new Vector2f((-b - d) / e, (-b + d) / e);
      }
    }
    return solution;
  }
  
  public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine)
  {
    this.ship = ship;
    this.system = system;
  }
  
  public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target)
  {
    if ((Global.getCombatEngine().isPaused()) || (Global.getCombatEngine() == null)) {
      return;
    }
    
    tracker.advance(amount);
    Vector2f shipLoc = new Vector2f(ship.getLocation());
    boolean shouldUseSystem;
    if (tracker.intervalElapsed()) {
      if (!AIUtils.canUseSystemThisFrame(ship)) {
        return;
      }
      
      shouldUseSystem = false;
      
      List<ShipAPI> nearbyShips = CombatUtils.getShipsWithinRange(shipLoc, 800.0F);
      for (ShipAPI tracking : nearbyShips) {
        if (tracking.getOwner() != ship.getOwner())
        {

          Vector2f track = intercept(ship.getLocation(), 1000.0F, tracking.getLocation(), tracking.getVelocity());
          if ((track != null) && (ship.getLocation() != null)) {
            priority = MathUtils.getRandomNumberInRange(0.3F, 0.6F);
          }
          float angle = MathUtils.getShortestRotation(ship.getFacing(), Vector2f.angle(ship.getLocation(), tracking.getLocation()));
          ShieldAPI shield = tracking.getShield();
          FluxTrackerAPI fluxer = tracking.getFluxTracker();
          








          if ((tracking.getCollisionClass() != CollisionClass.NONE) && (angle < 10.0F)) {
            float usageChance = (float)(Math.random() - getAlliedPresence(tracking, 800.0F) / 100.0F);
            
            if (shield != null) {
              if (((fluxer.isOverloadedOrVenting()) && (Math.random() < 0.8999999761581421D)) || ((shield.isOn()) && 
                (!shield.isWithinArc(VectorUtils.getDirectionalVector(tracking.getLocation(), shipLoc))) && 
                (Math.random() < priority)) || ((shield.isOff()) && (fluxer.getCurrFlux() >= fluxer.getMaxFlux() * 0.9F) && 
                (Math.random() < priority / 2.0F))) {
                shouldUseSystem = true;
              }
            } else if ((shield != null) && (shield.getType() == ShieldAPI.ShieldType.PHASE)) {
              if ((fluxer.isOverloadedOrVenting()) && (usageChance < 0.9F)) {
                shouldUseSystem = true;
              }
            }
            else if (usageChance < 0.9F) {
              shouldUseSystem = true;
            }
          }
          

          if ((ship.getSystem().isActive() ^ shouldUseSystem)) {
            ship.useSystem();
          }
        }
      }
    }
  }
  




  public float getAlliedPresence(ShipAPI target, float range)
  {
    List<ShipAPI> allies = AIUtils.getNearbyAllies(ship, range);
    float presence = 0.0F;
    
    for (ShipAPI ally : allies) {
      if (!ally.isRetreating()) {
        switch (1.$SwitchMap$com$fs$starfarer$api$combat$ShipAPI$HullSize[ally.getHullSize().ordinal()]) {
        case 1: 
          presence += 1.0F;
          break;
        case 2: 
          presence += 4.0F;
          break;
        case 3: 
          presence += 8.0F;
          break;
        case 4: 
          presence += 16.0F;
          break;
        case 5: 
          presence += 32.0F;
        }
        
      }
    }
    
    if (presence > 100.0F)
      return 100.0F;
    if (presence < 0.0F) {
      return 0.0F;
    }
    return presence;
  }
}
