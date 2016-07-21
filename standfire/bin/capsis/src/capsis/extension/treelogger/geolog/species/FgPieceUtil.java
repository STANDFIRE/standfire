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
import capsis.extension.treelogger.GPiece;
import capsis.extension.treelogger.GPieceDisc;
import capsis.extension.treelogger.geolog.util.PiecePropertyMeasurer;
import capsis.extension.treelogger.geolog.util.PieceUtil;

/**	FgPieceUtil : utility methods for GPiece, GPieceDisc, GPieceRing
*	using the Fagacées wood density model
*	All the methods are static
*
*	@author F. Mothe - marsh 2006
*/
public class FgPieceUtil extends PieceUtil {

	// TEMPO : applies the FgOak density model to all the species !!!

	// Mass per height unit (kg/m) :
	// cambialAgeMin and cambialAgeMax should be valid (wood, bark or pith)
	public static double getMass_kgpm (GPieceDisc disc, int cambialAgeMin,
			int cambialAgeMax)
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

	// Basic density of a cylinder defined by 2 rings (included) :
	public static double getWoodBasicDensity_kgpm3 (GPieceDisc disc, int cambialAgeMin,	int cambialAgeMax) {
		double R1 = disc.getIntRadius_mm(cambialAgeMax);
		double R2 = disc.getExtRadius_mm(cambialAgeMin);
		double surf_mm2 = Math.PI * (R2*R2 - R1*R1);
		double d = 0.;
		if (surf_mm2 > 0.) {
			double mass_kgpm = getMass_kgpm (disc, cambialAgeMin, cambialAgeMax);
			d = mass_kgpm / surf_mm2 * 1.e6;
		}
		return d;
	}

	// Basic density of a cylinder defined by 2 radius :
	// (result approximated to a cylinder defined by 2 ring limits)
	public static double getWoodBasicDensity_kgpm3 (GPieceDisc disc, double intRadius_mm,
			double extRadius_mm)
	{
		// CAUTION : argument in inverse order !
		return getWoodBasicDensity_kgpm3 (disc, disc.getCambialAge(extRadius_mm),
			disc.getCambialAge(intRadius_mm));
	}

	// Mean mass :
	public static class MeasurerMass_kg extends PiecePropertyMeasurer {
		public MeasurerMass_kg (GPiece piece) {
			super (piece);
		}
		public double getSurfacicMeasure (GPieceDisc disc,
				int cambialAgeMin, int cambialAgeMax) {
			return getMass_kgpm (disc, cambialAgeMin, cambialAgeMax);
		}
		public double getVolumicMeasure (double integral) {
			// kg/m*m = kg :
			return integral;
		}
	}

	public static double getMass_kg (GPiece piece,
			Compartment compartment) {
		return new MeasurerMass_kg (piece).getMeasure (compartment);
	}

}
