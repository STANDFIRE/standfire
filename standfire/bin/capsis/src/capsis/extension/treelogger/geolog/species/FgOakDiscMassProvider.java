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

import lerfob.fagacees.model.FgCarbonCalculator;
import capsis.extension.treelogger.GPieceDisc;
import capsis.util.methodprovider.DiscMassProvider;

/**	FgOakDiscMassProvider : DiscMassProvider for Fagacees oak
*
*	@author F. Mothe - august 2008
*/
public class FgOakDiscMassProvider
		 implements DiscMassProvider
{
	private static final long serialVersionUID = 20080901L;	// avoid java warning

	// TEMPO : applies the FgOak density model to all the species !!!
	// Mass per height unit (kg/m) :
	// cambialAgeMin and cambialAgeMax should be valid (wood, bark or pith)
	public double getDiscMass_kgpm (GPieceDisc disc,
		int cambialAgeMin, int cambialAgeMax)
	{
		double sum = 0.0;
		double R2 = disc.getExtRadius_mm(cambialAgeMin);
		for (int cambialAge = cambialAgeMin; cambialAge <= cambialAgeMax; cambialAge++) {
			double R1 = disc.getIntRadius_mm(cambialAge);
			double surf_mm2 = Math.PI * (R2*R2 - R1*R1);
			double den_kgpm3;
			if (cambialAge == 0) {
				// Bark :
				den_kgpm3 = FgCarbonCalculator.getOakBarkBasicDensity_kgpm3();
			} else {
				int pithAge = disc.getAgeFromPith(cambialAge);
				if (pithAge <= 0) {
					// Pith : no density model (and very low surface) :
					den_kgpm3 = 0.0;
				} else {
					// Wood :
					double rw_mm = R2 - R1;
					boolean isHeartwood = R2 < disc.getHeartWoodRadius_mm();
					den_kgpm3 = FgCarbonCalculator.getOakWoodBasicDensity_kgpm3 (
							pithAge, rw_mm, isHeartwood);
				}
			}
			sum += den_kgpm3 * surf_mm2;	// (kg.mm2/m3)
			R2 = R1;
		}
		return sum / 1.e6;	// kg/m
	}

}
