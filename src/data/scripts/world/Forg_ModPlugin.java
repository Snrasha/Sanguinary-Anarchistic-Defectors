package src.data.scripts.world;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import exerelin.campaign.SectorManager;  
import com.thoughtworks.xstream.XStream;
import src.data.scripts.campaign.Forg_ThemeGenerator;
import src.data.utils.XStreamConfig;

public class Forg_ModPlugin extends BaseModPlugin {

    

    private static void initforgotten() {
      boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");  
      if (!haveNexerelin || SectorManager.getCorvusMode())  
        new Forg_Gen().generate(Global.getSector());


    }
  
    @Override
    public void configureXStream(XStream x) {
        XStreamConfig.configureXStream(x);
    }
 

    @Override
    public void onNewGame() {
        initforgotten();
        
       Forg_ThemeGenerator generator = new Forg_ThemeGenerator();
      
    }
   
}
