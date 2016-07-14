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
import fireparadox.model.FmModel;
import fireparadox.model.plant.FmDendromTreeProperties;

/**
 * SpeciesDependentBLCDBH : a mortality model,
 * based on species, BLC and DBH
 * 
 * @author F. Pimont - september 2009
 */
public class SpeciesDependentCVSBLCDBH implements MortalityModel {
	
	
	/**	Constructor.
	*/
	public SpeciesDependentCVSBLCDBH () {}

	/**	Returns the mortality probability
	*/
	public double getMortalityProbability(FiSeverity severity,
			String speciesName,
			double dbh // cm
	) throws Exception {	
		double meanBLC = severity.getMeanBoleLengthCharred();
		if (dbh == -1) {
			throw new Exception(
					"FiEmpiricalFireEffect.mortality.SpeciesDependentBLCDBH (): Could not get dbh for species "
							+ speciesName);
		}
		// pinus pinaster is desactivated because there is a already model with
		// CVS with
		// it
		/*
		 * if (speciesName.equals(FiInitialParameters.PINUS_PINASTER)) { // Pimont 2006 double
		 * barkThickness = FiDendromStandProperties.computeBarkThickness(speciesName, dbh); //
		 * NB : always available for Pinus pinaster if dbh // defined double bf
		 * = Tools.computeBarkFactor(barkThickness); // bark factor if (meanBLC
		 * == -1) { throw new Exception(
		 * "FiEmpiricalFireEffect.mortality.SpeciesDependentBLCDBH (): Could not get bole length charred"
		 * ); } return Tools.logit(-0.773 - 0.0679 * meanBLC + 5.39 * bf); }
		 */
		if (speciesName.equals(FmModel.PINUS_NIGRA_LARICIO)) {// Pimont 2006
			double barkThickness = FmDendromTreeProperties
					.computeBarkThickness(speciesName,
					dbh); // NB : always available for Pinus pinaster if dbh
							// defined
			double bf = Tools.computeBarkFactor(barkThickness); // bark factor
			if (meanBLC == -1) {
				throw new Exception(
						"FiEmpiricalFireEffect.mortality.SpeciesDependentBLCDBH (): Could not get bole length charred");
			}
			return Tools.logit(-1.13 - 0.118 * meanBLC + 8.78 * bf);
		}
		if (speciesName.equals(FmModel.PINUS_SYLVESTRIS)) {// Sidoroff 2007
			double barkThickness = FmDendromTreeProperties
					.computeBarkThickness(speciesName,
					dbh);
			if (meanBLC == -1) {
				throw new Exception(
						"FiEmpiricalFireEffect.mortality.SpeciesDependentBLCDBH (): Could not get bole length charred");
			}
			if (barkThickness == -1) {
				return Tools.logit(-1.52 - 0.191 * meanBLC + 0.287 * dbh);
			} else {
				return Tools.logit(3.33 - 0.187 * meanBLC + 3.31
						* barkThickness);
			}
			

		}
		SpeciesDependentCVSDBH otherSpecies = new SpeciesDependentCVSDBH();
		return otherSpecies.getMortalityProbability(severity, speciesName, dbh);
	}

	public String getName() {
		return Translator
				.swap("Species dependant : Crown Volume Scorched or Bole Length Charred (Default)");
	}
	
	@Override
	public String toString () {return getName ();}
	
}