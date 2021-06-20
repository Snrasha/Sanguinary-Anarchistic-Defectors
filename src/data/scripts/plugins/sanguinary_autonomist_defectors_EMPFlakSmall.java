package src.data.scripts.plugins;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import src.data.utils.sanguinary_autonomist_defectors_Utils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class sanguinary_autonomist_defectors_EMPFlakSmall extends BaseEveryFrameCombatPlugin
{

  private static final Color effectColor1 = new Color(100, 200, 255, 215);
  private static final Color effectColor2 = new Color(35, 50, 85, 150);

  private static final Vector2f ZERO = new Vector2f();
  
  private final Set<DamagingProjectileAPI> DO_NOT_EXPLODE = new HashSet();
  private CombatEngineAPI engine;
  
  public sanguinary_autonomist_defectors_EMPFlakSmall() {}
  
  public static void flakEMPExplode(DamagingProjectileAPI projectile, Vector2f point, CombatEngineAPI engine) {
    if (point == null)
    {
      return;
    }
    
    sanguinary_autonomist_defectors_effectsHook.createEMPShockwave(point);
    
    engine.addHitParticle(point, ZERO, 12.0F, 1.0F, 0.25F, Color.WHITE);
    engine.addHitParticle(point, ZERO, 15.0F, 0.4F, 0.4F, effectColor1);
    Vector2f vel = new Vector2f();
    for (int i = 0; i < 30; i++)
    {
      vel.set(((float)Math.random() * 1.25F + 0.25F) * 15.0F, 0.0F);
      VectorUtils.rotate(vel, (float)Math.random() * 360.0F, vel);
      engine.addSmoothParticle(projectile.getLocation(), vel, (float)Math.random() * 2.5F + 2.5F, 1.0F, 
        (float)Math.random() * 0.3F + 0.6F, effectColor2);
    }
    
    for (int i = 0; i < 3; i++) {
      float angle = (float)Math.random() * 360.0F;
      float distance = (float)Math.random() * 10.0F + 20.0F;
      Vector2f point1 = MathUtils.getPointOnCircumference(point, distance, angle);
      Vector2f point2 = new Vector2f(point);
      engine.spawnEmpArc(projectile.getSource(), point1, new SimpleEntity(point1), new SimpleEntity(point2), DamageType.ENERGY, 0.0F, 0.0F, 1000.0F, null, 15.0F, effectColor1, effectColor2);
    }
    


    StandardLight light = new StandardLight(projectile.getLocation(), ZERO, ZERO, null);
    light.setColor(effectColor1);
    light.setSize(16.5F);
    light.setIntensity(0.15F);
    light.fadeOut(0.2F);
    org.dark.shaders.light.LightShader.addLight(light);
    
    com.fs.starfarer.api.Global.getSoundPlayer().playSound("sanguinary_autonomist_defectors_lemp_shot_impact", 1.0F, 1.0F, point, projectile.getVelocity());
    
    List<ShipAPI> ships = CombatUtils.getShipsWithinRange(point, 30.0F);
    List<CombatEntityAPI> targets = CombatUtils.getAsteroidsWithinRange(point, 30.0F);
    
    List<MissileAPI> missiles = CombatUtils.getMissilesWithinRange(point, 30.0F);
    
    Iterator<ShipAPI> iter = ships.iterator();
    boolean remove; while (iter.hasNext())
    {
      ShipAPI ship = (ShipAPI)iter.next();
      if (ship.getCollisionClass() == CollisionClass.NONE)
      {
        iter.remove();


      }
      else if ((ship.isFighter()) || (ship.isDrone()))
      {



        remove = false;
        for (ShipAPI shp : ships)
        {
          if ((shp.getShield() != null) && (shp != ship))
          {
            if ((shp.getShield().isWithinArc(ship.getLocation())) && (shp.getShield().isOn()) && 
              (MathUtils.getDistance(ship.getLocation(), shp.getShield().getLocation()) <= shp.getShield().getRadius()))
            {
              remove = true;
            }
          }
        }
        
        if (remove)
        {
          iter.remove();
        }
      }
    }
    ships = sanguinary_autonomist_defectors_Utils.getSortedAreaList(point, ships);
    targets.addAll(ships);
    ShipAPI targ = null;
    
    for (CombatEntityAPI tgt : targets)
    {

      if (tgt.getOwner() != projectile.getOwner())
      {



        float distance = sanguinary_autonomist_defectors_Utils.getActualDistance(point, tgt, true);
        float reduction = 1.0F;
        if (distance > 30.0F)
        {
          reduction = (12.0F - distance) / -18.0F;
        }
        
        if (reduction > 0.0F)
        {



          List<CombatEntityAPI> rocks = CombatUtils.getAsteroidsWithinRange(point, 30.0F);
          rocks.addAll(missiles);
          
          if (!rocks.contains(tgt)) {
            targ = (ShipAPI)tgt;
          }
          
          boolean shieldHit = false;
          if ((tgt instanceof ShipAPI))
          {
            ShipAPI ship = (ShipAPI)tgt;
            if ((ship.getShield() != null) && (ship.getShield().isWithinArc(point)))
            {
              shieldHit = true;
            }
          }
          
          Vector2f damagePoint;
          if (shieldHit)
          {
            ShipAPI ship = (ShipAPI)tgt;
            damagePoint = MathUtils.getPointOnCircumference(null, ship.getShield().getRadius(), VectorUtils.getAngle(ship.getShield().getLocation(), point));
            Vector2f.add(damagePoint, tgt.getLocation(), damagePoint);
          }
          else
          {
            Vector2f projection = VectorUtils.getDirectionalVector(point, tgt.getLocation());
            projection.scale(tgt.getCollisionRadius());
            Vector2f.add(projection, tgt.getLocation(), projection);
            damagePoint = CollisionUtils.getCollisionPoint(point, projection, tgt);
          }
          if (damagePoint == null)
          {
            damagePoint = point;
          }
          
          engine.applyDamage(tgt, damagePoint, 30.0F * reduction, DamageType.ENERGY, 300.0F * reduction, false, false, projectile.getSource());
        }
      } }
    for (MissileAPI mtgt : missiles) {
      float distance = sanguinary_autonomist_defectors_Utils.getActualDistance(point, mtgt, true);
      float reduction = 1.0F;
      if (distance > 30.0F)
      {
        reduction = (12.0F - distance) / -18.0F;
      }
      
      if (reduction > 0.0F)
      {




        Vector2f projection = VectorUtils.getDirectionalVector(point, mtgt.getLocation());
        projection.scale(mtgt.getCollisionRadius());
        Vector2f.add(projection, mtgt.getLocation(), projection);
        Vector2f damagePoint = CollisionUtils.getCollisionPoint(point, projection, mtgt);
        

        if ((targ == null)) //|| (!targ.getVariant().getHullMods().contains("tem_latticeshield")) || ((sanguinary_autonomist_defectors_ModPlugin.templarsExist) && (data.scripts.hullmods.TEM_LatticeShield.shieldLevel(targ) <= 0.0F) && (targ.getVariant().getHullMods().contains("tem_latticeshield"))))
        {

          if (damagePoint != null) {
            engine.spawnEmpArc(projectile.getSource(), damagePoint, mtgt, mtgt, DamageType.ENERGY, 10.0F * reduction, 300.0F * reduction, 100.0F, null, 10.0F, effectColor1, effectColor1);
          }
        }
      }
    }
    








    engine.removeEntity(projectile);
  }
  

  @Override
  public void advance(float amount, List<InputEventAPI> events)
  {
    if (engine == null) {
      return;
    }
    if (engine.isPaused()) {
      return;
    }
    
    List<DamagingProjectileAPI> projectiles = engine.getProjectiles();
    

    List<DamagingProjectileAPI> toRemove = new ArrayList();
    for (DamagingProjectileAPI proj : DO_NOT_EXPLODE)
    {
      if (!projectiles.contains(proj))
      {
        toRemove.add(proj);
      }
    }
    DO_NOT_EXPLODE.removeAll(toRemove);
    
    int size = projectiles.size();
    for (int i = 0; i < size; i++) {
      DamagingProjectileAPI flare = (DamagingProjectileAPI)projectiles.get(i);
      String spec = flare.getProjectileSpecId();
      Vector2f loc = flare.getLocation();
      if (spec != null)
      {


        switch (spec)
        {
        case "sanguinary_autonomist_defectors_lemp_shot": 
          if (!flare.didDamage())
          {


            if (!DO_NOT_EXPLODE.contains(flare))
            {



              List<CombatEntityAPI> toCheck = new java.util.LinkedList();
              List<CombatEntityAPI> asteroids = CombatUtils.getAsteroidsWithinRange(loc, 30.0F);
              toCheck.addAll(CombatUtils.getShipsWithinRange(loc, 30.0F));
              toCheck.addAll(CombatUtils.getMissilesWithinRange(loc, 30.0F));
              toCheck.addAll(asteroids);
              
              for (CombatEntityAPI entity : toCheck)
                if ((entity.getCollisionClass() != CollisionClass.NONE) && 
                

                  (entity != flare.getSource()))
                {



                  if (entity.getOwner() == flare.getOwner())
                  {

                    if ((entity instanceof DamagingProjectileAPI)) {
                      continue;
                    }
                    


                    if ((entity instanceof ShipAPI))
                    {
                      ShipAPI ship = (ShipAPI)entity;
                      if (((ship.isFighter()) || (ship.isDrone())) && (ship.isAlive()) && (!ship.getEngineController().isFlamedOut())) {
                        continue;
                      }
                    }
                  }
                  


                  if (entity.getShield() != null)
                  {
                    Vector2f ahead = new Vector2f(loc).translate(flare.getVelocity().getX() * 0.067F, flare
                      .getVelocity().getY() * 0.067F);
                    ShieldAPI shield = entity.getShield();
                    if ((CollisionUtils.getCollides(loc, ahead, shield.getLocation(), shield.getRadius())) && 
                      (shield.isWithinArc(ahead)))
                    {
                      if ((entity.getOwner() == flare.getOwner()) || (entity.getOwner() > 1))
                      {
                        DO_NOT_EXPLODE.add(flare); break;
                      }
                      

                      DO_NOT_EXPLODE.add(flare);
                      flakEMPExplode(flare, loc, engine);
                      
                      break;
                    }
                  }
                  


                  if ((entity.getOwner() == flare.getOwner()) || (entity.getOwner() > 1))
                  {
                    float distance = sanguinary_autonomist_defectors_Utils.getActualDistance(loc, entity, true);
                    if (distance <= 30.0F)
                    {


                      Vector2f ahead = new Vector2f(loc).translate(flare.getVelocity().getX() * 0.067F, flare
                        .getVelocity().getY() * 0.067F);
                      if (CollisionUtils.getCollisionPoint(loc, ahead, entity) != null)
                      {
                        DO_NOT_EXPLODE.add(flare);
                        break;
                      }
                    }
                  }
                  

                  if ((!asteroids.contains(entity)) && 
                  




                    ((flare.getOwner() != 0) || (entity.getOwner() == 1)) && (
                    


                    (flare.getOwner() != 1) || (entity.getOwner() == 0)))
                  {




                    float distance = sanguinary_autonomist_defectors_Utils.getActualDistance(loc, entity, true);
                    if (distance <= 30.0F)
                    {
                      DO_NOT_EXPLODE.add(flare);
                      flakEMPExplode(flare, loc, engine);
                      break;
                    }
                  }
                }
              if ((flare.isFading()) && (!DO_NOT_EXPLODE.contains(flare)))
              {
                DO_NOT_EXPLODE.add(flare);
                flakEMPExplode(flare, loc, engine);
              }
            }
          }
          
          break;
        }
        
      }
    }
  }
  
  @Override
  public void init(CombatEngineAPI engine)
  {
    this.engine = engine;
  }
}
