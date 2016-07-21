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

import capsis.extension.treelogger.geolog.RadialProfile;


/**	LongitProfile_FixWidth : longitudinal profile with constant width.
*
*	@author F. Mothe - marsh 2006
*/
public class LongitProfile_FixWidth extends LongitProfile {

	double width_mm;
	boolean inside;
	
	//	Constructor
	public LongitProfile_FixWidth (double width_mm, boolean inside) {
		this.width_mm = width_mm;
		this.inside = inside;
	}

	//	Abstract methods :

	// Cambial age of the outer ring inside limit :
	// (= number of rings outside + 1)
	// Returns 0 if profile is empty.
	public int getCambialAge (RadialProfile profile) {
		return getCambialAge (profile, width_mm, inside);
	}
	
	// Distance pith - limit :
	public double getRadius_mm (RadialProfile profile) {
		return getRadius_mm (profile, width_mm, inside);
	}
	
	//	Overridable  methods :

	// public double getRadius_mm (GTree [] history, TreeRadius_cmProvider radMp, double height_m) {}
	// public int getNbRings (RadialProfile profile, boolean inside) {}

}
