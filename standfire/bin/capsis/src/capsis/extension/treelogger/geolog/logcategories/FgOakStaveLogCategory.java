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
 * FgOak "stave" product.
 * For barrel stave production.
 * @author F. Mothe - N. Robert, sept 2008
 */
public class FgOakStaveLogCategory extends GeoLogLogCategory {

	private static class StaveTester extends Tester {

		private FgOakStaveLogCategory logCategory;

		protected StaveTester (GeoLogTreeData td, double botHeight_m, FgOakStaveLogCategory logCategory) {
			super (td, botHeight_m);
			this.logCategory = logCategory;
		}

		// Returns true if (heartDiam_cm - knotDiam_cm >= 2. * staveWidth_cm)
		// (negative staveWidth_cm means no test)
		public boolean isValid (double length_m) {
			boolean valid = true;
			if (logCategory.staveWidth_cm >= 0) {
				double topHeight = getBottomHeight() + length_m;
				double knotDiam_cm =
						getTreeData().getKnottyCoreRadius_mm (topHeight) / 10.0 * 2;
				double heartDiam_cm =
						getTreeData().getHeartWoodRadius_mm (topHeight) / 10.0 * 2;
				valid = heartDiam_cm - knotDiam_cm >= 2. * logCategory.staveWidth_cm;
			}
			return valid;
		}

	}

	protected double staveWidth_cm;	// (negative means no test)

	private FgOakStavePanel guiInterface;

	/**
	 * 	Constructor.
	 */
	public FgOakStaveLogCategory (int id) {
		super(id, Translator.swap("FgOakStaveProduct.name"), FgSpecies.OAK.getName(), 2, false, 1.1, 1.1, 45, 0.5, true);
		this.staveWidth_cm = 9.;
	}


	//	Abstract function of GeoLogProduct
	public boolean testLogValid (GeoLogTreeData td) {
		boolean valid = false;
		if (testGeometry(td)) {
			Tester test = getTester(td);
			valid = testMaxLength (test, td);
		}

		return valid;
	}


	//	Helper method to estimate the mass of products. Returns the min and max radius
	//	of a region usable for making this product or null if the whole mass of the log
	//	should be preferred.
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
			double uv_m3 = getUsefulVolume_m3 (r [0], r [1], piece.getLengthM(), staveWidth_cm);
			yield = uv_m3 / v_m3;
		}
		return yield;
	}

	// Private method for computing the useful volume
	// (if staveWidth_cm <= 0., returns the volume of clear of knot heartwood)
	private static double getUsefulVolume_m3 (double topKcRadius_mm, double topHwRadius_mm,
			double length_m, double staveWidth_cm)
	{
		final double staveThickness_cm = 4.;
		// staveWidth_cm est une variable de la classe
		// (transmise en paramètre car la méthode est statique)
		double uV_m3 = 0.;
		if (staveWidth_cm <= 0) {
			double uS_m2 = 1e-6 * Math.PI *
					(topHwRadius_mm * topHwRadius_mm - topKcRadius_mm * topKcRadius_mm);
			uV_m3 = Math.max (0.,  uS_m2 * length_m);
		} else if (length_m > 0) {
			if (topHwRadius_mm > topKcRadius_mm) {
				//~ Calculer ici la surface utile avec :
				//~ topKcRadius_mm
				//~ topHwRadius_mm
				//~ length_m
				//~ staveWidth_cm
				//~ staveThickness_cm

				int CrownNb = 0;
				// The table DintCrownC[] stores all interior diameters of stave crowns in mm.
				// It is impossible to create more than 10 crowns, however,
				// the number of crowns should usually not exceed 3.
				// TODO : calcul à revoir !!
				double DintCrownC[] = new double[10];

				// CrownNb+1 is used because the index of the table starts from 1. (???)
				DintCrownC[CrownNb+1]=2 * topHwRadius_mm ;
				while (CrownNb < 8 && (Math.sqrt(DintCrownC[CrownNb+1] * DintCrownC[CrownNb+1] - staveThickness_cm * staveThickness_cm*100) - 2* staveWidth_cm * 10) >= 2 * topKcRadius_mm) {
					DintCrownC[CrownNb+2] = Math.sqrt(DintCrownC[CrownNb+1] * DintCrownC[CrownNb+1] - staveThickness_cm * staveThickness_cm*100) - 2 * staveWidth_cm * 10 ;
					CrownNb ++ ;
				} ;

				// Compute the number of sectors defined on the smallest crown
				int NbSectors = (int) (Math.floor( 2 * Math.PI / (Math.atan(staveThickness_cm*10 / DintCrownC[CrownNb+1])) ) ) ;

				// Compute the number of stave per sector ;
				// each sector contains one and only one stave in the interior sector.
				int NbStavesInSector = 1 ;
				for (int i = 1; i < CrownNb ; i++) {
					NbStavesInSector = (int) (Math.floor( Math.PI / (NbSectors * Math.atan( staveThickness_cm*10 / DintCrownC[i])) ) ) ;
				};

				int NbStaves = NbSectors * NbStavesInSector;

				double uS_m2 = NbStaves * staveThickness_cm * staveWidth_cm / 10000 ;

				uV_m3 = uS_m2 * length_m;
			}
		}
		return uV_m3;
	}


	@Override
	public FgOakStavePanel getUI() {
		if (guiInterface == null) {
			guiInterface = new FgOakStavePanel(this);
		}
		return guiInterface;
	}


	@Override
	protected StaveTester getTester(GeoLogTreeData td) {
		return new StaveTester (td, td.getLoggingContext().getHeight (), this);
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}

		FgOakStaveLogCategory refCategory = (FgOakStaveLogCategory) obj;

		if (refCategory.staveWidth_cm != this.staveWidth_cm) {return false;}

		return true;
	}

}
