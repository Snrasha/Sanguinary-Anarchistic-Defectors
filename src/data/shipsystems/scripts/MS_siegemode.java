package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;

public class MS_siegemode implements com.fs.starfarer.api.plugins.ShipSystemStatsScript
{
  private static final float RANGE_BONUS = 200.0F;
  private static final float COST_BONUS = -20.0F;
  private static final float SHIELD_BONUS = 10.0F;
  
  public MS_siegemode() {}
  
  public void apply(MutableShipStatsAPI stats, String id, com.fs.starfarer.api.plugins.ShipSystemStatsScript.State state, float effectLevel)
  {
    com.fs.starfarer.api.combat.CombatEntityAPI entity = stats.getEntity();
    if (!(entity instanceof com.fs.starfarer.api.combat.ShipAPI)) {
      return;
    }
    

    stats.getTurnAcceleration().modifyFlat(id, 45.0F * effectLevel);
    stats.getMaxTurnRate().modifyFlat(id, 30.0F);
    
    stats.getEnergyWeaponRangeBonus().modifyFlat(id, 200.0F * effectLevel);
    stats.getEnergyWeaponFluxCostMod().modifyPercent(id, -20.0F * effectLevel);
    stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -20.0F * effectLevel);
    
    stats.getMaxSpeed().modifyPercent(id, -80.0F * effectLevel);
    stats.getZeroFluxSpeedBoost().modifyMult(id, 1.0F - 0.5F * effectLevel);
    
    stats.getShieldAbsorptionMult().modifyPercent(id, -1.0F * effectLevel * 10.0F);
  }
  
  public void unapply(MutableShipStatsAPI stats, String id)
  {
    stats.getMaxSpeed().unmodify(id);
    stats.getMaxTurnRate().unmodify(id);
    stats.getTurnAcceleration().unmodify(id);
    stats.getEnergyWeaponRangeBonus().unmodify(id);
    stats.getEnergyWeaponFluxCostMod().unmodify(id);
    stats.getZeroFluxSpeedBoost().unmodify(id);
    stats.getShieldAbsorptionMult().unmodify(id);
  }
  
  public com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData getStatusData(int index, com.fs.starfarer.api.plugins.ShipSystemStatsScript.State state, float effectLevel)
  {
    if (index == 0)
      return new com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData("engine power redirected", false);
    if (index == 1)
      return new com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData("energy weapon range +" + (int)(200.0F * effectLevel), false);
    if (index == 2)
      return new com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData("energy weapon flux costs " + (int)(-20.0F * effectLevel) + "%", false);
    if (index == 3) {
      return new com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData("damage to shields reduced by " + (int)(10.0F * effectLevel) + "%", false);
    }
    return null;
  }
}
