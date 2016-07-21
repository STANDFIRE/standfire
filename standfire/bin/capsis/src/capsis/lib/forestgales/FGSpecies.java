package capsis.lib.forestgales;

import java.io.Serializable;

import capsis.lib.forestgales.function.Function;

/**
 * The ForestGales species.
 *
 * @author B. Gardiner, K. Kamimura - August 2013
 */
public class FGSpecies implements Serializable {

	private int id;
	private String name;
	private double topHeightMultiplier; // unitless
	private double topHeightIntercept; // m
	private Function canopyWidthFunction; // f(dbh)
	private Function canopyDepthFunction; // f(height)
	private double greenWoodDensity; // kg/m3
	private double canopyDensity; // kg/m3 branches + leaves / needles
	private double modulusOfRupture; // Pa
	private double knotFactor; // percentage, unitless, e.g. 0.8

	private double crownFactor; // unitless

	private double modulusOfElasticity; // Pa
	private double canopyStreamliningC; // unitless
	private double canopyStreamliningN; // unitless
	private double rootBendingK; // degrees, TO BE CHECKED
	private double[][] overturningMomentMultipliers; // N.m/kg
	private double[][] overturningMomentMaximumStemWeights; // kg



	/**
	 * Constructor.
	 */
	public FGSpecies (int id, String name, double topHeightMultiplier, double topHeightIntercept,
			Function canopyWidthFunction, Function canopyDepthFunction, double greenWoodDensity, double canopyDensity,
			double modulusOfRupture, double knotFactor, double crownFactor, double modulusOfElasticity, double canopyStreamliningC,
			double canopyStreamliningN, double rootBendingK, double[][] overturningMomentMultipliers,
			double[][] overturningMomentMaximumStemWeights) {
		super ();
		this.id = id;
		this.name = name;
		this.topHeightMultiplier = topHeightMultiplier;
		this.topHeightIntercept = topHeightIntercept;
		this.canopyWidthFunction = canopyWidthFunction;
		this.canopyDepthFunction = canopyDepthFunction;
		this.greenWoodDensity = greenWoodDensity;
		this.canopyDensity = canopyDensity;
		this.modulusOfRupture = modulusOfRupture;
		this.knotFactor = knotFactor;
		this.crownFactor = crownFactor;
		this.modulusOfElasticity = modulusOfElasticity;
		this.canopyStreamliningC = canopyStreamliningC;
		this.canopyStreamliningN = canopyStreamliningN;
		this.rootBendingK = rootBendingK;
		this.overturningMomentMultipliers = overturningMomentMultipliers;
		this.overturningMomentMaximumStemWeights = overturningMomentMaximumStemWeights;
	}

	public int getId () {
		return id;
	}

	public String getName () {
		return name;
	}

	public double getTopHeightMultiplier () {
		return topHeightMultiplier;
	}
	public void setTopHeightMultiplier (double topHeightMultiplier) {
		this.topHeightMultiplier = topHeightMultiplier;
	}

	public double getTopHeightIntercept () {
		return topHeightIntercept;
	}
	public void setTopHeightIntercept (double topHeightIntercept) {
		this.topHeightIntercept = topHeightIntercept;
	}

	public Function getCanopyWidthFunction () {
		return canopyWidthFunction;
	}
	public void setCanopyWidthFunction (Function canopyWidthFunction) {
		this.canopyWidthFunction = canopyWidthFunction;
	}

	public Function getCanopyDepthFunction () {
		return canopyDepthFunction;
	}
	public void setCanopyDepthFunction (Function canopyDepthFunction) {
		this.canopyDepthFunction = canopyDepthFunction;
	}

	public double getGreenWoodDensity () {
		return greenWoodDensity;
	}
	public void setGreenWoodDensity (double greenWoodDensity) {
		this.greenWoodDensity = greenWoodDensity;
	}
	public double getCanopyDensity () {
		return canopyDensity;
	}
	public void setCanopyDensity (double canopyDensity) {
		this.canopyDensity = canopyDensity;
	}

