/* 
 * The Standfire model.
 *
 * Copyright (C) September 2013: F. Pimont (INRA URFM).
 * 
 * This file is part of the Standfire model and is NOT free software.
 * It is the property of its authors and must not be copied without their 
 * permission. 
 * It can be shared by the modellers of the Capsis co-development community 
 * in agreement with the Capsis charter (http://capsis.cirad.fr/capsis/charter).
 * See the license.txt file in the Capsis installation directory 
 * for further information about licenses in Capsis.
 */

package standfire.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import jeeb.lib.util.Log;
import capsis.defaulttype.Tree;
import capsis.kernel.GScene;
import capsis.util.TreeDbhComparator;
import capsis.util.methodprovider.DdomProvider;
import capsis.util.methodprovider.DgProvider;


/**	The method provider for Standfire.
 *	Contains calculation methods that can be detected by external 
 *	tools. Some extensions may evaluate their compatibility
 *	with Standfire by requesting this object.
 *
 * 	@author F. Pimont - September 2013
 */
public class SFMethodProvider implements DgProvider, DdomProvider {

	/**	Dg: Diameter of the mean tree (cm) : sqrt (Sum(d^2) / n).
	 */
	public double getDg (GScene stand, Collection trees) {
		try {
			if (trees == null) {return -1d;}
			if (trees.isEmpty ()) {return 0d;}  // if no trees, return 0
			
			double cum = 0;
			double Dg = 0;
			for (Iterator i = trees.iterator (); i.hasNext ();) {
				Tree t = (Tree) i.next ();
				double d = t.getDbh ();
				cum += d * d;
			}
			if (trees.size () != 0) {
				Dg = Math.sqrt (cum / trees.size ());
			}
			return Dg;
		} catch (Exception e) {
			Log.println (Log.ERROR, "SFMethodProvider.getDg ()", 
					"Error while computing Dg", e);
			return -1d;
		}
	}


	/**	Dominant diameter: Quadratic mean Dbh of the 100 trees / ha with bigger Dbh (cm).
	 */
	public double getDdom (GScene stand, Collection trees) {
		try {
			if (trees == null) {return -1d;}
			if (trees.isEmpty ()) {return 0d;}  // if no trees, return 0
			
			// Sorting is necessary
			Object[] trees2 = trees.toArray ();
			Arrays.sort (trees2, new TreeDbhComparator (false));  // false: sort in descending order / true: sort in ascending order
			
			double Ddom = 0;
			double cum = 0;
			int dominantTreeNb = (int) (stand.getArea () / 100);
			if (trees2.length < dominantTreeNb) {
				dominantTreeNb = trees2.length;
			}
			for (int k = 0; k < dominantTreeNb; k++) {
				Tree t = (Tree) trees2[k];
				double d = t.getDbh ();
				cum += d * d;
			}
			if (dominantTreeNb > 0) {
				Ddom = Math.sqrt (cum / dominantTreeNb);
			}
			return Ddom;
		} catch (Exception e) {
			Log.println (Log.ERROR, "SFMethodProvider.getDdom ()", 
					"Error while computing Ddom", e);
			return -1d;
		}
	}


}
