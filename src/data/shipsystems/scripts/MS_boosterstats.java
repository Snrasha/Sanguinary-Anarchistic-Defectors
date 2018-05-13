package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

public class MS_boosterstats implements com.fs.starfarer.api.plugins.ShipSystemStatsScript
{
  public MS_boosterstats() {}
  
  public void apply(MutableShipStatsAPI stats, String id, com.fs.starfarer.api.plugins.ShipSystemStatsScript.State state, float effectLevel) {
    if (state == com.fs.starfarer.api.plugins.ShipSystemStatsScript.State.OUT) {
      stats.getMaxSpeed().modifyPercent(id, 100.0F * effectLevel);
      stats.getMaxTurnRate().modifyPercent(id, 100.0F * effectLevel);
      stats.getDeceleration().modifyPercent(id, 100.0F * effectLevel);
    } else {
      stats.getMaxSpeed().modifyFlat(id, 80.0F * effectLevel);
      stats.getMaxSpeed().modifyPercent(id, 5.0F * effectLevel);
      stats.getAcceleration().modifyPercent(id, 220.0F * effectLevel);
      stats.getDeceleration().modifyPercent(id, 150.0F * effectLevel);
      stats.getTurnAcceleration().modifyFlat(id, 100.0F * effectLevel);
      stats.getTurnAcceleration().modifyPercent(id, 200.0F * effectLevel);
      stats.getMaxTurnRate().modifyFlat(id, 50.0F * effectLevel);
      stats.getMaxTurnRate().modifyPercent(id, 100.0F * effectLevel);
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
  
  public com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData getStatusData(int index, com.fs.starfarer.api.plugins.ShipSystemStatsScript.State state, float effectLevel)
  {
    if (index == 0)
      return new com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData("improved maneuverability", false);
    if (index == 1) {
      return new com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData("increased top speed", false);
    }
    return null;
  }
}
