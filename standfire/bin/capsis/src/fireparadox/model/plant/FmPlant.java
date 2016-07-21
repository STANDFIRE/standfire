package fireparadox.model.plant;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import jeeb.lib.util.RGB;
import capsis.lib.fire.fuelitem.FiParticle;
import capsis.lib.fire.fuelitem.FiPlant;
import capsis.lib.fire.fuelitem.FiSpecies;
import fireparadox.model.FmModel;
import fireparadox.model.FmStand;

/**
 * FmPlant is a tree in the fireparadox module.
 * 
 * @author F. Pimont, F. de Coligny - Septembre 2013
 */
public class FmPlant extends FiPlant implements RGB {

	// in fireparadox, every plant can have its own particles
	protected Set<FiParticle> particles; // mass to volume ratio, surface to
											// volume ratio, moisture content
	// protected boolean particleIsSet = false;
	// protected Map<String,Double> moistures;

	// protected String fileId; // TODO check if used
	// protected double LAI; // LAI is not computed as in FiPlant
	protected int populationGroup; // PhD 2008-09-18
	// Beetle status
	// 0: no attack, green, 1: attacked, yellow 50% moisture, 2: red, all of the
	// fuel 15% moisture 3:
	// orange, half of the fuel 15% moisture 4: brown,
	// dead, no fuel
	protected byte beetleStatus;
	protected String patternName; // name of the current associated pattern
	// These properties may become immutable

	// protected FiSpecies species;
	protected int[] rgb; // an optional color
	public static final boolean TWIGSb = false;
	public static final boolean LEAVESb = true;
	public static final boolean DEADb = false;
	public static final boolean ALIVEb = true;
	public static final Integer LEAVES_AND_TWIGS = 2;
	public static final Integer TWIGS = 0;
	public static final Integer LEAVES = 1;
	public static final Integer ALIVE_AND_DEAD = 2;
	public static final Integer DEAD = 0;
	public static final Integer ALIVE = 1;

	
	
	/**
	 * Constructor.
	 */
	public FmPlant(int id, FmStand stand, FmModel model, // fc-2.2.2015
			int age, double x, // trunk base
			double y, // trunk base
			double z, // trunk base
			String fileId, // fc + ov - 6.6.2007
			double dbh, double height, double crownBaseHeight, double crownDiameter, FiSpecies species, int pop, // PhD
																													// 2008-09-18
			double liveMoisture, double deadMoisture, double liveTwigMoisture, boolean database) throws Exception {

		super(id, stand, model, age, x, y, z, dbh, height, crownBaseHeight, crownDiameter, species);

		Set<String> particleNames = model.particleNames; 
		// definition of particle names when a specific moisture is given for
		// individual trees (otherwhise species is used as in FiPlant)
		if (!(liveMoisture < 0 & deadMoisture < 0 & liveTwigMoisture < 0)) {
			this.particles = new HashSet<FiParticle>();
			for (String ptName : particleNames) {
				FiParticle fp = this.species.getParticle(ptName).copy();
				if (ptName.endsWith(FiParticle.DEAD)) {
					fp.moisture = deadMoisture;
				} else if (ptName.startsWith(FiParticle.LEAVE)) {
					fp.moisture = liveMoisture;
				} else {
					fp.moisture = liveTwigMoisture;
				}
				this.particles.add(fp);
			}
		}
		// definition of biomass
		if (!database) {
			FmLocalPlantBiomass.setBiomassWithoutPruning(this, particleNames);
		}
		this.updateTotalThinMass(); 

		this.populationGroup = pop;

		// double crownProjectedArea = computeCrownProjectedArea (crownDiameter,
		// crownDiameter);
		// this.LAI = leafArea / crownProjectedArea;
	}

	public boolean isParticleSet() {
		return !(particles == null);
	}

	/**
	 * get particle is overwritten when the plant has a particle
	 */
	public FiParticle getParticle(String particleName) {
		if (isParticleSet()) {
			for (FiParticle particle : particles) {
				if (particle.name.equals(particleName)) {
					return particle;
				}
			}
		}
		return super.getParticle(particleName);
	}

	/**
	 * get particles is overwritten when the plant has a particle
	 */
	public Set<FiParticle> getParticles() {
		if (isParticleSet()) {
			return particles;
		}
		return super.getParticles();
	}

	// /**
	// * get LAI is overwriten
	// */
	// public double getLai () {
	// return LAI;
	// }

