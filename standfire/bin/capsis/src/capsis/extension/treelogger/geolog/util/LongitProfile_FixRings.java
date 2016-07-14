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


/**	LongitProfile_FixRings : longitudinal profile with constant 
*	number of rings.
*
*	@author F. Mothe - marsh 2006
*/
public class LongitProfile_FixRings extends LongitProfile {

	protected int nbRings;
	protected boolean inside;
	
	//	Constructor
	public LongitProfile_FixRings (int nbRings, boolean inside) {
		this.nbRings = nbRings;
		this.inside = inside;
	}

	//	Abstract methods :

	// Cambial age of the outer ring inside limit :
	// (= number of rings outside + 1)
	// Returns 0 if profile is empty.
	public int getCambialAge (RadialProfile profile) {
		return getCambialAge (profile, nbRings, inside);
	}
	
	// Distance pith - limit :
	public double getRadius_mm (RadialProfile profile) {
		return getRadius_mm (profile, nbRings, inside);
	}
	
	//	Overridable  methods :

	// public double getRadius_mm (GTree [] history, TreeRadius_cmProvider radMp, double height_m) {}
	// public int getNbRings (RadialProfile profile, boolean inside) {}

}
