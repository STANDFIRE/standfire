package capsis.lib.fire.exporter.wfds;

import java.awt.Color;
import java.text.NumberFormat;

import jeeb.lib.util.DefaultNumberFormat;
import capsis.lib.fire.fuelitem.FiParticle;

/**
 * A canopy particle in the WFDS export.
 * 
 * @author F. Pimont, - January 2014
 */
public class WFDSCanopyParticle {

	private boolean empty;
	private StringBuffer lines;
	private NumberFormat nf;

	/**
	 * Constructor When we are not using constantBulkDensityOption=true,
	 * bulkDensity=-1
	 */
	public WFDSCanopyParticle(FiParticle particle, String id, WFDSParam p, double bulkDensity, Color rgb) {
		//Color rgb = new Color(103, 186, 40);
		this.empty = true;
		lines = new StringBuffer();
		nf = DefaultNumberFormat.getInstance();
		lines.append("!====================================== /" + "\n");
		lines.append("!ELEMENT model for " + particle.getFullName() + "\n");
		if (particle != null) {
			empty = false;
			// String id = particle.getFullName ();
			String tree = ".TRUE.";
			String remove_charred = ".FALSE.";
			if (p.veg_remove_charred) {
				remove_charred = ".TRUE.";
			}
			String quantities = "'VEG_TEMPERATURE'";
			lines.append("!====================================== /" + "\n");
			lines.append("&PART ID='" + id + "',TREE=" + tree + ",QUANTITIES=" + quantities + "," + "\n");
			lines.append("VEG_INITIAL_TEMPERATURE=" + p.veg_initial_temperature + "," + "\n");
			lines.append("VEG_SV=" + particle.svr + ",VEG_MOISTURE=" + 0.01 * particle.moisture + ",VEG_CHAR_FRACTION="
					+ p.veg_char_fraction + "," + "\n");
			lines.append("VEG_DRAG_COEFFICIENT=" + p.veg_drag_coefficient + ",VEG_DENSITY=" + particle.mvr
					+ ",VEG_BULK_DENSITY=" + bulkDensity + "," + "\n");
			lines.append("VEG_BURNING_RATE_MAX=" + p.veg_burning_rate_max + ",VEG_DEHYDRATION_RATE_MAX="
					+ p.veg_dehydratation_rate_max + "," + "\n");
			lines.append("VEG_REMOVE_CHARRED=" + remove_charred + "," + "\n");
			lines.append("RGB=" + rgb.getRed() + "," + rgb.getGreen() + "," + rgb.getBlue() + "  /" + "\n");
		}
	}

	public boolean isEmpty() {
		return empty;
	}

	public String getLines() {
		return lines.toString();
	}

}
