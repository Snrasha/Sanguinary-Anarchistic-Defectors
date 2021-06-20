package src.data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.FluxTrackerAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import java.util.HashMap;
import java.util.Map;

public class sanguinary_autonomist_defectors_harShields extends BaseHullMod
{
  private static final Map<ShipAPI.HullSize, Float> MAG = new HashMap();
  public static String HS_ICON = "graphics/icons/sanguinary_autonomist_defectors_hsConduitIcon.png";
  public static String HS_BUFFID = "sanguinary_autonomist_defectors_harShields";
  public static String HS_NAME = "UnHarmonic Shield Conduits";
  
  static {
    MAG.put(ShipAPI.HullSize.FIGHTER, Float.valueOf(0.4F));
    MAG.put(ShipAPI.HullSize.FRIGATE, Float.valueOf(0.4F));
    MAG.put(ShipAPI.HullSize.DESTROYER, Float.valueOf(0.3F));
    MAG.put(ShipAPI.HullSize.CRUISER, Float.valueOf(0.25F));
    MAG.put(ShipAPI.HullSize.CAPITAL_SHIP, Float.valueOf(0.2F));
  }
  
  @Override
  public String getDescriptionParam(int index, ShipAPI.HullSize hullSize)
  {
    if (index == 0) {
      return "" + (int)((MAG.get(ShipAPI.HullSize.FRIGATE)) * 100.0F);
    }
    if (index == 1) {
      return "" + (int)((MAG.get(ShipAPI.HullSize.DESTROYER)) * 100.0F);
    }
    if (index == 2) {
      return "" + (int)((MAG.get(ShipAPI.HullSize.CRUISER)) * 100.0F);
    }
    if (index == 3) {
      return "" + (int)((MAG.get(ShipAPI.HullSize.CAPITAL_SHIP)) * 100.0F);
    }
    return null;
  }
  
  @Override
  public boolean isApplicableToShip(ShipAPI ship)
  {
    return false;
  }
  
  @Override
  public void advanceInCombat(ShipAPI ship, float amount)
  {
    if ((Global.getCombatEngine().isPaused()) || (ship.isHulk())) {
      return;
    }
         FluxTrackerAPI fluxTracker = ship.getFluxTracker();
        float harShieldsBonus = 100f * (MAG.get(ship.getHullSize()) * (getFluxCurve(fluxTracker.getHardFlux() / fluxTracker.getMaxFlux(), 1.4f)));

        ship.getMutableStats().getFluxDissipation().modifyPercent("harShields", 100f * (MAG.get(ship.getHullSize()) * (getFluxCurve(fluxTracker.getHardFlux() / fluxTracker.getMaxFlux(), 1.4f))));
        
        if (ship == Global.getCombatEngine().getPlayerShip() && fluxTracker.getHardFlux() > 0) {
            Global.getCombatEngine().maintainStatusForPlayerShip(HS_BUFFID, HS_ICON, HS_NAME, "Flux dissipation increased by "+(int) harShieldsBonus+"%", true);
        }
  }
 
    private static float getFluxCurve(float ratio, float curveStrength) {
        //this method returns a value that is adjusted to the amount of flux.
        //the ratio is the hardFlux / totalFlux , and the curve strength determines how quickly the curve rises
        float A = curveStrength * ratio + 1;
        float result = -1 / (A * A) + 1;
        return result;
    }
}
