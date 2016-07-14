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

/**	KnottyCoreProfile manages the longitudinal profile of
*	the knotty core radius for a given tree using tree history,
*	a TreeRadius_cmProvider and a TreeCrownBaseHeightProvider.
*
*	The missing years are linearly interpolated.
*	The knot core is supposed to follow the ring limit within
*	each growth unit.
*
*	@author F. Mothe - february 2006
*/

package capsis.extension.treelogger.geolog.util;

import capsis.defaulttype.Tree;
import capsis.util.methodprovider.TreeCrownBaseHeightProvider;
import capsis.util.methodprovider.TreeRadius_cmProvider;

public class KnottyCoreProfile extends LongitProfile_FromHistory {

	private TreeCrownBaseHeightProvider cbhMp;

	public KnottyCoreProfile (Tree[] history, TreeRadius_cmProvider radMp, TreeCrownBaseHeightProvider cbhMp) {
		// Two times construction since we need cbhMp for initialising :
		super ();
		this.cbhMp = cbhMp;
		initialise (history, radMp);
	}

	/**
	*	Interpolated crown base height for any age between 1 and tree age.
	*/
	public double getCrownBaseHeight_m (int age) {
		return getProfileHeight_m (age);
	}

	/**
	*	Abstract method of LongitProfile_FromHistory
	*/
	public double getProfileHeight_m (Tree t) {
		return cbhMp.getTreeCrownBaseHeight (t);	// (m)
	}

}
