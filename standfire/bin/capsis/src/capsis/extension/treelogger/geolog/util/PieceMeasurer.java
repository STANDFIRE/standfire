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

/**	PieceMeasurer : class used from PieceUtil to compute
*	volume measurements (at the piece level) from
*	surfacic disc measurements.
*	It may be used for properties like volume and mass.
*
*	@author F. Mothe - marsh 2006
*/
public abstract class PieceMeasurer {

	private interface DiscMeasurer {
		public double getSurfacicMeasure (GPieceDisc disc);
	}
	private GPiece piece;

	// Constructor :
	public PieceMeasurer (GPiece piece) {
		this.piece = piece;
	}

	// Abstract methods :

	// Method returning the disc measurement to be integrated :
	// - it should work for wood, pith and bark
	// - it does not need to verify if the ring range is valid
	//  (see getSurfacicMeasure_Verified)
	public abstract double getSurfacicMeasure (GPieceDisc disc,
			int cambialAgeMin, int cambialAgeMax) ;

	// Method returning the volumic measurement from the integrated value :
	public abstract double getVolumicMeasure (double integral) ;

	public GPiece getPiece () {
		return piece;
	}

	/*
	// In PieceUtil for symplifying the caller code :
	public enum Compartment {
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
	*/

	public double getMeasure (PieceUtil.Compartment compartment) {
		switch (compartment) {
		case TOTAL : return getMeasure_Total ();
		case WOOD_BARK : return getMeasure_WoodBark ();
		case WOOD : return getMeasure_Wood ();
		case BARK : return getMeasure_Bark ();
		case PITH : return getMeasure_Pith ();
		case HEARTWOOD : return getMeasure_HeartWood ();
		case SAPWOOD : return getMeasure_SapWood ();
		case KNOTTYCORE : return getMeasure_KnottyCore ();
		case CLEARKNOT : return getMeasure_ClearKnot ();
		case KNOTTYCORE_HEARTWOOD : return getMeasure_KnottyHeartWood ();
		case CLEARKNOT_HEARTWOOD : return getMeasure_ClearKnotHeartWood ();
		case KNOTTYCORE_SAPWOOD : return getMeasure_KnottySapWood ();
		case CLEARKNOT_SAPWOOD : return getMeasure_ClearKnotSapWood ();
		case JUVENILE : return getMeasure_JuvenileWood ();
		case ADULT : return getMeasure_AdultWood ();
		default : return 0.0;
		}
	}

	public double getMeasure_Total () {
		double integral = getIntegral (new DiscMeasurer () {
			public double getSurfacicMeasure (GPieceDisc disc) {
				int cambialAgeMin = 0;
//				int cambialAgeMax = PieceUtil.getNbWoodRings (disc) + 1;
				int cambialAgeMax = disc.getNbWoodRings() + 1;
				return PieceMeasurer.this.getSurfacicMeasure (
						disc, cambialAgeMin, cambialAgeMax);
			}
		});
		return getVolumicMeasure (integral);
	}

	public double getMeasure_WoodBark () {
		double integral = getIntegral (new DiscMeasurer () {
			public double getSurfacicMeasure (GPieceDisc disc) {
				int cambialAgeMin = 0;
//				int cambialAgeMax = PieceUtil.getNbWoodRings (disc);
				int cambialAgeMax = disc.getNbWoodRings();
				return PieceMeasurer.this.getSurfacicMeasure (
						disc, cambialAgeMin, cambialAgeMax);
			}
		});
		return getVolumicMeasure (integral);
	}

	public double getMeasure_Wood () {
		double integral = getIntegral (new DiscMeasurer () {
			public double getSurfacicMeasure (GPieceDisc disc) {
				int cambialAgeMin = 1;
//				int cambialAgeMax = PieceUtil.getNbWoodRings (disc);
				int cambialAgeMax = disc.getNbWoodRings();
				return getSurfacicMeasure_Verified (
						disc, cambialAgeMin, cambialAgeMax);
			}
		});
		return getVolumicMeasure (integral);
	}

	public double getMeasure_Bark () {
		double integral = getIntegral (new DiscMeasurer () {
			public double getSurfacicMeasure (GPieceDisc disc) {
				int cambialAgeMin = 0;
				int cambialAgeMax = 0;
				return PieceMeasurer.this.getSurfacicMeasure (
						disc, cambialAgeMin, cambialAgeMax);
			}
		});
		return getVolumicMeasure (integral);
	}

	public double getMeasure_Pith () {
		double integral = getIntegral (new DiscMeasurer () {
			public double getSurfacicMeasure (GPieceDisc disc) {
//				int cambialAgeMin = PieceUtil.getNbWoodRings (disc) + 1;
				int cambialAgeMin = disc.getNbWoodRings() + 1;
				int cambialAgeMax = cambialAgeMin;
				return PieceMeasurer.this.getSurfacicMeasure (
						disc, cambialAgeMin, cambialAgeMax);
			}
		});
		return getVolumicMeasure (integral);
	}

	public double getMeasure_HeartWood () {
		double integral = getIntegral (new DiscMeasurer () {
			public double getSurfacicMeasure (GPieceDisc disc) {
				int cambialAgeMin = disc.getNbSapWoodRings() + 1;
//				int cambialAgeMax = PieceUtil.getNbWoodRings (disc);
				int cambialAgeMax = disc.getNbWoodRings();
				return getSurfacicMeasure_Verified (
						disc, cambialAgeMin, cambialAgeMax);
			}
		});
		return getVolumicMeasure (integral);
	}

