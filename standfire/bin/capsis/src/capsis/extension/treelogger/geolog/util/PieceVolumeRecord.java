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

/**	PieceVolumeRecord : a numeric record for piece volume related data
*
*	@author F. Mothe - august 2006
*/

public class PieceVolumeRecord extends NumericRecord {

	// VARNUM_xxx, names & appendValuesToRecord () have to be synchro !!
	static final int VARNUM_v_m3 = 0;
	static final int VARNUM_vb_m3 = 1;

	static
	public String [] names = {
		"v_m3",		// total
		// "vw_m3",	// wood
		"vb_m3",		// bark
		 "vkc_m3",	// knotty core
		// "vck_m3",	// clear of knots
		// "vhw_m3",	// heartwood
		// "vsw_m3",	// sapwood
		// "vkhw_m3",	// knotty heartwood
		"vchw_m3",	// clear of knots heartwood
		// "vksw_m3",	// knotty sapwood
		"vcsw_m3",	// clear of knots sapwood
		"vjw_m3",		// juvenile wood
		// "vaw_m3",	// adult wood
	};

	public PieceVolumeRecord (GPiece piece) {
		super ();
		appendValuesToRecord (this, piece);
	}

	public double getVolume_m3 () {
		return vars.get (VARNUM_v_m3);
	}

	public double getWoodVolume_m3 () {
		//~ return vars.get (VARNUM_wv_m3);
		return getVolume_m3 () - getBarkVolume_m3 ();
	}

	public double getBarkVolume_m3 () {
		return vars.get (VARNUM_vb_m3);
	}

	static
	public void appendValuesToRecord (NumericRecord record, GPiece piece) {
		PieceUtil.MeasurerVolume_m3 meas =
			new PieceUtil.MeasurerVolume_m3 (piece);
		record.append (meas.getMeasure_WoodBark ());
		// record.append (meas.getMeasure_Wood ());
		record.append (meas.getMeasure_Bark ());
		record.append (meas.getMeasure_KnottyCore ());
		// record.append (meas.getMeasure_ClearKnot ());
		// record.append (meas.getMeasure_HeartWood ());
		// record.append (meas.getMeasure_SapWood ());
		// record.append (meas.getMeasure_KnottyHeartWood ());
		record.append (meas.getMeasure_ClearKnotHeartWood ());
		// record.append (meas.getMeasure_KnottySapWood ());
		record.append (meas.getMeasure_ClearKnotSapWood ());
		record.append (meas.getMeasure_JuvenileWood ());
		// record.append (meas.getMeasure_AdultWood ());
	}

	// Usage :
	//	RecordMaker <GPiece> maker = new PieceVolumeRecord.Maker ();
	//	NumericRecord record = maker.makeRecord (piece);
	//	double v = ((PieceVolumeRecord) record).getVolume_m3 ();
	//	String names = maker.makeTitle (",");
	static
	public class Maker extends RecordMaker <GPiece> {
		// Abstract methods of RecordMaker :
		public void appendValues (NumericRecord record, GPiece piece) {
			appendValuesToRecord (record, piece);
		}
		public void finaliseSumRecord (NumericRecord sumRecord, int firstIndex) {
			// Volumes are *not* averaged
		}
		public String makeTitle (String sep) {
			return super.makeTitle (names, sep);
		}
		public int getNbValues () {
			return names.length;
		}
		// Overrides RecordMaker.makeRecord to return a PieceVolumeRecord :
		public NumericRecord makeRecord (GPiece piece) {
			return new PieceVolumeRecord (piece);
		}
	}

}
