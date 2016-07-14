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

import capsis.defaulttype.Tree;
import capsis.extension.treelogger.geolog.GeoLogTreeData;

/**	LongitProfile_Null : longitudinal profile with no limit.
*
*	@author F. Mothe - marsh 2006
*/
public class LongitProfile_Null extends LongitProfile_FixRings {

	//	Constructors

	// Gives a profile with radius = (noRingInside ? 0 : underBarkRadius) :
	public LongitProfile_Null (boolean noRingInside) {
		super (0, noRingInside);
	}

	// Gives a profile with radius = 0 :
	public LongitProfile_Null () {
		super (0, true);
	}

	//	Abstract methods :

	//	Overridable  methods :

	@Override
//	public double getRadius_mm (Tree [] history,
//			TreeRadius_cmProvider radMp, double height_m) {
	public double getRadius_mm (GeoLogTreeData treeData, double height_m) {
		double r = 0.0;
		Tree[] history = treeData.getTreeHistory();
		if (!inside && history.length>0) {
			Tree refTree = history [history.length - 1];
//			r = radMp.getTreeRadius_cm(refTree, height_m, false) * 10;	// under bark
			r = treeData.getTreeRadiusMethodProvider().getTreeRadius_cm(refTree, height_m, false) * 10; // underbark;
		}
		return r;
	}

	@Override
	public boolean isNull () {
		return true;
	}

	// public int getNbRings (RadialProfile profile, boolean inside) {}

}
