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

package capsis.extension.treelogger.geolog.util;

import capsis.extension.treelogger.GPiece;
import capsis.extension.treelogger.GPieceDisc;
import capsis.util.methodprovider.DiscMassProvider;

/**	PieceUtil : utility methods for GPiece, GPieceDisc, GPieceRing
*	All the methods are static
*
*	@author F. Mothe - february 2006
*/
public class PieceUtil {


//	// Returns true if all disks have heartwood :
//	public static boolean hasHeartWood (GPiece piece) {
////		for (GPieceDisc d : getDiscs (piece)) {
//		for (GPieceDisc d : piece.getDiscs()) {
//			if (!d.getContours().containsKey ("heartWood")) {
//				return false;
//			}
//		}
//		return true;
//	}

//	// Returns true if all disks have knottycore :
//	public static boolean hasKnottyCore (GPiece piece) {
////		for (GPieceDisc d : getDiscs (piece)) {
//		for (GPieceDisc d : piece.getDiscs()) {
//			if (!d.getContours().containsKey ("crownBase")) {
//				return false;
//			}
//		}
//		return true;
//	}

//	// Returns true if all disks have first dead branch height :
//	public static boolean hasFirstDeadBranch (GPiece piece) {
////		for (GPieceDisc d : getDiscs (piece)) {
//		for (GPieceDisc d : piece.getDiscs()) {
//			if (!d.getContours().containsKey ("firstDeadBranch")) {
//				return false;
//			}
//		}
//		return true;
//	}

//	// Returns true if all disks have juvenile wood :
//	public static boolean hasJuvenileWood (GPiece piece) {
////		for (GPieceDisc d : getDiscs (piece)) {
//		for (GPieceDisc d : piece.getDiscs()) {
//			if (!d.getContours().containsKey ("juvenileWood")) {
//				return false;
//			}
//		}
//		return true;
//	}

//	//	getRing and related methods :
//	//	Cambial age is counted from bark (0) to pith (getNbWoodRings+1)
//	//	(it does not depend on firstRingIsBark or lastRingIsPith)
//
//	public static ArrayList getRings (GPieceDisc disc) {
//		return (ArrayList) disc.rings;
//	}

//	public static int getNbRings (GPieceDisc disc) {
//		return disc.getRings().size ();
//	}


	/*
	// useless :
	public static int getRingIndex (GPieceDisc disc, int cambialAge) {
		return cambialAge;
		// return disc.firstRingIsBark ? cambialAge : cambialAge-1;
	}
	*/

	// Get ring by cambial age.
	// Examples :
	// Most external wood ring after bark :
	//	ring = getRing (disc, 1);
	// Most internal wood ring before pith :
	//	ring = getRing (disc, getNbWoodRings (disc));
	// Pith :
	//	ring = getRing (disc, disc.rings.size () - 1);
	// Bark :
	//	ring = getRing (disc, 0);
//	public static GPieceRing getRing (GPieceDisc disc, int cambialAge) {
//		return disc.getRings().get(cambialAge);
//	}

//	public static GPieceRing getBarkRing (GPieceDisc disc) {
//		// assert disc.firstRingIsBark;
//		return getRing (disc, 0);
//	}


//	public static int getCambialAge (GPieceDisc disc, int pithAge) {
//		return disc.getNbWoodRings() + 1 - pithAge;
//	}

//	public static int getAgeFromPith (GPieceDisc disc, int cambialAge) {
//		return disc.getNbWoodRings() + 1 - cambialAge;
//	}

//	public static boolean isWood (GPieceDisc disc, int cambialAge) {
//		return cambialAge > 0 && cambialAge <= disc.getNbWoodRings();
//	}

//	public static boolean isHeartWood (GPieceDisc disc, int cambialAge) {
//		return getExtRadius_mm (disc, cambialAge) <=
//				getHeartWoodRadius_mm (disc);
//	}

//	public static boolean isKnottyCore (GPieceDisc disc, int cambialAge) {
//		return getExtRadius_mm (disc, cambialAge) <=
//				getKnottyCoreRadius_mm (disc);
//	}

//	public static boolean isJuvenileWood (GPieceDisc disc, int cambialAge) {
//		return getExtRadius_mm (disc, cambialAge) <=
//				getJuvenileWoodRadius_mm (disc);
//	}

