package capsis.lib.fire.fuelitem;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import capsis.lib.fire.fuelitem.FuelMatrix.FuelMatrixOptions;
import capsis.lib.fire.fuelitem.FuelMatrix.HorizontalDistribution;

/**
 * A layer is an element in a layer set (for example a kermes oak strata in a
 * garrigue layerSet)
 * 
 * @author F. de Coligny, F Pimont - march 2009
 */
public class FiLayer implements Serializable {

	// protected FiSpecies species;
	protected String layerType; // e.g. shrub, broadleaved, quercus...
	protected double height; // height of the fuel in m (trunk+crown)
	protected double baseHeight; // base height in m
	// protected double modelMeanBulkDensity; // mean bulk density of all
	// particules in the model
	protected Map<FiParticle, Double> bulkDensityMap; // <particule, bulkDensity
														// of the particle>
	// protected double lai; // leaf area index
	protected double characteristicSize; // clump size in the layer (when
											// coverFraction is not 1)
	protected double coverFraction; // between 0 and 1
	protected int spatialGroup;
	// the species that are in a same spatial group will be spatialized in
	// exclusion
	// exemple: quercus coccifera and rosmarinus are in a same spatial group:
	// they don't grow one
	// below the other
	// whereas brachypodium is a different one, cause it can be below the others
	public FiSpecies species;
	// if a FiSpecies with same speciesName as the the layerType exist.
	// Otherwhise, defaultSpecies

	public static String SHRUB = "Shrub";
	public static String HERB = "Herb";
	public static String LITTER = "Litter";
	public static String DUFF = "Duff";

	public FiLayer() {
	}

	/**
	 * Constructor for every properties, but the particles and bulkDensities
	 * (set with a function addFiLayerParticle...)
	 */

	public FiLayer(String layerType, double height, double baseHeight, double coverFraction, double characteristicSize,
			int spatialGroup, FiSpecies defaultSpecies) {
		this.layerType = layerType;
		this.height = height;
		this.baseHeight = baseHeight;
		this.characteristicSize = characteristicSize;
		if (characteristicSize <= 0d) {
			this.coverFraction = 1d;
		} else {
			this.coverFraction = coverFraction;
		}
		this.spatialGroup = spatialGroup;
		// this.particles = new HashSet<FiParticle> ();
		this.bulkDensityMap = new HashMap<FiParticle, Double>();
		if (defaultSpecies.containsSpecies(layerType)) {
			this.species = defaultSpecies.getSpecies(layerType);
		} else {
			this.species = defaultSpecies;
		}
	}

	/**
	 * add a particle to the FiLayer
	 * 
	 * @param bulkDensity
	 *            : kg/m3 of the particle
	 * @param particleName
	 * @param particle
	 * @throws Exception
	 */
	public void addFiLayerParticle(double bulkDensity, FiParticle particle) throws Exception {
		// TODO FP: check if the bulkDensity test is required
		if (this.bulkDensityMap.containsKey(particle)) {
			bulkDensityMap.put(particle, bulkDensity + this.bulkDensityMap.get(particle));
		} else {
			bulkDensityMap.put(particle, bulkDensity);
		}
	}

	/**
	 * This method add a particle to the FiLayer, from a load value instead of a
	 * bulkdensity (heigh, baseHeight, coverFraction must have been set first
	 * 
	 * @param load
	 * @param particleName
	 * @param particle
	 * @throws Exception
	 */
	public void addFiLayerParticleFromLoad(double load, FiParticle particle) throws Exception {
		double bulkDensity = 0d;
		if (height > baseHeight & coverFraction > 0d & coverFraction <= 1d) {
			bulkDensity = load / ((height - baseHeight) * coverFraction);
		}
		addFiLayerParticle(bulkDensity, particle);
	}

	/**
	 * get the set of FiParticles from the bulkDensity map
	 * 
	 * @return
	 */
	public Set<FiParticle> getParticles() {
		return bulkDensityMap.keySet();
	}

	// /**
	// *
	// */
	// public boolean isSurfaceFuel() {
	// if (this.getParticles ().size()==1) {
	// for (FiParticle fp:this.getParticles ()) {
	// return fp.isSurfaceFuel();
	// }
	// }
	// return false;
	// }
	//

