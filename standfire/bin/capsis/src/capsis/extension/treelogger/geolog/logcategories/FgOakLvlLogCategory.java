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

/**	FgOak "Lvl" product.
*	For Laminated Veneer Lumber production.
*	@author F. Mothe - january 2006
*/
public class FgOakLvlLogCategory extends GeoLogLogCategory {
	
	private static class LvlTester extends Tester {

		private FgOakLvlLogCategory logCategory;
		
		protected LvlTester (GeoLogTreeData td, double botHeight_m, FgOakLvlLogCategory logCategory) {
			super (td, botHeight_m);
			this.logCategory = logCategory;
		}

		// Returns true if knotDiam_cm <= maxKnotDiam_cm
		// && heartDiam_cm>=minHeartDiam_cm :
		// (negative min/max diams mean no test)
		public boolean isValid (double length_m) {
			boolean valid = true;
			double topHeight = getBottomHeight() + length_m;
			if (logCategory.maxKnotDiam_cm >= 0) {
				double knotDiam_cm =
						getTreeData().getKnottyCoreRadius_mm (topHeight) / 10.0 * 2;
				valid = knotDiam_cm <= logCategory.maxKnotDiam_cm;
			}
			if (valid && logCategory.minHeartDiam_cm >= 0) {
				double heartDiam_cm =
						getTreeData().getHeartWoodRadius_mm (topHeight) / 10.0 * 2;
				valid = heartDiam_cm >= logCategory.minHeartDiam_cm;
			}
			return valid;
		}
	}

	public final double coreDiam_cm = 8;	// diameter of the peeling core
	
	protected double maxKnotDiam_cm;	// (negative means no test, knotty core accepted)
	protected double minHeartDiam_cm;	// (negative means no test, sapwood accepted)

	private transient FgOakLvlPanel guiInterface;
	

	//	Constructor
	public FgOakLvlLogCategory (int id) {
		super(id, Translator.swap("FgOakLvlProduct.name"), FgSpecies.OAK.getName(), -1, true, 1.5, 1.5, 18, 1, true);
		this.maxKnotDiam_cm = 12.0;
		this.minHeartDiam_cm = 14.0;
	}

	//	Abstract function of GeoLogProduct
	public boolean testLogValid (GeoLogTreeData td) {
		boolean valid = false;
		if (testGeometry(td)) {
			LvlTester test = getTester(td);
			valid = testMaxLength (test, td);
		}

		return valid;
	}
	
//	protected Map<EndProductFeature,Double> setDefaultEndUseProductDistribution() {
//		return AbstractTreeLogCategory.getReferenceEnduseDistributionMap().get("oak").get(LogProductCategory.LVL);
//	}

	//	Helper method to estimate the mass of products. Returns the min and max radius
	//	of a region usable for making this product or null if the whole mass of the log
	//	should be prefered.
	public double [] getMinMaxUsefulRadius_mm (capsis.extension.treelogger.GPiece piece) {
		capsis.extension.treelogger.GPieceDisc disc = piece.getTopDisc();
		double r [] = new double [2];
		double coreRadius_mm = coreDiam_cm * 10 / 2;
		
		if(maxKnotDiam_cm < 0.) {
			// Knotty core accepted :
			r [0] = coreRadius_mm;
		} else {
			r [0] = Math.max(disc.getKnottyCoreRadius_mm(), coreRadius_mm);
		}
		if(minHeartDiam_cm < 0.) {
			// Sapwood accepted :
			r [1] = disc.getRadius_mm (false);	// without bark
		} else {
			r [1] = disc.getHeartWoodRadius_mm();	// without bark
		}
		return r;
	}

//	//	Returns the volumic yield for making this product (i.e. usefulVolume / v_m3)
//	public double getYield (capsis.extension.treelogger.GPiece piece, double v_m3) {
//		double yield = 1.;
//		if (v_m3 > 0) {
//			double r [] = getMinMaxUsefulRadius_mm (piece);
//			double uv_m3 = getUsefulVolume_m3 (r [0], r [1], PieceUtil.getLength_m (piece));
//			yield = uv_m3 / v_m3;
//		}
//		return yield;
//	}

	//	Returns the volumic yield for making this product (i.e. usefulVolume / v_m3)
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
		double uS_m2 = 1e-6 * Math.PI *
				(topRadius_mm * topRadius_mm - coreRadius_mm * coreRadius_mm);
		return Math.max(0., uS_m2 * length_m);
	}

	@Override
	public FgOakLvlPanel getUI() {
		if (guiInterface == null) {
			guiInterface = new FgOakLvlPanel(this);
		}
		return guiInterface; 
	}

	@Override
	protected LvlTester getTester(GeoLogTreeData td) {
		return new LvlTester (td, td.getLoggingContext().getHeight(), this);
	}

	
	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		
		FgOakLvlLogCategory refCategory = (FgOakLvlLogCategory) obj;
		
		if (refCategory.maxKnotDiam_cm != this.maxKnotDiam_cm) {return false;}
		if (refCategory.minHeartDiam_cm != this.minHeartDiam_cm) {return false;}
		if (refCategory.coreDiam_cm != this.coreDiam_cm) {return false;}

		return true;
	}
	
}
