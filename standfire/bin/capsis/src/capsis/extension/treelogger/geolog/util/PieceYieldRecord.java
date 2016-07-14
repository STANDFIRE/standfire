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

import java.util.Vector;

import capsis.extension.treelogger.GPiece;
import capsis.extension.treelogger.GPieceDisc;
import capsis.extension.treelogger.geolog.GeoLogTreeLoggerParameters;
import capsis.extension.treelogger.geolog.logcategories.GeoLogLogCategory;
import capsis.kernel.MethodProvider;
import capsis.util.methodprovider.DiscMassProvider;

/**	PieceYieldRecord : a numeric record for piece useful volume and yield
*
*	@author F. Mothe - november 2006
*/

public class PieceYieldRecord extends NumericRecord {

	// VARNUM_xxx, names & appendValuesToRecord () have to be synchro !!
	static final int VARNUM_vw_m3 = 0;
	static final int VARNUM_mw_kg = 1;
	static final int VARNUM_vu_m3 = 2;
	static final int VARNUM_mu_kg = 3;
	static final int VARNUM_yield_pct = 4;
	static final int VARNUM_du_kgpm3 = 5;

	static
	public String [] names = {
		"vw_m3",		// total wood volume (may be also in PieceVolumeRecord, no matter...)
		"mw_kg",		// total wood mass (may be also in PieceVolumeRecord, no matter...)
		"vu_m3",		// useful volume
		"mu_kg",		// useful mass
		"yield_%",		// vu_m3 / vw_m3 * 100
		"du_kgpm3",	// density of usable wood = mu_kg / vu_m3
	};

	public PieceYieldRecord (DiscMassProvider dmp, GPiece piece,
			double vw_m3, double mw_kg, GeoLogTreeLoggerParameters starter)
	{
		super ();
		appendValuesToRecord (dmp, this, piece, vw_m3, mw_kg, starter);
	}

	public PieceYieldRecord (DiscMassProvider dmp, GPiece piece, GeoLogTreeLoggerParameters starter) {
		super ();
		PieceUtil.MeasurerVolume_m3 measVol =
			new PieceUtil.MeasurerVolume_m3 (piece);
		double vw_m3 = measVol.getMeasure_Wood ();
		PieceUtil.MeasurerMass_kg measMas =
			new PieceUtil.MeasurerMass_kg (dmp, piece);
		double mw_kg = measMas.getMeasure_Wood ();
		appendValuesToRecord (dmp, this, piece, vw_m3, mw_kg, starter);
	}

	static public void appendValuesToRecord (DiscMassProvider dmp,
			NumericRecord record, 
			GPiece piece,
			double vw_m3, 
			double mw_kg, 
			GeoLogTreeLoggerParameters starter) {
//		double medianDiam_cm = PieceUtil.getmedianDiameter_cm (piece);
//		GeoLogLogCategory product = (GeoLogLogCategory) starter.getTreeLogCategory (piece.pieceProduct);	// cast added MFortin 2010-02-01 because the GeoLogStarter now implements the LogProcessingStarter interface
		GeoLogLogCategory product = (GeoLogLogCategory) piece.getLogCategory();	
//		GeoLogProduct product = starter.getProduct (piece.pieceProduct);
//		double yield = product.getYield (piece, vw_m3);
		double yield = 0.0;
		try {
			yield = product.getYieldFromThisPiece (piece);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double vu_m3 = yield * vw_m3;
		double mu_kg = 0.;
		double du_kgpm3 = 0.;
		if (vu_m3 > 0.) {
			double r [] = product.getMinMaxUsefulRadius_mm (piece);
			if (r != null && r [0] < r [1]) {
				// Average wood density of the cylinder defined by r [0] and r [1] :
				GPieceDisc topDisc = piece.getTopDisc();
				GPieceDisc botDisc = piece.getBottomDisc();
				du_kgpm3 = (PieceUtil.getWoodBasicDensity_kgpm3 (dmp, topDisc, r [0], r [1]) +
					PieceUtil.getWoodBasicDensity_kgpm3 (dmp, botDisc,  r [0], r [1])) / 2;
			} else if (vw_m3 > 0.) {
				// Average wood density of the piece :
				du_kgpm3 = mw_kg / vw_m3;
			}
			mu_kg = du_kgpm3 * vu_m3;
		}

		record.append (vw_m3);
		record.append (mw_kg);
		record.append (vu_m3);
		record.append (mu_kg);
		record.append (yield * 100);
		record.append (du_kgpm3);
	}

	static
	public class Data {
		public GPiece piece;
		public double vw_m3;
		public double mw_kg;
		public
		Data (GPiece piece, double vw_m3, double mw_kg) {
			this.piece = piece;
			this.vw_m3 = vw_m3;
			this.mw_kg = mw_kg;
		}
	}

	// Usage :
	//	double v = ((PieceYieldRecord) record).getVolume_m3 ();
	//	RecordMaker <PieceYieldRecord.Data> maker = new PieceYieldRecord.Maker ();
	//	maker.appendValues (record, new PieceYieldRecord.Data (piece, v));
	static
	public class Maker extends RecordMaker <Data> {
		private GeoLogTreeLoggerParameters starter;
		private DiscMassProvider dmp;

		public Maker (MethodProvider mp, GeoLogTreeLoggerParameters starter) {
			this.starter = starter;
			this.dmp = mp instanceof DiscMassProvider
					? (DiscMassProvider) mp
					: new PieceMassRecord.Default_DiscMassProvider ();
		}
		// Abstract methods of RecordMaker :
		public void appendValues (NumericRecord record, Data data) {
			appendValuesToRecord (dmp, record, data.piece, data.vw_m3, data.mw_kg, starter);
		}
		public void finaliseSumRecord (NumericRecord sumRecord, int firstIndex) {
			// Volumes and masses are *not* averaged
			// Density and yield are recomputed :
			Vector <Double> values = sumRecord.getValues ();
			double vw_m3 = values.get (firstIndex + VARNUM_vw_m3);
			double yield_pct = 0.;
			double du_kgpm3 = 0.;
			if (vw_m3 > 0.) {
				double vu_m3 = values.get (firstIndex + VARNUM_vu_m3);
				double mu_kg = values.get (firstIndex + VARNUM_mu_kg);
				du_kgpm3 = vu_m3 > 0. ? mu_kg / vu_m3 : 0.;
				yield_pct = vu_m3 / vw_m3 * 100;
			}
			values.set (firstIndex + VARNUM_yield_pct, yield_pct);
			values.set (firstIndex + VARNUM_du_kgpm3, du_kgpm3);
		}
		public String makeTitle (String sep) {
			return super.makeTitle (names, sep);
		}
		public int getNbValues () {
			return names.length;
		}
		/*
		// Overrides RecordMaker.makeRecord to return a PieceMassRecord :
		public NumericRecord makeRecord (Data data) {
			return new PieceYieldRecord (dmp, data.piece, data.vw_m3, starter);
		}
		*/
	}

}
