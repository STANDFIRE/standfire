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

package capsis.lib.fire.intervener.fireeffect.cambiumdamagemodel;

import jeeb.lib.util.Translator;
import fireparadox.model.plant.FmDendromTreeProperties;

/**
 * BovaAndDickinson : a cambium damage model
 * 
 * @author F. Pimont - september 2009
 */
public class BovaAndDickinson implements CambiumDamageModel {

	/**	Constructor.
	*/
	public BovaAndDickinson () {}

	/**
	 * Returns status of cambium
	 */
	public boolean isCambiumKilled(String speciesName, double dbh, // cm
			double fireIntensity, 		// kW/m
			double residenceTime) throws Exception { // s
		double barkThickness = FmDendromTreeProperties.computeBarkThickness(
				speciesName, dbh);
		if (barkThickness == -1) {
			throw new Exception(
					"FiEmpiricalFireEffect.cambiumDamage.BovaAndDickinson (): Could not get compute barkThickness  for a tree of species "
							+ speciesName + " and of dbh " + dbh);
		}
		double r = 0.21 * Math.pow(fireIntensity, 0.2)
				* Math.pow(residenceTime, 0.64);// r is in mm, fireintensity
												// (kW/m) residence time in (s)
		//System.out.println("barkThickness (cm)" + barkThickness + " rkill (cm)"
		//		+ r * 0.1);
		return (r * 0.1 >= barkThickness);

	}
	public String getName() {
		return Translator
				.swap("Residence Time & Fire Intensity Criteria  (Bova & Dickinson 2005) (Default)");
	}
	
	@Override
	public String toString () {return getName ();}
	
}












