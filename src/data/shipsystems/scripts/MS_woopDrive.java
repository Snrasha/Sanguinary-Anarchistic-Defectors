package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.State;
import java.util.Map;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_woopDrive implements com.fs.starfarer.api.plugins.ShipSystemStatsScript
{
  private static final String DATA_KEY = "ms_woopDrive";
  
  public MS_woopDrive() {}
  
  public static float effectLevel(ShipAPI ship)
  {
    CladeData cladeData = (CladeData)Global.getCombatEngine().getCustomData().get("ms_woopDrive");
    if (cladeData == null) {
      return 0.0F;
    }
    
    Map<ShipAPI, Float> acting = acting;
    
    if (acting.containsKey(ship)) {
      return ((Float)acting.get(ship)).floatValue();
    }
    return 0.0F;
  }
  

  private boolean started = false;
  
  public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel)
  {
    CombatEngineAPI engine = Global.getCombatEngine();
    
    if (engine.isPaused()) {
      return;
    }
    
    CladeData localData = (CladeData)engine.getCustomData().get("ms_woopDrive");
    Map<ShipAPI, Float> acting = acting;
    
    ShipAPI ship = (ShipAPI)stats.getEntity();
    
    if (ship.isAlive()) {
      if (effectLevel > 0.0F) {
        acting.put(ship, Float.valueOf(effectLevel));
      }
      


      Vector2f point = new Vector2f(-50.0F, 0.0F);
      VectorUtils.rotate(point, ship.getFacing(), point);
      Vector2f.add(point, ship.getLocation(), point);
      
      Vector2f dir = (Vector2f)VectorUtils.getDirectionalVector(ship.getLocation(), point).scale(50.0F);
      Vector2f.add(ship.getVelocity(), dir, ship.getVelocity());
      
      if (state == ShipSystemStatsScript.State.OUT) {
        stats.getMaxSpeed().unmodify(id);
        
        ship.getEngineController().isDecelerating();
        
        float speed = ship.getVelocity().length();
        if (speed < 300.0F) {
          ship.getVelocity().normalise();
          ship.getVelocity().scale(getMaxSpeedmodified);
        }
      } else {
        if (!started) {
          started = true;
        }
        
        stats.getMaxSpeed().modifyFlat(id, 145.0F * effectLevel);
        stats.getAcceleration().modifyFlat(id, 200.0F * effectLevel);
        
        ship.getEngineController().isAcceleratingBackwards();
        
        float speed = ship.getVelocity().length();
        if (speed <= 0.1F)
        {
          ship.getVelocity().set(VectorUtils.getDirectionalVector(ship.getLocation(), dir)).scale(getMaxSpeedmodified);
        }
        if (speed < 300.0F) {
          ship.getVelocity().normalise();
          ship.getVelocity().scale(getMaxSpeedmodified);
        }
      }
    }
  }
  
  public void unapply(MutableShipStatsAPI stats, String id)
  {
    ShipAPI ship = (ShipAPI)stats.getEntity();
    started = false;
    
    if (ship != null) {
      if (!Global.getCombatEngine().getCustomData().containsKey("ms_woopDrive")) {
        Global.getCombatEngine().getCustomData().put("ms_woopDrive", new CladeData(null));
      }
      CladeData localData = (CladeData)Global.getCombatEngine().getCustomData().get("ms_woopDrive");
      if (localData != null) {
        Map<ShipAPI, Float> acting = acting;
        
        acting.remove(ship);
      }
    }
    
    stats.getAcceleration().unmodify(id);
  }
  
  public com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel)
  {
    return new com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData("retro-thrusters overcharged", false);
  }
  
  private static final class CladeData {
    final Map<ShipAPI, Float> acting = new java.util.HashMap(50);
    
    private CladeData() {}
  }
}
