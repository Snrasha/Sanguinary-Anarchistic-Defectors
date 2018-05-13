package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.PersonalityAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI.AssignmentInfo;
import com.fs.starfarer.api.combat.CombatTaskManagerAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.lazywizard.lazylib.CollectionUtils;
import org.lazywizard.lazylib.CollectionUtils.CollectionFilter;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_mimirSlideDriveAI implements com.fs.starfarer.api.combat.ShipSystemAIScript
{
  private static final float SECONDS_FOR_PATH = 1.5F;
  private static final float SECONDS_TO_LOOK_AHEAD = 3.0F;
  private float bashNum = 0.0F;
  
  private static Vector2f intercept(Vector2f point, float speed, Vector2f target, Vector2f targetVel) {
    Vector2f difference = new Vector2f(x - x, y - y);
    
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
  



  private final CollectionUtils.CollectionFilter<DamagingProjectileAPI> filterMisses = new CollectionUtils.CollectionFilter()
  {

    public boolean accept(DamagingProjectileAPI proj)
    {
      if ((proj.getOwner() == ship.getOwner()) && ((!(proj instanceof MissileAPI)) || (!((MissileAPI)proj).isFizzling())))
      {
        return false;
      }
      
      if ((proj instanceof MissileAPI)) {
        MissileAPI missile = (MissileAPI)proj;
        if (missile.isFlare()) {
          return false;
        }
      }
      

      if (CollisionUtils.getCollides(proj.getLocation(), Vector2f.add(proj.getLocation(), (Vector2f)new Vector2f(proj.getVelocity()).scale(3.0F), null), 
        ship.getLocation(), ship.getCollisionRadius())) {}
      return 
      
        Math.abs(MathUtils.getShortestRotation(proj.getFacing(), VectorUtils.getAngle(proj.getLocation(), ship.getLocation()))) <= 90.0F;
    }
  };
  

  private static final Map<ShipAPI.HullSize, Float> mag = new HashMap();
  
  static { mag.put(ShipAPI.HullSize.FIGHTER, Float.valueOf(0.0F));
    mag.put(ShipAPI.HullSize.FRIGATE, Float.valueOf(0.4F));
    mag.put(ShipAPI.HullSize.DESTROYER, Float.valueOf(0.6F));
    mag.put(ShipAPI.HullSize.CRUISER, Float.valueOf(0.8F));
    mag.put(ShipAPI.HullSize.CAPITAL_SHIP, Float.valueOf(1.0F));
  }
  

  private String name;
  protected String getPersonality(FleetMemberAPI member)
  {
    PersonAPI captain = member.getCaptain();
    if (captain != null)
    {
      switch (captain.getPersonalityAPI().getId()) {
      case "timid": 
        name = "timid";
        break;
      case "cautious": 
        name = "cautious";
        break;
      case "steady": 
        name = "steady";
        break;
      case "aggressive": 
        name = "aggressive";
      }
      
    }
    
    return name;
  }
  

  private CombatEngineAPI engine;
  
  private ShipAPI ship;
  
  private PersonAPI officer;
  
  private final boolean mission = false;
  
  private final IntervalUtil tracker = new IntervalUtil(0.1F, 0.2F);
  
  public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target)
  {
    if (engine == null) {
      return;
    }
    
    if (engine.isPaused()) {
      return;
    }
    




    tracker.advance(amount);
    Vector2f shipLoc = new Vector2f(ship.getLocation());
    Vector2f shipDir = new Vector2f(ship.getVelocity());
    








    if (tracker.intervalElapsed()) {
      if (!AIUtils.canUseSystemThisFrame(ship)) {
        return;
      }
      
      float incoming = data.scripts.util.MS_Utils.estimateIncomingDamage(ship);
      
      boolean shouldUseSystem = false;
      float hitRad = Math.max(ship.getCollisionRadius(), 50.0F);
      
      List<DamagingProjectileAPI> nearbyThreats = CombatUtils.getProjectilesWithinRange(shipLoc, hitRad);
      for (Iterator localIterator = engine.getProjectiles().iterator(); localIterator.hasNext();) { tmp = (DamagingProjectileAPI)localIterator.next();
        if (MathUtils.isWithinRange(tmp.getLocation(), ship.getLocation(), ship.getCollisionRadius() + hitRad))
          nearbyThreats.add(tmp);
      }
      DamagingProjectileAPI tmp;
      nearbyThreats = CollectionUtils.filter(nearbyThreats, filterMisses);
      Object nearbyMissiles = AIUtils.getNearbyEnemyMissiles(ship, ship.getCollisionRadius() + hitRad);
      for (MissileAPI missile : (List)nearbyMissiles) {
        if ((missile.getEngineController().isTurningLeft()) || (missile.getEngineController().isTurningRight()))
        {


          nearbyThreats.add(missile);
        }
      }
      float crashRad = Math.max(ship.getCollisionRadius(), 1750.0F);
      
      List<ShipAPI> willCrash = CombatUtils.getShipsWithinRange(shipLoc, crashRad);
      if (!willCrash.isEmpty()) {
        if (shipDir.lengthSquared() <= 0.01F) {
          shipDir = VectorUtils.getDirectionalVector(shipLoc, new Vector2f(ship.getVelocity()));
          Vector2f.sub(shipDir, ship.getLocation(), shipDir);
          if (shipDir.lengthSquared() <= 0.01F) {
            shipDir = new Vector2f(1.0F, 0.0F);
          }
        }
        shipDir.normalise();
        shipDir.scale(crashRad);
        Vector2f.add(shipDir, ship.getLocation(), shipDir);
        
        java.util.Collections.sort(willCrash, new org.lazywizard.lazylib.CollectionUtils.SortEntitiesByDistance(ship.getLocation()));
        ListIterator<ShipAPI> iter = willCrash.listIterator();
        while (iter.hasNext()) {
          ShipAPI tmp = (ShipAPI)iter.next();
          if ((tmp != ship) && (ship.getCollisionClass() != com.fs.starfarer.api.combat.CollisionClass.NONE) && (!tmp.isFighter()) && (!tmp.isDrone())) {
            Vector2f bash = intercept(ship.getLocation(), 1750.0F, tmp.getLocation(), tmp.getVelocity());
            
            if (bash == null) {
              Vector2f projection = new Vector2f(tmp.getVelocity());
              float scalar = MathUtils.getDistance(tmp.getLocation(), ship.getLocation()) / 1500.0F;
              projection.scale(scalar);
              Vector2f.add(tmp.getLocation(), projection, bash);
            }
            
            if ((bash != null) && (ship.getLocation() != null)) {
              float areaChange = 1.0F;
              float aMass = ship.getMass();
              float bMass = tmp.getMass();
              
              if (CollisionUtils.getCollides(ship.getLocation(), shipDir, bash, tmp.getCollisionRadius() * 0.5F + ship.getCollisionRadius() * 0.75F * areaChange))
              {


                if (tmp.getOwner() == ship.getOwner())
                {
                  bashNum = (bMass + aMass * 2.0F);
                } else {
                  bashNum = (bMass + aMass);
                }
              }
            }
          }
        }
      }
      












      CombatFleetManagerAPI.AssignmentInfo assignment = engine.getFleetManager(ship.getOwner()).getTaskManager(false).getAssignmentFor(ship);
      Vector2f targetSpot;
      Vector2f targetSpot; if ((assignment != null) && (assignment.getTarget() != null)) {
        targetSpot = assignment.getTarget().getLocation();
      } else {
        targetSpot = null;
      }
      















      if (((!nearbyThreats.isEmpty()) && (bashNum == 0.0F)) || ((!nearbyThreats.isEmpty()) && 
        (bashNum * MathUtils.getRandomNumberInRange(0.8F, 1.2F) < incoming)) || ((nearbyThreats.isEmpty()) && (bashNum == 0.0F) && (targetSpot == null) && 
        (Math.random() > 0.5D))) {
        shouldUseSystem = true;
      }
      






















      if ((ship.getSystem().isActive() ^ shouldUseSystem)) {
        ship.useSystem();
      }
    }
  }
  

  public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine)
  {
    this.engine = engine;
    this.ship = ship;
  }
  
  public MS_mimirSlideDriveAI() {}
}
