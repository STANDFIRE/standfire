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

import pp3.model.Pp3ModelParameters;
import capsis.extension.treelogger.geolog.util.LongitProfile_FixRings;

/**	Pp3JuveWoodProfile : longitudinal profile of juvenile wood
*	for a Pp3 maritime pine
*
*	The missing years are linearly interpolated.
*	@author F. Mothe - april 2006
*/
public class Pp3JuveWoodProfile extends LongitProfile_FixRings {

	//	Constructor
	public Pp3JuveWoodProfile () {
		// Fixed number of juvenile wood (i.e. inside) rings :
		// TODO : something like : super (Pp3Annex.getNbJuvenileWoodRings (), true);
		super (Pp3ModelParameters.JUVENILE_WOOD_AGE, true);
	}

}
