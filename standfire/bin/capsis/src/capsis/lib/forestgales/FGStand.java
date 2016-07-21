package capsis.lib.forestgales;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The stand description for ForestGales.
 *
 * @author B. Gardiner, K. Kamimura, C. Meredieu, T. Labbe  - August 2013 - update May 2014
 */
public class FGStand  implements Serializable {

	private double nha;
	private FGSoilType soilType = FGSoilType.SOIL_TYPE_A;
	private FGRootingDepth rootingDepth = FGRootingDepth.MEDIUM;
	private double gapWidth = 0; // m
	private double gapHeight = 0; // m
	//private double surroundingLandRoughness = 0.06; // m
	private double dominantHeight; // m
	private boolean windClimateAvailable = false;
	private double windClimateWeibullA = 6; // m/s
	private double windClimateWeibullK = 1.9; // unitless
	//private double overturningMomentMultiplier = 100; // unitless NOT USE HERE species dependant

	private double treeHeightsNumberFromEdge = 9; // number of heights = DistanceFromForestEdge / height
	private double sizeOfUpwindGap = 10; // number of heights


	private List<FGTree> trees;

	/**
	 * Default constructor (with default values).
	 */
	public FGStand () {

	}

	/**
	 * Constructor 2.
	 */
	public FGStand (double nha, FGSoilType soilType, FGRootingDepth rootingDepth, double gapWidth, double gapHeight,
			double dominantHeight, boolean windClimateAvailable,
			double windClimateWeibullA, double windClimateWeibullK, double treeHeightsNumberFromEdge , double sizeOfUpwindGap ) {
		super ();
		this.nha = nha;
		this.soilType = soilType;
		this.rootingDepth = rootingDepth;
		this.gapWidth = gapWidth;
		this.gapHeight = gapHeight;
		this.dominantHeight = dominantHeight;
		this.windClimateAvailable = windClimateAvailable;
		this.windClimateWeibullA = windClimateWeibullA;
		this.windClimateWeibullK = windClimateWeibullK;
		this.treeHeightsNumberFromEdge = treeHeightsNumberFromEdge  ;
		this.sizeOfUpwindGap = sizeOfUpwindGap  ;
	}

	public double getNha () {
		return nha;
	}

	public FGSoilType getSoilType () {
		return soilType;
	}

	public FGRootingDepth getRootingDepth () {
		return rootingDepth;
	}

	public double getGapWidth () {
		return gapWidth;
	}

	public double getGapHeight () {
		return gapHeight;
	}

	public double getDominantHeight () {
		return dominantHeight;
	}

	public boolean isWindClimateAvailable () {
		return windClimateAvailable;
	}

	public double getWindClimateWeibullA () {
		return windClimateWeibullA;
	}

	public double getWindClimateWeibullK () {
		return windClimateWeibullK;
	}

	public void setNha (double nha) {
		this.nha = nha;
	}

	public void setSoilType (FGSoilType soilType) {
		this.soilType = soilType;
	}

	public void setRootingDepth (FGRootingDepth rootingDepth) {
		this.rootingDepth = rootingDepth;
	}

	public void setGapWidth (double gapWidth) {
		this.gapWidth = gapWidth;
	}

	public void setGapHeight (double gapHeight) {
		this.gapHeight = gapHeight;
	}

	public void setDominantHeight (double dominantHeight) {
		this.dominantHeight = dominantHeight;
	}

	public void setWindClimateAvailable (boolean windClimateAvailable) {
		this.windClimateAvailable = windClimateAvailable;
	}

	public void setWindClimateWeibullA (double windClimateWeibullA) {
		this.windClimateWeibullA = windClimateWeibullA;
	}

	public void setWindClimateWeibullK (double windClimateWeibullK) {
		this.windClimateWeibullK = windClimateWeibullK;
	}

	public double getTreeHeightsNumberFromEdge () {
		return treeHeightsNumberFromEdge;
	}

	public void setTreeHeightsNumberFromEdge (double treeHeightsNumberFromEdge) {
		this.treeHeightsNumberFromEdge = treeHeightsNumberFromEdge;
	}

	public double getSizeOfUpwindGap () {
		return sizeOfUpwindGap;
	}

	public void setSizeOfUpwindGap (double sizeOfUpwindGap) {
		this.sizeOfUpwindGap = sizeOfUpwindGap;
	}


	public void addTree (FGTree t) {
		if (trees == null) trees = new ArrayList<FGTree> ();
		trees.add (t);
	}

	public List<FGTree> getTrees () {
		return trees;
	}

	public String toString () {
		StringBuffer b = new StringBuffer ("FGStand\n");
		b.append ("  nha: " + nha + "\n");
		b.append ("  soilType: " + soilType + "\n");
		b.append ("  rootingDepth: " + rootingDepth + "\n");
		b.append ("  gapWidth: " + gapWidth + "\n");
		b.append ("  gapHeight: " + gapHeight + "\n");
		b.append ("  dominantHeight: " + dominantHeight + "\n");
		b.append ("  windClimateAvailable: " + windClimateAvailable + "\n");
		b.append ("  windClimateWeibullA: " + windClimateWeibullA + "\n");
		b.append ("  windClimateWeibullK: " + windClimateWeibullK + "\n");
		b.append ("  trees: \n");

		if (trees == null) {
			b.append ("  null \n");
		} else {
			for (FGTree t : trees) {
				b.append (t); // already ends with a \n
			}
		}
		return b.toString ();

	}

}
