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

package capsis.lib.fire.intervener.fireeffect.crowndamagemodel;

import jeeb.lib.util.Translator;

/**
 * NoCrownDamageComputation
 * 
 * @author F. Pimont - september 2009
 */
public class NoCrownDamageComputation implements CrownDamageModel {

	/**	Constructor.
	*/
	public NoCrownDamageComputation () {}

	/**	Returns the damage height in the crown (m). 
	*	The height is relative to the ground level.
	*/
	public double getDamageHeight(double thresholdTemperature, // threshold
																// temperature
																// for the
			// damage �C
			double fireIntensity, 		// kW/m
			double residenceTime,		// s
			double ambiantTemperature, // �C
			double windVelocity, String damageType) {		// m/s

		return 0.0;
	}
	
	public String getName() {
		return Translator.swap("-----------------------");
	}
	
	@Override
	public String toString () {return getName ();}
	
}












