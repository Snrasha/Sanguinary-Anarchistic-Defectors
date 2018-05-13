package data.shipsystems.scripts;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.State;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_mimirSlideDrive implements ShipSystemStatsScript
{
  private AnimationAPI theInvert;
  private AnimationAPI theLight;
  private final List<WeaponAPI> theBlack = new ArrayList();
  private final IntervalUtil anim = new IntervalUtil(0.03F, 0.03F);
  private int frameL = 0;
  private int maxFrameL = 0;
  private float invert;
  private static final String DATA_KEY = "ms_RRSDrive";
  
  public MS_mimirSlideDrive() {}
  
  public static float effectLevel(ShipAPI ship) {
    LocalData localData = (LocalData)Global.getCombatEngine().getCustomData().get("ms_RRSDrive");
    if (localData == null) {
      return 0.0F;
    }
    
    Map<ShipAPI, Float> acting = acting;
    
    if (acting.containsKey(ship)) {
      return ((Float)acting.get(ship)).floatValue();
    }
    return 0.0F;
  }
  

  private boolean runOnce = false;
  private boolean started = false;
  
  public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel)
  {
    CombatEngineAPI engine = Global.getCombatEngine();
    if (!engine.getCustomData().containsKey("ms_RRSDrive")) {
      engine.getCustomData().put("ms_RRSDrive", new LocalData(null));
    }
    
    ShipAPI ship = (ShipAPI)stats.getEntity();
    List<WeaponAPI> allWeapons = ship.getAllWeapons();
    if (!runOnce) {
      runOnce = true;
      theBlack.clear();
      for (WeaponAPI w : allWeapons) {
        switch (w.getSlot().getId()) {
        case "INVERT": 
          w.getAnimation().setFrame(1);
          theInvert = w.getAnimation();
          theInvert.setAlphaMult(0.0F);
          break;
        case "LIGHTER": 
          theLight = w.getAnimation();
          maxFrameL = theLight.getNumFrames();
        }
        
      }
      return;
    }
    
    float amount = engine.getElapsedInLastFrame();
    
    if (engine.isPaused()) {
      return;
    }
    
    LocalData localData = (LocalData)engine.getCustomData().get("ms_RRSDrive");
    Object acting = acting;
    
    if (ship.isAlive()) {
      if (effectLevel > 0.0F) {
        ((Map)acting).put(ship, Float.valueOf(effectLevel));
        
        anim.advance(amount);
        
        if ((anim.intervalElapsed()) && (state != ShipSystemStatsScript.State.OUT)) {
          frameL = MathUtils.getRandomNumberInRange(0, maxFrameL - 1);
        }
        
        invert = MathUtils.getRandomNumberInRange(0.9F, 1.0F);
        if (state == ShipSystemStatsScript.State.OUT) {
          invert = Math.max(invert - (float)Math.cos(3.141592653589793D * effectLevel), 0.0F);
          frameL = 0;
        }
        
        theInvert.setAlphaMult(invert);
        theInvert.setFrame(1);
        theLight.setFrame(frameL);
      }
      
      if (state == ShipSystemStatsScript.State.IN) {
        if (!started) {
          started = true;
        }
        
        float speed = ship.getVelocity().length();
        if (speed <= 0.1F) {
          ship.getVelocity().set(org.lazywizard.lazylib.VectorUtils.getDirectionalVector(ship.getLocation(), ship.getVelocity()));
        }
        if (speed < 900.0F) {
          ship.getVelocity().normalise();
          ship.getVelocity().scale(speed + amount * 3600.0F);
        }
      } else if (state == ShipSystemStatsScript.State.ACTIVE) {
        float speed = ship.getVelocity().length();
        if (speed < 900.0F) {
          ship.getVelocity().normalise();
          ship.getVelocity().scale(speed + amount * 3600.0F);
        }
        
        stats.getArmorDamageTakenMult().modifyPercent(id, 0.5F);
        stats.getHullDamageTakenMult().modifyPercent(id, 0.5F);
      } else {
        float speed = ship.getVelocity().length();
        if (speed > ship.getMutableStats().getMaxSpeed().getModifiedValue()) {
          ship.getVelocity().normalise();
          ship.getVelocity().scale(speed - amount * 3600.0F);
        }
      }
    }
  }
  
  public void unapply(MutableShipStatsAPI stats, String id)
  {
    ShipAPI ship = (ShipAPI)stats.getEntity();
    started = false;
    

    if (ship != null) {
      if (!Global.getCombatEngine().getCustomData().containsKey("ms_RRSDrive")) {
        Global.getCombatEngine().getCustomData().put("ms_RRSDrive", new LocalData(null));
      }
      LocalData localData = (LocalData)Global.getCombatEngine().getCustomData().get("ms_RRSDrive");
      if (localData != null) {
        Map<ShipAPI, Float> acting = acting;
        
        acting.remove(ship);
      }
    }
    
    stats.getArmorDamageTakenMult().unmodify();
    stats.getHullDamageTakenMult().unmodify();
  }
  
  public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel)
  {
    if (index == 0) {
      return new ShipSystemStatsScript.StatusData("vector locked", false);
    }
    if (index == 1) {
      return new ShipSystemStatsScript.StatusData("armor plates locked", false);
    }
    return null;
  }
  
  private static final class LocalData
  {
    final Map<ShipAPI, Float> acting = new HashMap(50);
    
    private LocalData() {}
  }
}
