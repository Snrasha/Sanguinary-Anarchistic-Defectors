package src.data.scripts.campaign.intels;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.listeners.DiscoverEntityListener;
import org.apache.log4j.Logger;
import src.data.utils.sanguinary_autonomist_defectors_Tags;

public class sanguinary_autonomist_defectors_DiscoverEntityListener implements DiscoverEntityListener {

    public static final Logger LOG = Global.getLogger(sanguinary_autonomist_defectors_DiscoverEntityListener.class);
    
    @Override
    public void reportEntityDiscovered(SectorEntityToken entity) {

        if (entity.hasTag(sanguinary_autonomist_defectors_Tags.sanguinary_autonomist_defectors_WARNING_BEACON)) {
            
            //Weirdly, they spawn three times and like the beacon is a bit useless after.
            entity.removeTag(sanguinary_autonomist_defectors_Tags.sanguinary_autonomist_defectors_WARNING_BEACON);
            entity.addTag(sanguinary_autonomist_defectors_Tags.sanguinary_autonomist_defectors_WARNING_BEACON2);
           /* LOG.info("sanguinary_autonomist_defectors_WARNING_BEACON");
            for (String tag : entity.getTags()) {
                LOG.info("--- " + tag);
                //  Global.getSector().getCampaignUI().addMessage("--- " + tag);
            }*/
            sanguinary_autonomist_defectors_WarningBeaconIntel intel = new sanguinary_autonomist_defectors_WarningBeaconIntel(entity);
            Global.getSector().getIntelManager().addIntel(intel);
        }
    }

}
