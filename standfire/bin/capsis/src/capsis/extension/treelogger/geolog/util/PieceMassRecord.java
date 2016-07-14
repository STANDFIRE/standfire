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
import capsis.kernel.MethodProvider;
import capsis.util.methodprovider.DiscMassProvider;

/**	PieceMassRecord : a numeric record for piece mass related data
*	using a DiscMassProvider
*
*	@author F. Mothe - august 2006
*/

public class PieceMassRecord extends NumericRecord {
	public static class Default_DiscMassProvider
			 implements DiscMassProvider
	{
		private static final long serialVersionUID = 20080901L;	// avoid java warning
		public double getDiscMass_kgpm (GPieceDisc disc,
			int cambialAgeMin, int cambialAgeMax)
		{
			return 0.;
		}
	}

	// VARNUM_xxx, names & appendValuesToRecord () have to be synchro !!
	static final int VARNUM_m_kg = 0;
	static final int VARNUM_mb_kg = 1;

	static
	public String [] names = {
		"m_kg",		// total
		// "vw_kg",	// wood
		"mb_kg",		// bark
		 "mkc_kg",	// knotty core
		// "mck_kg",	// clear of knots
		// "mhw_kg",	// heartwood
		// "msw_kg",	// sapwood
		// "mkhw_kg",	// knotty heartwood
		"mchw_kg",	// clear of knots heartwood
		// "mksw_kg",	// knotty sapwood
		"mcsw_kg",	// clear of knots sapwood
		"mjw_kg",		// juvenile wood
		// "maw_kg",	// adult wood
	};

	//~ public PieceMassRecord (DiscMassProvider dmp, GPiece piece) {
		//~ super ();
		//~ appendValuesToRecord (dmp, this, piece);
	//~ }

	//~ public PieceMassRecord (MethodProvider mp, GPiece piece) {
		//~ super ();
		//~ appendValuesToRecord (new Default_DiscMassProvider (), this, piece);
	//~ }
	public PieceMassRecord (MethodProvider mp, GPiece piece) {
		super ();
		// ARRANGER :
		DiscMassProvider dmp = mp instanceof DiscMassProvider
				? (DiscMassProvider) mp
				: new Default_DiscMassProvider ();
		appendValuesToRecord (dmp, this, piece);
	}

	public double getMass_kg () {
		return vars.get (VARNUM_m_kg);
	}

	public double getWoodMass_kg () {
		//~ return vars.get (VARNUM_wv_m3);
		return getMass_kg () - getBarkMass_kg ();
	}

	public double getBarkMass_kg () {
		return vars.get (VARNUM_mb_kg);
	}

	static
	public void appendValuesToRecord (DiscMassProvider dmp, NumericRecord record,
			GPiece piece)
	{
		PieceUtil.MeasurerMass_kg meas =
			new PieceUtil.MeasurerMass_kg (dmp, piece);
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
	//	RecordMaker <GPiece> maker = new PieceMassRecord.Maker (mp);
	//	NumericRecord record = maker.makeRecord (piece);
	//	String names = maker.makeTitle (",");
	static
	public class Maker extends RecordMaker <GPiece> {
		private DiscMassProvider dmp;

		public Maker (MethodProvider mp) {
			this.dmp = mp instanceof DiscMassProvider
					? (DiscMassProvider) mp
					: new Default_DiscMassProvider ();
		}
		// Abstract methods of RecordMaker :
		public void appendValues (NumericRecord record, GPiece piece) {
			appendValuesToRecord (dmp, record, piece);
		}
		public void finaliseSumRecord (NumericRecord sumRecord, int firstIndex) {
			// Masses are *not* averaged
		}
		public String makeTitle (String sep) {
			return super.makeTitle (names, sep);
		}
		public int getNbValues () {
			return names.length;
		}
		// Overrides RecordMaker.makeRecord to return a PieceMassRecord :
		public NumericRecord makeRecord (GPiece piece) {
			return new PieceMassRecord ((MethodProvider) dmp, piece);
		}
	}

}
