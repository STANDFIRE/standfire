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

/**
 * PetersonAndRyan : a mortality model
 * 
 * @author F. Pimont - september 2009
 */
public class PetersonAndRyanMortality implements MortalityModel {
	private double residenceTime;



	/**
	 * This model needs specific extra parameters. Call this set method before
	 * getMortalityProbability for each plant.
	 */
	public void set(double residenceTime) { // s
		this.residenceTime = residenceTime;
	}

	/**
	 * Constructor.
	 */
	public PetersonAndRyanMortality () {}

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
					"FiEmpiricalFireEffect.mortality.PetersonAndRyan (): Could not get compute barkThickness  for a tree of species "
							+ speciesName + " and of dbh " + dbh);
		}
		double cvk = severity.getCrownVolumeKilled();
		if (cvk == -1) {
			throw new Exception(
					"FiEmpiricalFireEffect.mortality.PetersonAndRyan (): Could not get compute crown Volume killed  for a tree of species "
							+ speciesName);
		} else {
			double tauc = 2.9 * barkThickness * barkThickness * 60.;// tauc in s
																	// here
			return Math.pow(cvk / 100.0, Math.max(0.0, tauc / residenceTime
					- 0.5));
		}
	}

	public String getName() {
		return Translator
		.swap("Generic species based on Crown killed and Bark Thickness (Peterson & Ryan 1986)");
	}

	@Override
	public String toString () {return getName ();}

}












