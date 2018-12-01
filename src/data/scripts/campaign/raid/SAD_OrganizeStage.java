package src.data.scripts.campaign.raid;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.intel.raid.BaseRaidStage;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;

public class SAD_OrganizeStage extends BaseRaidStage {

    protected CampaignFleetAPI from;
    protected IntervalUtil interval = new IntervalUtil(0.1f, 0.2f);

    public SAD_OrganizeStage(RaidIntel raid, CampaignFleetAPI from, float durDays) {
        //super(raid, market, durDays);
        super(raid);
        this.from = from;
        this.maxDays = durDays;
    }


    protected String getForcesString() {
        return "The crazy raiding forces";
    }

    protected String getRaidString() {
        return "Crazy raid";
    }
    @Override
    public void advance(float amount) {
        super.advance(amount);
    }

    @Override
    protected void updateStatus() {
        if (maxDays <= elapsed) {
            status = RaidIntel.RaidStageStatus.SUCCESS;
        }
    }

    public void abort() {
        status = RaidIntel.RaidStageStatus.FAILURE;
    }

    public CampaignFleetAPI getFrom() {
        return from;
    }

    @Override
    public void showStageInfo(TooltipMakerAPI info) {
        int curr = intel.getCurrentStage();
        int index = intel.getStageIndex(this);

        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 3f;
        float opad = 10f;

        int days = Math.round(maxDays - elapsed);
        String strDays = RaidIntel.getDaysString(days);

        String timing = getForcesString() + " should begin assembling in %s " + strDays + ".";
        if (days < 2) {
            timing = getForcesString() + " should begin assembling shortly.";
        }

        String raid = getRaidString();
        if (status == RaidIntel.RaidStageStatus.FAILURE) {
            info.addPara("The " + raid + " has been disrupted in the planning stages and will not happen.", opad);
        } else if (curr == index) {
            info.addPara("The " + raid + " is currently being planned "
                    + "on " +from.getContainingLocation().getNameWithLowercaseType() + ". " + timing,
                    opad, h, "" + days);

        }
    }

}