	public double getModulusOfRupture () {
		return modulusOfRupture;
	}
	public void setModulusOfRupture (double modulusOfRupture) {
		this.modulusOfRupture = modulusOfRupture;
	}

	public double getKnotFactor () {
		return knotFactor;
	}
	public void setKnotFactor (double knotFactor) {
		this.knotFactor = knotFactor;
	}

	public double getCrownFactor () {
		return crownFactor;
	}
	public void setCrownFactor (double crownFactor) {
		this.crownFactor = crownFactor;
	}

	public double getModulusOfElasticity () {
		return modulusOfElasticity;
	}
	public void setModulusOfElasticity (double modulusOfElasticity) {
		this.modulusOfElasticity = modulusOfElasticity;
	}

	public double getCanopyStreamliningC () {
		return canopyStreamliningC;
	}
	public void setCanopyStreamliningC (double canopyStreamliningC) {
		this.canopyStreamliningC = canopyStreamliningC;
	}

	public double getCanopyStreamliningN () {
		return canopyStreamliningN;
	}
	public void setCanopyStreamliningN (double canopyStreamliningN) {
		this.canopyStreamliningN = canopyStreamliningN;
	}

	public double getRootBendingK () {
		return rootBendingK;
	}
	public void setRootBendingK (double rootBendingK) {
		this.rootBendingK = rootBendingK;
	}

	public double[][] getOverturningMomentMultipliers () {
		return overturningMomentMultipliers;
	}
	public void setOverturningMomentMultipliers (double [][] overturningMomentMultipliers ) {
		this.overturningMomentMultipliers = overturningMomentMultipliers;
	}

	public double[][] getOverturningMomentMaximumStemWeights () {
		return overturningMomentMaximumStemWeights;
	}
	public void setOverturningMomentMaximumStemWeights (double [][] overturningMomentMaximumStemWeights ) {
		this.overturningMomentMaximumStemWeights = overturningMomentMaximumStemWeights;
	}

	public String toString () {
		StringBuffer b = new StringBuffer ("  FGSpecies "+name+"\n");
		b.append ("    id: "+id+"\n");
		b.append ("    name: "+name+"\n");
		b.append ("    topHeightMultiplier: "+topHeightMultiplier+"\n");
		b.append ("    topHeightIntercept: "+topHeightIntercept+"\n");
		b.append ("    canopyWidthFunction: "+canopyWidthFunction+"\n");
		b.append ("    canopyDepthFunction: "+canopyDepthFunction+"\n");
		b.append ("    greenWoodDensity: "+greenWoodDensity+"\n");
		b.append ("    canopyDensity: "+canopyDensity+"\n");
		b.append ("    modulusOfRupture: "+modulusOfRupture+"\n");
		b.append ("    knotFactor: "+knotFactor+"\n");
		b.append ("    crownFactor: "+crownFactor+"\n");
		b.append ("    modulusOfElasticity: "+modulusOfElasticity+"\n");
		b.append ("    canopyStreamliningC: "+canopyStreamliningC+"\n");
		b.append ("    canopyStreamliningN: "+canopyStreamliningN+"\n");
		b.append ("    rootBendingK: "+rootBendingK+"\n");
		b.append ("    overturningMomentMultipliers: \n    ");
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 3; j++) {
				b.append (overturningMomentMultipliers[i][j]);
				b.append (" ");
			}
			b.append ("\n    ");
		}
		b.append ("overturningMomentMaximumStemWeights: \n    ");
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 3; j++) {
				b.append (overturningMomentMaximumStemWeights[i][j]);
				b.append (" ");
			}
			b.append ("\n    ");
		}
		b.deleteCharAt (b.length () - 1);
		b.deleteCharAt (b.length () - 1);
		b.deleteCharAt (b.length () - 1);
		b.deleteCharAt (b.length () - 1);
		return b.toString ();
	}


}
