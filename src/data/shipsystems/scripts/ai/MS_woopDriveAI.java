package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.FluxTrackerAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.Iterator;
import java.util.List;
import org.lazywizard.lazylib.CollectionUtils;
import org.lazywizard.lazylib.CollectionUtils.CollectionFilter;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_woopDriveAI implements ShipSystemAIScript
{
  private static final float SECONDS_TO_LOOK_AHEAD = 3.0F;
  private static final float RANGE_TO_CHECK = 2500.0F;
  private static final float EDGE_CHECK = 700.0F;
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
  
  private float mapX;
  
  private float mapY;
  private ShipAPI ship;
  private boolean runOnce = false;
  
  private final IntervalUtil tracker = new IntervalUtil(0.1F, 0.2F);
  
  public MS_woopDriveAI() {}
  
  public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) { CombatEngineAPI engine = Global.getCombatEngine();
    if ((engine == null) || (engine.isPaused())) {
      return;
    }
    
    if (!runOnce) {
      runOnce = true;
      
      mapX = engine.getMapWidth();
      mapY = engine.getMapHeight();
    }
    
    FluxTrackerAPI fluxer = ship.getFluxTracker();
    tracker.advance(amount);
    Vector2f shipLoc = new Vector2f(ship.getLocation());
    
    if (tracker.intervalElapsed()) {
      if (!AIUtils.canUseSystemThisFrame(ship)) {
        return;
      }
      
      float incoming = data.scripts.util.MS_Utils.estimateIncomingDamage(ship);
      
      boolean shouldUseSystem = false;
      boolean clear = true;
      float hitRad = Math.max(ship.getCollisionRadius(), 100.0F);
      
      List<DamagingProjectileAPI> nearbyThreats = CombatUtils.getProjectilesWithinRange(shipLoc, hitRad);
      for (Iterator localIterator = engine.getProjectiles().iterator(); localIterator.hasNext();) { tmp = (DamagingProjectileAPI)localIterator.next();
        if (MathUtils.isWithinRange(tmp.getLocation(), ship.getLocation(), ship.getCollisionRadius() + hitRad)) {
          nearbyThreats.add(tmp);
        }
      }
      nearbyThreats = CollectionUtils.filter(nearbyThreats, filterMisses);
      Object nearbyMissiles = AIUtils.getNearbyEnemyMissiles(ship, ship.getCollisionRadius() + hitRad);
      for (DamagingProjectileAPI tmp = ((List)nearbyMissiles).iterator(); tmp.hasNext();) { missile = (MissileAPI)tmp.next();
        if ((missile.getEngineController().isTurningLeft()) || (missile.getEngineController().isTurningRight()))
        {


          nearbyThreats.add(missile); }
      }
      MissileAPI missile;
      List<ShipAPI> ships = CombatUtils.getShipsWithinRange(shipLoc, 700.0F);
      for (ShipAPI s : ships) {
        if ((MathUtils.isWithinRange(s.getLocation(), shipLoc, 700.0F)) && 
          (VectorUtils.getAngle(s.getLocation(), shipLoc) > 170.0F)) {
          clear = false;
        }
      }
      if ((ship.getLocation().x + 700.0F > mapX) || (ship.getLocation().y + 700.0F > mapY) || 
        (ship.getLocation().x - 700.0F < mapX) || (ship.getLocation().y - 700.0F < mapY)) {
        clear = false;
      }
      



      boolean shield_on = false;
      if (ship.getShield() != null) shield_on = ship.getShield().isOn();
      if (((!nearbyThreats.isEmpty()) && (shield_on) && (ship.getShield().isOn()) && (fluxer.getCurrFlux() >= fluxer.getMaxFlux() * 0.8F) && 
        (incoming >= fluxer.getMaxFlux() * 0.2F)) || ((!nearbyThreats.isEmpty()) && (!shield_on) && (ship.getShield().isOff()) && 
        (incoming >= ship.getHitpoints() * 0.25F)) || ((ship.getAIFlags().hasFlag(com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags.RUN_QUICKLY)) && 
        (!AIUtils.getNearbyEnemies(ship, 2500.0F).isEmpty()) && (clear == true)))
      {
        shouldUseSystem = true;
      }
      


      if ((ship.getSystem().isActive() ^ shouldUseSystem))
      {
        ship.useSystem();
      }
    }
  }
  

  public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine)
  {
    this.ship = ship;
  }
}