	// /**
	// * FP 29-09-2009: leaf in m2 (total, not single sided) and biomass in a
	// voxel for different
	// * species
	// *
	// * @throws Exception
	// */
	// public double biomassInVoxel (double x, double y, double z, double
	// voxelVolume, boolean status,
	// boolean particles) throws Exception {
	//
	// String speciesName = this.getSpeciesName ();
	// // crown lenght including pruned zone
	//
	// // special model for ponderosa pine
	// if (speciesName.equals (FmModel.PINUS_PONDEROSA_LANL)) {
	// double crownLength = this.getHeight () -
	// this.getCrownBaseHeightBeforePruning ();
	//
	// // pruning:
	// if ((this.getCrownBaseHeight () > this.getCrownBaseHeightBeforePruning
	// ())
	// && z < this.getCrownBaseHeight ()) {
	// System.out.println ("prunned tree:" + this.getId ());
	// return 0d;
	// }
	// // model for alive needles
	// if (status == FmPlant.ALIVEb && particles == FmPlant.LEAVESb) {
	// // Rod Linn's model
	// double rhomax = 0.4 * 6.0 / 5.0; // kg/m3
	// double h = crownLength * 0.2;
	// double d = crownLength * 0.8;
	// double R = this.getCrownRadius ();
	// // relative ray squarred in crown
	// double relRayToCrownRadius2 = (x * x + y * y) / (R * R);
	// double relRay2 = Double.MAX_VALUE;
	// if (z > 0. && z <= h) {// lower part
	// relRay2 = relRayToCrownRadius2 * h / z;
	// } else if (z > h && z <= crownLength) { // upper part
	// relRay2 = relRayToCrownRadius2 * d / (crownLength - z);
	// }
	// if (relRay2 <= 1.0) { // in crown
	// double bulkDensity = (z + d * relRayToCrownRadius2) / crownLength *
	// rhomax;
	// return bulkDensity * voxelVolume;
	// }
	// }
	// return 0.0;
	// }
	// double distanceToTrunc = Math.sqrt (x * x + y * y);
	// // TODO for clarity bulkDensity should use z instead of z/crownLenght
	// // nevertheless hrelfromground is computed in bulk density so it should
	// // be OK
	// //TODO
	// double bulkDensity = 0d; //bulkDensity (this, distanceToTrunc, z, status,
	// particles);
	// // distanceToTrunc, z / crownLength, status, particles);modified by FP
	// //
	// System.out.println("x="+x+" y="+y+" z="+z+" distanceToTrunc="+distanceToTrunc+" crownLength="+crownLength);
	// return bulkDensity * voxelVolume;
	// }

	// /**
	// * Compute the relative radius of the crown, given the relative Height in
	// crown from the
	// * crownProfile of the species
	// *
	// * @param relHeight
	// * @return
	// * @throws Exception
	// */
	// static public double relativeRadiusFromCrownProfileIncludingDead (double
	// relHeight, String speciesName)
	// throws Exception {
	// if (relHeight >= 1.0) return 0.0;
	// double[][] crownProfile = FmPlant.crownGeometry (speciesName);
	// // computation of relMaxHeight
	// double maxR = 0d;
	// //double maxRHeight = 0d;
	// int nDiamMaxR = 1;
	//
	// for (int ndiam = 1; ndiam < crownProfile.length; ndiam++) {
	// if (crownProfile[ndiam][1] >= maxR) {
	// maxR = crownProfile[ndiam][1];
	// nDiamMaxR = ndiam;
	// maxRHeight = crownProfile[ndiam][0];
	// }
	// }
	//
	// // computation of relBelowMaxHeight;=> reference below maxHeight
	// double rBelowMaxHeight = crownProfile[nDiamMaxR - 1][1];
	// double hBelowMaxHeight = crownProfile[nDiamMaxR - 1][0];
	//
	// // below hBelowMaxHeight:
	// if (relHeight <= hBelowMaxHeight) return 0.01 * rBelowMaxHeight;
	// // above
	// double result = 0d;
	// for (int ndiam = nDiamMaxR; ndiam < crownProfile.length - 1; ndiam++) {
	// if (crownProfile[ndiam][0] <= relHeight) {
	// result = 0.01
	// * (crownProfile[ndiam + 1][1] * (relHeight - crownProfile[ndiam][0]) +
	// crownProfile[ndiam][1]
	// * (crownProfile[ndiam + 1][0] - relHeight))
	// / (crownProfile[ndiam + 1][0] - crownProfile[ndiam][0]);
	// }
	// }
	// return result;
	// }

