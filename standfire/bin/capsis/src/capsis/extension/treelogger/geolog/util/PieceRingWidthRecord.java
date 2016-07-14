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

/**	PieceRingWidthRecord : a numeric record of ring widths of a GPiece
*
*	@author F. Mothe - august 2006
*/

public class PieceRingWidthRecord extends NumericRecord {
	static
	public String [] names = {
		// "rw_mm",		// total
		"rww_mm",	// wood
		// "rwb_mm",		// bark
		// "rwkc_mm",	// knotty core
		// "rwck_mm",	// clear of knots
		// "rwhw_mm",	// heartwood
		// "rwsw_mm",	// sapwood
		// "rwkhw_mm",	// knotty heartwood
		"rwchw_mm",	// clear of knots heartwood
		// "rwksw_mm",	// knotty sapwood
		// "rwcsw_mm",	// clear of knots sapwood
		// "rwjw_mm",		// juvenile wood
		// "rwaw_mm",	// adult wood
	};

	public PieceRingWidthRecord (GPiece piece) {
		super ();
		appendValuesToRecord (this, piece);
	}

	static
	public void appendValuesToRecord (NumericRecord record, GPiece piece) {
		PieceUtil.MeasurerRingWidth_mm meas =
			new PieceUtil.MeasurerRingWidth_mm (piece);
		// record.append (meas.getMeasure_WoodBark ());
		record.append (meas.getMeasure_Wood ());
		// record.append (meas.getMeasure_Bark ());
		// record.append (meas.getMeasure_KnottyCore ());
		// record.append (meas.getMeasure_ClearKnot ());
		// record.append (meas.getMeasure_HeartWood ());
		// record.append (meas.getMeasure_SapWood ());
		// record.append (meas.getMeasure_KnottyHeartWood ());
		record.append (meas.getMeasure_ClearKnotHeartWood ());
		// record.append (meas.getMeasure_KnottySapWood ());
		// record.append (meas.getMeasure_ClearKnotSapWood ());
		// record.append (meas.getMeasure_JuvenileWood ());
		// record.append (meas.getMeasure_AdultWood ());
	}

	// Usage :
	//	RecordMaker <GPiece> maker = new PieceRingWidthRecord.Maker ();
	//	NumericRecord record = maker.makeRecord (piece);
	//	String names = maker.makeTitle (",");
	static
	public class Maker extends RecordMaker <GPiece> {
		// Abstract methods of RecordMaker :
		public void appendValues (NumericRecord record, GPiece piece) {
			appendValuesToRecord (record, piece);
		}
		// Compute the averaged ring widths (the sum does not make sense) :
		public void finaliseSumRecord (NumericRecord sumRecord, int firstIndex) {
			averageSumRecord (sumRecord, firstIndex, getNbValues ());
		}
		public String makeTitle (String sep) {
			return super.makeTitle (names, sep);
		}
		public int getNbValues () {
			return names.length;
		}
		// Overrides RecordMaker.makeRecord to return a PieceRingWidthRecord :
		public NumericRecord makeRecord (GPiece piece) {
			return new PieceRingWidthRecord (piece);
		}
	}

}
