package src.data.scripts.world;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModSpecAPI;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.campaign.GenericPluginManagerAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.thoughtworks.xstream.XStream;
import java.util.Arrays;
import java.util.List;
import src.data.utils.XStreamConfig;
//import data.scripts.hullmods.TEM_LatticeShield;
import src.data.scripts.ai.SAD_SimpleMissileAI;
import src.data.scripts.campaign.SAD_respawnManager;
//import src.data.scripts.campaign.raid.SAD_raidManager;

public class SAD_ModPlugin extends BaseModPlugin {

    public static final String SIMPLE_SHRIKE = "SAD_shrike";
    public static boolean HASAJUSTEDSECTOR;
    public static final List<String> modAuthorBlacklist = Arrays.asList(new String[] // These all have good reasons
    {
        "xenoargh" // Makes many pseudo total conversions, but are not tagged as such
    });
        public static final List<String> modBlacklist = Arrays.asList(new String[] // These all have good reasons
    {
        "xxx_ss_FX_example_project",
           "xxx_ss_FX_mod_core",
                   "@_ss_rebal_@",
                           "explorer_society",
                                   "ezfaction",
                                           "xxx_Starsector_AI_Overhaul",
    });
    
    @Override
    public void onApplicationLoad() {
        for (ModSpecAPI mod : Global.getSettings().getModManager().getEnabledModsCopy())
        {
            if (modAuthorBlacklist.contains(mod.getAuthor()))
            {
                throw new RuntimeException("" + mod.getName() + " is not compatible with Snrasha mods! (See Snrasha on the Discord)");
            }
           
            if(modBlacklist.contains(mod.getId())){
                throw new RuntimeException("" + mod.getName() + " is not compatible with Snrasha mods! (See Snrasha on the Discord)");
            }
        }
        
        boolean hasLazyLib = Global.getSettings().getModManager().isModEnabled("lw_lazylib");
        if (!hasLazyLib) {
            throw new RuntimeException("Require LazyLib!"
                    + "\nGet it at http://fractalsoftworks.com/forum/index.php?topic=5444");
        }
        boolean hasMagicLib = Global.getSettings().getModManager().isModEnabled("MagicLib");
        if (!hasMagicLib) {
            throw new RuntimeException("Require MagicLib!"
                    + "\nGet it at http://fractalsoftworks.com/forum/index.php?topic=13718");
        }
        boolean hasShaderLib = Global.getSettings().getModManager().isModEnabled("shaderLib");
        if (!hasShaderLib) {
            throw new RuntimeException("Require GraphicsLib!"
                    + "\nGet it at http://fractalsoftworks.com/forum/index.php?topic=10982");
        }
        HASAJUSTEDSECTOR = Global.getSettings().getModManager().isModEnabled("Adjusted Sector");

        
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