	// Number of sapwood rings :
	// (cambial age of the first heartwood ring = getNbSapWoodRings (disc) + 1)

	// Number of clear of knots rings :
	// (cambial age of the first knotty core ring = getNbClearKnotRings (disc) + 1)
//	public static int getNbClearKnotRings (GPieceDisc disc) {
//		return disc.getCambialAge(getKnottyCoreRadius_mm (disc));
//	}

//	// Number of juvenile wood rings :
//	public static int getNbJuvenileWoodRings (GPieceDisc disc) {
//		return disc.getCambialAge(disc.getJuvenileWoodRadius_mm());
//		// Corrected 26.08.2008, was :
//		//~ int nbRings = getNbWoodRings (disc);
//		//~ int nbJuvRings = nbRings;
//		//~ double rjuv = getJuvenileWoodRadius_mm (disc);
//		//~ if (rjuv > 0.0) {
//			//~ for (int cambialAge = nbRings; cambialAge > 0; cambialAge--) {
//			//~ // for (int cambialAge = 1; cambialAge <= nbRings; cambialAge++) {
//				//~ double r = getExtRadius_mm (disc, cambialAge);
//				//~ if (r >= rjuv) {
//					//~ nbJuvRings = cambialAge - 1;
//					//~ break;
//				//~ }
//			//~ }
//		//~ }
//		//~ return nbJuvRings;
//	}

//	// Returns cambial age of the ring including radius_mm :
//	// TODO : optimise !
//	public static int getCambialAge (GPieceDisc disc, double radius_mm) {
//		int nbRings = disc.getNbWoodRings();
//		int cambialAge = nbRings;
//		if (radius_mm > 0.0) {
//			for (; cambialAge > 0; cambialAge --) {
//				double r = disc.getExtRadius_mm(cambialAge);
//				if (r > radius_mm) {
//					// cambialAge --;
//					break;
//				}
//			}
//		}
//		return cambialAge;
//	}

	//	Radius, height, length :

	// Radius at a given height (by interpolation between discs) :
//	public static double getRadius_mm (GPiece piece, double height_m, boolean overBark) {
//		return piece.getExtRadius_mm (height_m, overBark ? 0 : 1);
//	}
//
//	public static double getRadius_mm (GPieceDisc disc, boolean overBark) {
//		GContour contour = disc.getRing (overBark ? 0 : 1);
//		return contour.getMeanRadius_mm ();
//	}

//	public static double getHeartWoodRadius_mm (GPieceDisc disc) {
//		return disc.getContours().get ("heartWood").getMeanRadius_mm ();
//	}

//	public static double getKnottyCoreRadius_mm (GPieceDisc disc) {
//		return disc.getContours().get ("crownBase").getMeanRadius_mm ();
//	}

//	public static double getFirstDeadBranchRadius_mm (GPieceDisc disc) {
//		return disc.getContours().get ("firstDeadBranch").getMeanRadius_mm ();
//	}
//
//	public static double getJuvenileWoodRadius_mm (GPieceDisc disc) {
//		return disc.getContours().get ("juvenileWood").getMeanRadius_mm ();
//	}
//

	//	Crown ratio :


	//	Ring width and related methods :

