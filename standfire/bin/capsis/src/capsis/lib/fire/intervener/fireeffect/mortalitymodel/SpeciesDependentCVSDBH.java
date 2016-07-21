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
 * SpeciesDependentCVSDBH : a mortality model,
 * based on species, CVS and DBH
 * 
 * @author F. Pimont - september 2009
 */
public class SpeciesDependentCVSDBH implements MortalityModel {
	
	
	/**	Constructor.
	*/
	public SpeciesDependentCVSDBH () {}

	/**	Returns the mortality probability
	*/
	public double getMortalityProbability(FiSeverity severity,
			String speciesName,
			double dbh // cm
	) throws Exception {	
		double cvs = severity.getCrownVolumeScorched();
		if (cvs == -1) {
			throw new Exception(
					"FiEmpiricalFireEffect.mortality.SpeciesDependentCVSDBH (): Could not get crown volume scorched");
		}
		
		if (speciesName.equals(FmModel.PINUS_HALEPENSIS)
				|| speciesName.equals(FmModel.PINUS_BRUTIA)) {
			double barkThickness = FmDendromTreeProperties
					.computeBarkThickness(speciesName,
					dbh);
			if (barkThickness == -1) { // ph1 in Rigolot et al 2004
				return Tools.logit(1.7539 - 0.0385 * cvs);
			} else { // derived from ph3 in Rigolot
				// et al 2004
				double bf = Tools.computeBarkFactor(barkThickness); // bark
																	// factor
				// derived from
				// dbh
				return Tools.logit(-2.0110 - 0.0004 * cvs * cvs + 4.169 * bf);
			}
			// (model.equals("CVS-DBH-maxBCN")) { // derived from ph4 in
			// Rigolot et al
			// 2004
			// double mBCN = damage.getMaxBarkCharNote();
			// return logit(2.3224 - 0.00038 * cvs*cvs + 0.1119 * dbh - 1.6490
			// * mBCN);
		}
		if (speciesName.equals(FmModel.PINUS_PINEA)) {
			// pp1 in Rigolot et al 2004
			return Tools.logit(23.0058 - 0.2529 * cvs);
			// (model.equals("CVS-meanBLC")) { // derived from pp2 in
			// Rigolot et al 2004
			// double mBLC = damage.getMeanBoleLengthCharred();
			// return logit(23.97 - 0.2363 * cvs - 0.0547 * mBLC);
			// (model.equals("CVS-meanBCN")) { // derived from pp3 in
			// Rigolot et al 2004
			// double mBCN = damage.getMeanBarkCharNote();
			// return logit(33.0865 - 0.3125 + 0.1119 * cvs - 1.9415 * mBCN);
		}
		if (speciesName.equals(FmModel.PINUS_PINASTER)) { // Botelho et al 1998
			if (dbh == -1) {
				throw new Exception(
						"FiEmpiricalFireEffect.mortality.SpeciesDependentCVSDBH (): Could not get dbh  for a tree of species "
								+ speciesName);
			} else {
				return Tools.logit(7.390 - 0.101 * cvs + 0.00381 * dbh);
			}
		} 
		GenericCVSDBH otherSpecies = new GenericCVSDBH();
		return otherSpecies.getMortalityProbability(severity, speciesName, dbh);
	}

	public String getName() {
		return Translator
				.swap("Species dependant : Crown Volume Scorched only");
	}
	
	@Override
	public String toString () {return getName ();}
	
}