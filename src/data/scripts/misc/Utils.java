package data.scripts.misc;

import java.util.Random;
import org.lazywizard.lazylib.FastTrig;
import org.lwjgl.util.vector.Vector2f;

public class Utils
{
  public Utils() {}
  
  private static final Random rand = new Random();
  
  public static Vector2f translate_polar(Vector2f center, float radius, float angle) {
    float radians = (float)Math.toRadians(angle);
    return new Vector2f(
      (float)FastTrig.cos(radians) * radius + (center == null ? 0.0F : x), 
      (float)FastTrig.sin(radians) * radius + (center == null ? 0.0F : y));
  }
  
  public static float get_angle(Vector2f vector)
  {
    return (float)Math.toDegrees(Math.atan2(y, x));
  }
  
  public static float get_angle(Vector2f from, Vector2f to) {
    return get_angle(new Vector2f(x - x, y - y));
  }
  

  public static float get_random(float low, float high)
  {
    return rand.nextFloat() * (high - low) + low;
  }
  
  public static boolean can_be_loaded(String fullyQualifiedClassName) {
    try {
      com.fs.starfarer.api.Global.getSettings().getScriptClassLoader().loadClass(fullyQualifiedClassName);
      return true;
    } catch (ClassNotFoundException ex) {}
    return false;
  }
}