	// Radius at the external side (toward bark) of the ring at a given height
	// (by interpolation between discs) :
//	public static double getExtRadius_mm (GPiece piece,
//			double height_m, int cambialAge) {
//		double radius = 0.0;
//		GPieceDisc d0 = piece.getBottomDisc();
//		GPieceDisc d1 = piece.getTopDisc();
//		if (d0.getHeight_m() <= height_m && d1.getHeight_m() >= height_m) {
////			for (GPieceDisc d : getDiscs (piece)) {
//			for (GPieceDisc d : piece.getDiscs()) {
//				double z = d.getHeight_m();
//				if (z < height_m) {
//					d0 = d;
//				}
//				if (z >= height_m) {
//					d1 = d;
//					break;
//				}
//			}
//
//			double z0 = d0.getHeight_m();
//			double z1 = d1.getHeight_m();
//			double r0 = d0.getExtRadius_mm(cambialAge);
//			if (z1 > z0) {
//				double r1 = d1.getExtRadius_mm (cambialAge);
//				double ratio = (height_m - z0) / (z1 - z0);
//				radius = r0 * (1.0 - ratio) + r1 * ratio;
//			} else {
//				radius = r0;
//			}
//		}
//		return radius;
//	}

//	// Radius at the external side (toward bark) of the ring :
//	public static double getExtRadius_mm (GPieceDisc disc, int cambialAge) {
//		return (cambialAge < disc.getRings().size())
//			? disc.getRing(cambialAge).getMeanRadius_mm ()
//			: 0.0;
//	}



//	// Return the width of the segment [cambialAgeMin, cambialAgeMax]
//	// (bounds included) :
//	public static double getWidth_mm (GPieceDisc disc, int cambialAgeMin, int cambialAgeMax) {
//		// Should works for pith (if lastRingIsPith) and bark (if firstRingisBark).
//		return disc.getExtRadius_mm (cambialAgeMin) - disc.getIntRadius_mm (cambialAgeMax);
//	}

	


//	// Return the half diameter !
//	public static double getPithWidth_mm (GPieceDisc disc) {
//		GPieceRing pith = disc.getPithRing();
//		return pith.getMeanRadius_mm ();
//		/*
//		// Should work for the top of the tree :
//		double r = 0.0;
//		if (disc.lastRingIsPith) {
//			int cambialAge = getCambialAge (disc, 0);
//			if (cambialAge>=0) {
//				r = getExtRadius_mm (disc, cambialAge);
//			}
//		}
//		return r;
//		*/
//	}


//	public static double getSurface_mm2 (GPieceDisc disc, int cambialAgeMin, int cambialAgeMax) {
//		// Should works for pith (if lastRingIsPith) and bark (if firstRingisBark).
//		double R2 = disc.getExtRadius_mm(cambialAgeMin);
//		double R1 = disc.getIntRadius_mm(cambialAgeMax);
//		return Math.PI * (R2*R2 - R1*R1);
//	}


	



	//	Piece name :

	// Returns a printable name (unique for a tree, neither empty)
	// in the form "rankInTree" + "pieceProduct" | "[pieceOrigin]"

	//	Volume integration using PiecePropertyMeasurer :

	public static enum Compartment {
		TOTAL,		// wood + bark  + pith
		WOOD_BARK,	// wood + bark
		WOOD,
		BARK,
		PITH,
		HEARTWOOD,
		SAPWOOD,
		KNOTTYCORE,
		CLEARKNOT,
		KNOTTYCORE_HEARTWOOD,
		CLEARKNOT_HEARTWOOD,
		KNOTTYCORE_SAPWOOD,
		CLEARKNOT_SAPWOOD,
		JUVENILE,
		ADULT,
	}

	// Mean diameter :
	public static class MeasurerDiameter_cm extends PiecePropertyMeasurer {
		public MeasurerDiameter_cm (GPiece piece) {
			super (piece);
		}
		public double getSurfacicMeasure (GPieceDisc disc,
				int cambialAgeMin, int cambialAgeMax) {
			return disc.getExtRadius_mm(cambialAgeMin);
		}
		public double getVolumicMeasure (double integral) {
			// mm*m/m/10 = cm, radius * 2 = diameter :
//			return integral / getLength_m (getPiece ()) * 2 / 10.0;
			return integral / getPiece().getLengthM() * 2 / 10.0;
		}
	}

//	public static double getDiameter_cm (GPiece piece,
//			Compartment compartment) {
//		return new MeasurerDiameter_cm (piece).getMeasure (compartment);
//	}

