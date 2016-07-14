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
import capsis.extension.treelogger.geolog.LoggingContext;

/**	FgOak "slicing" product.
*	For sliced veneers production.
*	@author F. Mothe - january 2006
*/
public class FgOakSlicingLogCategory extends GeoLogLogCategory {

	private static class SlicingTester extends Tester {
		
		private FgOakSlicingLogCategory logCategory;

		protected SlicingTester (GeoLogTreeData td, double botHeight_m, FgOakSlicingLogCategory logCategory) {
			super (td, botHeight_m);
			this.logCategory = logCategory;
		}

		// Returns true if  knotDiam_cm <= maxKnotDiam_cm
		// 		&& knotDiam_cm/heartDiam_cm)^2 <= knotHeartRatio ) :
		// (negative min/max diams/ratios mean no test)
		// (maxDiam_cm is tested separately, see note below)
		public boolean isValid (double length_m) {
			boolean valid = true;
			// System.out.println (getName () + ".isValid : length=" + length_m);
			double topHeight = getBottomHeight() + length_m;
			double knotDiam_cm = getTreeData().getKnottyCoreRadius_mm (topHeight) / 10.0 * 2;
			if (logCategory.maxKnotDiam_cm >= 0) {
				valid = knotDiam_cm <= logCategory.maxKnotDiam_cm;
				// System.out.println ("\tknot=" + knotDiam_cm + " v=" + valid);
			}
			if (valid && logCategory.knotHeartRatio >= 0) {
				double heartDiam_cm =
						getTreeData().getHeartWoodRadius_mm (topHeight) / 10.0 * 2;
				double ratio = heartDiam_cm > 0 ? knotDiam_cm/heartDiam_cm : 1.0;
				ratio *= ratio;
				valid = ratio <= logCategory.knotHeartRatio;
				// System.out.println ("\theart=" + heartDiam_cm + " ratio=" + ratio + " v=" + valid);
			}
			return valid;
		}

	}


	// Max diameter (depending on diamOverBark && diamRelPos) :
	protected double maxDiam_cm;	// (negative means no test)

	protected double maxKnotDiam_cm;	// (negative means no test)
	protected double knotHeartRatio;	// (negative means no test)

	private transient FgOakSlicingPanel guiInterface;
	
	/**
	 * Constructor.
	 */	
	public FgOakSlicingLogCategory (int id) {
		super(id, Translator.swap("FgOakSlicingProduct.name"), FgSpecies.OAK.getName(), 1, false, 1.0, 2.2, 50, 0.5, true);
		this.maxDiam_cm = 140.;
		this.maxKnotDiam_cm = 10.;
		this.knotHeartRatio = 1.;
	}


	//	Abstract function of GeoLogProduct
	public boolean testLogValid (GeoLogTreeData td) {
		LoggingContext lc = td.getLoggingContext();
		// System.out.println (getName () + ".testLogValid : height=" + lc.getHeight ());
		boolean valid = false;
		if (testGeometry(td)) {
			// maxDiam_cm must be tested *after* (see notes below) :
			Tester test = getTester(td);
			valid = testMaxLength (test, td);
			if (valid && maxDiam_cm >= 0) {
				double height = lc.getHeight () + lc.getLength () * diamRelPos;
				double diam = td.getTreeRadius_cm (height, diamOverBark) * 2;
				valid = diam <= maxDiam_cm;
				// System.out.println ("\tdiam=" + diam + " v=" + valid);

				// Note 1 : if the median diameter is too high we could find a lower one
				// by increasing the length. But it would not make sense to increase
				// the length because the piece is too large, and moreover the current
				// length is the maximal admissible length to fulfill the  other rules.

				// Note 2 : if !valid, no other slicing logs will be cut, even if another log
				// (upper) could be valid
			}
		}

		// System.out.println (getName () + ".testLogValid : valid=" + valid);
		return valid;
	}
	



	//	Helper method to estimate the mass of products. Returns the min and max radius
	//	of a region usable for making this product or null if the whole mass of the log
	//	should be prefered.
	@Override
	public double [] getMinMaxUsefulRadius_mm (capsis.extension.treelogger.GPiece piece) {
		capsis.extension.treelogger.GPieceDisc disc = piece.getTopDisc();
		double r [] = new double [2];
		r [0] = disc.getKnottyCoreRadius_mm();
		r [1] = disc.getHeartWoodRadius_mm();
		return r;
	}


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

