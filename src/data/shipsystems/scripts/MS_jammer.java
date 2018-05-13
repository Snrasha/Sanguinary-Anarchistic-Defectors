package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.State;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class MS_jammer implements com.fs.starfarer.api.plugins.ShipSystemStatsScript
{
  private static CombatEngineAPI engine = null;
  
  public static final float RANGE = 1200.0F;
  
  public static final float ACCURACY_BONUS = -50.0F;
  public static final float RANGE_BONUS = -20.0F;
  private static final Map<ShipAPI, ShipAPI> jamming = new java.util.HashMap();
  private static final String staticID = "shadowyJammerDebuff";
  
  public MS_jammer() {}
  
  public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
    if (engine != Global.getCombatEngine()) {
      engine = Global.getCombatEngine();
      jamming.clear();
    }
    

    ShipAPI host_ship = (ShipAPI)stats.getEntity();
    
    for (ShipAPI ship : engine.getShips()) {
      if ((ship.isAlive()) && 
      

        (ship != host_ship))
      {


        if ((host_ship.getOwner() != ship.getOwner()) && (org.lazywizard.lazylib.MathUtils.getDistance(ship, host_ship) <= 1200.0F))
        {
          ship.getMutableStats().getAutofireAimAccuracy().modifyPercent("shadowyJammerDebuff", -50.0F);
          ship.getMutableStats().getBallisticWeaponRangeBonus().modifyPercent("shadowyJammerDebuff", -20.0F);
          ship.getMutableStats().getEnergyWeaponRangeBonus().modifyPercent("shadowyJammerDebuff", -20.0F);
          ship.getMutableStats().getSightRadiusMod().modifyPercent("shadowyJammerDebuff", -20.0F);
          

          jamming.put(ship, host_ship);
        }
        else if ((jamming.containsKey(ship)) && (jamming.get(ship) == host_ship))
        {
          ship.getMutableStats().getAutofireAimAccuracy().unmodify("shadowyJammerDebuff");
          ship.getMutableStats().getBallisticWeaponRangeBonus().unmodify("shadowyJammerDebuff");
          ship.getMutableStats().getEnergyWeaponRangeBonus().unmodify("shadowyJammerDebuff");
          ship.getMutableStats().getSightRadiusMod().unmodify("shadowyJammerDebuff");
          

          jamming.remove(ship);
        }
      }
    }
  }
  
  public void unapply(MutableShipStatsAPI stats, String id)
  {
    ShipAPI host_ship = (ShipAPI)stats.getEntity();
    
    Iterator<Map.Entry<ShipAPI, ShipAPI>> iter = jamming.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry<ShipAPI, ShipAPI> entry = (Map.Entry)iter.next();
      ShipAPI ship = (ShipAPI)entry.getKey();
      

      if (entry.getValue() == host_ship)
      {
        ship.getMutableStats().getAutofireAimAccuracy().unmodify("shadowyJammerDebuff");
        ship.getMutableStats().getBallisticWeaponRangeBonus().unmodify("shadowyJammerDebuff");
        ship.getMutableStats().getEnergyWeaponRangeBonus().unmodify("shadowyJammerDebuff");
        
        iter.remove();
      }
    }
  }
  
  public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel)
  {
    if (index == 0) {
      return new ShipSystemStatsScript.StatusData("wide spectrum jamming active", false);
    }
    return null;
  }
}
