/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2003  Francois de Coligny
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package capsis.extension.treelogger.geolog;

import java.util.Vector;

import capsis.defaulttype.Tree;
import capsis.util.methodprovider.TreeRadius_cmProvider;

/**	
 * RadialProfile computes and manages a list of annual ring radius
 * (including pith and bark) for a given tree and height, by using
 * a TreeRadius_cmProvider.<br>
 *<br>
 * For performance reason the constructor takes a yet calculated
 * tree history as parameter.<br>
 *<br>
 * The static function getNbWoodRings (treeHistory, height_m) may
 * be used to get the number of rings without computing the profile.
 *
 * @author F. Mothe - january 2006
 */
public class RadialProfile {

	//	Note about ring age in RadialeProfile :
	//	- cambialAge is counted from bark to pith :
	//		age = 0 for bark, getNbWoodRings ()+1 for pith
	//		age = 1 for the most external wood ring (after bark)
	//		age = getNbWoodRings () for the most inner ring (before pith)
	//	- pithAge is counted from pith to bark :
	//		age = 0 for pith, getNbWoodRings ()+1 for bark
	//		age = 1 for the most inner ring (after pith)
	//		age = getNbWoodRings () for the most external wood ring (before bark)

	//	There are no error management : an exception may occur
	//	for invalid ages.

	private double height_m;
	private Vector<Double> rads;

	//	Constructor
//	protected RadialProfile(Tree[] history, double height_m, TreeRadius_cmProvider mp) {
	protected RadialProfile(GeoLogTreeData treeData, double height_m) {
		this.height_m = height_m;
		this.rads = getRadiusHistory_mm (treeData.getTreeHistory(), height_m, treeData.getTreeRadiusMethodProvider());
	}

	public int getNbWoodRings () {
		// first ring = bark, last ring = pith,
		return rads.size () - 2;
	}

	public int getNbRings () {
		return rads.size ();
	}

	public Vector < Double > getRadiusHistory_mm () {
		// size = getNbWoodRings () + 2
		return rads;
	}

	// Return the external radius of the ring :
	public double getRadius_mm (int cambialAge) {
		// The radius are sorted by cambial age in rads :
		/*
		if (cambialAge<0)
			cambialAge=0;
		else if (cambialAge>=rads.size ())
			cambialAge=rads.size ()-1;
		*/
		return rads.get (cambialAge);
	}

	public double getExtRadius_mm (int cambialAge) {
		return getRadius_mm (cambialAge);
	}

	public double getIntRadius_mm (int cambialAge) {
		// Returns 0 for pith :
		return (cambialAge+1 >= rads.size ()) ?
				0. : getRadius_mm (cambialAge+1);
	}

	public double getRingWidth_mm (int cambialAge) {
		// Works also for pith and bark.
		// = getWidth_mm (cambialAge, cambialAge)
		return getExtRadius_mm (cambialAge) - getIntRadius_mm (cambialAge);
	}

	// Return the width of the segment [cambialAgeMin, cambialAgeMax]
	// (bounds included) :
	public double getWidth_mm (int cambialAgeMin, int cambialAgeMax) {
		return getExtRadius_mm (cambialAgeMin) -
				getIntRadius_mm (cambialAgeMax);
	}

	// Return the mean ring width of the segment [cambialAgeMin, cambialAgeMax]
	// (bounds included) :
	public double getRingWidth_mm (int cambialAgeMin, int cambialAgeMax) {
		int nbRings = cambialAgeMax - cambialAgeMin + 1;
		return nbRings>0 ? getWidth_mm (cambialAgeMin, cambialAgeMax) / nbRings : 0.0;
	}

	// Return the mean wood ring width :
	public double getRingWidth_mm () {
		return getRingWidth_mm (1, getNbWoodRings ());
	}

	/*
	// Return the cambial age of the last ring of history [step] :
	public static int getCambialAge (GTree [] history, int step) {
		int stepAge = history [step].getAge ();
		int finalAge = history [history.length-1].getAge ();
		int cambialAge = finalAge-stepAge+1;
		return cambialAge;
	}
	*/