	//	Area of the circle of diameter d between 2 opposite chords distant of l
	public static double getBetweenChordsArea (double d, double l) {
		assert l > 0 && l < d;	// => java -ea
		return 0.5 * (l * Math.sqrt(d*d - l*l) + d*d * Math.asin (l / d));
	}

	//	Method for computing the useful volume (also used by FgBeech)
	public static double getUsefulVolume_m3 (double topKcRadius_mm, double topHwRadius_mm,
			double length_m)
	{
		final double minSheetWidth_mm = 150.;
		final double remainingThickness_mm = 20.;
		double uV_m3 = 0.;
		if (length_m > 0) {
			// Max knotty core radius (at top height) :
			double r1 = topKcRadius_mm;
			// Min heart wood radius (at top height) :
			double r2 = topHwRadius_mm;

			// Veneers are sliced in clear of wood heartwood with a minimal width
			// of minSheetWidth_mm and the remainning board must be thicker
			// than remainingThickness_mm :
			if (r2 > r1) {
				double w = minSheetWidth_mm;
				double ws = w * w;
				double d1 = 2. * r1;
				double d2 = 2. * r2;
				double r1s = r1 * r1;
				double r2s = r2 * r2;

				// Half length of the line tangent to knotty core :
				// (no veneers can be sliced if w > 2. * l0)
				double w0 = Math.sqrt (r2s - r1s);
				if (w < 2. * w0) {
					// Surface of heartwood minus both flat sides (mm2) :
					double S1;
					// Surface of the remaining board (mm2) :
					double S2;
					// Surface of knotty core not included into S2 (mm2) :
					double S3 = 0.;

					// Height of the sliceable block :
					double hMax = Math.sqrt (4. * r2s - ws);
					// Theorical height of the remaining board :
					// (may be 0 or -1 for no remaining board)
					double h0 = Math.max (0, remainingThickness_mm);
					// True height of the remaining board :
					double hMin = h0;
					if (d1 >= h0) {
						// The knotty core is not completely included into the
						// remaining board
						if (w < r2 - r1) {
							// Veneers can be sliced radially between knotty core
							// and sapwood
							// Nota : w may be <= 0 (i.e. no constraint on the minimal
							// sheet width)

							hMin = h0;
						} else if (w < w0) {
							// Some veneers can be sliced radially but the remaining
							// board may be thicker than h0 :
							double K = (r2s - r1s - ws) / 2. / w;
							// (r1s - K * K) should never be < 0 :
							hMin = 2. * Math.sqrt (r1s - K * K);
							hMin = Math.max (hMin, h0);
						} else {
							// No space left for radial veneers.
							// The remaining board must include the knotty core :
							hMin = d1;
						}
						S3 = Math.PI * r1s - getBetweenChordsArea (d1, hMin);
					}
					S1 = getBetweenChordsArea (d2, hMax);
					S2 = getBetweenChordsArea (d2, hMin);

					// Useful surface on the cross section :
					double uS_m2 = (S1 - S2 - S3) * 1e-6;
					uV_m3 = uS_m2 * length_m;
				}
			}
		}
		return uV_m3;
	}


	@Override
	public FgOakSlicingPanel getUI() {
		if (guiInterface == null) {
			guiInterface = new FgOakSlicingPanel(this);
		}
		return guiInterface;
	}


	@Override
	protected SlicingTester getTester(GeoLogTreeData td) {
		return new SlicingTester (td, td.getLoggingContext().getHeight(), this);
	}

	
	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		
		FgOakSlicingLogCategory refCategory = (FgOakSlicingLogCategory) obj;
		
		if (refCategory.maxKnotDiam_cm != this.maxKnotDiam_cm) {return false;}
		if (refCategory.maxDiam_cm != this.maxDiam_cm) {return false;}
		if (refCategory.knotHeartRatio != this.knotHeartRatio) {return false;}

		return true;
	}

}
