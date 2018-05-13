package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.State;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_swacs extends com.fs.starfarer.api.impl.combat.BaseShipSystemScript
{
  private CombatEngineAPI engine;
  public static final Object KEY_JITTER = new Object();
  
  private static final Color COLOR1 = new Color(210, 125, 105, 155);
  
  public static final Color JITTER_UNDER_COLOR = new Color(255, 50, 0, 125);
  public static final Color JITTER_COLOR = new Color(255, 50, 0, 75);
  
  public static final float ACCURACY_BONUS = 20.0F;
  
  public static final float RANGE_BONUS = 10.0F;
  public static final float DAMAGE_BOOST = 33.0F;
  public static final float AGILITY_BONUS = 15.0F;
  private static final Vector2f ZERO = new Vector2f();
  

  private static final Map<ShipAPI, ShipAPI> receiving = new HashMap();
  
  public MS_swacs() {}
  
  public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) { if (engine != Global.getCombatEngine()) {
      engine = Global.getCombatEngine();
      receiving.clear();
    }
    
    ShipAPI ship = null;
    if ((stats.getEntity() instanceof ShipAPI))
      ship = (ShipAPI)stats.getEntity(); else {
      return;
    }
    float jitterLevel;
    float jitterRangeBonus;
    if (effectLevel > 0.0F) {
      jitterLevel = effectLevel;
      float maxRangeBonus = 5.0F;
      jitterRangeBonus = jitterLevel * maxRangeBonus;
      for (ShipAPI fighter : getFighters(ship)) {
        if (!fighter.isHulk()) {
          MutableShipStatsAPI fStats = fighter.getMutableStats();
          
          fStats.getBallisticWeaponDamageMult().modifyPercent(id, 1.0F + 0.32999998F * effectLevel);
          fStats.getEnergyWeaponDamageMult().modifyPercent(id, 1.0F + 0.32999998F * effectLevel);
          fStats.getMissileWeaponDamageMult().modifyPercent(id, 1.0F + 0.32999998F * effectLevel);
          
          fStats.getAutofireAimAccuracy().modifyPercent(id, 20.0F);
          fStats.getBallisticWeaponRangeBonus().modifyPercent(id, 10.0F);
          fStats.getEnergyWeaponRangeBonus().modifyPercent(id, 10.0F);
          fStats.getBeamWeaponRangeBonus().modifyPercent(id, 10.0F);
          
          fStats.getMaxSpeed().modifyPercent(id, 15.0F);
          fStats.getAcceleration().modifyPercent(id, 15.0F);
          fStats.getDeceleration().modifyPercent(id, 15.0F);
          fStats.getMaxTurnRate().modifyPercent(id, 15.0F);
          fStats.getTurnAcceleration().modifyPercent(id, 15.0F);
          
          if (jitterLevel > 0.0F)
          {
            fighter.setWeaponGlow(effectLevel, Misc.setAlpha(JITTER_UNDER_COLOR, 255), java.util.EnumSet.allOf(com.fs.starfarer.api.combat.WeaponAPI.WeaponType.class));
            
            fighter.setJitterUnder(KEY_JITTER, JITTER_COLOR, jitterLevel, 5, 0.0F, jitterRangeBonus);
            fighter.setJitter(KEY_JITTER, JITTER_UNDER_COLOR, jitterLevel, 2, 0.0F, 0.0F + jitterRangeBonus * 1.0F);
            Global.getSoundPlayer().playLoop("system_targeting_feed_loop", ship, 1.0F, 1.0F, fighter.getLocation(), fighter.getVelocity());
            
            for (ShipEngineControllerAPI.ShipEngineAPI engines : fighter.getEngineController().getShipEngines()) {
              if (!engines.isDisabled())
              {
                for (int i = 0; i < 5; i++) {
                  float size = MathUtils.getRandomNumberInRange(8.0F, 2.0F);
                  Vector2f spawn = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius());
                  
                  if ((Math.random() > 0.9D) && (!engine.isPaused()))
                    engine.addSmoothParticle(spawn, ZERO, size, (float)Math.random() * 1.0F, 1.0F, COLOR1);
                }
              }
            }
          }
        }
      }
    }
  }
  
  private List<ShipAPI> getFighters(ShipAPI carrier) {
    List<ShipAPI> result = new ArrayList();
    
    for (ShipAPI ship : Global.getCombatEngine().getShips()) {
      if ((ship.isFighter()) && 
        (ship.getWing() != null)) {
        if (ship.getWing().getSourceShip() == carrier) {
          result.add(ship);
        }
      }
    }
    return result;
  }
  
  public void unapply(MutableShipStatsAPI stats, String id)
  {
    ShipAPI ship = null;
    
    if ((stats.getEntity() instanceof ShipAPI)) {
      ship = (ShipAPI)stats.getEntity();
    } else {
      return;
    }
    for (ShipAPI fighter : getFighters(ship)) {
      if (!fighter.isHulk()) {
        MutableShipStatsAPI fStats = fighter.getMutableStats();
        
        fStats.getBallisticWeaponDamageMult().unmodify(id);
        fStats.getEnergyWeaponDamageMult().unmodify(id);
        fStats.getMissileWeaponDamageMult().unmodify(id);
        
        fStats.getAutofireAimAccuracy().unmodify(id);
        fStats.getBallisticWeaponRangeBonus().unmodify(id);
        fStats.getEnergyWeaponRangeBonus().unmodify(id);
        fStats.getMaxSpeed().unmodify(id);
        
        fStats.getAcceleration().unmodify(id);
        fStats.getDeceleration().unmodify(id);
        fStats.getTurnAcceleration().unmodify(id);
        fStats.getMaxTurnRate().unmodify(id);
      }
    }
  }
  
  public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {
    if (index == 0)
      return new ShipSystemStatsScript.StatusData("" + Misc.getRoundedValueMaxOneAfterDecimal(1.0F + 33.0F * effectLevel * 0.01F) + "x fighter damage", false);
    if (index == 1) {
      return new ShipSystemStatsScript.StatusData("" + Misc.getRoundedValueMaxOneAfterDecimal(1.0F + 15.0F * effectLevel * 0.01F) + "x fighter speed", false);
    }
    return null;
  }
  
  public void init(CombatEngineAPI engine, ShipAPI host) {
    this.engine = engine;
  }
}