	public int getCambialAge (int pithAge) {
		// = NbWoodRings () - pithAge + 1
		return rads.size () - pithAge - 1;
	}

	public int getPithAge (int cambialAge) {
		// = NbWoodRings () - cambialAge + 1
		return rads.size () - cambialAge - 1;
	}

	/*
	public double getRadius_mm (int age, boolean isCambialAge) {
		int n = isCambialAge ? age : getCambialAge(age);
		return rads.get (n);
	}

	public double getRingWidth_mm (int age, boolean isCambialAge) {
		int n = isCambialAge ? age : getCambialAge(age);
		return getRingWidth_mm (n);
	}
	*/

	public double getPithRadius_mm () {
		return rads.get (rads.size () - 1);
	}

	public double getOverBarkRadius_mm () {
		return rads.get (0);
	}

	public double getUnderBarkRadius_mm () {
		return rads.get (1);
	}

	public double getPithDiameter_mm () {
		return getPithRadius_mm() * 2;
	}

	public double getBarkWidth_mm () {
		return getRingWidth_mm (0);
	}

	public double getHeight_m () {
		return height_m;
	}

	//	Static methods called by the non static methods

	/*
	//	Return a vector of ring widths (sorted from bark to pith) at height_m.
	//	(first ring is bark, last ring (=0) is pith)
	//
	public static Vector < Double > getRingWidths_mm (GTree [] treeHistory,
			double height_m, TreeRadius_cmProvider mp) {
		Vector < Double > ringWidths =  getRadiusHistory_mm (
				treeHistory, height_m, mp);
		int nbRings = ringWidths.size ();
		for (int r = 0; r<nbRings-1; r++) {	// r==nbRings-1 excluded
			ringWidths.set (r, ringWidths.get (r) - ringWidths.get (r+1));
		}
		return ringWidths;
	}
	*/

	
	/**
	 * This method returns a vector of radius sorted from bark to pith at a given height. It returns a vector with two 0 values if
	 * the height is out of the tree. The first element of the vector is the radius over bark. The last element is the radius at the pith,
	 * i.e. 0. All the elements in between are the wood rings. NOTE: Missing rings are interpolated.
	 * @param treeHistory an array of Tree instances that corresponds to the tree history
	 * @param height_m the height at which the radius history is to be calculated (m)
	 * @param mp a TreeRadius_cmProvider instance
	 * @return a Vector of radius (mm)
	 */
	private Vector<Double> getRadiusHistory_mm (Tree[] treeHistory, double height_m, TreeRadius_cmProvider mp) {
		// 27/05/2009 : modified to compute pith and bark in all cases

		int nbSteps = treeHistory.length;
		Vector<Double> rads = new Vector<Double>();
		
		// Search for the first step where the tree reaches height_m.
		int firstStep = nbSteps ;
		for (int s = 0; s < nbSteps; s++) {
			if (treeHistory[s].getHeight() > height_m) {
				firstStep = s;
				break;
			}
		}

		if (firstStep < nbSteps) {
			Tree tree = treeHistory[nbSteps-1];

			// First ring = radius over bark :
			rads.add(mp.getTreeRadius_cm(tree, height_m, true) * 10);	// over bark  * 10 : to shift from cm to mm
			// Following rings until firstStep
			// (assuming constant rw from s to s+1) :
			double rad2 = mp.getTreeRadius_cm(tree, height_m, false) * 10;	// under bark;
			int age2 = tree.getAge();

			// 04/06/2009 : removed interpolation of inner rings for invalid profiles
			// (now returns ring width = 0 for interlaced rings)

			for (int s = nbSteps-2; s >= firstStep; s--) {	// s==nbSteps-1 excluded
				double rad1 = mp.getTreeRadius_cm (treeHistory [s], height_m, false) * 10;	// under bark

				// 17.10.2008 : test for invalid stem profiles (ring crossing) :
				if (rad1 > rad2) {
					rad1 = rad2;
				}

				int age1 = treeHistory[s].getAge ();
				double width =  rad2 - rad1;
				int nbRings =  age2 - age1;
				double rw = width / nbRings ;
				for (int r = 0; r<nbRings; r++) {
					rads.add (rad2 - r*rw);
				}
				rad2 = rad1;
				age2 = age1;
			}

			// Inner rings from firstStep to pith assuming that
			// radial growth is proportional to height growth
			// (rw is constant except for the most inner incomplete ring).

			// Height of the tree at firstStep (age = age2, radius = rad2) :
			double h2 = treeHistory[firstStep].getHeight ();

			// Age and height of the tree at firstStep-1 (radius = 0) :
			int age1;
			double h1;
			if (firstStep == 0) {
				age1 = 0;
				h1 = 0;
			} else {
				age1 = treeHistory[firstStep-1].getAge ();
				h1 = treeHistory[firstStep-1].getHeight ();
			}

			if (h2 <= h1) {
				System.out.println ("ERROR : no height growth for tree " + tree.getId ()
						+ " during step " + firstStep
						+ " (age=" + treeHistory [firstStep].getAge () + ") !");
				// h1 = h2 / age2 * age1;
			}

			// Search for the number of rings from age2 to pith
			// with and without the 1st incomplete ring :
			double nbRingsToPith = (h2-height_m) / (h2-h1) * (age2-age1) ;
			int nbCompleteRings = (int) nbRingsToPith;
			int nbWoodRings = (int) Math.ceil (nbRingsToPith);

			if (nbRingsToPith <= 0.) {
				// No ring to add for firstStep
				// (should not occur because h2 > height_m)
			} else {
				// Completion of rads for the inner rings :

				// rw of the first incomplete ring :
				double rwFirstIncompleteRing = rad2;
				if (nbCompleteRings > 0) {
					// rw of the complete rings :
					double rw = rad2 / nbRingsToPith;
					for (int r = 0; r<nbCompleteRings; r++) {
						rads.add (rad2 - r*rw);
					}
					rwFirstIncompleteRing -= nbCompleteRings * rw;
				}
				if (nbWoodRings > nbCompleteRings) {
					// There is one more incomplete ring :
					rads.add (rwFirstIncompleteRing);
				}
			}

		} else {
			// outside tree :
			rads.add (0.);	// bark
		}

		// Last ring = pith (pith width is supposed to be null)
		rads.add (0.);

		return rads;
	}

