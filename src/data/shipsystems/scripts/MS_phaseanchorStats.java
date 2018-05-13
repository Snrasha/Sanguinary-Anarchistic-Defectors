package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.State;
import java.util.Map;

public class MS_phaseanchorStats implements com.fs.starfarer.api.plugins.ShipSystemStatsScript
{
  public static final java.awt.Color JITTER_COLOR = new java.awt.Color(255, 175, 255, 255);
  
  public static final float JITTER_FADE_TIME = 0.5F;
  
  private static final Map<ShipAPI.HullSize, Float> mag = new java.util.HashMap();
  
  static { mag.put(ShipAPI.HullSize.FIGHTER, Float.valueOf(60.0F));
    mag.put(ShipAPI.HullSize.FRIGATE, Float.valueOf(60.0F));
    mag.put(ShipAPI.HullSize.DESTROYER, Float.valueOf(50.0F));
    mag.put(ShipAPI.HullSize.CRUISER, Float.valueOf(40.0F));
    mag.put(ShipAPI.HullSize.CAPITAL_SHIP, Float.valueOf(30.0F));
  }
  

  public static final float SHIP_ALPHA_MULT = 0.25F;
  
  public static final float VULNERABLE_FRACTION = 0.0F;
  
  public static final float INCOMING_DAMAGE_MULT = 0.25F;
  public static final float MAX_TIME_MULT = 3.0F;
  protected Object STATUSKEY1 = new Object();
  protected Object STATUSKEY2 = new Object();
  protected Object STATUSKEY3 = new Object();
  protected Object STATUSKEY4 = new Object();
  
  private void maintainStatus(ShipAPI playerShip, ShipSystemStatsScript.State state, float effectLevel) {
    float level = effectLevel;
    float f = 0.0F;
    
    ShipSystemAPI cloak = playerShip.getPhaseCloak();
    if (cloak == null) cloak = playerShip.getSystem();
    if (cloak == null) { return;
    }
    if (level > f)
    {

      Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY2, cloak
        .getSpecAPI().getIconSpriteName(), cloak.getDisplayName(), "time flow altered", false);
    }
  }
  





  public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel)
  {
    ShipAPI ship = null;
    boolean player = false;
    if ((stats.getEntity() instanceof ShipAPI)) {
      ship = (ShipAPI)stats.getEntity();
      player = ship == Global.getCombatEngine().getPlayerShip();
      id = id + "_" + ship.getId();
    } else {
      return;
    }
    
    if (player) {
      maintainStatus(ship, state, effectLevel);
    }
    
    if (Global.getCombatEngine().isPaused()) {
      return;
    }
    
    if ((state == ShipSystemStatsScript.State.COOLDOWN) || (state == ShipSystemStatsScript.State.IDLE)) {
      unapply(stats, id);
      return;
    }
    
    float level = effectLevel;
    

    float jitterLevel = 0.0F;
    float jitterRangeBonus = 0.0F;
    float levelForAlpha = level;
    
    ShipSystemAPI cloak = ship.getPhaseCloak();
    if (cloak == null) { cloak = ship.getSystem();
    }
    if ((state == ShipSystemStatsScript.State.IN) || (state == ShipSystemStatsScript.State.ACTIVE)) {
      ship.setPhased(true);
      levelForAlpha = level;
      
      stats.getAcceleration().modifyFlat(id, ((Float)mag.get(ship.getHullSize())).floatValue() * effectLevel);
      stats.getDeceleration().modifyFlat(id, ((Float)mag.get(ship.getHullSize())).floatValue() * effectLevel);
      
      stats.getTurnAcceleration().modifyPercent(id, 180.0F * effectLevel);
      stats.getMaxTurnRate().modifyPercent(id, 140.0F);
      
      stats.getMaxSpeed().modifyFlat(id, ((Float)mag.get(ship.getHullSize())).floatValue() * effectLevel);
    } else if (state == ShipSystemStatsScript.State.OUT) {
      ship.setPhased(true);
      levelForAlpha = level;
      
      stats.getMaxSpeed().unmodify(id);
      stats.getMaxTurnRate().unmodify(id);
    }
    
    ship.setExtraAlphaMult(1.0F - 0.75F * levelForAlpha);
    ship.setApplyExtraAlphaToEngines(true);
    

    float shipTimeMult = 1.0F + 2.0F * levelForAlpha;
    stats.getTimeMult().modifyMult(id, shipTimeMult);
    if (player) {
      Global.getCombatEngine().getTimeMult().modifyMult(id, 1.0F / shipTimeMult);
    } else {
      Global.getCombatEngine().getTimeMult().unmodify(id);
    }
  }
  
  public void unapply(MutableShipStatsAPI stats, String id)
  {
    ShipAPI ship = null;
    
    if ((stats.getEntity() instanceof ShipAPI)) {
      ship = (ShipAPI)stats.getEntity();
    }
    else
    {
      return;
    }
    
    Global.getCombatEngine().getTimeMult().unmodify(id);
    stats.getTimeMult().unmodify(id);
    
    ship.setPhased(false);
    ship.setExtraAlphaMult(1.0F);
    
    stats.getMaxSpeed().unmodify(id);
    stats.getMaxTurnRate().unmodify(id);
    stats.getTurnAcceleration().unmodify(id);
    stats.getAcceleration().unmodify(id);
    stats.getDeceleration().unmodify(id);
  }
  






  public com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel)
  {
    return null;
  }
  
  public MS_phaseanchorStats() {}
}
