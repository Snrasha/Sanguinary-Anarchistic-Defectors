package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.State;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData;
import org.lwjgl.util.vector.Vector2f;

public class MS_drivechargerstats implements com.fs.starfarer.api.plugins.ShipSystemStatsScript
{
  private Vector2f vel = new Vector2f();
  
  public MS_drivechargerstats() {}
  
  public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) { stats.getMaxSpeed().modifyFlat(id, 300.0F * effectLevel);
    stats.getMaxTurnRate().modifyMult(id, 10.0F * effectLevel);
    stats.getTurnAcceleration().modifyPercent(id, 2000.0F * effectLevel);
    stats.getAcceleration().modifyPercent(id, 2000.0F * effectLevel);
    stats.getDeceleration().modifyPercent(id, 2000.0F * effectLevel);
    
    if (!com.fs.starfarer.api.Global.getCombatEngine().isPaused()) {
      ShipAPI ship = (ShipAPI)stats.getEntity();
      if ((vel != new Vector2f()) || (effectLevel != 0.0F))
      {
        Vector2f offset = new Vector2f();
        Vector2f.sub(vel, ship.getVelocity(), offset);
        

        org.lazywizard.lazylib.VectorUtils.rotate(offset, -ship.getFacing() + 90.0F, offset);
        

        offset = new Vector2f(0.0F, y * 0.1F);
        

        org.lazywizard.lazylib.VectorUtils.rotate(offset, ship.getFacing() - 90.0F, offset);
        

        Vector2f.add(ship.getVelocity(), offset, ship.getVelocity());
      }
      
      vel = new Vector2f(ship.getVelocity());
    }
  }
  

  public void unapply(MutableShipStatsAPI stats, String id)
  {
    stats.getMaxSpeed().unmodify(id);
    stats.getMaxTurnRate().unmodify(id);
    stats.getTurnAcceleration().unmodify(id);
    stats.getAcceleration().unmodify(id);
    stats.getDeceleration().unmodify(id);
  }
  
  public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel)
  {
    if (index == 0)
      return new ShipSystemStatsScript.StatusData("lateral thrusters online", false);
    if (index == 1) {
      return new ShipSystemStatsScript.StatusData("releasing stored drive plasma", false);
    }
    return null;
  }
}