	// Mean ring width :
	public static class MeasurerRingWidth_mm extends PiecePropertyMeasurer {
		public MeasurerRingWidth_mm (GPiece piece) {
			super (piece);
		}
		public double getSurfacicMeasure (GPieceDisc disc, int cambialAgeMin, int cambialAgeMax) {
			return disc.getRingWidth_mm(cambialAgeMin, cambialAgeMax);
		}
		public double getVolumicMeasure (double integral) {
			// mm*m/m = mm :
//			return integral / getLength_m (getPiece ());
			return integral / getPiece().getLengthM();
		}
	}

//	public static double getRingWidth_mm (GPiece piece,
//			Compartment compartment) {
//		return new MeasurerRingWidth_mm (piece).getMeasure (compartment);
//	}

	// Mean volume :
	public static class MeasurerVolume_m3 extends PiecePropertyMeasurer {
		
		/**
		 * Constructor
		 * @param piece a GPiece
		 */
		public MeasurerVolume_m3(GPiece piece) {
			super (piece);
		}
		
		@Override
		public double getSurfacicMeasure(GPieceDisc disc, int cambialAgeMin, int cambialAgeMax) {
			return disc.getSurface_mm2(cambialAgeMin, cambialAgeMax);
		}
		
		@Override
		public double getVolumicMeasure(double integral) {
			// mm2*m* 1e-6 = m3 :
			return integral * 1e-6;
		}
	}

//	public static double getVolume_m3 (GPiece piece,
//			Compartment compartment) {
//		return new MeasurerVolume_m3 (piece).getMeasure (compartment);
//	}

	// Just for testing :
	public static class MeasurerLength_m extends PiecePropertyMeasurer {
		public MeasurerLength_m (GPiece piece) {
			super (piece);
		}
		public double getSurfacicMeasure (GPieceDisc disc,
				int cambialAgeMin, int cambialAgeMax) {
			return 1.0;
		}
		public double getVolumicMeasure (double integral) {
			return integral;
		}
	}

	// Mass and density  using a DiscMassProvider :

	// Mass per height unit (kg/m) :
	// cambialAgeMin and cambialAgeMax should be valid (wood, bark or pith)
	public static double getMass_kgpm (DiscMassProvider dmp, GPieceDisc disc,
			int cambialAgeMin, int cambialAgeMax)
	{
		return dmp == null ? 0. : dmp.getDiscMass_kgpm (disc, cambialAgeMin, cambialAgeMax);
	}

	// Basic density of a cylinder defined by 2 rings (included) :
	public static double getWoodBasicDensity_kgpm3 (DiscMassProvider dmp, GPieceDisc disc,
			int cambialAgeMin, int cambialAgeMax)
	{
		double R1 = disc.getIntRadius_mm(cambialAgeMax);
		double R2 = disc.getExtRadius_mm(cambialAgeMin);
		double surf_mm2 = Math.PI * (R2*R2 - R1*R1);
		double d = 0.;
		if (surf_mm2 > 0.) {
			double mass_kgpm = getMass_kgpm (dmp, disc, cambialAgeMin, cambialAgeMax);
			d = mass_kgpm / surf_mm2 * 1.e6;
		}
		return d;
	}

	// Basic density of a cylinder defined by 2 radius :
	// (result approximated to a cylinder defined by 2 ring limits)
	public static double getWoodBasicDensity_kgpm3 (DiscMassProvider dmp, GPieceDisc disc,
			double intRadius_mm, double extRadius_mm)
	{
		// CAUTION : argument in inverse order !
		return getWoodBasicDensity_kgpm3 (dmp, disc, disc.getCambialAge(extRadius_mm),
				disc.getCambialAge(intRadius_mm));
	}

	// Mean mass :
	public static class MeasurerMass_kg extends PiecePropertyMeasurer {
		private DiscMassProvider dmp;
		public MeasurerMass_kg (DiscMassProvider dmp, GPiece piece) {
			super (piece);
			this.dmp = dmp;
		}
		public double getSurfacicMeasure (GPieceDisc disc,
				int cambialAgeMin, int cambialAgeMax) {
			return getMass_kgpm (dmp, disc, cambialAgeMin, cambialAgeMax);
		}
		public double getVolumicMeasure (double integral) {
			// kg/m*m = kg :
			return integral;
		}
	}

	public static double getMass_kg (DiscMassProvider dmp, GPiece piece,
			Compartment compartment) {
		return new MeasurerMass_kg (dmp, piece).getMeasure (compartment);
	}
}
