package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.FluxTrackerAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.util.HashMap;
import java.util.Map;

public class MS_harShields extends BaseHullMod
{
  private static final Map<ShipAPI.HullSize, Float> mag = new HashMap();
  
  static {
    mag.put(ShipAPI.HullSize.FIGHTER, Float.valueOf(0.8F));
    mag.put(ShipAPI.HullSize.FRIGATE, Float.valueOf(0.8F));
    mag.put(ShipAPI.HullSize.DESTROYER, Float.valueOf(0.6F));
    mag.put(ShipAPI.HullSize.CRUISER, Float.valueOf(0.5F));
    mag.put(ShipAPI.HullSize.CAPITAL_SHIP, Float.valueOf(0.4F));
  }
  
  public String getDescriptionParam(int index, ShipAPI.HullSize hullSize)
  {
    if (index == 0) {
      return "" + (int)(((Float)mag.get(ShipAPI.HullSize.FRIGATE)).floatValue() * 100.0F);
    }
    if (index == 1) {
      return "" + (int)(((Float)mag.get(ShipAPI.HullSize.DESTROYER)).floatValue() * 100.0F);
    }
    if (index == 2) {
      return "" + (int)(((Float)mag.get(ShipAPI.HullSize.CRUISER)).floatValue() * 100.0F);
    }
    if (index == 3) {
      return "" + (int)(((Float)mag.get(ShipAPI.HullSize.CAPITAL_SHIP)).floatValue() * 100.0F);
    }
    return null;
  }
  
  public boolean isApplicableToShip(ShipAPI ship)
  {
    return false;
  }
  
  public void advanceInCombat(ShipAPI ship, float amount)
  {
    if ((Global.getCombatEngine().isPaused()) || (ship.isHulk())) {
      return;
    }
    
    FluxTrackerAPI fluxTracker = ship.getFluxTracker();
    

    ship.getMutableStats().getFluxDissipation().modifyPercent("harShields", 100.0F * (((Float)mag.get(ship.getHullSize())).floatValue() * getFluxCurve(fluxTracker.getHardFlux() / fluxTracker.getMaxFlux(), 1.4F)));
  }
  

  private static float getFluxCurve(float ratio, float curveStrength)
  {
    float A = curveStrength * ratio + 1.0F;
    float result = -1.0F / (A * A) + 1.0F;
    return result;
  }
  
  public MS_harShields() {}
}
