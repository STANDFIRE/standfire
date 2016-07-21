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

/**	
 * FgBeech "sawing" product.
 * For industrial sawing.
 * author F. Mothe - january 2006 / N. Robert - January 2009
 */
public class FgBeechFurnitureLogCategory extends GeoLogLogCategory {

	private static class BeechFurnitureTester extends Tester {

		private FgBeechFurnitureLogCategory logCategory;
		
		protected BeechFurnitureTester(GeoLogTreeData td, double botHeight_m, FgBeechFurnitureLogCategory logCategory) {
			super(td, botHeight_m);
			this.logCategory = logCategory;
		}

		// Returns true if
		// 	( knotDiam_cm/underBarkDiam_cm)^2 <= knotUnderBarkDiameterRatio ) :
		// (negative knotUnderBarkDiameterRatio means no test)
		public boolean isValid(double length_m) {
			boolean valid = true, underBark = false ;
			if (logCategory.knotUnderBarkDiameterRatio >= 0) {
				double topHeight = getBottomHeight() + length_m;
				double underBarkDiam_cm =
						getTreeData().getTreeRadius_cm (topHeight, underBark) / 10.0 * 2;
				double heartDiam_cm =
						getTreeData().getHeartWoodRadius_mm (topHeight) / 10.0 * 2;
				double ratio = heartDiam_cm > 0 ? underBarkDiam_cm/heartDiam_cm : 1.0;
				ratio *= ratio;
				valid = ratio <= logCategory.knotUnderBarkDiameterRatio;
			}
			return valid;
		}
	}


	protected double knotUnderBarkDiameterRatio;	// (negative means no test)

	private transient FgBeechFurniturePanel guiInterface;
	
	//	Constructor
	public FgBeechFurnitureLogCategory(int id) {
		super(id, Translator.swap("FgBeechFurnitureProduct.name"), FgSpecies.BEECH.getName(), -1, false, 2.5, 5.0, 40, 1, true);
		this.knotUnderBarkDiameterRatio = .13;
	}


	//	Abstract function of GeoLogProduct
	@Override
	public boolean testLogValid (GeoLogTreeData td) {
		boolean valid = false;
		if (testGeometry(td)) {
			Tester test = getTester(td);
			valid = testMaxLength(test, td);
		}

		return valid;
	}


	//	Helper method to estimate the mass of products. Returns the min and max radius
	//	of a region usable for making this product or null if the whole mass of the log
	//	should be prefered.
	@Override
	public double [] getMinMaxUsefulRadius_mm (capsis.extension.treelogger.GPiece piece) {
		double r [] = new double [2];
		r [0] = 0.;
		r [1] = piece.getTopDisc().getRadius_mm (false); // false : underbark radius
		return r;
	}

	@Override
	public double getYieldFromThisPiece (WoodPiece p) throws Exception {
		GPiece piece = (GPiece) p;
		double yield = 1.;
		double v_m3 = piece.getWeightedVolumeM3();
		if (v_m3 > 0) {
			double uv_m3 = getUsefulVolume_m3 (piece.getTopDisc().getRadius_mm (false), piece.getBottomDisc().getRadius_mm (false), piece.getLengthM());
			yield = uv_m3 / v_m3;
		}
		return yield;
	}

	//	Private method for computing the useful volume
	private static double getUsefulVolume_m3 (double topHwRadius_mm, double botHwRadius_mm,
			double length_m)
	{
		final double minPlankWidth_cm = 10.;
		final double plankThickness_cm = 5.;
		final double sawCutWidh_mm = 1.;
		double uV_m3 = 0.;
		if (length_m > 0) {
			if (botHwRadius_mm > topHwRadius_mm) {
				//~ Calculer ici la surface utile avec :
				//~ topHwRadius_mm
				//~ botHwRadius_mm
				//~ length_m
				//~ minPlankWidth_cm
				//~ plankThickness_cm
				//~ sawCutWidh_mm

				double topHwDiameter_square = 4 * topHwRadius_mm * topHwRadius_mm / 1000000 ;
				double botHwDiameter_square = 4 * botHwRadius_mm * botHwRadius_mm / 1000000 ;

				double x_square ;
				double xMinusPlankThickness_square ;

				double VplankHalfLog = 0 ;
				double VplankCentre = 0 ;
				double centrePlankThickness ;
				int NbPlanks = 0 ;

				double x = Math.sqrt(topHwDiameter_square - minPlankWidth_cm * minPlankWidth_cm / 10000) / 2 ; // x is the distance from the center of the log to the external part of a plank perpendicularily to the sawing axe (in m).

				while (x >= (3 * plankThickness_cm / 100 + 2 * sawCutWidh_mm/1000)/2 ){
					x_square = x * x ;
					xMinusPlankThickness_square = (x-plankThickness_cm/100) * (x-plankThickness_cm/100) ;
					VplankHalfLog += length_m * plankThickness_cm/100 * ( Math.sqrt(topHwDiameter_square-4*x_square)+ Math.sqrt(topHwDiameter_square-4*xMinusPlankThickness_square) + Math.sqrt(botHwDiameter_square-4*x_square)+ Math.sqrt(botHwDiameter_square-4*xMinusPlankThickness_square))/4;
					x = x - plankThickness_cm/100 - sawCutWidh_mm/1000 ;
					NbPlanks ++ ;
				}

				if (x >= ( plankThickness_cm / 100 + sawCutWidh_mm/1000 /2 ) ){
					x_square = x * x ;
					centrePlankThickness = x - sawCutWidh_mm/1000 /2 ;
					double xMinusCenterPlankThickness_square = (sawCutWidh_mm/1000 /2) * (sawCutWidh_mm/1000 /2) ;
					VplankHalfLog += length_m * centrePlankThickness * ( Math.sqrt(topHwDiameter_square-4*x_square)+ Math.sqrt(topHwDiameter_square-4*xMinusCenterPlankThickness_square) + Math.sqrt(botHwDiameter_square-4*x_square)+ Math.sqrt(4*xMinusCenterPlankThickness_square))/4;
					NbPlanks = 2*NbPlanks + 2 ;
				} else {
					x_square = x * x ;
					VplankCentre = length_m * x * ( Math.sqrt(topHwDiameter_square-4*x_square) + Math.sqrt(botHwDiameter_square-4*x_square) );
					NbPlanks = 2*NbPlanks + 1 ;
				}

				uV_m3 = 2 * VplankHalfLog + VplankCentre ;
			}
		}
		return uV_m3;
	}
	
	@Override
	public FgBeechFurniturePanel getUI() {
		if (guiInterface == null) {
			guiInterface = new FgBeechFurniturePanel(this);
		}
		return guiInterface;
	}


	@Override
	protected BeechFurnitureTester getTester(GeoLogTreeData td) {
		return new BeechFurnitureTester(td, td.getLoggingContext().getHeight(), this);
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		
		FgBeechFurnitureLogCategory refCategory = (FgBeechFurnitureLogCategory) obj;
		
		if (refCategory.knotUnderBarkDiameterRatio != this.knotUnderBarkDiameterRatio) {return false;}

		return true;
	}
}
