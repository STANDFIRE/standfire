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

package capsis.extension.treelogger.geolog.species;

import lerfob.fagacees.model.FgStemProfileCalculator;
import capsis.defaulttype.Tree;
import capsis.extension.treelogger.geolog.GeoLogTreeData;
import capsis.extension.treelogger.geolog.util.LongitProfile_FixRings;

/**	FgOakHeartWoodProfile : longitudinal profile of heartwood radius
*	for a Fagacées oak
*
*	The missing years are linearly interpolated.
*	@author F. Mothe - marsh 2006
*/
public class FgOakHeartWoodProfile extends LongitProfile_FixRings {

	private GeoLogTreeData treeData;
	
	//	Constructor
//	public FgOakHeartWoodProfile (Tree [] history) {
	public FgOakHeartWoodProfile (GeoLogTreeData treeData) {
		// Fixed number of sapwood (i.e. outside) rings :
		super (FgStemProfileCalculator.getOakNbSapwoodRings(treeData), false);
	}
	
}
