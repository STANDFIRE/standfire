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

import repicea.simulation.treelogger.WoodPiece.Property;
import capsis.extension.treelogger.GPiece;
import capsis.extension.treelogger.geolog.GeoLogTreeLoggerParameters;
import capsis.extension.treelogger.geolog.logcategories.GeoLogLogCategory;

/**	PiecePriceRecord : a numeric record for piece price
*
*	@author F. Mothe - november 2006
*/

public class PiecePriceRecord extends NumericRecord {
	static
	public String [] names = {
		"price_E",
	};

	public PiecePriceRecord (GPiece piece, double v_m3, GeoLogTreeLoggerParameters starter) {
		super ();
		appendValuesToRecord (this, piece, v_m3, starter);
	}

	public PiecePriceRecord (GPiece piece, GeoLogTreeLoggerParameters starter) {
		super ();
		PieceUtil.MeasurerVolume_m3 meas =
			new PieceUtil.MeasurerVolume_m3 (piece);
		double v_m3 = meas.getMeasure_WoodBark ();
		appendValuesToRecord (this, piece, v_m3, starter);
	}

	static
	public void appendValuesToRecord (NumericRecord record, GPiece piece,
			double v_m3, GeoLogTreeLoggerParameters starter) {
		double medianDiam_cm = piece.getProperty(Property.medianDiameter_cm);
		GeoLogLogCategory product = (GeoLogLogCategory) piece.getLogCategory(); // cast added MFortin2010-02-01
		double price_Epm3 = product.priceModel.getPrice_Epm3 (medianDiam_cm);
		double price_E = price_Epm3 * v_m3;
		record.append (price_E);
	}

	static
	public class Data {
		public GPiece piece;
		public double v_m3;
		public
		Data (GPiece piece, double v_m3) {
			this.piece = piece;
			this.v_m3 = v_m3;
		}
	}

	// Usage :
	//	double v = ((PiecePriceRecord) record).getVolume_m3 ();
	//	RecordMaker <PiecePriceRecord.Data> maker = new PiecePriceRecord.Maker ();
	//	maker.appendValues (record, new PiecePriceRecord.Data (piece, v));
	static
	public class Maker extends RecordMaker <Data> {
		private GeoLogTreeLoggerParameters starter;

		public
		Maker (GeoLogTreeLoggerParameters starter) {
			this.starter = starter;
		}

		// Abstract methods of RecordMaker :
		public void appendValues (NumericRecord record, Data data) {
			appendValuesToRecord (record, data.piece, data.v_m3, starter);
		}
		public void finaliseSumRecord (NumericRecord sumRecord, int firstIndex) {
			// Prices are *not* averaged
		}
		public String makeTitle (String sep) {
			return super.makeTitle (names, sep);
		}
		public int getNbValues () {
			return names.length;
		}
		// Overrides RecordMaker.makeRecord to return a PiecePriceRecord :
		public NumericRecord makeRecord (Data data) {
			return new PiecePriceRecord (data.piece, data.v_m3, starter);
		}
	}

}
