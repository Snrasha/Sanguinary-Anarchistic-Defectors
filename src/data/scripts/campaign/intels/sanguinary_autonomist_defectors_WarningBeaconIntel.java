package src.data.scripts.campaign.intels;

import java.awt.Color;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.misc.WarningBeaconIntel;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import src.data.utils.sanguinary_autonomist_defectors_Tags;

public class sanguinary_autonomist_defectors_WarningBeaconIntel extends WarningBeaconIntel {

    public sanguinary_autonomist_defectors_WarningBeaconIntel(SectorEntityToken beacon) {
        super(beacon);

    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 3f;
        float opad = 10f;
        Description desc = Global.getSettings().getDescription("warning_beacon", Type.CUSTOM);
        info.addPara(desc.getText1FirstPara(), opad);

        addBulletPoints(info, ListInfoMode.IN_DESC);

        if (beacon.isInHyperspace()) {
            StarSystemAPI system = Misc.getNearbyStarSystem(beacon, 1f);
            if (system != null) {
                info.addPara("This beacon is located near the " + system.getNameWithLowercaseType()
                        + ", warning of some fanatics that presumably lie within.", opad);

            }
        }
    }

    @Override
    public String getIcon() {
        if (isMedium()) {
            return Global.getSettings().getSpriteName("intel", "sanguinary_autonomist_defectors_beacon_medium");
        } else if (isHigh()) {
            return Global.getSettings().getSpriteName("intel", "sanguinary_autonomist_defectors_beacon_high");
        }
        return Global.getSettings().getSpriteName("intel", "sanguinary_autonomist_defectors_beacon_high");
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_BEACON);
        tags.add(sanguinary_autonomist_defectors_Tags.SAD_FACTION);
        return tags;
    }
}
