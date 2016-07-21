package fireparadox.model.plant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import jeeb.lib.sketch.scene.item.Polygon;
import jeeb.lib.util.Vertex3d;
import capsis.lib.fire.fuelitem.FiPlantPopulationGenerator;
import capsis.lib.fire.fuelitem.FiSpecies;
import fireparadox.model.FmModel;
import fireparadox.model.FmStand;

public class FmPlantPopulationGenerator extends FiPlantPopulationGenerator {
	private String speciesName; // Aleppo pine only for the moment
	private double hDom50; // dominant height at 50 yo in m (in term of ageDom
	// at 30 cm)
	private double hDom; // dominant height in stand

	private int ageTot; // stand age
	private double ageDom; // dominant age at 30cm, estimated with ageTot-5
	private double nHa; // number of stem/ha
	private double sP100; // spacing factor = space between trees/hDom in % hDom
	private double gHa; // basal area m2/ha
	private double dG; // mean quadratique d130 in stand
	private FmStand stand;
	public int maxId;

	/**
	 * this constructor for respatialization only (functionality of
	 * FiPlantPopulationGenerator only)
	 */
	public FmPlantPopulationGenerator(List<FmPlant> fmPlants, Polygon polygon, Random rnd) {
		super(polygon, rnd);
		for (FmPlant pt : fmPlants) {
			this.plants.add(pt);
		}
	}

	/**
	 * a constructor to build the fmPlants from dbh. No spatialization here, to
	 * enable to spatialize several group of tree together
	 * 
	 * @param stand
	 * @param maxId
	 * @param speciesName
	 * @param polygon
	 * @param lowerBoundDBH
	 * @param upperBoundDBH
	 * @param stemDensity
	 * @param groupAge
	 * @param liveNeedleMoistureContent
	 * @param deadTwigMoistureContent
	 * @param liveTwigMoistureContent
	 * @param crownInPolygon
	 * @param theseTrees
	 * @throws Exception
	 */
	public FmPlantPopulationGenerator(FmStand stand, int maxId, FiSpecies species, Polygon polygon,
			double lowerBoundDBH, double upperBoundDBH, double stemDensity, double groupAge,
			double liveNeedleMoistureContent, double deadTwigMoistureContent, double liveTwigMoistureContent,
			double crownInPolygon) throws Exception {
		super(polygon,stand.getModel().rnd);
		this.maxId = maxId;

		FmModel model = (FmModel) stand.getModel(); // fc-2.2.2015

		//FiSpecies species = stand.getModel().getSpecies(speciesName);
		// System.out.println(" specie:s"+species+","+speciesName);
		double area = polygon.getPolygon2().getPositiveArea();
		int stemNumber = (int) (area * stemDensity * 0.0001);
		double meanDBH = 0.5 * (lowerBoundDBH + upperBoundDBH);
		double domHeight = FmLocalPlantDimension.computeTreeHeight(species, upperBoundDBH, groupAge);
		double bA = stemDensity * meanDBH * meanDBH * Math.PI * 0.25 * 0.01 * 0.01;
		// double extraStemPercent = 30d;
		for (int nt = 0; nt < stemNumber; nt++) {
			double dbh = lowerBoundDBH + (upperBoundDBH - lowerBoundDBH) *rnd.nextDouble();// Math.random();
			double height = FmLocalPlantDimension.computeTreeHeight(species, dbh, groupAge);
			double crownDiameter = FmLocalPlantDimension.computeCrownDiameter(species, dbh, height);
			double crownBaseHeight = FmLocalPlantDimension.computeCrownBaseHeight(species, dbh, height,
					(int) groupAge, domHeight, bA);
			Vertex3d tPos = new Vertex3d(0d, 0d, 0d);
			this.maxId++;
			FmPlant tree = new FmPlant(maxId, stand,
					model, // fc-2.2.2015
					(int) groupAge, tPos.x, tPos.y, tPos.z, "" + maxId, dbh, height, crownBaseHeight, crownDiameter,
					species, // species
					0, // pop = 0 PhD 2008-09-25
					liveNeedleMoistureContent, deadTwigMoistureContent, liveTwigMoistureContent, false);
			this.plants.add(tree);
		}
	}

