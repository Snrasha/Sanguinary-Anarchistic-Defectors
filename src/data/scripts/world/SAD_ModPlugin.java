package src.data.scripts.world;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.thoughtworks.xstream.XStream;
import src.data.utils.XStreamConfig;
import data.scripts.hullmods.TEM_LatticeShield;
import src.data.scripts.ai.SAD_SimpleMissileAI;

public class SAD_ModPlugin extends BaseModPlugin {

    
  public static final String SIMPLE_SHRIKE = "SAD_shrike";
  public static boolean templarsExist = false;
  
  
  @Override
  public void onApplicationLoad() {
    templarsExist = Global.getSettings().getModManager().isModEnabled("Templars");

    try {
      if (TEM_LatticeShield.AEGIS_SHIELD_COLOR != null) {
        templarsExist = true;
      }
    }
    catch (NoClassDefFoundError localNoClassDefFoundError1) {}
  }
  
  
    private static void init() {
       new SAD_Gen().generate(Global.getSector());


    }
  
    @Override
    public void configureXStream(XStream x) {
        XStreamConfig.configureXStream(x);
    }
 

    @Override
    public void onNewGame() {
       init();
        
      
    }
   public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip)
  {
    switch (missile.getProjectileSpecId()) {
    case SIMPLE_SHRIKE: 
      return new PluginPick(new SAD_SimpleMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SET);
    }
    return null;
  }
  

}