	// Static method computing the number of wood rings at height_m
	// (using the same algo that getRadiusHistory_mm)
	// It may be used in place of RadialProfile.getNbWoodRings ()
	// when the profile would serve only to calculate the number of rings.
	public static int getNbWoodRings (Tree [] treeHistory, double height_m) {
		int nbSteps = treeHistory.length;

		// Search for the first step where the tree reaches height_m.
		int firstStep = nbSteps ;
		for (int s = 0; s<nbSteps; s++) {
			if (treeHistory [s].getHeight () > height_m) {
				firstStep = s;
				break;
			}
		}

		int nbWoodRings = 0;

		if (firstStep < nbSteps) {

			Tree tree = treeHistory [firstStep];

			// Age and height of the tree at firstStep :
			int age2 = tree.getAge ();
			double h2 = tree.getHeight ();

			// Age and height of the tree at firstStep-1 :
			int age1;
			double h1;
			if (firstStep==0) {
				age1 = 0;
				h1 = 0;
			} else {
				age1 = treeHistory [firstStep-1].getAge ();
				h1 = treeHistory [firstStep-1].getHeight ();
			}

			if (h2 <= h1) {
				System.out.println ("ERROR : no height growth for tree " + tree.getId() +
						" during step " + firstStep + " !");
			}

			double nbRingsToPith = (h2-height_m) / (h2-h1) * (age2-age1) ;
			if (nbRingsToPith > 0.) {
				nbWoodRings = (int) Math.ceil (nbRingsToPith);
			}
		}

		return nbWoodRings;
	}

}
