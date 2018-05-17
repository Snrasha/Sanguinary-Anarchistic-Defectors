package src.data.scripts.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import static com.fs.starfarer.api.impl.campaign.procgen.SectorProcGen.CELL_SIZE;
import static com.fs.starfarer.api.impl.campaign.procgen.SectorProcGen.CONSTELLATION_CELLS;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.ThemeGenContext;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.util.vector.Vector2f;
import src.data.scripts.campaign.SAD_ThemeGenerator;

public class SAD_Gen implements SectorGeneratorPlugin {

    @Override
    public void generate(SectorAPI sector) {
        initFactionRelationships(sector);
        ThemeGenContext context = new ThemeGenContext();
        SAD_ThemeGenerator gen = new SAD_ThemeGenerator();
        float w = Global.getSettings().getFloat("sectorWidth");
        float h = Global.getSettings().getFloat("sectorHeight");

        int cellsWide = (int) (w / CELL_SIZE);
        int cellsHigh = (int) (h / CELL_SIZE);

        boolean[][] cells = new boolean[cellsWide][cellsHigh];
        int count = 10;

        int vPad = CONSTELLATION_CELLS / 2;
        int hPad = CONSTELLATION_CELLS / 2;

        hPad = (int) (31000 / CELL_SIZE);
        vPad = (int) (19000 / CELL_SIZE);

        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                if (i <= hPad || j <= vPad || i >= cellsWide - hPad || j >= cellsHigh - vPad) {
                    cells[i][j] = true;
                }
            }
        }

        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            int[] index = getIndex(system.getLocation());
            int x = index[0];
            int y = index[1];
            if (x < 0) {
                x = 0;
            }
            if (y < 0) {
                y = 0;
            }
            if (x > cellsWide - 1) {
                x = cellsWide - 1;
            }
            if (y > cellsHigh - 1) {
                y = cellsHigh - 1;
            }
            blotOut(cells, x, y, 8);
        }

        blotOut(cells, 0, 0, 12);
        blotOut(cells, 6, 0, 12);
        blotOut(cells, 12, 0, 12);


        List<Constellation> constellations = new ArrayList<>();
        for (int k = 0; k < count; k++) {
            WeightedRandomPicker<Pair<Integer, Integer>> picker = new WeightedRandomPicker<>(StarSystemGenerator.random);
            for (int i = 0; i < cells.length; i++) {
                for (int j = 0; j < cells[0].length; j++) {
                    if (cells[i][j]) {
                        continue;
                    }

                    Pair<Integer, Integer> p = new Pair<>(i, j);
                    picker.add(p);
                }
            }

            Pair<Integer, Integer> pick = picker.pick();
            if (pick == null) {
                continue;
            }

            blotOut(cells, pick.one, pick.two, CONSTELLATION_CELLS);

            float x = pick.one * CELL_SIZE - w / 2f;
            float y = pick.two * CELL_SIZE - h / 2f;

            StarSystemGenerator.CustomConstellationParams params = new StarSystemGenerator.CustomConstellationParams(StarAge.ANY);

            StarAge age = StarAge.ANY;
            if (age == StarAge.ANY) {
                WeightedRandomPicker<StarAge> agePicker = new WeightedRandomPicker<>(StarSystemGenerator.random);
                agePicker.add(StarAge.YOUNG);
                agePicker.add(StarAge.AVERAGE);
                agePicker.add(StarAge.OLD);
                age = agePicker.pick();
            }

            params.age = age;

            params.location = new Vector2f(x, y);
            Constellation c = new StarSystemGenerator(params).generate();
            constellations.add(c);

        }

        context.constellations = constellations;
        gen.generateForSector(context, 3);

    }

    public static void blotOut(boolean[][] cells, int x, int y, int c) {
        //int c = CONSTELLATION_CELLS;
        for (int i = Math.max(0, x - c / 2); i <= x + c / 2 && i < cells.length; i++) {
            for (int j = Math.max(0, y - c / 2); j <= y + c / 2 && j < cells[0].length; j++) {
                cells[i][j] = true;
            }
        }
    }

    public static int[] getIndex(Vector2f loc) {
        float w = Global.getSettings().getFloat("sectorWidth");
        float h = Global.getSettings().getFloat("sectorHeight");

        int x = (int) ((loc.x + w / 2f) / CELL_SIZE);
        int y = (int) ((loc.y + h / 2f) / CELL_SIZE);

        return new int[]{x, y};
    }

    private static void initFactionRelationships(SectorAPI sector) {

        FactionAPI sad = sector.getFaction("sad");

        List<FactionAPI> factionList = sector.getAllFactions();
        factionList.remove(sad);

        for (FactionAPI faction : factionList) {
            sad.setRelationship(faction.getId(), RepLevel.NEUTRAL);
        }
        sad.setRelationship(Factions.PLAYER,RepLevel.HOSTILE);

    }
}