	/**
	 * this constructor used the model of Ph Dreyfus to generate a FmPlantList
	 * from empirical relationships of stem distribution of Aleppo pine.
	 * Position is set later
	 * 
	 * @param area
	 * @param speciesName
	 * @param hDom50
	 * @param ageTot
	 * @param nHa
	 * @param gHaETFactor
	 * @param polygon
	 * @throws Exception
	 */
	public FmPlantPopulationGenerator(FmStand stand, String speciesName, double hDom50, int ageTot, double nHa,
			double gHaETFactor, Polygon polygon, int maxId, double liveNeedleMoistureContent,
			double deadTwigMoistureContent, double liveTwigMoistureContent) throws Exception {
		super(polygon,stand.getModel().rnd);
		this.stand = stand;
		this.maxId = maxId;
		this.speciesName = speciesName;
		this.hDom50 = hDom50;
		this.ageTot = ageTot;
		this.ageDom = ageTot - 5d;
		this.nHa = nHa;// * (1 + 0.01 * excessPercent);
		if (speciesName.equals(FmModel.PINUS_HALEPENSIS)) {
			// Couhert et Duplat 1993
			// this.hDom = 0.3
			// + 2.005
			// * (hDom50 - 0.3)
			// * Math.pow(1d - Math
			// .exp(-0.3662 * Math.pow(ageDom, 0.4410)), 5.080);

			// FP added the following correction to compute more precisely hDom
			// according to IFN data for initial conditions
			double hDom50_2 = hDom50 * (-0.00382 * ageDom + 1.19);
			// DREYFUS 2001
			this.hDom = hDom50_2 * Math.exp(Math.pow(1d / (0.04 * 50d), 0.95) - Math.pow(1d / (0.04 * ageDom), 0.95));

		}

		this.sP100 = 10746 / (hDom * Math.sqrt(nHa));
		if (speciesName.equals(FmModel.PINUS_HALEPENSIS)) {
			// IFN derivation from pHd
			double meanGHa = 78.3 * Math.pow(sP100 - 6.96, -0.865) * Math.pow(ageDom, 0.521) * Math.pow(nHa, -0.0937);
			// standard deviation of GHa
			double stdGHa = 7.10 * Math.pow(0.981, sP100);
			this.gHa = meanGHa + gHaETFactor * stdGHa;
		}

		this.dG = 100d * Math.sqrt(4d * gHa / (Math.PI * nHa));

		FmModel model = (FmModel) stand.getModel(); // fc-2.2.2015

		this.generatePlantDimensionDistribution(liveNeedleMoistureContent, deadTwigMoistureContent,
				liveTwigMoistureContent, model);
	}

	/**
	 * A simiar to previous constructor when gha and hdom are already known
	 * already known. Position is set later.
	 * 
	 * @param area
	 *            : polygon area
	 * @param speciesName
	 * @param hDom
	 *            : dominant height at current ageDom
	 * @param ageTot
	 *            : total age of the stand
	 * @param nHa
	 *            : number of stem per ha
	 * @param gHa
	 * @throws Exception
	 * 
	 */
	public FmPlantPopulationGenerator(FmStand stand, double hDom, String speciesName, int ageTot, double nHa,
			double gHa, Polygon polygon, int maxId, double liveNeedleMoistureContent, double deadTwigMoistureContent,
			double liveTwigMoistureContent) throws Exception {
		super(polygon,stand.getModel().rnd);
		this.stand = stand;
		this.maxId = maxId;
		this.speciesName = speciesName;
		this.hDom = hDom;
		this.ageTot = ageTot;
		this.ageDom = ageTot - 5d;
		this.nHa = nHa;// * (1 + 0.01 * excessPercent);
		if (speciesName.equals(FmModel.PINUS_HALEPENSIS)) {
			// Couhert et Duplat 1993
			// this.hDom = 0.3
			// + 2.005
			// * (hDom50 - 0.3)
			// * Math.pow(1d - Math
			// .exp(-0.3662 * Math.pow(ageDom, 0.4410)), 5.080);

			double hDom50_2 = hDom / Math.exp(Math.pow(1d / (0.04 * 50d), 0.95) - Math.pow(1d / (0.04 * ageDom), 0.95));

			// FP added the following correction to compute more precisely hDom
			// according to IFN data for initial conditions
			this.hDom50 = hDom50_2 / (-0.00382 * ageDom + 1.19);
		}

		this.sP100 = 10746 / (hDom * Math.sqrt(nHa));

		this.gHa = gHa;
		this.dG = 100d * Math.sqrt(4d * gHa / (Math.PI * nHa));

		FmModel model = (FmModel) stand.getModel(); // fc-2.2.2015

		this.generatePlantDimensionDistribution(liveNeedleMoistureContent, deadTwigMoistureContent,
				liveTwigMoistureContent, model);
	}

