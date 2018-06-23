package src.data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class SAD_BurnDriveStats extends BaseShipSystemScript {

        @Override
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		if (state == ShipSystemStatsScript.State.OUT) {
                    	stats.getMaxTurnRate().unmodify(id);
                       // stats.getAcceleration().unmodify(id);
			stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
		} else {
			stats.getMaxSpeed().modifyFlat(id, 300f * effectLevel);
			stats.getAcceleration().modifyFlat(id, 300f * effectLevel);
			//stats.getAcceleration().modifyPercent(id, 200f * effectLevel);
                        stats.getMaxTurnRate().modifyMult(id, 0.2f * effectLevel);
		}
                
	}
        @Override
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);
	}
	
        @Override
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("increased engine power", false);
		}
		return null;
	}
}

