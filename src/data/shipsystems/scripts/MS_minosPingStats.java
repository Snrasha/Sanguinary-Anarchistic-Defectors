package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.State;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData;
import java.util.HashMap;
import java.util.Map;

public class MS_minosPingStats extends BaseShipSystemScript
{
  private CombatEngineAPI engine;
  private static final int ECM_BUFF = 1;
  private static final float SENSOR_BOOST = 33.0F;
  private static final Map<ShipAPI, ShipAPI> receiving = new HashMap();
  
  public MS_minosPingStats() {}
  
  public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) { if (engine != Global.getCombatEngine()) {
      engine = Global.getCombatEngine();
      receiving.clear();
    }
    
    ShipAPI host_ship = (ShipAPI)stats.getEntity();
    


    stats.getSightRadiusMod().modifyPercent(id, effectLevel * 33.0F);
    

    if (effectLevel > 0.0F) {
      for (ShipAPI ship : engine.getShips()) {
        if ((!ship.isHulk()) && (!ship.isFighter()) && (host_ship.getOwner() == ship.getOwner()))
        {


          if (state == ShipSystemStatsScript.State.OUT) {
            ship.getMutableStats().getEccmChance().modifyFlat(id, effectLevel);
          } else {
            ship.getMutableStats().getEccmChance().modifyFlat(id, effectLevel * 1.0F);
          }
        }
      }
    }
  }
  
  public void unapply(MutableShipStatsAPI stats, String id)
  {
    ShipAPI host_ship = (ShipAPI)stats.getEntity();
    
    if (engine != Global.getCombatEngine()) {
      engine = Global.getCombatEngine();
      receiving.clear();
    }
    
    stats.getSightRadiusMod().unmodify();
    
    for (ShipAPI ship : engine.getShips()) {
      if ((!ship.isHulk()) && (!ship.isFighter()) && (host_ship.getOwner() == ship.getOwner()))
      {


        ship.getMutableStats().getEccmChance().unmodify();
      }
    }
  }
  
  public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {
    if (index == 0)
      return new ShipSystemStatsScript.StatusData("sensor strength increased", false);
    if (index == 1) {
      return new ShipSystemStatsScript.StatusData("fleet ecm capalities improved", false);
    }
    return null;
  }
  
  public void init(CombatEngineAPI engine) {
    this.engine = engine;
  }
}
