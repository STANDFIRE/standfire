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

import lerfob.fagacees.model.FgTree;
import capsis.extension.treelogger.geolog.GeoLogTreeData;

/**	FgCrownExpansionFactor :
*	Crown expansion factor = volumeBFHoup / volumeBFTige_Houp, with :
*	- volumeBFHoup = total crown volume above FgUtil.DIAM_BF_cm (overbark)
*	  estimated through fagacees.model.FgVolume.getBoisFortVolume
*	- volumeBFTige_Houp = stem volume between crown base height and
*	  FgUtil.DIAM_BF_cm
*
*	@author F. Mothe - july 2006
*/
public class FgCrownExpansionFactor {

	private final static double MAX_FACTOR = 50.0;
	
	private final static double BREAST_HEIGHT_m = 1.30;

	// diameter > DIAM_BF_cm => commercial wood (= bois fort = BF)
	// diameter < DIAM_BF_cm => small wood (= petit bois = PB)
	private final static double DIAM_BF_cm = 7.0;


	// diameter > DIAM_BF_cm => commercial wood (= bois fort = BF)
	// diameter < DIAM_BF_cm => small wood (= petit bois = PB)

	// Compartments :
	// 	Stem :
	//	A (0.0, 0.3) = stump
	//	B (0.3, cbh) = bole above stump and below crown (diam > DIAM_BF_cm)
	//	C (cbh, hbf) = crown BF (diam > DIAM_BF_cm)
	//	D (hbf, htot) = small wood (diam < DIAM_BF_cm)
	// 	Stem + branches :
	//	De (hbf, htot) = total small wood (not computed :
	//			use volTotalBF_PV_m3-volTotal_PV_m3)
	//	Ce (cbh, hbf) = total crown BF = C * crownExpFactor

	// volumeBFTige_Grume = A + B
	// volumeBFTige_Houp = C
	// volumeBFTige = A + B + C
	// volumeBFTotal = A + B + Ce
	// volumeBFHoup = Ce
	// crownExpansionFactor = Ce / C

	public static double getCrownExpansionFactor (GeoLogTreeData td, double precisionLength_m, double precisionThickness_m) {
		// (set precisionLength_m and precisionThickness_m to 0. to use the default values)
		double heightBF_cm = td.getMaxHeight_m(DIAM_BF_cm, true, precisionLength_m);
		double volumeBFTige_Grume = td.getVolume_m3(0., td.getCrownBaseHeight(), true, precisionThickness_m);
		double volumeBFTige_Houp = td.getVolume_m3(td.getCrownBaseHeight(), heightBF_cm, true, precisionThickness_m);
		// System.out.println (" heightBF_cm=" + heightBF_cm + " crownBaseHeight_m=" + td.crownBaseHeight_m + " ht=" + ht);
		// System.out.println (" BFTiG="+ volumeBFTige_Grume*1e3 + " BFTiH="+ volumeBFTige_Houp*1e3);

		return getCrownExpansionFactor((FgTree) td.getTree(), volumeBFTige_Grume, volumeBFTige_Houp);
	}

	private static double getCrownExpansionFactor (FgTree tree, double volumeBFTige_Grume, double volumeBFTige_Houp) {
		double factor = 1.0;
		double ht = tree.getHeight ();
		final double h130 = BREAST_HEIGHT_m;
		if (ht > h130) {
//			double c130 = Math.PI * tree.getDbh ();

			// Old model by Jean-François Dhôte :
			// double volumeBFTotal =
			//		Math.max (0.563 + 0.375 * rho, 1.0) * volumeBFTige;

//			double volumeBFTotal = FgVolume.
//					getBoisFortVolume (c130, ht, FgUtil.getSpecies (tree));
			double volumeBFTotal = tree.getBranchAndStemMerchantableVolume();	// no expansion factor here otherwise a large tree could have a small volume

			/*
			// Modified 18.02.2008 : correction applied upstream in FgVolume

			// Note :	According to Patrick Vallet / Jean-François Dhote :
			//		volumeBFTotal given by Bouchon model is known
			//		to overestimate old trees whereas experimental
			//		data show that it should be around 95% of total
			//		volume for c130 > 120 cm
			i
			f (species.equals ("oak")) {
				double volTotal_PV_m3 = fagacees.model.FgVolume.
						getTotalAboveGroundVolume (c130, ht, species);
				volumeBFTotal = Math.min (
						volTotal_PV_m3 * .95, volumeBFTotal);
			}
			*/
			factor = getCrownExpansionFactor (volumeBFTotal, volumeBFTige_Grume, volumeBFTige_Houp);
		}
		return factor;
	}

	public static double getCrownExpansionFactor (double volumeBFTotal, double volumeBFTige_Grume, double volumeBFTige_Houp) {
		double volumeBFHoup = volumeBFTotal - volumeBFTige_Grume;
		double factor = (volumeBFTige_Houp > 0.0) ? volumeBFHoup / volumeBFTige_Houp : 1.0;
		if (factor > MAX_FACTOR) {
			factor = MAX_FACTOR;
		} else if (factor < 1.0) {
			factor = 1.0;
		}
		return factor;
	}
	
}
