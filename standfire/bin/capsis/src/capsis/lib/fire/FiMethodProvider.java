package capsis.lib.fire;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import jeeb.lib.util.Log;
import capsis.defaulttype.Tree;
import capsis.kernel.GScene;
import capsis.lib.fire.fuelitem.FiPlant;
import capsis.util.TreeDbhComparator;
import capsis.util.methodprovider.CanopyCoverProvider;
import capsis.util.methodprovider.CrownRadiusProvider;
import capsis.util.methodprovider.DdomProvider;
import capsis.util.methodprovider.DgProvider;
import capsis.util.methodprovider.GProvider;
import capsis.util.methodprovider.NProvider;

/**
 * Method provider for Fuel Manager and StandFire module. New MethodProvider
 * framework.
 * 
 * @author O. Vigy, E. Rigaud - september 2006
 */
public class FiMethodProvider implements CrownRadiusProvider, // PhD 2008-09-11
																// (for
																// NrgThinner2)
		CanopyCoverProvider, GProvider, NProvider, DgProvider, DdomProvider {

	/**
	 * Number of trees. Author : F. de Coligny - 2001.
	 */
	public double getN(GScene stand, Collection trees) { // fc - 22.8.2006 -
															// Numberable is
															// double
		if (trees == null) {
			return -1;
		}
		double N = trees.size(); // ov 18.01.07
		return N; // ov 18.01.07
		// return trees.size ();
	}

	public double getNAboveThreshold(GScene stand, Collection trees, double threshold) { // fp
																							// 14/05/2009
		if (trees == null) {
			return -1;
		}
		double N = 0;
		for (Iterator i = trees.iterator(); i.hasNext();) {
			FiPlant t = (FiPlant) i.next();
			if (t.getHeight() >= threshold) { // dgh & radius : cm
				N += 1;
			}
		}
		return N; // ov 18.01.07
		// return trees.size ();
	}

	// ----------- G
	// -------------------------------------------------------------------------------------------------------------->
	// ---------------------------------------------------------------------------------------------------------------------------->
	/**
	 * Basal area ("Surface terri�re") (m2) : Sum(pi r^2) for all the given
	 * trees. Dbh (diameter 1.3m) must be in cm.
	 */
	// fc - 24.3.2004 - Use given trees
	public double getG(GScene stand, Collection trees) {
		try {
			if (trees == null) {
				return -1d;
			}
			if (trees.isEmpty()) {
				return 0d;
			} // if no trees, return 0

			double radius;
			double G = 0;
			for (Iterator i = trees.iterator(); i.hasNext();) {
				FiPlant t = (FiPlant) i.next();
				radius = t.getDbh() / 2; // dgh & radius : cm
				G += Math.PI * radius * radius;
			}
			return G / 10000; // G : m2
		} catch (Exception e) {
			Log.println(Log.ERROR, "NrgMethodProvider.getG ()", "Error while computing G", e);
			return -1d;
		}
	}

	// ----------------- Crown Radius
	// ----------------------------------------------------------------------------------------------------->
	// ---------------------------------------------------------------------------------------------------------------------------->
	/**
	 * Tree crown radius (m). // PhD 2008-09-11 (for NrgThinner2)
	 */
	public double getCrownRadius(Tree gtree) {
		try {
			FiPlant tree = (FiPlant) gtree;
			return 0.5 * tree.getCrownDiameter();
		} catch (Exception e) {
			Log.println(Log.ERROR, "FiMethodProvider.getCrownRadius ()", "Error while computing Crownr", e);
			return -1d;
		}
	}

	// ----------------- Canopy Cover
	// ----------------------------------------------------------------------------------------------------->
	// ---------------------------------------------------------------------------------------------------------------------------->
	/**
	 * Stand canopy cover (%).
	 */
	public double getCanopyCover(GScene stand, Collection trees) {
		try {
			// FmStand fstand = (FmStand) stand; // fc-29.1.2015

			// return fstand.getCanCov(); // PhD 2008-02-13
			return FiComputeStateProperties.calcCanCov(stand, trees, 0); // PhD
																			// 2008-09-18
																			// ->
																			// usable
																			// with
																			// group
																			// of
																			// trees

		} catch (Exception e) {
			Log.println(Log.ERROR, "FiMethodProvider.getCanopyCover ()", "Error while computing Canopy Cover ", e);
			return -1d;
		}
	}

	// ----------------- Maximum Height (m)
	// ----------------------------------------------------------------------------------------------------->
	// ---------------------------------------------------------------------------------------------------------------------------->
	/**
	 * Maximum Height of the trees in the stand (m).
	 */
	public double getHmax(Collection trees) {
		try {
			if (trees == null) {
				return 0d;
			}
			if (trees.isEmpty()) {
				return 0d;
			}

			double hMax = 0;
			for (Iterator i = trees.iterator(); i.hasNext();) {
				Tree t = (Tree) i.next();
				double h = t.getHeight();

				if (h > hMax) {
					hMax = h;
				}
			}
			return hMax;
		} catch (Exception e) {
			Log.println(Log.ERROR, "FiMethodProvider.getHmax ()", "Error while computing Hmax ", e);
			return -1d;
		}
	}

	/**
	 * Dg: Diameter of the mean tree (cm) : sqrt (Sum(d^2) / n).
	 */
	public double getDg(GScene stand, Collection trees) {
		try {
			if (trees == null) {
				return -1d;
			}
			if (trees.isEmpty()) {
				return 0d;
			} // if no trees, return 0

			double cum = 0;
			double Dg = 0;
			for (Iterator i = trees.iterator(); i.hasNext();) {
				Tree t = (Tree) i.next();
				double d = t.getDbh();
				cum += d * d;
			}
			if (trees.size() != 0) {
				Dg = Math.sqrt(cum / trees.size());
			}
			return Dg;
		} catch (Exception e) {
			Log.println(Log.ERROR, "SFMethodProvider.getDg ()", "Error while computing Dg", e);
			return -1d;
		}
	}

	/**
	 * Dominant diameter: Quadratic mean Dbh of the 100 trees / ha with bigger
	 * Dbh (cm).
	 */
	public double getDdom(GScene stand, Collection trees) {
		try {
			if (trees == null) {
				return -1d;
			}
			if (trees.isEmpty()) {
				return 0d;
			} // if no trees, return 0

			// Sorting is necessary
			Object[] trees2 = trees.toArray();
			Arrays.sort(trees2, new TreeDbhComparator(false)); // false: sort in
																// descending
																// order / true:
																// sort in
																// ascending
																// order

			double Ddom = 0;
			double cum = 0;
			int dominantTreeNb = (int) (stand.getArea() / 100);
			if (trees2.length < dominantTreeNb) {
				dominantTreeNb = trees2.length;
			}
			for (int k = 0; k < dominantTreeNb; k++) {
				Tree t = (Tree) trees2[k];
				double d = t.getDbh();
				cum += d * d;
			}
			if (dominantTreeNb > 0) {
				Ddom = Math.sqrt(cum / dominantTreeNb);
			}
			return Ddom;
		} catch (Exception e) {
			Log.println(Log.ERROR, "SFMethodProvider.getDdom ()", "Error while computing Ddom", e);
			return -1d;
		}
	}
}
