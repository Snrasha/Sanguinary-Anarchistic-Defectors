package src.data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.lwjgl.util.vector.Vector2f;

public class SAD_effectsHook extends BaseEveryFrameCombatPlugin
{
  private CombatEngineAPI engine;
  
  public static void createFlakShockwave(Vector2f location)
  {
    LocalData localData = (LocalData)Global.getCombatEngine().getCustomData().get("SAD_effectsHook");
    if (localData == null)
    {
      return;
    }
    
    List<Shockwave> shockwaves = localData.shockwaves;
    
    shockwaves.add(new Shockwave(location, 0.2F, 0.8F, 0.15F));
  }
  
  public static void createShockwave(Vector2f location)
  {
    LocalData localData = (LocalData)Global.getCombatEngine().getCustomData().get("SAD_effectsHook");
    if (localData == null)
    {
      return;
    }
    
    List<Shockwave> shockwaves = localData.shockwaves;
    
    shockwaves.add(new Shockwave(location, 0.25F, 1.5F, 0.25F));
  }
  
  public static void createEMPShockwave(Vector2f location)
  {
    LocalData localData = (LocalData)Global.getCombatEngine().getCustomData().get("SAD_effectsHook");
    if (localData == null)
    {
      return;
    }
    
    List<Shockwave> shockwaves = localData.shockwaves;
    
    shockwaves.add(new Shockwave(location, 0.05F, 1.0F, 0.2F));
  }
  
  
 

  public static void createPing(Vector2f location, Vector2f velocity)
  {
    LocalData localData = (LocalData)Global.getCombatEngine().getCustomData().get("SAD_effectsHook");
    if (localData == null)
    {
      return;
    }
    
    List<tagPing> pings = localData.pings;
    
    pings.add(new tagPing(location, velocity, 3.0F, 2.5F, 0.55F));
  }
  

  @Override
  public void advance(float amount, List<InputEventAPI> events)
  {
    if (engine == null)
    {
      return;
    }
    
    if (engine.isPaused())
    {
      return;
    }
    
    LocalData localData = (LocalData)engine.getCustomData().get("SAD_effectsHook");
    List<Shockwave> shockwaves = localData.shockwaves;
    
    Iterator<Shockwave> iter = shockwaves.iterator();
    while (iter.hasNext())
    {
      Shockwave wave = (Shockwave)iter.next();
      
      wave.lifespan -= amount;
      if (wave.lifespan <= 0.0F)
      {
        iter.remove();
      }
      else
      {
        wave.alpha = (wave.lifespan / wave.maxLifespan);
        wave.scale = (wave.minScale + (wave.maxLifespan - wave.lifespan) / wave.maxLifespan * (wave.maxScale - wave.minScale));
      }
    }
    List<tagPing> pings = localData.pings;
    
    Iterator<tagPing> iterB = pings.iterator();
    while (iterB.hasNext())
    {
      tagPing ping = (tagPing)iterB.next();
      
      ping.lifespan -= amount;
      if (ping.lifespan <= 0.0F)
      {
        iterB.remove();
      }
      else
      {
        ping.alpha = (ping.lifespan / ping.maxLifespan);
        ping.scale = (ping.minScale + (ping.maxLifespan - ping.lifespan) / ping.maxLifespan * (ping.maxScale - ping.minScale));
      }
    }
 
    
 
  }
  
  @Override
  public void init(CombatEngineAPI engine)
  {
    this.engine = engine;
    engine.getCustomData().put("SAD_effectsHook", new LocalData());
  }
  

  @Override
  public void renderInWorldCoords(ViewportAPI viewport)
  {
    if (engine == null)
    {
      return;
    }
    


    LocalData localData = (LocalData)engine.getCustomData().get("SAD_effectsHook");
    List<Shockwave> shockwaves = localData.shockwaves;
    List<tagPing> pings = localData.pings;

    for (Shockwave wave : shockwaves)
    {
      SpriteAPI waveSprite = Global.getSettings().getSprite("concussion", "SAD_FlakWave");
      if (waveSprite != null)
      {
        waveSprite.setAlphaMult(wave.alpha);
        waveSprite.setAdditiveBlend();
        waveSprite.setAngle(wave.facing);
        waveSprite.setSize(wave.scale * 256.0F, wave.scale * 256.0F);
        waveSprite.renderAtCenter(wave.location.x, wave.location.y);
      }
    }
    
    for (tagPing ping : pings) {
      SpriteAPI waveSprite = Global.getSettings().getSprite("ping", "SAD_tagPing");
      if (waveSprite != null)
      {
        waveSprite.setAlphaMult(ping.alpha);
        waveSprite.setAdditiveBlend();
        waveSprite.setAngle(ping.facing);
        waveSprite.setSize(ping.scale * 256.0F, ping.scale * 256.0F);
        waveSprite.renderAtCenter(ping.location.x, ping.location.y);
      }
    }
   
  }
  
  private static final class LocalData
  {
    final List<SAD_effectsHook.Shockwave> shockwaves = new LinkedList();
  
    final List<SAD_effectsHook.tagPing> pings = new LinkedList();
    
    private LocalData() {}
  }
  
  static class Shockwave {
    float alpha;
    final float facing;
    float lifespan;
    final Vector2f location;
    float maxLifespan;
    float maxScale;
    float minScale;
    float scale;
    
    Shockwave(Vector2f location, float duration, float maxScale, float minScale) {
      this.location = new Vector2f(location);
      alpha = 1.0F;
      facing = ((float)Math.random() * 360.0F);
      maxLifespan = duration;
      lifespan = maxLifespan;
      this.maxScale = maxScale;
      this.minScale = minScale;
      scale = minScale;
    }
  }
  
 
  
  static class tagPing
  {
    float alpha;
    final float facing;
    float lifespan;
    final Vector2f location;
    final Vector2f velocity;
    float maxLifespan;
    float maxScale;
    float minScale;
    float scale;
    
    tagPing(Vector2f location, Vector2f velocity, float duration, float maxScale, float minScale)
    {
      this.location = new Vector2f(location);
      this.velocity = new Vector2f(velocity);
      alpha = 1.0F;
      facing = ((float)Math.random() * 360.0F);
      maxLifespan = duration;
      lifespan = maxLifespan;
      this.maxScale = maxScale;
      this.minScale = minScale;
      scale = minScale;
    }
  }
  
  
}