	/**
	 * FP 11-09-2009: Annabel porte relashionship for crownlenght and rad (in
	 * m), as a function of species and dbh
	 * 
	 * @throws Exception
	 */
	// TODO this method should be moved in FiLocalPlantDimension or removed
	// public static double[] crownDimensions(String speciesName, double dbh)
	// throws Exception {
	// double[] result = new double[2];
	// double a, b;
	// result[0]=-1;
	// result[1]=-1;
	// if (speciesName.equals(FiModel.PINUS_PINASTER)) {
	// a = 0.853;
	// b = 0.629;
	// double crownLenght = a * Math.pow(dbh, b);
	// a = 0.106;
	// b = 0.861;
	// double crownRad = a * Math.pow(dbh, b);
	// result[0] = crownLenght;
	// result[1] = crownRad;
	// return result;
	// }
	// return result;
	// }

	// /**
	// * Compute the relative radius of the crown, given the relative Height in
	// * crown
	// *
	// * @param relHeight
	// * @return
	// */
	// static double relativeRadiusPorte(double relHeight) {
	// if (relHeight >= 1.0 || relHeight <= 0.0)
	// return 0.0;
	// return 8.3 * relHeight - 23.4 * relHeight * relHeight + 27.0
	// * Math.pow(relHeight, 3.0) - 11.9 * Math.pow(relHeight, 4.0);
	// }

	// public static double[][] crownGeometry (String speciesName) throws
	// Exception {
	// double[][] crownProfile;
	//
	// // Create a crown profile for the tree (spheric crown)
	// // crownProfile = CrownProfileUtil.createRelativeCrownProfile (
	// // new double[] {0, 0, 50, 100, 100, 0}); // '50' could be
	// // recalculated with maxDiameterHeight
	// // FP put a crown profile from porte 2000
	// // detailed
	// // crownProfile = CrownProfileUtil
	// // .createRelativeCrownProfile(new double[] { 0, 0, 10, 62,
	// // 20, 92, 30, 100, 40, 99, 50, 93, 60, 85, 70, 75,
	// // 80, 61, 90, 39, 100, 0 });
	// // coarse
	// if (speciesName.equals (FmModel.PICEA_MARIANA) || speciesName.equals
	// (FmModel.PICEA_MARIANA_DEAD)
	// || speciesName.equals (FmModel.PINUS_BANKSIANA) || speciesName.equals
	// (FmModel.PICEA_MARIANA_DEAD)) {
	// crownProfile = CrownProfileUtil.createRelativeCrownProfile (new double[]
	// {0, 80, 25, 99, 50, 66, 75, 33,
	// 100, 0});
	// } else if (speciesName.equals (FmModel.PINUS_PONDEROSA_LANL) ||
	// speciesName.equals (FmModel.JUNIPER_TREE)
	// || speciesName.equals (FmModel.PINON_PINE) || speciesName.equals
	// (FmModel.PINON_PINE_DEAD)
	// || speciesName.equals (FmModel.PINUS_PONDEROSA_USFS1)) {
	// crownProfile = CrownProfileUtil.createRelativeCrownProfile (new double[]
	// {0, 0, 5, 50, 10, 71, 20, 100, 40,
	// 87, 60, 71, 80, 50, 100, 0});
	// } else if (speciesName.equals (FmModel.QUERCUS_COCCIFERA)) {
	// crownProfile = CrownProfileUtil.createRelativeCrownProfile (new double[]
	// {0, 80, 50, 100, 100, 90});
	//
	// } else if (speciesName.equals (FmModel.PINUS_PINASTER_NAVAS)
	// || speciesName.equals (FmModel.PINUS_PINASTER_TELENO)) {
	// crownProfile = CrownProfileUtil.createRelativeCrownProfile (new double[]
	// {0, 8, 10, 50, 25, 75, 45, 100,
	// 75, 67, 100, 0});
	// } else {
	// crownProfile = CrownProfileUtil.createRelativeCrownProfile (new double[]
	// {0, 5, 10, 60, 25, 99, 50, 93, 75,
	// 69, 100, 0});
	// }
	// return crownProfile;
	//
	// }
	public String getPatternName() {
		return patternName;
	}

	public void setPatternName(String patternName) {
		this.patternName = patternName;
	}

	/**
	 * SimpleCrownDescription
	 */
	// public float getTransparency() {
	// return 0; // removed from SimpleCrownProvider, 0: opaque
	// }

	public byte getBeetleStatus() {
		return beetleStatus;
	}

	public void setBeetleStatus(byte beetleStatus) {
		this.beetleStatus = beetleStatus;
	}

	// public String getFileId () {return fileId;}
	public int getPop() {
		return populationGroup;
	}

