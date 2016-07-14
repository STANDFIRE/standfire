package capsis.lib.crobas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import uqar.jackpine.model.JpTree;

import capsis.defaulttype.TreeHashMap;

/**
 * Stand attributes to be used in Crobas (A. Makela 1997 For. Sci.) and PipeQual
 * (Makela and Makinen 2003 For Eco and Manag) simulations.
 * 
 * @author R. Schneider - 20.5.2008
 */
public abstract class CStand extends TreeHashMap {

	public double BasalArea; // m2/ha
	public double N; // stand density, number of stems per ha
	public double meanQuadDbh; // mean quadratic diameter, cm
	public double maxHt; // top stand height, m
	public double maxHc; // crown length of tallest tree, m
	public double meanHt; // mean height, m
	public double meanHc; // mean crown length, m
	public double meanCrownBase; // mean crown base, m
	public double age; // years
	public double Vtot; // total volume, m3/ha
	public double Vmer; // total merchantable volume, m3/ha
	public double crownCovCrownBase; // crown coverage at crown base, m2/ha
	public double maxCrownCoverage; // maximum crown coverage, m2/ha

	private double area; // m2

	public CStand() {
		super();
		setArea(10000); // m2
	}
	
	/**
	 * Run inGrowth for this stand.
	 */
	abstract public void inGrowth (CSettings sets) throws Exception;

	public void setArea(double area) {
		this.area = area;
	}

	public double getArea() {
		return area;
	}

	public void setBasalArea(double v) {
		BasalArea = v;
	}

	public double getBasalArea() {
		return BasalArea;
	}

	/**
	 * Calculate stand variables.
	 */
	public void calculateStandVariables() {
		// MOVED to constructor (needed earlier at construction time)
		// setArea (10000); // m2

		BasalArea = 0;
		N = 0;
		meanQuadDbh = 0;
		maxHt = 0;
		maxHc = 0;
		meanHt = 0;
		meanHc = 0;
		meanCrownBase = 0;
		Vtot = 0;
		Vmer = 0;

		double sumQuadDbh = 0;
		double sumMeanHt = 0;
		double sumMeanHc = 0;
		double sumMeanCrownBase = 0;
		int numberOfTreesWithNonZeroN = 0;

		for (Iterator i = getTrees().iterator(); i.hasNext();) {
			CTree t = (CTree) i.next();

			if (t.getNumber() > 0) {
				++numberOfTreesWithNonZeroN;

				BasalArea += t.BasalArea;
				N += t.getNumber();
				Vtot += t.Vtot_ha;
				Vmer += t.Vmer_ha;

				sumQuadDbh += t.getDbh() * t.getDbh() * t.getNumber();
				maxHt = Math.max(maxHt, t.getHeight());
				if (t.getHeight() == maxHt) {
					maxHc = t.Hc;
				}
				sumMeanHt += t.getHeight() * t.getNumber();
				sumMeanHc += t.Hc * t.getNumber();
				sumMeanCrownBase += t.Hs * t.getNumber();
			}

		}
		if (numberOfTreesWithNonZeroN > 0) {
			meanQuadDbh = Math.pow(sumQuadDbh / N, 0.5);
			meanHt = sumMeanHt / N;
			meanHc = sumMeanHc / N;
			meanCrownBase = sumMeanCrownBase / N;
		}
	}

	public String toString() {
		return " CStand > area: "+getArea ()+" BasalArea: " + BasalArea + " N: " + N
				+ " meanQuadDbh: " + meanQuadDbh + " maxHt: " + maxHt
				+ " maxHc: " + maxHc + " meanHt: " + meanHt + " meanHc: "
				+ meanHc + " meanCrownBase: " + meanCrownBase + " age: " + age
				+ " Vtot: " + Vtot + " Vmer: " + Vmer + " crownCovCrownBase: "
				+ crownCovCrownBase + " maxCrownCoverage: " + maxCrownCoverage;

	}

}
