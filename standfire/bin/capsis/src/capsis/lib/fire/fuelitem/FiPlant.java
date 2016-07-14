package capsis.lib.fire.fuelitem;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jeeb.lib.defaulttype.SimpleCrownDescription;
import jeeb.lib.defaulttype.Tree;
import jeeb.lib.defaulttype.TreeWithCrownProfile;
import jeeb.lib.util.Vertex3d;
import capsis.defaulttype.SpatializedTree;
import capsis.defaulttype.Speciable;
import capsis.lib.fire.FiModel;
import capsis.lib.fire.FiStand;
import capsis.lib.fire.exporter.PhysExporter;
import capsis.lib.fire.fuelitem.FuelMatrix.FuelMatrixOptions;
import capsis.lib.fire.fuelitem.function.MassProfileFunction;
import capsis.lib.spatial.CrownAvoidancePattern;

/**
 * FiPlant is a tree in the fire lib.
 * 
 * @author F. Pimont, F. de coligny - September 2013
 */
public class FiPlant extends SpatializedTree implements TreeWithCrownProfile, SimpleCrownDescription, Speciable,
		FuelItem {

	// fc - september 2009 - review
	// WARNING: if references to objects (not primitive types) are added here,
	// implement a "public Object clone ()" method (see RectangularPlot.clone ()
	// for template)

	public FiModel model; // for access to particleNames fc-2.2.2015
	
	protected Color crownColor;
	protected FiSpecies species; // species name
	protected double crownBaseHeight; // crown base height in m
	protected double crownBaseHeightBeforePruning; // used for biomass
													// computation and
													// crownProfile
	// assessment similar to crownBaseHeight if no "prune" intervention is done
	protected double crownDiameter; // crown diameter in m
	protected double crownPerpendicularDiameter;// crown perpendicular diameter
												// in m
	// TODO FP maxDiameterHeight (could probably be removed)
	protected double maxDiameterHeight;// max diameter height in m
	protected double totalThinMass; // total thin biomass in kg (INCLUDING
									// PRUNING : MODIFIED AFTER PRUNING)
	public Map<String, Double> biomass; // biomass particle map BEFORE PRUNING
										// (NOT MODIFIED BY THE PRUNING)
	protected double[][] crownGeometry;
	// crownGeometry : List of height / radius
	// (in percentage of the crown height / the crown radius)
	// - first entry should be (0, 0) : bottom of the crown
	// - possibly several intermediate entries to describe the profile between
	// crownBaseHeight and treeHeight
	// - last entry should be (100, 0) : top of the crown

	// Fire variables
	protected FiFireParameters fire;
	// Damage variables
	protected FiSeverity severity;

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param stand
	 * @param age
	 *            in year (not required when no evolution...)
	 * @param x
	 *            (m) trunk
	 * @param y
	 *            (m)
	 * @param z
	 *            (m)
	 * @param dbh
	 *            (cm)
	 * @param height
	 *            (m)
	 * @param crownBaseHeight
	 *            (m)
	 * @param crownDiameter
	 *            (m)
	 * @param species
	 * @throws Exception
	 */
	public FiPlant(int id, FiStand stand, FiModel model, int age, double x, double y, double z, double dbh, double height,
			double crownBaseHeight, double crownDiameter, FiSpecies species) throws Exception {

		super(id, stand, age, height, dbh, false, x, y, z);
		
		this.model = model; // fc-2.2.2015
		
		this.species = species;
		this.crownBaseHeight = crownBaseHeight;
		this.crownBaseHeightBeforePruning = crownBaseHeight;
		this.crownDiameter = crownDiameter;
		this.crownPerpendicularDiameter = crownDiameter;
		this.biomass = new HashMap<String, Double>();
		this.crownGeometry = species.getCrownGeometry();
		this.setCrownDiameterHeightFromCrownGeometry();
		this.severity = new FiSeverity();

		this.crownColor = species.getColor();
	}

	/**
	 * this method add the FiPlant to the phyData, based on the grid, a list of
	 * particles and other option
	 * 
	 * @param physData
	 * @param grid
	 * @param stand
	 * @param resolution
	 * @param particles
	 * @throws Exception
	 */
	public double addFuelTo(PhysExporter exporter) throws Exception {
		String sourceName = "" + getClass().getSimpleName() + "_" + getId(); // unique
																				// key;
		return exporter.physData.addFuelMatrix(buildFuelMatrix(sourceName, exporter.fmo), exporter.grids, exporter.pdo);
	}
	
	public double getFuelMass(PhysExporter exporter) throws Exception {
		return this.computeThinMass(exporter.fmo.particleNames);
	}
	

	/**
	 * Builds a FuelMatrix, based on a Set of particles to get. The voxel size
	 * is a fraction of tree extension (see fmo.fiPlantDiscretization)
	 */
	public FuelMatrix buildFuelMatrix(String sourceName, FuelMatrixOptions fmo) throws Exception {
		// bounding box
		double x0 = getX() - 0.5 * crownDiameter;
		double x1 = getX() + 0.5 * crownDiameter;
		double y0 = getY() - 0.5 * crownPerpendicularDiameter;
		double y1 = getY() + 0.5 * crownPerpendicularDiameter;
		double z0 = getZ();
		double z1 = getZ() + height;

		double dx = fmo.fiPlantDiscretization * (x1 - x0);
		double dy = fmo.fiPlantDiscretization * (y1 - y0);
		double dz = fmo.fiPlantDiscretization * (z1 - z0);
		
		FuelMatrix fm = new FuelMatrix(sourceName, dx, dy, dz, x0, y0, z0, x1, y1, z1, fmo, crownColor,this.model.rnd);
		fm.plant = this;

		// definition of fm.particles and concernedParticles (list of particle
		// names common to species and particleNames)
		List<String> concernedParticles = new ArrayList<String>();
		// below, we build fm.particles, which is an arrayList of particles
		for (String pn : fmo.particleNames) {
			if (biomass.containsKey(pn)) {
				concernedParticles.add(pn);
				fm.particles.add(this.getParticle(pn)); // maybe default...
			}
		}
		// System.out.println (particleNames);
		// System.out.println (species.particles.keySet ());
		// System.out.println (concernedParticles);

		// definition of masses
		for (int i = 0; i < fm.nx; i++) {
			for (int j = 0; j < fm.ny; j++) {
				for (int k = 0; k < fm.nz; k++) {
					// coordinates in the crown (centered on trunc axis, for
					// crownX and crownY, and
					// crownZ started at 0 at the bottom of the crown
					double crownX = (i + 0.5) * fm.dx - 0.5 * crownDiameter;
					double crownY = (j + 0.5) * fm.dy - 0.5 * crownDiameter;
					double crownZ = (k + 0.5) * fm.dz;
					List<Double> bms = biomassInVoxel(crownX, crownY, crownZ, fm.getVoxelVolume(), concernedParticles);
					double voxBm = 0d;
					for (double bm : bms) {
						voxBm += bm;
					}
					if (voxBm > 0d) {
						FiMassVoxel massVox = new FiMassVoxel(i, j, k);
						for (int ipart = 0; ipart < bms.size(); ipart++) {
							FiParticle fp = fm.particles.get(ipart);
							double bm = bms.get(ipart);
							massVox.masses.put(fp, bm);
						}
						fm.massVoxels.add(massVox);
					}
				}
			}
		}
		if (fmo.verbose) {
			this.printSyntheticData(fmo.particleNames);
			fm.printData();
		}
		return fm;
	}

	public void printSyntheticData(Set <String> pName) {
		System.out.println("FiPlant in buildFuelMatrix, name " + getName() + "," + getSpeciesName());
		System.out.println("	biomass total in Plant=" + this.totalThinMass + " kg");
		try {
			System.out.println("	biomass of particle exported=" + this.computeThinMass(pName) + " kg");
			System.out.println("	position=" + this.getX() + " m," + this.getY() + " m");
			System.out.println("	h,cbh,cd,cd(0.2*h)=" + this.getHeight() + " m, "+ this.getCrownBaseHeight() + " m, "+ this.getCrownDiameter() + " m, "+ 2*this.getCrownRadiusAt(0.2*  this.getHeight(),true) + " m");
		} catch (Exception e) {
			// TODO FP Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This method computes the biomass of particle "particle" in a
	 * "small voxel" of volume voxelVolume centered at the position x, y, z
	 * 
	 * @throws Exception
	 */
	public List<Double> biomassInVoxel(double x, double y, double z, double voxelVolume, List<String> particleNames)
			throws Exception {
		List<Double> bm = new ArrayList<Double>();
		double distanceToTrunc = Math.sqrt(x * x + y * y);
		Map<String, Double> bd = bulkDensityAt(distanceToTrunc, z, particleNames);
		for (String particle : bd.keySet()) {
			bm.add(bd.get(particle) * voxelVolume);
		}
		return bm;
	}

	/**
	 * This method is used to compute the buldDensity (kg/m3) as a function of
	 * distance to trunk and height in tree. It used the horizontal and vertical
	 * biomass distributions.
	 * 
	 * @param distanceToTrunc
	 *            (m)
	 * @param height
	 *            in tree
	 * @return local bulk density (kg/m3)
	 * @throws Exception
	 * 
	 */
	private Map<String, Double> bulkDensityAt(double distanceToTrunc, double z, List<String> particleNames)
			throws Exception {

		Map<String, Double> bd = new HashMap<String, Double>();
		double radAtz = getCrownRadiusAt(z, true);// enable fuel below cbh
													// (when no pruning)
		if (z >= 0 && z <= height && distanceToTrunc < radAtz) { // in crown
			double relR = radAtz / this.getCrownRadius();
			double relHFromGround = z / height;
			for (String particleName : particleNames) {
				MassProfileFunction horizProfile = getHorizontalProfile(particleName);
				double horizDistrib = horizProfile.f(relR, this) / (Math.PI * radAtz * radAtz);
				MassProfileFunction vertiProfile = getVerticalProfile(particleName);
				double vertiDistrib = vertiProfile.f(relHFromGround, this) / getHeight();
				//vertiDistrib = 1d/getHeight();
				// System.out.println (" bulkdensityAt:" + particleName +
				// " list:" + biomass.keySet
				// ());
				bd.put(particleName, Math.max(0d, vertiDistrib * horizDistrib * biomass.get(particleName)));
			}
		} else { // no biomass
			for (String particleName : particleNames) {
				bd.put(particleName, 0d);
			}
		}
		return bd;
	}

	public double getBiomass(String particleName) {
		return biomass.get(particleName);
	}

	/**
	 * Below this line : geometric methods
	 */

	/**
	 * Compute the radius of the crown at a given height, based on a
	 * crownGeometry.
	 * 
	 * It entails some fuel below ground and rBelowMaxHeight (assumes a
	 * cylindric shape for biomass below rBelowMaxHeight)
	 * 
	 * @param z
	 * @return
	 * @throws Exception
	 */

	public double getCrownRadiusAt(double z, boolean enableFuelBelowCBH) throws Exception {
		if (z <= 0 || z >= height)
			return 0d;
		// pruning:
		if (z < crownBaseHeight && crownBaseHeight > crownBaseHeightBeforePruning)
			return 0d;

		if (enableFuelBelowCBH) { // entails some fuel below cbh
			// computation of relBelowMaxHeight;=> reference below maxHeight
			int nDiamMaxR = 1;
			double maxR = 0d;
			for (int ndiam = 1; ndiam < crownGeometry.length; ndiam++) {
				if (crownGeometry[ndiam][1] >= maxR) {
					maxR = crownGeometry[ndiam][1];
					nDiamMaxR = ndiam;
				}
			}
			double hBelowMaxHeight = 0.01 * crownGeometry[Math.max(nDiamMaxR - 1,0)][0] * (height-crownBaseHeight)+crownBaseHeight;
			double rBelowMaxHeight = 0.01 * crownGeometry[Math.max(nDiamMaxR - 1,0)][1] * getCrownRadius();
			// below hBelowMaxHeight:
			//System.out.println("getCrownRadiusAtz:"+hBelowMaxHeight+",z="+z+"nDiamMaxR"+nDiamMaxR);
			if (z <= hBelowMaxHeight) {
				return rBelowMaxHeight;
			} else {
				return getCrownRadiusAt(z);
			}
		} else { // !enableFuelBelowCBH
			return getCrownRadiusAt(z);
		}
	}

	/**
	 * Compute the radius of the crown at a given height. it take into account
	 * the pruning (radius is 0 for z<cbh) and is based on crownGeometry.
	 * 
	 * @param z
	 *            in m
	 * @return
	 * @throws Exception
	 */

	public double getCrownRadiusAt(double z) throws Exception {
		if (z < crownBaseHeight || z >= height)
			return 0d;
		// other cases (in crown)
		double relHeightPerc = 100d * (z - this.crownBaseHeightBeforePruning)
				/ (height - this.crownBaseHeightBeforePruning);
		// look for ndiam when relHeight is between ndiam and ndiam+1
		int ndiam = 0;
		while (crownGeometry[ndiam + 1][0] < relHeightPerc) {
			ndiam += 1;
		}
		return getCrownRadius()
				* 0.01
				* (crownGeometry[ndiam + 1][1] * (relHeightPerc - crownGeometry[ndiam][0]) + crownGeometry[ndiam][1]
						* (crownGeometry[ndiam + 1][0] - relHeightPerc))
				/ (crownGeometry[ndiam + 1][0] - crownGeometry[ndiam][0]);
	}

	/**
	 * this method compute the elliptic projected area of the crown
	 * 
	 * @param crownDiameter
	 *            (m)
	 * @param crownPerpendicularDiameter
	 *            (m)
	 * @author pimont
	 */
	public static double computeCrownProjectedArea(double crownDiameter, double crownPerpendicularDiameter) { // FP
		// 23/09/2009
		return Math.PI * crownDiameter * crownPerpendicularDiameter / 4.0;
	}

	public void setBiomass(String particleName, double value) {
		biomass.remove(particleName);
		biomass.put(particleName, value);
	}

	// the following methods are based on FiSpecies and the hypothesis that all
	// properties are
	// similar for a given species : They can possibly be overwriten in some of
	// the subclass of
	// FiPlant if it is not the case (see for example FmPlant)

	public FiParticle getParticle(String particleName) {
		return species.getParticle(particleName);
	}

	public Set<FiParticle> getParticles() {
		return species.getParticles();
	}

	public double getSLA() {
		return species.getSla();
	}

	public double getMVR(String particleName) {
		return getParticle(particleName).mvr;
	}

	public double getSVR(String particleName) {
		return getParticle(particleName).svr;
	}

	public double getMoisture(String particleName) {
		return getParticle(particleName).moisture;
	}

	public MassProfileFunction getHorizontalProfile(String particleName) {
		return species.getHorizontalProfile(particleName);
	}

	public MassProfileFunction getVerticalProfile(String particleName) {
		return species.getVerticalProfile(particleName);
	}

	// public void setTotalThinMass (double v) {
	// this.totalThinMass = v;
	// } // kg
	/**
	 * NB totalThinMass includes prunning
	 */

	public double getTotalThinMass() {
		return totalThinMass;
	} // kg

	public void updateTotalThinMass() throws Exception {
		totalThinMass = this.computeThinMass(model.particleNames);
	}
	
	public double computeThinMass(Set<String> particles) throws Exception {
		double pruneLenght = getCrownBaseHeight() - getCrownBaseHeightBeforePruning();
		if (pruneLenght <= 0d) {// nopruning
			double bm = 0d;
			for (String particle : particles) {
				if (biomass.containsKey(particle)) {
					bm += biomass.get(particle);
				}
			}
			return bm;
		} else {// pruning
			double thinBiomassAfterPruning = 0;
			for (String particleName : particles) {
				if (biomass.containsKey(particleName)) {
					double biomassBelowPruneHeightFraction = this.getSpecies().getCumulativeVerticalFraction(getCrownBaseHeight()/getHeight(), particleName, this);
					if (biomassBelowPruneHeightFraction > 1.1d || biomassBelowPruneHeightFraction < -0.1d) {
						throw new Exception ("FiPlant.computeThinBiomass: error in computation in case of pruning: biomassBelowPruneHeightFraction="+biomassBelowPruneHeightFraction);
					}
					thinBiomassAfterPruning += biomass.get(particleName) * Math.min(Math.max(0d, 1d - biomassBelowPruneHeightFraction),1d);
				}
			}
			return thinBiomassAfterPruning;
			
			
//			double crownLength = getCrownLengthBeforePruning();
//			
//			double dHrel = 0.1 * pruneLenght / getCrownLengthBeforePruning();
//			// hrel inside crown:
//			double Hrel = 0.5 * dHrel; 
//						 
//			for (String particleName : particles) {
//				if (biomass.containsKey(particleName)) {
//					double biomassBelowPruneHeightFraction = 0d;
//					for (int nstep = 1; nstep <= 10; nstep++) {
//						double HrelFromGround = (Hrel * crownLength + getCrownBaseHeightBeforePruning()) / getHeight();
//
//						biomassBelowPruneHeightFraction += dHrel
//								* getSpecies().getVerticalProfile(particleName).f(HrelFromGround, this);
//						Hrel += dHrel;
//					}
//					if (biomassBelowPruneHeightFraction > 1d) {
//						throw new Exception ("FiPlant.computeThinBiomass: error in computation in case of pruning...");
//					}
//					thinBiomassAfterPruning += biomass.get(particleName) * (1d - biomassBelowPruneHeightFraction);
//				}
//			}
			
		}
	}

	// fc-2.2.2015
//	// A method similar that apply the previous one to the set of particles of
//	// the model
//	public void updateTotalThinMass() {
//		this.updateTotalThinMass(FiModel.particleNames);
//	}

	public double getCrownBaseHeight() {
		return crownBaseHeight;
	} // m

	public double getCrownBaseHeightBeforePruning() {
		return crownBaseHeightBeforePruning;
	} // m

	public double getCrownRadius() {
		return crownDiameter * 0.5;
	} // m

	public double getCrownLength() {
		return height - crownBaseHeight;
	} // m

	public double getCrownLengthBeforePruning() {
		return height - crownBaseHeightBeforePruning;
	} // m

	public Color getCrownColor() {
		return crownColor;
	}

	public int getCrownType() {
		return Tree.SPHERIC_CROWN;
	} // CONIC_CROWN or SPHERIC_CROWN

	public double[][] getCrownGeometry() {
		return crownGeometry;
	} // array of (height%, radius%)

	public void setCrownGeometry(double[][] value) {
		crownGeometry = value;
	}

	// Bounding box, relative to (x,y,z)
	public Vertex3d getRelativeMin() { // m
		return new Vertex3d(-getCrownRadius(), -getCrownRadius(), 0);
	}

	public Vertex3d getRelativeMax() { // m
		return new Vertex3d(getCrownRadius(), getCrownRadius(), getHeight());
	}

	@Override
	public void setX(double x) {
		getImmutable().x = (float) x;
	}

	@Override
	public void setY(double y) {
		getImmutable().y = (float) y;
	}

	@Override
	public void setZ(double z) {
		getImmutable().z = (float) z;
	}

	public String getName() {
		return getSpeciesName() + " " + getId();
	}

	/**
	 * Speciable interface.
	 */
	public FiSpecies getSpecies() {
		return species;
	}

	public String getSpeciesName() {
		return species.getName();
	}

	public double getCrownDiameter() {
		return crownDiameter;
	}

	public double getCrownPerpendicularDiameter() {
		return crownPerpendicularDiameter;
	}

	public double getMaxDiameterHeight() {
		return maxDiameterHeight;
	}

	public double getLai() {
		String liveLeave = FiParticle.makeKey(FiParticle.LEAVE, FiParticle.LIVE);
		if (biomass.containsKey(liveLeave)) {
			return biomass.get(liveLeave) * species.getSla() / computeCrownProjectedArea(crownDiameter, crownPerpendicularDiameter);
		} else {
			return 0d;
		}
	}

	public double computeCylindricCrownVolume() {
		return computeCylindricCrownVolume(height, crownBaseHeight, crownDiameter, crownPerpendicularDiameter);
	}

	/**
	 * this method compute the volume of crown, assuming a cylindric shape of
	 * diameter "crown diameter" and or height ="height-cbh" as a function of
	 * height, cbh and diameter
	 * 
	 * @param height
	 *            (m)
	 * @param crownBaseHeight
	 *            (m)
	 * @param crownDiameter
	 *            (m)
	 * @param crownPerpendicularDiameter
	 *            (m)
	 * @author pimont
	 */
	public static double computeCylindricCrownVolume(double height, double crownBaseHeight, double crownDiameter,
			double crownPerpendicularDiameter) { // FP 23/09/2009
		double surface = FiPlant.computeCrownProjectedArea(crownDiameter, crownPerpendicularDiameter);
		double crownheight = height - crownBaseHeight;
		return surface * crownheight;
	}

	public double computeLoad() {
		return this.totalThinMass / computeCrownProjectedArea(crownDiameter, crownPerpendicularDiameter);
	}

	// Fire variables
	public FiFireParameters getFire() {
		return fire;
	}

	public void setFire(FiFireParameters v) {
		this.fire = v;
	}

	// Damage variables
	public FiSeverity getSeverity() {
		return severity;
	}

	public void setSeverity(FiSeverity v) {
		this.severity = v;
	}

	public void setCrownBaseHeight(double v) {
		this.crownBaseHeight = v;
	}

	public void setCrownBaseHeightBeforePruning(double v) {
		this.crownBaseHeightBeforePruning = v;
	}

	public void setCrownRadius(double v) {
		this.crownDiameter = v * 2d;
		this.crownPerpendicularDiameter = v * 2d;
	}

	public void setId(int v) {
		getImmutable().id = v;
	}

	// public void setMeanBulkDensity0_2mm (double v) {meanThinBulkDensity = v;}

	// public void setLai (double v) {
	// lai = v;
	// }

	/**
	 * Define coordinates
	 */
	@Override
	public void setXYZ(double x, double y, double z) {
		setX(x);
		setY(y);
		setZ(z);
	}

	/**
	 * Method that computes crown volume intersection with FiPlant t2 using
	 * CrownGeometry
	 * 
	 * @throws Exception
	 */

	public double computeCrownIntersectionWith(FiPlant t2) throws Exception {
		double[][] crownGeomDim = new double[crownGeometry.length][2];
		for (int i = 0; i < crownGeometry.length; i++) {
			crownGeomDim[i][0] = crownGeometry[i][1] * getCrownRadius() * 0.01;
			crownGeomDim[i][1] = crownGeometry[i][0] * (height - crownBaseHeight) * 0.01 + crownBaseHeight;
			System.out.println("tree1 : i=" + i + " r=" + crownGeomDim[i][0] + " z=" + crownGeomDim[i][1]);
		}
		double[][] crownGeom2 = t2.getCrownGeometry();
		double[][] crownGeomDim2 = new double[crownGeom2.length][2];
		for (int i = 0; i < crownGeom2.length; i++) {
			crownGeomDim2[i][0] = crownGeom2[i][1] * t2.getCrownRadius() * 0.01;
			crownGeomDim2[i][1] = crownGeom2[i][0] * (t2.getHeight() - t2.getCrownBaseHeight()) * 0.01
					+ t2.getCrownBaseHeight();
			System.out.println("tree2 : i=" + i + " r=" + crownGeomDim2[i][0] + " z=" + crownGeomDim2[i][1]);
		}
		double d = Math.sqrt((getX() - getX()) * (getX() - t2.getX()) + (getY() - t2.getY()) * (getY() - t2.getY()));
		return CrownAvoidancePattern.computeCrownIntersection(d, crownGeomDim, crownGeomDim2);
	}

	/**
	 * Method that computes the distance to FiPlant t2 using CrownGeometry
	 * 
	 * @throws Exception
	 */
	public double computeCrownDistanceWith(FiPlant t2, double variability) throws Exception {
		double[][] crownGeomDim1 = new double[crownGeometry.length][2];
		// System.out.println("crownGeometry.length:"+crownGeometry.length);
		for (int i = 0; i < crownGeometry.length; i++) {
			crownGeomDim1[i][0] = crownGeometry[i][1] * getCrownRadius() * 0.01 * (1d - 0.01 * variability);
			crownGeomDim1[i][1] = crownGeometry[i][0] * (height - crownBaseHeight) * 0.01 + crownBaseHeight;
			// System.out.println("tree1:i="+i+"  r="+radius1[i][0]+"  z="+radius1[i][1]);
		}
		double[][] crownGeom2 = t2.getCrownGeometry();
		double[][] crownGeomDim2 = new double[crownGeom2.length][2];
		for (int i = 0; i < crownGeom2.length; i++) {
			crownGeomDim2[i][0] = crownGeom2[i][1] * t2.getCrownRadius() * 0.01;
			crownGeomDim2[i][1] = crownGeom2[i][0] * (t2.getHeight() - t2.getCrownBaseHeight()) * 0.01
					+ t2.getCrownBaseHeight();
		}
		double d = Math.sqrt((getX() - t2.getX()) * (getX() - t2.getX()) + (getY() - t2.getY()) * (getY() - t2.getY()));
		double dist = CrownAvoidancePattern.computeCrownDistance(d, crownGeomDim1, crownGeomDim2);
		// System.out.println("d=" + d + ";dist=" + dist);
		return dist;

	}

	/**
	 * This method can force "CrownDiameterHeight" to be in agreement with a
	 * predefined CrownGeometry
	 */
	public void setCrownDiameterHeightFromCrownGeometry() {
		double h = this.height;
		double cbh = this.crownBaseHeight;
		double maxDPercent = 0d;// max
		double maxDPercent2 = 0d;// snd max

		double hPercentAtmaxDPercent = 0d; // max
		double hPercentAtmaxDPercent2 = 0d; // snd mas

		for (int ndiam = 0; ndiam < crownGeometry.length; ndiam++) {
			if (crownGeometry[ndiam][1] >= maxDPercent) {
				maxDPercent2 = maxDPercent;
				hPercentAtmaxDPercent2 = hPercentAtmaxDPercent;
				maxDPercent = crownGeometry[ndiam][1];
				hPercentAtmaxDPercent = crownGeometry[ndiam][0];
			} else if (crownGeometry[ndiam][1] >= maxDPercent2) {
				maxDPercent2 = crownGeometry[ndiam][1];
				hPercentAtmaxDPercent2 = crownGeometry[ndiam][0];
			}
		}
		double hPercent = 0d;
		if (maxDPercent + maxDPercent2 == 200d) {
			hPercent = 0.5 * (hPercentAtmaxDPercent2 + hPercentAtmaxDPercent);
		} else {
			hPercent = (hPercentAtmaxDPercent2 * (100d - maxDPercent) + hPercentAtmaxDPercent * (100d - maxDPercent2))
					/ (200d - maxDPercent - maxDPercent2);
		}
		this.maxDiameterHeight = cbh + (h - cbh) * hPercent * 0.01;
	}

	public boolean isInRectangle(double xMin, double xMax, double yMin, double yMax) {
		// System.out.println(" "+xMin+" "+xMax+" "+yMin+" "+yMax+" "+
		// getX()+" "+ getY());
		if (getX() + 0.5 * getCrownDiameter() < xMin)
			return false;
		if (getX() - 0.5 * getCrownDiameter() > xMax)
			return false;
		if (getY() + 0.5 * getCrownDiameter() < yMin)
			return false;
		if (getY() - 0.5 * getCrownDiameter() > yMax)
			return false;
		return true;
	}

	/**
	 * getCrownProfile is a getter for crownGeometry, which is a list of height
	 * / radius (in percentage of the crown height / the crown radius) - first
	 * entry should be (0, 0) : bottom of the crown - possibly several
	 * intermediate entries to describe the profile between crownBaseHeight and
	 * treeHeight - last entry should be (100, 0) : top of the crown
	 */

	public double[][] getCrownProfile() {
		return getCrownGeometry();
	} // array of (height%, radius%)

	public void setCrownProfile(double[][] value) {
		crownGeometry = value;
	}
}
