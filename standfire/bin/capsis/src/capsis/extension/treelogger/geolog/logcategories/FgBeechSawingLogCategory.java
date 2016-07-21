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
import capsis.extension.treelogger.geolog.GeoLogTreeData;
import capsis.extension.treelogger.geolog.logcategories.GeoLogLogCategory;
import capsis.extension.treelogger.geolog.logcategories.Tester;

/**	
 * FgBeech "sawing" product.
 * For industrial sawing.
 * @author F. Mothe - N. Robert, Jan 2009
 */
public class FgBeechSawingLogCategory extends GeoLogLogCategory {

	private static final long serialVersionUID = 20101025L;

	/**
	 * Constructor.
	 */	
	public FgBeechSawingLogCategory (int id) {
		super (id, Translator.swap("FgBeechSawingProduct.name"), FgSpecies.BEECH.getName(), -1, false, 2.0, 13.0, 30, 0.5, true);
	}

	//	Abstract function of GeoLogProduct
	public boolean testLogValid (GeoLogTreeData td) {
		return testGeometry(td) ;
	}

	//	Helper method to estimate the mass of products. Returns the min and max radius
	//	of a region usable for making this product or null if the whole mass of the log
	//	should be prefered.
	@Override
	public double [] getMinMaxUsefulRadius_mm (capsis.extension.treelogger.GPiece piece) {
		double r [] = new double [2];
		r [0] = 0.;
		r [1] = piece.getTopDisc().getRadius_mm(false);  // false : underbark
		return r;
	}

	public double getYieldFromThisPiece (WoodPiece p) throws Exception {
		GPiece piece = (GPiece) p;
		double yield = 1.;
		double v_m3 = p.getWeightedVolumeM3();
		if (v_m3 > 0) {
			double uv_m3 = getUsefulVolume_m3 (piece.getTopDisc().getRadius_mm (false),	piece.getBottomDisc().getRadius_mm (false), piece.getLengthM());
			yield = uv_m3 / v_m3;
		}
		return yield;
	}

	//	Private method for computing the useful volume
	private double getUsefulVolume_m3 (double topHwRadius_mm, double botHwRadius_mm,
			double length_m)
	{
		final double minSlabWidth_cm = 10.;
		final double slabThickness_cm = 3.;
		final double sawCutWidh_mm = 1.;
		double uV_m3 = 0.;
		if (length_m > 0) {
			if (botHwRadius_mm > topHwRadius_mm) {
				//~ Calculer ici la surface utile avec :
				//~ topHwRadius_mm
				//~ botHwRadius_mm
				//~ length_m
				//~ minSlabWidth_cm
				//~ slabThickness_cm
				//~ sawCutWidh_mm

				double topHwDiameter_square = 4 * topHwRadius_mm * topHwRadius_mm / 1000000 ;

				double x_square ;

				double VslabHalfLog = 0 ;
				double VslabCentre = 0 ;
				double centreSlabThickness ;
				int NbSlabs = 0 ;

				double x = Math.sqrt(topHwDiameter_square - minSlabWidth_cm * minSlabWidth_cm / 10000) / 2 ; // x is the distance from the center of the log to the external part of a plank perpendicularily to the sawing axe (in m).

				while (x >= (3 * slabThickness_cm / 100 + 2 * sawCutWidh_mm/1000)/2 ){
					x_square = x * x ;
					VslabHalfLog += length_m * slabThickness_cm/100 * ( Math.sqrt(topHwDiameter_square-4*x_square));
					x = x - slabThickness_cm/100 - sawCutWidh_mm/1000 ;
					NbSlabs ++ ;
				}

				if (x >= ( slabThickness_cm / 100 + sawCutWidh_mm/1000 /2 ) ){
					x_square = x * x ;
					centreSlabThickness = x - sawCutWidh_mm/1000 /2 ;
					VslabHalfLog += length_m * centreSlabThickness * ( Math.sqrt(topHwDiameter_square-4*x_square));
					NbSlabs = 2*NbSlabs + 2 ;
				} else {
					x_square = x * x ;
					VslabCentre = length_m * x * ( Math.sqrt(topHwDiameter_square-4*x_square) );
					NbSlabs = 2*NbSlabs + 1 ;
				}

				uV_m3 = 2 * VslabHalfLog + VslabCentre ;

			}
		}
		return uV_m3;
	}

	@Override
	@Deprecated
	protected Tester getTester(GeoLogTreeData td) {return null;}
	

}