	/**
	 * this method add a given fuelLayer to the FuelMatrix
	 * 
	 * @param fm
	 * @param fmo
	 * @param layerSet
	 */
	public void addToFuelMatrix(FuelMatrix fm, FuelMatrixOptions fmo, FiLayerSet layerSet) {
		for (FiParticle particle : getParticles(fmo.particleNames)) {
			fm.particles.add(particle);
			if (fmo.verbose) {
				System.out.println("	layer moisture  for particle " + particle.name + " is " + particle.moisture
						+ " and bd is " + getBulkDensity(particle));
			}
		}

		HorizontalDistribution hd = fm.horizontalDistribution;
		for (int i = 0; i < fm.nx; i++) {
			for (int j = 0; j < fm.ny; j++) {
				if (fm.isCellCenterInPoly(i, j, layerSet)) {
					// this edgecoeff splits biomasse in two when the egde of
					// the poly is exactly on the center of the
					double edgeCoef = 1d;
					if (fm.isPointOnEdge(i, j, layerSet))
						edgeCoef = 0.5;
					// coordinate of the voxel on the horizontal Distribution
					// (whole polygon)
					int ihd = (int) Math.round((fm.getX(i) - hd.x0) / fm.dx);
					int jhd = (int) Math.round((fm.getY(j) - hd.y0) / fm.dy);
					// System.out.println ("ihd,jhd=" + ihd + "," + jhd);

					// TODO FP : fm.effectiveHeight = 0d;
					double horizDistrib = hd.get(this, ihd, jhd) * edgeCoef;
					if (horizDistrib > 0) {
						for (int k = 0; k < fm.nz; k++) {
							double cz = fm.getZ(k + 0.5, i, j);
							// distance between the top of the layer and current
							// voxel center
							double distanceToTop = getHeight() - cz;
							// distance between the current voxel center andthe
							// bottom of the layer
							double distanceToBase = cz - getBaseHeight();
							double hdz = 0.5 * fm.dz;
							// if this condition is satisfied the voxel should
							// be filled with "contribution"
							if (distanceToBase >= -hdz && distanceToTop >= -hdz) {
								double contribution = 1.0;
								if (distanceToTop <= hdz) {
									// varies between 0 and 1 (cause
									// distanceToTop varies between -hdz and
									// hdz)
									contribution *= 0.5 + distanceToTop / fm.dz;
								}
								if (distanceToBase <= hdz) {
									contribution *= 0.5 + distanceToBase / fm.dz;
								}
								FiMassVoxel massVox = new FiMassVoxel(i, j, k);
								for (FiParticle particle : getParticles(fmo.particleNames)) {
									massVox.masses.put(particle, getBulkDensity(particle) * horizDistrib * contribution
											* fm.getVoxelVolume());
								}
								fm.massVoxels.add(massVox);
							} // endif
						} // for k
					} // if (horizDistrib > 0) {
				} // end if (fm.cell
			} // for j
		} // for i
	}
	
