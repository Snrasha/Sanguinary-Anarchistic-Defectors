package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

public class MS_ScanDroneStats implements com.fs.starfarer.api.plugins.ShipSystemStatsScript
{
  public static final float SENSOR_RANGE_PERCENT = 10.0F;
  public static final float WEAPON_RANGE_PERCENT = 25.0F;
  
  public MS_ScanDroneStats() {}
  
  public void apply(MutableShipStatsAPI stats, String id, com.fs.starfarer.api.plugins.ShipSystemStatsScript.State state, float effectLevel) {
    float sensorRangePercent = 10.0F * effectLevel;
    float weaponRangePercent = 25.0F * effectLevel;
    
    stats.getSightRadiusMod().modifyPercent(id, sensorRangePercent);
    
    stats.getBallisticWeaponRangeBonus().modifyPercent(id, weaponRangePercent);
    stats.getEnergyWeaponRangeBonus().modifyPercent(id, weaponRangePercent);
  }
  
  public void unapply(MutableShipStatsAPI stats, String id) {
    stats.getSightRadiusMod().unmodify(id);
    
    stats.getBallisticWeaponRangeBonus().unmodify(id);
    stats.getEnergyWeaponRangeBonus().unmodify(id);
  }
  
  public com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData getStatusData(int index, com.fs.starfarer.api.plugins.ShipSystemStatsScript.State state, float effectLevel)
  {
    float sensorRangePercent = 10.0F * effectLevel;
    float weaponRangePercent = 25.0F * effectLevel;
    if (index == 0)
      return new com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData("sensor range +" + (int)sensorRangePercent + "%", false);
    if (index == 1)
    {
      return null; }
    if (index == 2) {
      return new com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData("weapon range +" + (int)weaponRangePercent + "%", false);
    }
    return null;
  }
}
