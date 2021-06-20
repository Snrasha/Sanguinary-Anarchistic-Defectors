package src.data.scripts.world;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModSpecAPI;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.campaign.GenericPluginManagerAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.thoughtworks.xstream.XStream;
import java.util.Arrays;
import java.util.List;
import src.data.utils.XStreamConfig;
//import data.scripts.hullmods.TEM_LatticeShield;
import src.data.scripts.ai.sanguinary_autonomist_defectors_SimpleMissileAI;
import src.data.scripts.campaign.sanguinary_autonomist_defectors_respawnManager;
import src.data.scripts.campaign.intels.sanguinary_autonomist_defectors_DiscoverEntityListener;

public class sanguinary_autonomist_defectors_ModPlugin extends BaseModPlugin {

    public static final String SIMPLE_SHRIKE = "sanguinary_autonomist_defectors_shrike";
    public static boolean HASAJUSTEDSECTOR;

    @Override
    public void onApplicationLoad() {
        HASAJUSTEDSECTOR = Global.getSettings().getModManager().isModEnabled("Adjusted Sector");
    }

    private static void init() {
        new sanguinary_autonomist_defectors_Gen().generate(Global.getSector());
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
        ListenerManagerAPI listeners = Global.getSector().getListenerManager();

        if (!sector.hasScript(sanguinary_autonomist_defectors_respawnManager.class)) {
            sector.addScript(new sanguinary_autonomist_defectors_respawnManager());
        }
        if (!listeners.hasListener(sanguinary_autonomist_defectors_DiscoverEntityListener.class)) {
            listeners.addListener(new sanguinary_autonomist_defectors_DiscoverEntityListener());
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

    private static void initSanguinary() {
        new sanguinary_autonomist_defectors_Gen().sanguinary(Global.getSector());
    }

    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        switch (missile.getProjectileSpecId()) {
            case SIMPLE_SHRIKE:
                return new PluginPick(new sanguinary_autonomist_defectors_SimpleMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SET);
        }
        return null;
    }

}
