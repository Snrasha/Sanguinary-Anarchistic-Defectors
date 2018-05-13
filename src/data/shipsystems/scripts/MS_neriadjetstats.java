package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

public class MS_neriadjetstats implements com.fs.starfarer.api.plugins.ShipSystemStatsScript
{
  public MS_neriadjetstats() {}
  
  public void apply(MutableShipStatsAPI stats, String id, com.fs.starfarer.api.plugins.ShipSystemStatsScript.State state, float effectLevel) {
    if (state == com.fs.starfarer.api.plugins.ShipSystemStatsScript.State.OUT) {
      stats.getMaxSpeed().unmodify(id);
    } else {
      stats.getMaxSpeed().modifyFlat(id, 200.0F);
      stats.getAcceleration().modifyPercent(id, 200.0F * effectLevel);
    }
  }
  
  public void unapply(MutableShipStatsAPI stats, String id)
  {
    stats.getMaxSpeed().unmodify(id);
    stats.getAcceleration().unmodify(id);
  }
  
  public com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData getStatusData(int index, com.fs.starfarer.api.plugins.ShipSystemStatsScript.State state, float effectLevel)
  {
    if (index == 0) {
      return new com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData("+200 top speed", false);
    }
    return null;
  }
}
