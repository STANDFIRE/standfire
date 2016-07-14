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

package capsis.lib.fire.intervener.fireeffect.mortalitymodel;

import capsis.lib.fire.fuelitem.FiSeverity;
import jeeb.lib.util.Translator;

/**
 * MichaletzAndJohnson: a mortality model
 * Not implemented completely
 * 
 * @author F. Pimont - september 2009
 */
public class MichaletzAndJohnsonMortality implements MortalityModel {
	
	/**
	 * Constructor.
	 */
	public MichaletzAndJohnsonMortality () {}

	/**
	 * Returns the mortality probability
	 * 
	 * @throws Exception
	 */
	public double getMortalityProbability(FiSeverity severity,
			String speciesName, double dbh // cm
	) throws Exception {
		double Nn = 1.0;// number of buds killed
		double Nt = 1.0;// number of buds
		// 
		if (severity.getCambiumIsKilled()) {
			return 1.0;
		} else {
			return (Nn / Nt);
		}	
	}

	public String getName() {
		return Translator
		.swap("Generic species based on bud and cambium kill (Michaletz & Johnson 2008)-NOT OPERATIONAL");
	}

	@Override
	public String toString () {return getName ();}

}












