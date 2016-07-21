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

package capsis.extension.treelogger.geolog.util;

import java.util.Vector;

import capsis.defaulttype.Tree;
import capsis.extension.treelogger.geolog.RadialProfile;
import capsis.util.methodprovider.TreeRadius_cmProvider;

/**	LongitProfile_FromHistory : longitudinal profile (hi, ri) where
*	- i is in the range [1, age of the tree]
*	- hi is a height computed by getProfileHeight_m at age i
*	- ri is the tree diameter at height hi
*	The missing years are linearly interpolated.
*
*	(e.g. : returns the knotty core profile if getProfileHeight_m returns the crown base height)
*
*	@author F. Mothe - august 2011
*/

public abstract class LongitProfile_FromHistory extends LongitProfile {

	private  Vector < Double > heights;	// m
	private  Vector < Double > profileRads;	// mm

	//	Constructors

	/**
	*	Takes a yet calculated tree history for performance reason.
	*/
	public LongitProfile_FromHistory (Tree [] history, TreeRadius_cmProvider radMp)
	{
		initialise (history, radMp);
	}

	/**
	*	For two times construction
	*/
	protected LongitProfile_FromHistory ()
	{
		// initialise should be call later
	}

	/**
	*	For two times construction
	*/
	protected void initialise (Tree [] history, TreeRadius_cmProvider radMp)
	{
		makeProfileHistory (history, radMp);
	}

	//	Abstract methods of LongitProfile :

	/**
	*	Cambial age of the outer ring inside limit (i.e. number of rings outside + 1).
	*	Returns 0 if profile is empty.
	*/
	public int getCambialAge (RadialProfile profile) {
		double r = getRadius_mm (profile);
		return getCambialAge (profile, r, true);
	}

	/**
	*	Distance pith - limit.
	*/
	public double getRadius_mm (RadialProfile profile) {
		return getInterpolatedProfileRadius_mm (profile);
	}

	/**
	*	Interpolated profile height for any age between 1 and tree age.
	*/
	public double getProfileHeight_m (int age) {
		return (heights.size () > age-1) ? heights.get (age-1) : 0.0;
	}

	/**
	*	Interpolated profile radius for any age between 1 and tree age.
	*/
	public double getProfileRadius_mm (int age) {
		return (heights.size () > age-1) ? profileRads.get (age-1) : 0.0;
	}

	/**
	*	Abstract method of LongitProfile_FromHistory
	*/
	public abstract double getProfileHeight_m (Tree t);


	//	Overridable methods of LongitProfile :

	/**
	*	Returns the profile radius.
	*	(overrides the method of LongitProfile)
	*/
	public double getRadius_mm (Tree [] history,
			TreeRadius_cmProvider radMp, double height_m)
	{
		double profileRad = 0.;
		int nbSteps = history.length;
		if (nbSteps > 0.) {
			Tree refTree = history [nbSteps - 1];
			double totalRadius_mm =
					10. * radMp.getTreeRadius_cm (refTree, height_m, false);	// under bark
			profileRad = getInterpolatedProfileRadius_mm (height_m, totalRadius_mm);
		}
		return profileRad;
	}

	// public int getNbRings (RadialProfile profile, boolean inside);

	//	Local methods :

	/**
	*	Returns the ratio between profile radius and under bark
	*	radius at height_m.
	*/
	public double getProfileRatio (RadialProfile profile) {
		double ratio = 0;
		double totalRadius = profile.getUnderBarkRadius_mm ();
		if (totalRadius > 0.) {
			double profileRad = getRadius_mm (profile);
			ratio = profileRad / profile.getUnderBarkRadius_mm ();
		}
		return ratio;
	}

	//	Returns the radius for profile :
	private double getInterpolatedProfileRadius_mm (RadialProfile profile) {
		double profileRad = 0.;	// out of tree
		return getInterpolatedProfileRadius_mm (profile.getHeight_m (),
				profile.getUnderBarkRadius_mm ());
	}

	//	Returns the profile radius at height_m :
	private double getInterpolatedProfileRadius_mm (double height_m, double totalRadius_mm)
	{
		double profileRad = totalRadius_mm;	// out of tree (totalRadius should be 0.) or inside crown
		int nbAges = heights.size ();
		if (nbAges > 0 && height_m >= 0. && height_m <= heights.get (nbAges - 1)) {
			// TODO : optimise !
			int age = nbAges - 1;
			for (int a = 0; a < nbAges - 1; a++) {
				if (height_m < heights.get (a)) {
					age = a;
					break;
				}
			}
			double h1 = (age == 0) ? 0.0 : heights.get (age-1);
			double r1 = (age == 0) ? 0.0 : profileRads.get (age-1);;
			double h2 = heights.get (age);
			double r2 = profileRads.get (age);
			profileRad = ( r2 * (height_m - h1) + r1 * (h2 - height_m) ) / (h2 - h1);

			// Should not be needed but may be necessary if some rings were interlaced :
			profileRad = Math.min (profileRad, totalRadius_mm);
		}
		return profileRad;
	}

	//	Complete heights with profile radiuses.
	//	The missing heights are interpolated.
	//
	private void makeProfileHistory (Tree [] history,
			TreeRadius_cmProvider radMp)
	{

		int nbSteps = history.length;
		Tree refTree = history [nbSteps - 1];
		heights = new Vector < Double > (refTree.getAge ());	// Reserve memory only
		profileRads = new Vector < Double > (refTree.getAge ());

		double r1 = 0;
		double h1 = 0;
		int age1 = 0;
		for (int s = 0; s<nbSteps; s++) {
			double h2 = getProfileHeight_m (history [s]);
			double r2 = 10. * radMp.getTreeRadius_cm (history [s], h2, false);	// under bark (mm)
			int age2 = history [s].getAge ();
			for (int a = age1+1; a <= age2; a++) {	// without age 0
			//for (int a = age1; a < age2; a++) {		// with age 0
				double h = (h1 * (age2 - a) + h2 * (a - age1)) / (age2 - age1);
				double profileRad = (r1 * (age2 - a) + r2 * (a - age1)) / (age2 - age1);
				heights.add (h);
				profileRads.add (profileRad);
			}
			r1 = r2;
			h1 = h2;
			age1 = age2;
		}

	}

}