	/**
	 * True is contains particleName, false elsewhere
	 * 
	 * @param particleName
	 * @return
	 */
	public boolean containsParticle(String particleName) {
		for (FiParticle p : getParticles()) {
			if (p.name.equals(particleName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return the particle from its name : 1 from the particle list. 2 from the
	 * species.particles list. 3 from the defaultSpecies.particles list
	 * 
	 * @param particleName
	 * @return
	 */
	public FiParticle getParticle(String particleName) {
		for (FiParticle p : getParticles()) {
			if (p.name.equals(particleName)) {
				return p;
			}
		}
		return null;
		// return species.getParticle (particleName);
	}

	/**
	 * return the set of particle from their names
	 * 
	 * @param particleNames
	 * @return
	 */
	public Set<FiParticle> getParticles(Set<String> particleNames) {
		Set<FiParticle> particles = new HashSet<FiParticle>();
		for (String particleName : particleNames) {
			for (FiParticle p : getParticles()) {
				if (p.name.equals(particleName)) {
					particles.add(p);
				}
			}
		}
		return particles;
	}

	/**
	 * MVR is got from particule
	 * 
	 * @param ptName
	 * @return
	 */
	public double getMVR(String ptName) {
		return getParticle(ptName).mvr;
	}

	/**
	 * SVR is got from particule
	 * 
	 * @param ptName
	 * @return
	 */
	public double getSVR(String ptName) {
		return getParticle(ptName).svr;
	}

	/**
	 * Moisture is got from particule
	 * 
	 * @param ptName
	 * @return
	 */
	public double getMoisture(String ptName) {
		return getParticle(ptName).moisture;
	}

	/**
	 * Return the bulk density from its particle name
	 * 
	 * @param particle
	 * @return
	 */
	public double getBulkDensity(FiParticle particle) {
		if (bulkDensityMap.containsKey(particle)) {
			return bulkDensityMap.get(particle);
		}
		return 0d;
	}

	/**
	 * get the summed bulkDensity of a collection of particles. NB if the
	 * collection if FiParticle.ALL, all the particles would be counted.
	 * 
	 * @return
	 */
	public double getSumBulkDensity(Set<String> particleNames) {
		double bd = 0d;
		// Set<FiParticle> particles = getParticles(particleNames);
		// System.out.println(this.layerType+"	getSumBulkDensity:"+particleNames);
		// System.out.println("	getParticle(particleNames):"+particles);
		// System.out.println("	getParticles():"+this.getParticles ());
		for (FiParticle particle : getParticles(particleNames)) {
			bd += this.getBulkDensity(particle);
		}
		// System.out.println("	bulkdensity:"+bd);
		return bd;
	}

	/**
	 * get the map of bulk density (the key is the FiParticle
	 * 
	 * @return
	 */
	public Map<FiParticle, Double> getBulkDensityMap() {
		return this.bulkDensityMap;
	}

	/**
	 * Returns a copy of the FiLayer.
	 * 
	 * @throws Exception
	 *             LAI update
	 */
	public FiLayer copy() throws Exception {
		FiLayer cp = new FiLayer(layerType, height, baseHeight, coverFraction, characteristicSize, spatialGroup,
				species.getDefaultSpecies());
		for (FiParticle fp : getParticles()) {
			FiParticle newfp = fp.copy();
			cp.bulkDensityMap.put(newfp, getBulkDensity(fp));
		}
		return cp;
	}

	public double getSLA() throws Exception {
		// TODO FP: LAI formulation for FiLayer to be checked...
		double mvrl = getMVR(FiParticle.makeKey(FiParticle.LEAVE, FiParticle.LIVE));
		double svrl = getSVR(FiParticle.makeKey(FiParticle.LEAVE, FiParticle.LIVE));
		double moisturel = getMoisture(FiParticle.makeKey(FiParticle.LEAVE, FiParticle.LIVE));
		double correctionFactor = Math.sqrt(mvrl * (1.0 + 0.01 * moisturel) * 0.001);
		return svrl / mvrl * correctionFactor;
	}

	public double getHeight() {
		return height;
	}

	public double getBaseHeight() {
		return baseHeight;
	}

	public void setBaseHeight(double v) {
		baseHeight = v;
	}

	public void setHeight(double v) {
		height = v;
	}

	public double getCharacteristicSize() {
		return characteristicSize;
	}

	public double getCoverFraction() {
		return coverFraction;
	}

	public int getSpatialGroup() {
		return spatialGroup;
	}

	public void setCharacteristicSize(double v) {
		characteristicSize = v;
		if (characteristicSize <= 0d) {
			coverFraction = 1d;
		}
	}

	public void setCoverFraction(double v) {
		coverFraction = v;
	}

	public void setSpatialGroup(int spatialGroup) {
		this.spatialGroup = spatialGroup;
	}

	public double getLoad(Set<String> particuleNames) {
		return this.getSumBulkDensity(particuleNames) * coverFraction * (height - baseHeight);
	}

	public double getWaterLoad(Set<String> particuleNames) {
		double wbd = 0d;
		for (FiParticle particle : getParticles(particuleNames)) {
			wbd += this.getBulkDensity(particle) * particle.moisture * 0.01;
		}
		return wbd * coverFraction * (height - baseHeight);
	}

	public double getLoadLoadBelow(double threshold, Set<String> particuleNames) {
		if (getBaseHeight() <= threshold) {
			return this.getSumBulkDensity(particuleNames) * coverFraction * (Math.min(height, threshold) - baseHeight);
		}
		return 0d;
	}

	public String getLayerType() {
		return layerType;
	}

	public void setLayerType(String v) {
		layerType = v;
	}

	public double getLai() throws Exception {
		double sla = getSLA();
		double liveBulkDensity0 = getBulkDensity(getParticle(FiParticle.makeKey(FiParticle.LEAVE, FiParticle.LIVE)));
		return 0.5 * liveBulkDensity0 * sla * (this.height - this.baseHeight) * this.coverFraction;
	}

	@Override
	public String toString() {
		return "FiLayer";
	}
}