	public void setPop(int v) {
		populationGroup = v;
	}

	public Color getCrownColor() {
		// rgb may be used for beetles status, can be set by a 2D viewer and
		// usable by a 3D viewer
		// if set, use it
		if (rgb != null) {
			return new Color(rgb[0], rgb[1], rgb[2]);
		}
		// return crownColor;
		return species.getColor(); // fc-7.11.2014
	}

	public int[] getRGB() {
		return rgb;
	}

	public void setRGB(int[] v) {
		this.rgb = v;
	}
	public FmPlant clone() {
		FmPlant t = (FmPlant)super.clone();
		t.particles = new HashSet<FiParticle>();
		for (FiParticle pt:this.particles) {
			t.particles.add(pt);
		}
		t.patternName = this.patternName;
		t.beetleStatus = this.beetleStatus;
		t.populationGroup = this.populationGroup;
		if (this.rgb!=null) {
			t.rgb = new int[3];
			t.rgb[0]=this.rgb[0];
			t.rgb[1]=this.rgb[1];
			t.rgb[2]=this.rgb[2];
		}
		return t;
	}

	/**
	 * 
	 * @param fmModel
	 * @param ageTot
	 * @param dDom : dominant diameter after growth
	 * @param hDom : dominant height after growth
	 * @param BA : basal area after growth
	 * @param delta5_hdom 
	 * @param BAub: basal area under bark before growth
	 * @param dDomub : dominant diameter under bark before growth
	 * @throws Exception
	 */
	public void processGrowth(FmModel fmModel,int ageTot, double dDom, double hDom, double BA, double delta5_hdom, double BAub, double dDomub) throws Exception {
		if (!(this.getSpeciesName().equals(FmModel.PINUS_HALEPENSIS))) {
				throw new Exception(
				"FmPlant.growthFrom: impossible to compute evolution for plant "+this+" because not aleppo pine");
		}
		double dbhub= 0.847 * dbh- 0.252;
		//equation for the evolution of dbh under bark
		double delta5_dbhub = 0.812 + (1d-Math.exp(-0.0152*Math.min(delta5_hdom, 3d)))*
				(39.3-0.576*BAub+49.6*Math.max(Math.min(dbhub/dDomub,1.1),0.3));
		double oldHeightFromModel = FmLocalPlantDimension.computeAleppoPineHeight(this.dbh, hDom, dDom);
		this.dbh += delta5_dbhub/(5 * 0.847);
		double newHeightFromModel = FmLocalPlantDimension.computeAleppoPineHeight(this.dbh, hDom, dDom);
		//this.height = (float) Math.max(newHeightFromModel,this.height+newHeightFromModel-oldHeightFromModel);
		this.height = (float) Math.max(newHeightFromModel, this.height);
		//System.out.println("height="+this.height+",hDom="+hDom+",dDom="+dDom+",dbh="+dbh+",deltadbh="+delta5_dbhub/(5 * 0.847)+",oldHmod="+oldHeightFromModel+",newHmod="+newHeightFromModel);
		//this.height += (float) Math.max(0,newHeightFromModel-oldHeightFromModel);
		this.age += 1;
		//System.out.println("Evolution:"+crownBaseHeight);
		if (crownBaseHeight>crownBaseHeightBeforePruning) {// the tree was pruned
			crownBaseHeightBeforePruning = FmLocalPlantDimension.computeCrownBaseHeight(this.species, dbh, height, ageTot,
					hDom, BA);
			crownBaseHeight = Math.max(crownBaseHeight, crownBaseHeightBeforePruning);
		} else {
			crownBaseHeight = FmLocalPlantDimension.computeCrownBaseHeight(this.species, dbh, height, ageTot,
					hDom, BA);
			crownBaseHeightBeforePruning = crownBaseHeight;
		}
			
		crownDiameter = FmLocalPlantDimension.computeCrownDiameter(this.species, dbh, height);
		this.setCrownDiameterHeightFromCrownGeometry();
		crownPerpendicularDiameter = crownDiameter;
		FmLocalPlantBiomass.setBiomassWithoutPruning(this, fmModel.particleNames);
		updateTotalThinMass();
	}

	// public Map<String,Double> getMoistures () {
	// return moistures;
	// }

	// public void setMoistures (Map<String,Double> moistures) {
	// this.moistures = moistures;
	// }

	// public boolean isMoistureIsSet () {
	// return moistureIsSet;
	// }

	// public void setMoistureIsSet (boolean moistureIsSet) {
	// this.moistureIsSet = moistureIsSet;
	// }

}
