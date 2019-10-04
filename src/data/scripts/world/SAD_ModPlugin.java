package src.data.scripts.world;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.campaign.GenericPluginManagerAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.thoughtworks.xstream.XStream;
import src.data.utils.XStreamConfig;
//import data.scripts.hullmods.TEM_LatticeShield;
import src.data.scripts.ai.SAD_SimpleMissileAI;
import src.data.scripts.campaign.SAD_respawnManager;
//import src.data.scripts.campaign.raid.SAD_raidManager;

public class SAD_ModPlugin extends BaseModPlugin {

    public static final String SIMPLE_SHRIKE = "SAD_shrike";
    
    @Override
    public void onApplicationLoad() {
        boolean hasLazyLib = Global.getSettings().getModManager().isModEnabled("lw_lazylib");
        if (!hasLazyLib) {
            throw new RuntimeException("High tech armada requires LazyLib!"
                    + "\nGet it at http://fractalsoftworks.com/forum/index.php?topic=5444");
        }
        boolean hasMagicLib = Global.getSettings().getModManager().isModEnabled("MagicLib");
        if (!hasMagicLib) {
            throw new RuntimeException("High tech armada requires MagicLib!"
                    + "\nGet it at http://fractalsoftworks.com/forum/index.php?topic=13718");
        }
        boolean hasShaderLib = Global.getSettings().getModManager().isModEnabled("shaderLib");
        if (!hasShaderLib) {
            throw new RuntimeException("High tech armada requires GraphicsLib!"
                    + "\nGet it at http://fractalsoftworks.com/forum/index.php?topic=10982");
        }
    }

    private static void init() {
        new SAD_Gen().generate(Global.getSector());

    }

    @Override
    public void configureXStream(XStream x) {
        XStreamConfig.configureXStream(x);
    }

    @Override
    public void onGameLoad(boolean newGame) {
        addScriptsIfNeeded();
    }

    protected void addScriptsIfNeeded() {

        SectorAPI sector = Global.getSector();
        GenericPluginManagerAPI plugins = sector.getGenericPlugins();
        /*if (!sector.hasScript(SAD_raidManager.class)) {
            sector.addScript(new SAD_raidManager());
        }*/
        if (!sector.hasScript(SAD_respawnManager.class)) {
            sector.addScript(new SAD_respawnManager());
        }
        
    }

    @Override
    public void onNewGameAfterEconomyLoad() {
       addScriptsIfNeeded();

    }

    @Override
    public void onNewGame() {
        init();
        initSanguinary();
    }
    private static void initSanguinary()
    {
        new SAD_Gen().sanguinary(Global.getSector());
    }
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        switch (missile.getProjectileSpecId()) {
            case SIMPLE_SHRIKE:
                return new PluginPick(new SAD_SimpleMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SET);
        }
        return null;
    }

}
