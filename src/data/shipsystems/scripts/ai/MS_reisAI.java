package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.Iterator;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;


public class MS_reisAI
  implements ShipSystemAIScript
{
  private ShipAPI ship;
  private ShipSystemAPI system;
  private CombatEngineAPI engine;
  private boolean runOnce = false;
  private boolean allyBlock = false;
  
  private List<WeaponAPI> weapons;
  
  private final IntervalUtil tracker = new IntervalUtil(0.1F, 0.2F);
  private static final float LOOK_AHEAD_TIME = 0.067F;
  
  public MS_reisAI() {}
  
  public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
    this.ship = ship;
    this.system = system;
    this.engine = engine;
  }
  
  public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target)
  {
    if ((engine.isPaused()) || (engine == null)) {
      return;
    }
    

    if (!runOnce) {
      weapons = ship.getAllWeapons();
      
      runOnce = true;
    }
    
    tracker.advance(amount);
    
    if (tracker.intervalElapsed()) {
      if (!AIUtils.canUseSystemThisFrame(ship)) {
        return;
      }
      
      boolean shouldUseSystem = false;
      
      for (Iterator localIterator1 = weapons.iterator(); localIterator1.hasNext();) { w = (WeaponAPI)localIterator1.next();
        
        if (w.getId().equals("ms_stopper1"))
        {


          List<ShipAPI> nearbyShips = CombatUtils.getShipsWithinRange(ship.getLocation(), 800.0F);
          for (localIterator2 = nearbyShips.iterator(); localIterator2.hasNext();) { drive = (ShipAPI)localIterator2.next();
            
            if ((!drive.isFighter()) && (!drive.isHulk()))
            {


              Vector2f loc = drive.getLocation();
              
              ahead = new Vector2f(loc).translate(drive.getVelocity().getX() * 0.067F, drive
                .getVelocity().getY() * 0.067F);
              



              allyBlock = ((drive.getOwner() == ship.getOwner()) && (MathUtils.getShortestRotation(w.getArcFacing(), Vector2f.angle(w.getLocation(), ahead)) < 10.0F) && (MathUtils.getDistance(ahead, w.getLocation()) < 600.0F));
              
              List<ShipEngineControllerAPI.ShipEngineAPI> shipEngines = drive.getEngineController().getShipEngines();
              for (ShipEngineControllerAPI.ShipEngineAPI shipEngine : shipEngines)
              {

                if ((drive.getOwner() != ship.getOwner()) && (!shipEngine.isDisabled()) && 
                  (MathUtils.getShortestRotation(w.getArcFacing(), Vector2f.angle(w.getLocation(), ahead)) < 10.0F) && 
                  (MathUtils.getDistance(ahead, w.getLocation()) < 600.0F) && (!allyBlock))
                  shouldUseSystem = true; }
            } } } }
      WeaponAPI w;
      Iterator localIterator2;
      ShipAPI drive;
      Vector2f ahead;
      if ((ship.getSystem().isActive() ^ shouldUseSystem)) {
        ship.useSystem();
      }
    }
  }
}
