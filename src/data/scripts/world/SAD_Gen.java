package src.data.scripts.world;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SectorThemeGenerator;
import java.util.List;
import src.data.scripts.campaign.SAD_ThemeGenerator;

public class SAD_Gen implements SectorGeneratorPlugin {

    @Override
    public void generate(SectorAPI sector) {
      initFactionRelationships(sector);
    }
    
        public void sanguinary(SectorAPI sector)
    {
          SectorThemeGenerator.generators.add(1, new SAD_ThemeGenerator());
    }


    private static void initFactionRelationships(SectorAPI sector) {

        FactionAPI sad = sector.getFaction("sad");

        List<FactionAPI> factionList = sector.getAllFactions();
        factionList.remove(sad);

        for (FactionAPI faction : factionList) {
            sad.setRelationship(faction.getId(), RepLevel.HOSTILE);
        }
        sad.setRelationship(Factions.INDEPENDENT, RepLevel.NEUTRAL);
        sad.setRelationship(Factions.PLAYER,RepLevel.HOSTILE);

    }
}
