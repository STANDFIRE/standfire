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

package capsis.extension.treelogger.geolog.logcategories;

import jeeb.lib.util.Translator;
import lerfob.fagacees.FagaceesSpeciesProvider.FgSpecies;
import repicea.simulation.treelogger.WoodPiece;
import capsis.extension.treelogger.GPiece;

/**	FgBeech "slicing" product.
*	For sliced veneers production.
*	@author F. Mothe - January 2006
*/
public class FgBeechPeelingLogCategory extends GeoLogLogCategory {

	private static final long serialVersionUID = 20101025L;

	public final double coreDiam_cm = 8;	// diameter of the peeling core

	//	Constructor
	public FgBeechPeelingLogCategory (int id) {
		super (id, Translator.swap("FgBeechPeelingProduct.name"), FgSpecies.BEECH.getName(), 1, false, 2.1, 3.5, 35, 0.5, true);
	}


	//	Helper method to estimate the mass of products. Returns the min and max radius
	//	of a region usable for making this product or null if the whole mass of the log
	//	should be preferred.
	public double [] getMinMaxUsefulRadius_mm (capsis.extension.treelogger.GPiece piece) {
		capsis.extension.treelogger.GPieceDisc disc = piece.getTopDisc();
		double r [] = new double [2];
		r [0] = coreDiam_cm * 10 / 2;	// core radius (mm)
		r [1] = disc.getRadius_mm (false);	// without bark
		return r;
	}

	@Override
	public double getYieldFromThisPiece(WoodPiece p) throws Exception {
		GPiece piece = (GPiece) p;
		double yield = 1.;
		double v_m3 = piece.getWeightedVolumeM3();
		if (v_m3 > 0) {
			double r [] = getMinMaxUsefulRadius_mm (piece);
			double uv_m3 = getUsefulVolume_m3 (r [0], r [1], piece.getLengthM());
			yield = uv_m3 / v_m3;
		}
		return yield;
	}

	//	Private method for computing the useful volume
	private double getUsefulVolume_m3 (double coreRadius_mm, double topRadius_mm, double length_m) {
		double uS_m2 = 1e-6 * Math.PI *	(topRadius_mm * topRadius_mm - coreRadius_mm * coreRadius_mm);
		return uS_m2 * length_m;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		
		FgBeechPeelingLogCategory refCategory = (FgBeechPeelingLogCategory) obj;
		
		if (refCategory.coreDiam_cm != this.coreDiam_cm) {return false;}

		return true;
	}


}
