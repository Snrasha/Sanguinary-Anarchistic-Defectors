package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.State;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData;
import org.lwjgl.util.vector.Vector2f;



public class MS_fractalDispalcer
  implements ShipSystemStatsScript
{
  private static final Vector2f ZERO = new Vector2f();
  
  public MS_fractalDispalcer() {}
  
  public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
    if (!(stats.getEntity() instanceof ShipAPI)) {
      return;
    }
    
    ShipAPI ship = (ShipAPI)stats.getEntity();
  }
  

  public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float f)
  {
    if ((state == ShipSystemStatsScript.State.IN) && 
      (index == 0)) {
      return new ShipSystemStatsScript.StatusData("fractal displacer active", false);
    }
    

    return null;
  }
  
  public void unapply(MutableShipStatsAPI mssapi, String string) {}
}
