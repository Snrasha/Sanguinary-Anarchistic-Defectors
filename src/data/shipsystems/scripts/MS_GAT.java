package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.State;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData;

public class MS_GAT implements com.fs.starfarer.api.plugins.ShipSystemStatsScript
{
  public static final float ROF_BONUS = 0.25F;
  public static final float PROJ_DAM_MULT = 1.25F;
  public static final float BEAM_DAM_MULT = 1.55F;
  public static final float RECOIL_MULT = 1.66F;
  public static final float FLUX_USE_MULT = 0.75F;
  
  public MS_GAT() {}
  
  public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel)
  {
    com.fs.starfarer.api.combat.ShipAPI ship = (com.fs.starfarer.api.combat.ShipAPI)stats.getEntity();
    java.util.List<WeaponAPI> weaps = ship.getAllWeapons();
    
    if (state == ShipSystemStatsScript.State.ACTIVE) {
      float mult = 1.0F + 0.25F * effectLevel;
      stats.getEnergyRoFMult().modifyMult(id, mult);
      stats.getMaxRecoilMult().modifyMult(id, 1.66F * effectLevel);
      stats.getRecoilDecayMult().modifyMult(id, 1.66F * effectLevel);
      stats.getRecoilPerShotMult().modifyMult(id, 1.66F * effectLevel);
      stats.getEnergyWeaponFluxCostMod().modifyMult(id, 0.75F * effectLevel);
      stats.getMissileMaxSpeedBonus().modifyMult(id, mult);
      for (WeaponAPI w : weaps) {
        if (w.getType().equals(com.fs.starfarer.api.combat.WeaponAPI.WeaponType.ENERGY))
        {


          if (w.isBeam()) {
            stats.getEnergyWeaponDamageMult().modifyMult(id, 1.55F * effectLevel);
          } else {
            stats.getEnergyWeaponDamageMult().modifyMult(id, 1.25F * effectLevel);
          }
        }
      }
    }
  }
  
  public void unapply(MutableShipStatsAPI stats, String id) {
    stats.getEnergyRoFMult().unmodify(id);
    stats.getMaxRecoilMult().unmodify(id);
    stats.getRecoilDecayMult().unmodify(id);
    stats.getRecoilPerShotMult().unmodify(id);
    stats.getEnergyWeaponFluxCostMod().unmodify(id);
    stats.getMissileMaxSpeedBonus().unmodify(id);
    stats.getEnergyWeaponDamageMult().unmodify(id);
  }
  
  public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel)
  {
    float mult = 1.0F + 0.25F * effectLevel;
    float bonusPercent = (int)(mult - 1.0F) * 100.0F;
    if (index == 0)
      return new ShipSystemStatsScript.StatusData("all weapons rate of fire +" + (int)bonusPercent + "%", false);
    if (index == 1)
      return new ShipSystemStatsScript.StatusData("energy weapon damage increased", false);
    if (index == 2) {
      return new ShipSystemStatsScript.StatusData("weapon accuracy decreased", false);
    }
    return null;
  }
}
