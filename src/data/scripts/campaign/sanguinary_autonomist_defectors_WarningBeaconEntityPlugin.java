package src.data.scripts.campaign;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import src.data.scripts.campaign.sanguinary_autonomist_defectors_ThemeGenerator.sanguinary_autonomist_defectors_SystemType;

public class sanguinary_autonomist_defectors_WarningBeaconEntityPlugin extends BaseCustomEntityPlugin {

	public static String GLOW_COLOR_KEY = "$sanguinary_autonomist_defectors_beaconGlowColor";
	public static String PING_COLOR_KEY = "$sanguinary_autonomist_defectors_beaconPingColor";
	
	public static float GLOW_FREQUENCY = 1f; // on/off cycles per second
	
	
	//private SectorEntityToken entity;
	
	transient private SpriteAPI sprite;
	transient private SpriteAPI glow;
	
        @Override
	public void init(SectorEntityToken entity, Object pluginParams) {
		super.init(entity, pluginParams);
		//this.entity = entity;
		entity.setDetectionRangeDetailsOverrideMult(0.75f);
		readResolve();
                
	}
	
	Object readResolve() {
		sprite = Global.getSettings().getSprite("campaignEntities", "sanguinary_autonomist_defectors_warning_beacon");
		glow = Global.getSettings().getSprite("campaignEntities", "sanguinary_autonomist_defectors_warning_beacon_glow");
		return this;
	}
	
	private float phase = 0f;
	private float freqMult = 1f;
	private float sincePing = 10f;
        @Override
	public void advance(float amount) {
		phase += amount * GLOW_FREQUENCY * freqMult;
		while (phase > 1) phase --;
		
		if (entity.isInCurrentLocation()) {
			sincePing += amount;
			if (sincePing >= 6f && phase > 0.1f && phase < 0.2f) {
				sincePing = 0f;
				CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
				if (playerFleet != null && 
					entity.getVisibilityLevelTo(playerFleet) == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS) {
					
					
					String pingId = "sanguinary_autonomist_defectors_warning_beacon2";
					freqMult = 1.25f;
					 if (entity.getMemoryWithoutUpdate().getBoolean(sanguinary_autonomist_defectors_SystemType.RESURGENT.getBeaconFlag())) {
						pingId = "sanguinary_autonomist_defectors_warning_beacon3";
						freqMult = 1.5f;
					}
					Color pingColor = null;
					if (entity.getMemoryWithoutUpdate().contains(PING_COLOR_KEY)) {
						pingColor = (Color) entity.getMemoryWithoutUpdate().get(PING_COLOR_KEY);
					}
					
					Global.getSector().addPing(entity, pingId, pingColor);
				}
			}
		}
	}

        @Override
	public float getRenderRange() {
		return entity.getRadius() + 100f;
	}

        @Override
	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		float alphaMult = viewport.getAlphaMult();
		if (alphaMult <= 0f) return;
		
		CustomEntitySpecAPI spec = entity.getCustomEntitySpec();
		if (spec == null) return;
		
		float w = spec.getSpriteWidth();
		float h = spec.getSpriteHeight();
		
		Vector2f loc = entity.getLocation();
		
		sprite.setAngle(entity.getFacing() - 90f);
		sprite.setSize(w, h);
		sprite.setAlphaMult(alphaMult);
		sprite.setNormalBlend();
		sprite.renderAtCenter(loc.x, loc.y);
		
		
		float glowAlpha = 0f;
		if (phase < 0.5f) glowAlpha = phase * 2f;
		if (phase >= 0.5f) glowAlpha = (1f - (phase - 0.5f) * 2f);
		
		float glowAngle1 = (((phase * 1.3f) % 1) - 0.5f) * 12f;
		float glowAngle2 = (((phase * 1.9f) % 1) - 0.5f) * 12f;
//		glowAngle1 = 0f;
//		glowAngle2 = 0f;
		
		boolean glowAsLayer = true;
		if (glowAsLayer) {
			//glow.setAngle(entity.getFacing() - 90f);
			Color glowColor = new Color(100,100,255,255);
			//Color glowColor = entity.getFaction().getBrightUIColor();
			if (entity.getMemoryWithoutUpdate().contains(GLOW_COLOR_KEY)) {
				glowColor = (Color) entity.getMemoryWithoutUpdate().get(GLOW_COLOR_KEY);
			}

			//glow.setColor(Color.white);
			glow.setColor(glowColor);
			
			glow.setSize(w, h);
			glow.setAlphaMult(alphaMult * glowAlpha);
			glow.setAdditiveBlend();
			
			glow.setAngle(entity.getFacing() - 90f + glowAngle1);
			glow.renderAtCenter(loc.x, loc.y);
			
			glow.setAngle(entity.getFacing() - 90f + glowAngle2);
			glow.setAlphaMult(alphaMult * glowAlpha * 0.5f);
			glow.renderAtCenter(loc.x, loc.y);
		} else {
			glow.setAngle(entity.getFacing() - 90f);
			glow.setColor(new Color(100,165,255));
			float gs = w * 3;
			glow.setSize(gs, gs);
			glow.setAdditiveBlend();
			
			float spacing = 10;
			glow.setAlphaMult(alphaMult * glowAlpha * 0.5f);
			glow.renderAtCenter(loc.x - spacing, loc.y);
			glow.renderAtCenter(loc.x + spacing, loc.y);
			
			glow.setAlphaMult(alphaMult * glowAlpha);
			glow.setSize(gs * 0.25f, gs * 0.25f);
			glow.renderAtCenter(loc.x - spacing, loc.y);
			glow.renderAtCenter(loc.x + spacing, loc.y);
		}
	}
	@Override
	public void createMapTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		String post = "";
		Color color = entity.getFaction().getBaseUIColor();
		Color postColor = color;
		 if (entity.getMemoryWithoutUpdate().getBoolean(sanguinary_autonomist_defectors_SystemType.SUPPRESSED.getBeaconFlag())) {
			post = " - Medium";
			postColor = Misc.getHighlightColor();
		} else if (entity.getMemoryWithoutUpdate().getBoolean(sanguinary_autonomist_defectors_SystemType.RESURGENT.getBeaconFlag())) {
			post = " - High";
			postColor = Misc.getNegativeHighlightColor();
		}
		
		tooltip.addPara(entity.getName() + post, 0f, color, postColor, post.replaceFirst(" - ", ""));
	}

	@Override
	public boolean hasCustomMapTooltip() {
		return true;
	}
	
	@Override
	public void appendToCampaignTooltip(TooltipMakerAPI tooltip, VisibilityLevel level) {
		if (level == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS || 
				level == VisibilityLevel.COMPOSITION_DETAILS) {
			
			String post = "";
			Color color = Misc.getTextColor();
			Color postColor = color;
			if (entity.getMemoryWithoutUpdate().getBoolean(sanguinary_autonomist_defectors_SystemType.SUPPRESSED.getBeaconFlag())) {
				post = "medium";
				postColor = Misc.getHighlightColor();
			} else if (entity.getMemoryWithoutUpdate().getBoolean(sanguinary_autonomist_defectors_SystemType.RESURGENT.getBeaconFlag())) {
				post = "high";
				postColor = Misc.getNegativeHighlightColor();
			}
			if (!post.isEmpty()) {
				tooltip.setParaFontDefault();
				tooltip.addPara(BaseIntelPlugin.BULLET + "Danger level: " + post, 10f, color, postColor, post);
			}
		}
		
	}

}