	/**
	 * 
	 * @return result[n][i] : i=0 dbh, i=1 height of tree n-1
	 * @throws Exception
	 */
	private void generatePlantDimensionDistribution(double liveNeedleMoistureContent, double deadTwigMoistureContent,
			double liveTwigMoistureContent, FmModel model) throws Exception {
		int treeNumber = (int) Math.round((float) nHa * polygon.getPolygon2().getPositiveArea() * 0.0001);
		System.out.println("generation of a tree distribution of " + treeNumber + " " + speciesName);

		// assesment of weibull parameter
		double aw = 0d;
		double bw = 0d;
		double cw = 0d;

		if (speciesName.equals(FmModel.PINUS_HALEPENSIS)) {
			aw = 3.18 + 0.253 * dG;
			bw = -1.53 + 0.697 * dG;
			cw = 2.3 / (1d - 1.08 * Math.exp(-0.517 * bw));
		}
		FiSpecies s = model.getSpecies(speciesName);
		double dDom = computeDDomWeibull(aw, bw, cw);
		System.out.println("dominant height and diameter are " + hDom + " (m) and " + dDom + " (cm)");

		for (int n = 0; n < treeNumber; n++) {
			maxId++;
			double dbh = weibull(aw, bw, cw);
			double height = FmLocalPlantDimension.computeAleppoPineHeight(dbh, hDom, dDom);
			double bA = 0d; // unused
			double domHeight = 0d; // unused
			double crownBaseHeight = FmLocalPlantDimension.computeCrownBaseHeight(s, dbh, height, ageTot,
					domHeight, bA);
			double crownDiameter = FmLocalPlantDimension.computeCrownDiameter(s, dbh, height);
			FmPlant tree = new FmPlant(maxId, stand, 
					model, // fc-2.2.2015 
					ageTot, 0d, 0d, 0d, "" + maxId, dbh, height, crownBaseHeight,
					crownDiameter, s, 0, // pop = 0 PhD 2008-09-25
					liveNeedleMoistureContent, deadTwigMoistureContent, liveTwigMoistureContent, false);
			this.plants.add(tree);
		}
	}

	/**
	 * compute weibull number, based on repartition function f(x)=
	 * 1-e-((x-a)/b)**c)
	 */
	private double weibull(double a, double b, double c) {
		//double p = Math.min(Math.random(), 0.97);
		double p = Math.min(rnd.nextDouble(), 0.97);
		return a + b * Math.pow(-Math.log(1d - p), 1d / c);
	}

	private double computeDDomWeibull(double a, double b, double c) {
		List<Double> dbhList = new ArrayList();
		int tn = 1000;
		for (int i = 0; i < tn; i++) {
			dbhList.add(weibull(a, b, c));
		}
		Collections.sort(dbhList);
		double quadMean100bigger = 0d;
		for (int i = 0; i < 100; i++) {
			double dbh = dbhList.get(tn - 1 - i);
			// System.out.println("diam = " + dbh);
			quadMean100bigger += dbh * dbh;
		}
		return Math.sqrt(0.01 * quadMean100bigger);

	}

	/**
	 * getter for <FmPlants>
	 */

	public List<FmPlant> getFmPlants() {
		List<FmPlant> fmPlants = new ArrayList<FmPlant>();
		for (int ipt = 0; ipt < plants.size(); ipt++) {
			fmPlants.add(ipt, (FmPlant) plants.get(ipt));
		}
		return fmPlants;
	}
}
