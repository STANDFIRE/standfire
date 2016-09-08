package capsis.lib.fire.exporter.wfds;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.List;

import jeeb.lib.util.DefaultNumberFormat;
import capsis.lib.fire.fuelitem.FiParticle;

/**
 * A surface fuel in the WFDS export.
 * 
 * @author F. Pimont, F. de Coligny - September 2013
 */
public class WFDSSurfaceFuel {

	private PhysDataWFDS data;
	private boolean surfaceFuel; // tell if a fuel is surface fuel or not
	private boolean empty;
	private StringBuffer lines;
	private NumberFormat nf;
	public double exportedMass=0d;

	/**
	 * Constructor
	 * 
	 * @throws Exception
	 */
	public WFDSSurfaceFuel(PhysDataWFDS data, WFDSParam p) throws Exception {
		this.data = data;
		this.empty = true;
		lines = new StringBuffer();
		nf = DefaultNumberFormat.getInstance();
		FiParticle sfp = null;
		List<BulkDensityVoxel> bdvs = null;
		// System.out.println("WRITING SURFACE FUEL ");

		// here we temporary check that the "surfaceFuel" contains only one
		// bulkdensity
		if (data.bulkDensities.keySet().size() > 1) {
			throw new Exception("WFDSSurfaceFuel : thus PhysDataWFDS should contain one bulk density and has "
					+ data.bulkDensities.keySet().size());
		}
		if (data.bulkDensities.keySet().size() == 0) {
			System.out.println("WFDS.SurfaceFuel : no particles in SurfaceFuel");
		} else {

			for (FiParticle fp : data.bulkDensities.keySet()) {
				empty = false;
				bdvs = data.bulkDensities.get(fp);
				sfp = fp;
			}
			// check if distribution is homogeneous
			for (BulkDensityVoxel bdv : bdvs) {
				if (bdv.bulkDensity != bdvs.get(0).bulkDensity) {
					throw new Exception(
							"WFDSSurfaceFuel : thus PhysDataWFDS should contain one homogeneous bulk density distribution "
									+ bdvs.size());
					// System.out.println(bdv.bulkDensity+","+bdvs.get
					// (0).bulkDensity);
				}
			}

			double vegetationLoad = bdvs.get(0).bulkDensity * data.dz1;

			String vegetation_arrhenius_degrad = p.vegetation_arrhenius_degrad ? ".TRUE." : ".FALSE.";
			// first voxel of the list should have bd. dz1 is wfds grid dz in
			// cell 1
			double vegetationHeight = data.actualFuelDepth;
			lines.append("- BOUNDARY FUEL " + sfp.speciesName + "\n");
			lines.append("&SURF ID        = '" + sfp.speciesName + "'" + "\n");
			lines.append("VEGETATION = .TRUE." + "\n");
			lines.append("VEGETATION_CDRAG    = " + p.vegetation_cdrag + "\n");
			lines.append("VEGETATION_LOAD     = " + vegetationLoad + "\n");
			lines.append("VEGETATION_HEIGHT   = " + vegetationHeight + "\n");
			lines.append("VEGETATION_MOISTURE = " + 0.01 * sfp.moisture + "\n");
			lines.append("VEGETATION_SVRATIO  = " + sfp.svr + "\n");
			lines.append("VEGETATION_CHAR_FRACTION  = " + p.vegetation_char_fraction + "\n");
			lines.append("VEGETATION_ELEMENT_DENSITY= " + sfp.mvr + "\n");
			lines.append("EMISSIVITY = " + p.emissivity + "\n");
			lines.append("VEGETATION_ARRHENIUS_DEGRAD= " + vegetation_arrhenius_degrad + "\n");
			lines.append("FIRELINE_MLR_MAX = " + p.fireline_mlr_max + "\n");
			Color rgb = data.color; //new Color(191, 205, 53);
			lines.append("RGB        = " + rgb.getRed() + "," + rgb.getGreen() + "," + rgb.getBlue() + " /" + "\n");
			lines.append("!Surface fuel represented with BOUNDARY fuel model /" + "\n");
			lines.append("&VENT XB=" + data.p0.x + "," + data.p1.x + "," + data.p0.y + "," + data.p1.y + ","
					+ data.p0.z + "," + data.p0.z + ",SURF_ID='" + sfp.speciesName + "' /" + "\n");// plane
																									// in
																									// the
																									// z
																									// dimension
																									// corresponding
																									// z=zmin
																									// (bc)
			exportedMass = vegetationLoad * (data.p1.x - data.p0.x) * (data.p1.y - data.p0.y);
		}
	}

	// private void createOneBlock (PhysDataWFDS data, int i, int j) {

	// empty = false;

	/*
	 * !========================= / !SURFACE FUEL MODEL DEFINITIONS
	 * !========================= / - BOUNDARY FUEL Grass &SURF ID = 'Grass'
	 * VEGETATION = .TRUE. VEGETATION_CDRAG = 0.05 VEGETATION_LOAD = 1.7
	 * VEGETATION_HEIGHT = 0.6 VEGETATION_MOISTURE = 0.06 VEGETATION_SVRATIO =
	 * 9710 VEGETATION_CHAR_FRACTION = 0.2 VEGETATION_ELEMENT_DENSITY= 512
	 * EMISSIVITY = 0.99 VEGETATION_ARRHENIUS_DEGRAD=.FALSE. FIRELINE_MLR_MAX =
	 * 0.05 RGB = 191,205,53 / !Surface fuel represented with BOUNDARY fuel
	 * model / &VENT XB=0.0,70.0,0.0,50.0,0.0,0.0,,SURF_ID='Grass
	 */

	// }

	public boolean isEmpty() {
		return empty;
	}

	public String getLines() {
		return lines.toString();
	}

}
