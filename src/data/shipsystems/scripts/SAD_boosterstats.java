package src.data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

public class SAD_boosterstats implements com.fs.starfarer.api.plugins.ShipSystemStatsScript
{
  public SAD_boosterstats() {}
  
  @Override
  public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
    if (state == State.OUT) {
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
  
  @Override
  public void unapply(MutableShipStatsAPI stats, String id)
  {
    stats.getMaxSpeed().unmodify(id);
    stats.getMaxTurnRate().unmodify(id);
    stats.getTurnAcceleration().unmodify(id);
    stats.getAcceleration().unmodify(id);
    stats.getDeceleration().unmodify(id);
  }
  
  @Override
  public StatusData getStatusData(int index, State state, float effectLevel)
  {
    if (index == 0)
      return new StatusData("improved maneuverability", false);
    if (index == 1) {
      return new StatusData("increased top speed", false);
    }
    return null;
  }
}
