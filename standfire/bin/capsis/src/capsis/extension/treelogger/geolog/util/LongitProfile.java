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

import capsis.extension.treelogger.geolog.GeoLogTreeData;
import capsis.extension.treelogger.geolog.RadialProfile;

/**	
 * LongitProfile is an abstract class that handles the longitudinal
 * profile of radial distance to pith for a tree property
 * (e.g. heartwood, juvenile wood, knotty core...)
 * using tree history and a TreeRadius_cmProvider. <br>
 * @author F. Mothe - March 2006
 * @author Mathieu Fortin - December 2011 (refactoring)
 */
public abstract class LongitProfile {

	//	Abstract methods :

	// Cambial age of the outer ring inside limit :
	// (= number of rings outside + 1)
	// Returns 0 if profile is empty.
	public abstract int getCambialAge(RadialProfile profile);

	// Distance pith - limit :
	public abstract double getRadius_mm(RadialProfile profile);

	//	Overridable  methods :

//	public double getRadius_mm(Tree[] history, TreeRadius_cmProvider radMp, double height_m) {
	public double getRadius_mm(GeoLogTreeData treeData, double height_m) {
//		RadialProfile profile = new RadialProfile(history, height_m, radMp);
		RadialProfile profile = treeData.getRadialProfileAtThisHeight(height_m);
		return getRadius_mm(profile);
	}

	public int getNbRings(RadialProfile profile, boolean inside) {
		int nbRings = 0;
		int age = getCambialAge(profile);
		if (age > 0) {
			nbRings = age - 1;
			if (inside) {
				nbRings = profile.getNbWoodRings() - nbRings;
			}
		}
		return nbRings;
	}

	// Overrided in LongitProfile_Null only :
	public boolean isNull () {
		return false;
	}

	//	Utilitary static methods :

	// Cambial age of the outer ring inside limit :
	// (= number of rings outside + 1)
	// Returns 0 if profile is empty.
	protected static int getCambialAge (RadialProfile profile,
			int nbRings, boolean inside) {
		int age = 0;
		int nbWoodRings = profile.getNbWoodRings ();
		if (nbWoodRings > 0) {
			age = 1 + (inside
				? Math.max (nbWoodRings - nbRings, 0)
				: Math.min (nbWoodRings, nbRings)
			);
		}
		return age;
	}

	protected static int getCambialAge (RadialProfile profile,
			double width_mm, boolean inside) {
		int age = 0;
		int nbWoodRings = profile.getNbWoodRings ();
		if (nbWoodRings > 0) {
			age = nbWoodRings + 1;	// pith
			double totalRadius_mm = profile.getUnderBarkRadius_mm ();
			double outsideWidth =
					inside ? totalRadius_mm - width_mm : width_mm;
			if (totalRadius_mm>outsideWidth) {
				for (int a=1; a<=nbWoodRings; a++) {
					double w = totalRadius_mm - profile.getRadius_mm (a);
					if (w>=outsideWidth) {
						age = a;
						break;
					}
				}
			}
		}
		return age;
	}

	protected static double getRadius_mm (RadialProfile profile,
			int nbRings, boolean inside) {
		int age = getCambialAge (profile, nbRings, inside);
		return age > 0 ? profile.getExtRadius_mm (age) : 0.0;
	}

	protected static double getRadius_mm (RadialProfile profile,
			double width_mm, boolean inside) {
		int age = getCambialAge (profile, width_mm, inside);
		return age > 0 ? profile.getExtRadius_mm (age) : 0.0;
	}

}
