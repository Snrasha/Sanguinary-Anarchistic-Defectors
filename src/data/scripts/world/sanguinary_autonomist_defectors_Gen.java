package src.data.scripts.world;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SectorThemeGenerator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import src.data.scripts.campaign.sanguinary_autonomist_defectors_ThemeGenerator;
import src.data.utils.sanguinary_autonomist_defectors_Tags;

public class sanguinary_autonomist_defectors_Gen implements SectorGeneratorPlugin {

    @Override
    public void generate(SectorAPI sector) {
        initFactionRelationships(sector);
    }

    public void sanguinary(SectorAPI sector) {
        SectorThemeGenerator.generators.add(1, new sanguinary_autonomist_defectors_ThemeGenerator());
        /*Class c;
        try {
            c = Class.forName("src.data.scripts.campaign.sanguinary_autonomist_defectors_ThemeGenerator");

            BaseThemeGenerator o = (BaseThemeGenerator) (c.newInstance());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(sanguinary_autonomist_defectors_Gen.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(sanguinary_autonomist_defectors_Gen.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(sanguinary_autonomist_defectors_Gen.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }

    private static void initFactionRelationships(SectorAPI sector) {

        FactionAPI sad = sector.getFaction(sanguinary_autonomist_defectors_Tags.SAD_FACTION);

        List<FactionAPI> factionList = sector.getAllFactions();
        factionList.remove(sad);

        for (FactionAPI faction : factionList) {
            sad.setRelationship(faction.getId(), RepLevel.HOSTILE);
        }
        sad.setRelationship(Factions.INDEPENDENT, RepLevel.NEUTRAL);
        sad.setRelationship(Factions.REMNANTS, RepLevel.NEUTRAL);

        sad.setRelationship(Factions.PLAYER, RepLevel.HOSTILE);

    }
}
