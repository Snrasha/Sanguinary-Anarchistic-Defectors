package src.data.scripts.campaign.raid.sub;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import src.data.scripts.campaign.raid.sanguinary_autonomist_defectors_raidIntel;
import src.data.scripts.campaign.raid.sanguinary_autonomist_defectors_raidIntel.RaidStageStatus;

public class sanguinary_autonomist_defectors_OrganizeStage extends sanguinary_autonomist_defectors_BaseRaidStage {
	

    protected CampaignFleetAPI from;
    protected IntervalUtil interval = new IntervalUtil(0.1f, 0.2f);

    public sanguinary_autonomist_defectors_OrganizeStage(sanguinary_autonomist_defectors_raidIntel raid, CampaignFleetAPI from, float durDays) {
        //super(raid, market, durDays);
        super(raid);
        this.from = from;
        this.maxDays = durDays;
    }


    protected String getForcesString() {
        return "The crazy raiding forces";
    }

    protected String getRaidString() {
        return "crazy raid";
    }
    @Override
    public void advance(float amount) {
        super.advance(amount);
    }

    @Override
    protected void updateStatus() {
        if (maxDays <= elapsed) {
            status = RaidStageStatus.SUCCESS;
        }
    }

    public void abort() {
        status = RaidStageStatus.FAILURE;
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
        String strDays = sanguinary_autonomist_defectors_raidIntel.getDaysString(days);

        String timing = getForcesString() + " should begin assembling in %s " + strDays + ".";
        if (days < 2) {
            timing = getForcesString() + " should begin assembling shortly.";
        }

        String raid = getRaidString();
        if (status == RaidStageStatus.FAILURE) {
            info.addPara("The " + raid + " has been disrupted in the planning stages and will not happen.", opad);
        } else if (curr == index) {
            info.addPara("The " + raid + " is currently being planned "
                    + "on " +from.getContainingLocation().getNameWithLowercaseType() + ". " + timing,
                    opad, h, "" + days);

        }
    }
}






