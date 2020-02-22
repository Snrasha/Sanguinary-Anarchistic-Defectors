package src.data.scripts.campaign.intels;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.listeners.DiscoverEntityListener;
import org.apache.log4j.Logger;
import src.data.utils.SAD_Tags;

public class SAD_DiscoverEntityListener implements DiscoverEntityListener {

    public static final Logger LOG = Global.getLogger(SAD_DiscoverEntityListener.class);
    
    @Override
    public void reportEntityDiscovered(SectorEntityToken entity) {

        if (entity.hasTag(SAD_Tags.SAD_WARNING_BEACON)) {
            
            //Weirdly, they spawn three times and like the beacon is a bit useless after.
            entity.removeTag(SAD_Tags.SAD_WARNING_BEACON);
            entity.addTag(SAD_Tags.SAD_WARNING_BEACON2);
           /* LOG.info("SAD_WARNING_BEACON");
            for (String tag : entity.getTags()) {
                LOG.info("--- " + tag);
                //  Global.getSector().getCampaignUI().addMessage("--- " + tag);
            }*/
            SAD_WarningBeaconIntel intel = new SAD_WarningBeaconIntel(entity);
            Global.getSector().getIntelManager().addIntel(intel);
        }
    }

}
