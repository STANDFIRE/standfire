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
import fireparadox.model.plant.FmDendromTreeProperties;


/**	GenericCVSDBH : a mortality model,
 * based on Crown Volume Scorched and DBH
*	@author F. Pimont - september 2009
*/
public class GenericCVSDBH implements MortalityModel {

	/**	Constructor.
	*/
	public GenericCVSDBH () {}

	/**
	 * Returns the mortality probability
	 * 
	 * @throws Exception
	 */
	public double getMortalityProbability(FiSeverity severity,
			String speciesName, double dbh // cm
	) throws Exception {
		double barkThickness = FmDendromTreeProperties.computeBarkThickness(
				speciesName, dbh);
		if (barkThickness == -1) {
			throw new Exception(
					"FiEmpiricalFireEffect.mortality.GenericCVSDBH (): Could not get compute barkThickness  for a tree of species "
							+ speciesName + " and of dbh " + dbh);
		}
		double cvs = severity.getCrownVolumeScorched();
		double bf = Tools.computeBarkFactor(0.394 * barkThickness); // bark
																	// factor
		// System.out.println("cvs " + cvs + "barkThickness " + barkThickness
		// + " bf " + bf + " res "
		// + (-1.94 - 0.000535 * cvs * cvs + 6.32 * bf));
		return Tools.logit(-1.94 - 0.000535 * cvs * cvs + 6.32 * bf);
	}
	public String getName() {
		return Translator
				.swap("Generic species : Crown scorched and Bark Thickness (Ryan & Amman 1994)");
	}
	
	@Override
	public String toString () {return getName ();}
	
}












