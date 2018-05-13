package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.Iterator;
import java.util.List;
import org.lazywizard.lazylib.CollectionUtils.CollectionFilter;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_drivechargerai implements com.fs.starfarer.api.combat.ShipSystemAIScript
{
  private static final float SECONDS_TO_LOOK_AHEAD = 3.0F;
  private float THRESHHOLD;
  private ShipAPI ship;
  private CombatEngineAPI engine;
  private final IntervalUtil tracker = new IntervalUtil(0.1F, 0.2F);
  
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
    } };
  
  public MS_drivechargerai() {}
  
  private static float damageReader(ShipAPI ship, float damageWindowSeconds) { float accumulator = 0.0F;
    float shipFace = ship.getFacing();
    
    accumulator += beamReader(ship, damageWindowSeconds);
    
    for (DamagingProjectileAPI proj : Global.getCombatEngine().getProjectiles())
    {
      if (proj.getOwner() != ship.getOwner())
      {
        Vector2f endPoint = new Vector2f(proj.getVelocity());
        endPoint.scale(damageWindowSeconds);
        Vector2f.add(endPoint, proj.getLocation(), endPoint);
        
        if (((ship.getShield() == null) || (!ship.getShield().isWithinArc(proj.getLocation()))) && 
          (CollisionUtils.getCollides(proj.getLocation(), endPoint, new Vector2f(ship
          .getLocation()), ship.getCollisionRadius())))
        {

          if ((shipFace - 45.0F <= Math.abs(MathUtils.getShortestRotation(proj.getFacing(), VectorUtils.getAngle(proj.getLocation(), ship.getLocation())))) || 
            (shipFace + 45.0F >= Math.abs(MathUtils.getShortestRotation(proj.getFacing(), VectorUtils.getAngle(proj.getLocation(), ship.getLocation())))))
            accumulator += proj.getDamageAmount() + proj.getEmpAmount();
        }
      }
    }
    return accumulator;
  }
  
  private static float beamReader(ShipAPI ship, float damageWindowSeconds) { float accumulator = 0.0F;
    float shipFace = ship.getFacing();
    
    for (BeamAPI beam : Global.getCombatEngine().getBeams()) {
      if (beam.getDamageTarget() == ship) {
        float beamX = getFromx;
        float beamY = getFromy;
        float beamLoc = beamX + beamY;
        
        if ((shipFace - 45.0F <= Math.abs(MathUtils.getShortestRotation(beamLoc, VectorUtils.getAngle(beam.getFrom(), ship.getLocation())))) || 
          (shipFace + 45.0F >= Math.abs(MathUtils.getShortestRotation(beamLoc, VectorUtils.getAngle(beam.getFrom(), ship.getLocation()))))) {
          float dps = beam.getWeapon().getDerivedStats().getDamageOver30Sec() / 30.0F;
          float emp = beam.getWeapon().getDerivedStats().getEmpPerSecond();
          
          accumulator += (dps + emp) * damageWindowSeconds;
        }
      }
    }
    return accumulator;
  }
  

  public void init(ShipAPI ship, com.fs.starfarer.api.combat.ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine)
  {
    this.ship = ship;
    this.engine = engine;
  }
  



  public void advance(float amount, Vector2f position, Vector2f collisionDanger, ShipAPI target)
  {
    if (engine.isPaused()) {
      return;
    }
    
    tracker.advance(amount);
    Vector2f shipLoc = new Vector2f(ship.getLocation());
    

    if (tracker.intervalElapsed()) {
      if (!AIUtils.canUseSystemThisFrame(ship)) {
        return;
      }
      
      float incoming = damageReader(ship, 3.0F);
      
      THRESHHOLD = (ship.getHitpoints() * 0.1F);
      
      float hitRad = Math.max(ship.getCollisionRadius(), 1250.0F);
      
      List<DamagingProjectileAPI> nearbyThreats = org.lazywizard.lazylib.combat.CombatUtils.getProjectilesWithinRange(shipLoc, hitRad);
      for (Iterator localIterator = engine.getProjectiles().iterator(); localIterator.hasNext();) { tmp = (DamagingProjectileAPI)localIterator.next();
        if ((MathUtils.isWithinRange(tmp.getLocation(), ship.getLocation(), ship.getCollisionRadius() + hitRad)) && (tmp.getDamage().getDamage() > 400.0F))
          nearbyThreats.add(tmp);
      }
      DamagingProjectileAPI tmp;
      nearbyThreats = org.lazywizard.lazylib.CollectionUtils.filter(nearbyThreats, filterMisses);
      Object nearbyMissiles = AIUtils.getNearbyEnemyMissiles(ship, ship.getCollisionRadius() + hitRad);
      for (MissileAPI missile : (List)nearbyMissiles) {
        if ((missile.getEngineController().isTurningLeft()) || (missile.getEngineController().isTurningRight()) || (missile.getDamage().getDamage() <= 400.0F))
        {


          nearbyThreats.add(missile);
        }
      }
      
      if (((ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.HAS_INCOMING_DAMAGE)) || (ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.MANEUVER_TARGET)) || (ship.isRetreating())) && 
        (!nearbyThreats.isEmpty()) && (THRESHHOLD * MathUtils.getRandomNumberInRange(0.8F, 1.2F) < incoming))
      {


        ship.useSystem();
      }
    }
  }
}
