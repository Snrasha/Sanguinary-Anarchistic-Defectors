package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.FluxTrackerAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_siegemodeAI implements com.fs.starfarer.api.combat.ShipSystemAIScript
{
  private ShipSystemAPI system;
  private ShipAPI ship;
  private ShipwideAIFlags flags;
  private float siegeModeTrigger;
  private float siegeModeTriggerMin;
  private float siegeModeTriggerMax;
  
  public MS_siegemodeAI() {}
  
  public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, com.fs.starfarer.api.combat.CombatEngineAPI engine)
  {
    this.ship = ship;
    this.system = system;
    this.flags = flags;
    
    if (ship.getShield().getType() == com.fs.starfarer.api.combat.ShieldAPI.ShieldType.FRONT) {
      siegeModeTrigger = 265.0F;
      siegeModeTriggerMin = 120.0F;
      siegeModeTriggerMax = 240.0F;
    } else if (ship.getShield().getType() == com.fs.starfarer.api.combat.ShieldAPI.ShieldType.OMNI) {
      siegeModeTrigger = 60.0F;
      siegeModeTriggerMin = 30.0F;
      siegeModeTriggerMax = 330.0F;
    } else {
      siegeModeTrigger = 999.0F;
      siegeModeTriggerMin = 999.0F;
      siegeModeTriggerMax = 0.0F;
    }
  }
  
  public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target)
  {
    boolean shieldExtensionNeeded = false;
    float facing; if (ship.getShield().getActiveArc() > siegeModeTrigger) {
      facing = ship.getShield().getFacing();
      if (missileDangerDir != null) {
        for (com.fs.starfarer.api.combat.CombatEntityAPI entity : AIUtils.getNearbyEnemyMissiles(ship, 800.0F)) {
          float relativeFacing = MathUtils.clampAngle(VectorUtils.getFacing(VectorUtils.getDirectionalVector(ship.getLocation(), entity.getLocation())) - facing);
          if ((relativeFacing < siegeModeTriggerMax) && (relativeFacing > siegeModeTriggerMin)) {
            shieldExtensionNeeded = true;
            break;
          }
        }
      }
      if (!shieldExtensionNeeded) {
        for (ShipAPI shp : AIUtils.getNearbyEnemies(ship, 1600.0F))
          if (((!shp.getFluxTracker().isOverloaded()) || (shp.getFluxTracker().getOverloadTimeRemaining() <= 1.8F)) && ((!shp.getFluxTracker().isVenting()) || (shp.getFluxTracker().getTimeToVent() <= 1.8F)))
          {

            float relativeFacing = MathUtils.clampAngle(VectorUtils.getFacing(VectorUtils.getDirectionalVector(ship.getLocation(), shp.getLocation())) - facing);
            if ((relativeFacing < siegeModeTriggerMax) && (relativeFacing > siegeModeTriggerMin)) {
              shieldExtensionNeeded = true;
              break;
            }
          }
      }
    }
    if ((shieldExtensionNeeded) || (flags.hasFlag(com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags.TURN_QUICKLY))) {
      activateSystem();
    } else {
      deactivateSystem();
    }
  }
  
  private void deactivateSystem() {
    if (system.isOn()) {
      ship.useSystem();
    }
  }
  
  private void activateSystem() {
    if (!system.isOn()) {
      ship.useSystem();
    }
  }
}
