package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

public class MS_overdriveStats implements com.fs.starfarer.api.plugins.ShipSystemStatsScript
{
  public static final float ROF_BONUS = 1.0F;
  public static final float FLUX_MULT = 0.8F;
  public static final float ROF_MALUS = 0.5F;
  
  public MS_overdriveStats() {}
  
  public void apply(MutableShipStatsAPI stats, String id, com.fs.starfarer.api.plugins.ShipSystemStatsScript.State state, float effectLevel) {
    if (state == com.fs.starfarer.api.plugins.ShipSystemStatsScript.State.ACTIVE) {
      float mult = 1.0F + 1.0F * effectLevel;
      stats.getBallisticRoFMult().modifyMult(id, mult);
      stats.getBallisticWeaponFluxCostMod().modifyMult(id, 0.8F);
      stats.getMissileRoFMult().modifyMult(id, mult);
      stats.getEnergyRoFMult().modifyMult(id, mult);
      stats.getEnergyWeaponFluxCostMod().modifyMult(id, 0.8F);
      stats.getBeamWeaponFluxCostMult().modifyFlat(id, 0.8F);
    } else if (state == com.fs.starfarer.api.plugins.ShipSystemStatsScript.State.OUT) {
      float mult = 1.0F - 0.5F * effectLevel;
      stats.getBallisticRoFMult().modifyMult(id, mult);
      stats.getMissileRoFMult().modifyMult(id, mult);
      stats.getEnergyRoFMult().modifyMult(id, mult);
    }
  }
  
  public void unapply(MutableShipStatsAPI stats, String id)
  {
    stats.getBallisticRoFMult().unmodify(id);
    stats.getBallisticWeaponFluxCostMod().unmodify(id);
    stats.getMissileRoFMult().unmodify(id);
    stats.getEnergyRoFMult().unmodify(id);
    stats.getEnergyWeaponFluxCostMod().unmodify(id);
    stats.getBeamWeaponFluxCostMult().unmodify(id);
  }
  
  public com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData getStatusData(int index, com.fs.starfarer.api.plugins.ShipSystemStatsScript.State state, float effectLevel)
  {
    float mult = 1.0F + 1.0F * effectLevel;
    float curs = 1.0F - 0.5F * effectLevel;
    float bonusPercent = (int)(mult - 1.0F) * 100.0F;
    float malusPercent = (int)(curs - 1.0F) * 100.0F;
    if (index == 0)
      return new com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData("all weapons rate of fire +" + (int)bonusPercent + "%", false);
    if (index == 1) {
      return new com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData("all weapons rate of fire -" + (int)malusPercent + "%", false);
    }
    return null;
  }
}