	public double getMeasure_SapWood () {
		double integral = getIntegral (new DiscMeasurer () {
			public double getSurfacicMeasure (GPieceDisc disc) {
				int cambialAgeMin = 1;
				int cambialAgeMax = disc.getNbSapWoodRings();
				return getSurfacicMeasure_Verified (
						disc, cambialAgeMin, cambialAgeMax);
			}
		});
		return getVolumicMeasure (integral);
	}

	public double getMeasure_KnottyCore () {
		double integral = getIntegral (new DiscMeasurer () {
			public double getSurfacicMeasure (GPieceDisc disc) {
				int cambialAgeMin = disc.getNbClearKnotRings() + 1;
				int cambialAgeMax = disc.getNbWoodRings();
				return getSurfacicMeasure_Verified (
						disc, cambialAgeMin, cambialAgeMax);
			}
		});
		return getVolumicMeasure (integral);
	}

	public double getMeasure_ClearKnot () {
		double integral = getIntegral (new DiscMeasurer () {
			public double getSurfacicMeasure (GPieceDisc disc) {
				int cambialAgeMin = 1;
				int cambialAgeMax = disc.getNbClearKnotRings();
				return getSurfacicMeasure_Verified (
						disc, cambialAgeMin, cambialAgeMax);
			}
		});
		return getVolumicMeasure (integral);
	}

	public double getMeasure_KnottyHeartWood () {
		double integral = getIntegral (new DiscMeasurer () {
			public double getSurfacicMeasure (GPieceDisc disc) {
				int cambialAgeMin = Math.max (
					disc.getNbSapWoodRings(),
					disc.getNbClearKnotRings()
				) + 1;
				int cambialAgeMax = disc.getNbWoodRings();
				return getSurfacicMeasure_Verified (
						disc, cambialAgeMin, cambialAgeMax);
			}
		});
		return getVolumicMeasure (integral);
	}

	public double getMeasure_ClearKnotHeartWood () {
		double integral = getIntegral (new DiscMeasurer () {
			public double getSurfacicMeasure (GPieceDisc disc) {
				int cambialAgeMin = disc.getNbSapWoodRings() + 1;
				int cambialAgeMax = disc.getNbClearKnotRings();
				return getSurfacicMeasure_Verified (
						disc, cambialAgeMin, cambialAgeMax);
			}
		});
		return getVolumicMeasure (integral);
	}

	public double getMeasure_KnottySapWood () {
		double integral = getIntegral (new DiscMeasurer () {
			public double getSurfacicMeasure (GPieceDisc disc) {
				int cambialAgeMin = disc.getNbClearKnotRings() + 1;
				int cambialAgeMax = disc.getNbSapWoodRings();
				return getSurfacicMeasure_Verified (
						disc, cambialAgeMin, cambialAgeMax);
			}
		});
		return getVolumicMeasure (integral);
	}

	public double getMeasure_ClearKnotSapWood () {
		double integral = getIntegral (new DiscMeasurer () {
			public double getSurfacicMeasure (GPieceDisc disc) {
				int cambialAgeMin = 1;
				int cambialAgeMax = Math.min (disc.getNbSapWoodRings(), disc.getNbClearKnotRings());
				return getSurfacicMeasure_Verified (
						disc, cambialAgeMin, cambialAgeMax);
			}
		});
		return getVolumicMeasure (integral);
	}

	public double getMeasure_JuvenileWood () {
		double integral = getIntegral (new DiscMeasurer () {
			public double getSurfacicMeasure (GPieceDisc disc) {
				int cambialAgeMin = disc.getNbJuvenileWoodRings() + 1;
				int cambialAgeMax = disc.getNbWoodRings();
				return getSurfacicMeasure_Verified (
						disc, cambialAgeMin, cambialAgeMax);
			}
		});
		return getVolumicMeasure (integral);
	}

	public double getMeasure_AdultWood () {
		double integral = getIntegral (new DiscMeasurer () {
			public double getSurfacicMeasure (GPieceDisc disc) {
				int cambialAgeMin = 1;
				int cambialAgeMax = disc.getNbJuvenileWoodRings();
				return getSurfacicMeasure_Verified (
						disc, cambialAgeMin, cambialAgeMax);
			}
		});
		return getVolumicMeasure (integral);
	}

	// Verify if the ring range is valid before calling getSurfacicMeasure () :
	private double getSurfacicMeasure_Verified (GPieceDisc disc,
			int cambialAgeMin, int cambialAgeMax) {
		return cambialAgeMax >= cambialAgeMin
				? getSurfacicMeasure (disc, cambialAgeMin, cambialAgeMax)
				: 0.0;
	}

	//	getIntegral () compute a measurement for the whole piece by
	//	integration  of surfacic disc measurements.
	//	It returns the sum from n=0 to nbDiscs-2 of :
	//		(measure[n+1] + measure[n]) / 2.0 * (height[n+1] - height[n])
	private double getIntegral (DiscMeasurer dm) {
		double sum = 0.0;
		boolean firstDisc = true;
		double measure1 = 0.0;
		double height1 = 0.0;
		for (Object o : piece.getDiscs()) {
			GPieceDisc disc2 = (GPieceDisc) o;
			double measure2 = dm.getSurfacicMeasure (disc2);
			double height2 = disc2.getHeight_m();// PieceUtil.getHeight_m (disc2);
			if (firstDisc) {
				firstDisc = false;
			} else if (height2 > height1) {
				// the discs should be sorted by increasing height
				sum += (measure2 + measure1) / 2.0 * (height2 - height1) ;
			} else if (height2 == height1) {
				// two discs at the same height
				// may occur if pieces of height=0 are accepted
				// nothing to do
			} else {
				System.out.println ("PieceMeasurer.getIntegral : discs not sorted !");
				// System.out.println ("h1=" + height1 + " h2=" + height2);
			}
			measure1 = measure2;
			height1 = height2;
		}
		return sum;
	}
}

