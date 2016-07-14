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

package capsis.extension.treelogger.geolog.species;

import capsis.extension.treelogger.GPiece;
import capsis.extension.treelogger.geolog.GeoLogTreeLoggerParameters;
import capsis.extension.treelogger.geolog.util.NumericRecord;
import capsis.extension.treelogger.geolog.util.PieceMassRecord;
import capsis.extension.treelogger.geolog.util.PiecePriceRecord;
import capsis.extension.treelogger.geolog.util.PieceRingWidthRecord;
import capsis.extension.treelogger.geolog.util.PieceVolumeRecord;
import capsis.extension.treelogger.geolog.util.PieceYieldRecord;
import capsis.extension.treelogger.geolog.util.RecordMaker;
import capsis.kernel.MethodProvider;

/**	FgRecordMaker : factory of GPiece records for Fagacées (oak and beech)
*
*	@author F. Mothe - february 2006
*/

public class FgRecordMaker extends RecordMaker <GPiece> {

	/* TODO :
		A simplifier, beaucoup trop compliqué...
		Problème = on a besoin du volume et du starter pour calculer le prix
		donc priceMaker ne peut pas être un RecordMaker <GPiece>
		Sinon on pourrait faire :
		static
		public RecordMaker <GPiece> getMaker (GeoLogStarter starter, boolean withMasses) {
			Vector <RecordMaker <GPiece>> makers = new Vector <RecordMaker <GPiece>> ();
			makers.add (new PieceRingWidthRecord.Maker ());
			makers.add (new PieceVolumeRecord.Maker ());
			makers.add (withMasses ? new FgOakPieceMassRecord.Maker () : new RecordMaker.Null <GPiece> ());
			return RecordMaker.merge (makers);
		}
	*/
	private RecordMaker <GPiece> rwMaker;
	private RecordMaker <GPiece> volumeMaker;
	private RecordMaker <GPiece> massMaker;
	private RecordMaker <PiecePriceRecord.Data> priceMaker;
	private RecordMaker <PieceYieldRecord.Data> yieldMaker;

	private boolean withPrices;
	private boolean withYields;

	// Constructor :
	public FgRecordMaker (GeoLogTreeLoggerParameters starter, MethodProvider mp,
			boolean withMasses, boolean withPrices, boolean withYields)
	{
		this.withPrices = withPrices;
		this.withYields = withYields;
		this.rwMaker = new PieceRingWidthRecord.Maker ();
		this.volumeMaker = new PieceVolumeRecord.Maker ();
		this.massMaker = withMasses
			? new PieceMassRecord.Maker (mp)
			: new RecordMaker.Null <GPiece> ()
		;

		if (withPrices) {
			this.priceMaker = new PiecePriceRecord.Maker (starter);
		} else {
			this.priceMaker = new RecordMaker.Null <PiecePriceRecord.Data> ();
		}

		if (withYields) {
			this.yieldMaker = new PieceYieldRecord.Maker (mp, starter);
		} else {
			this.yieldMaker = new RecordMaker.Null <PieceYieldRecord.Data> ();
		}
	}

	// Abstract methods of RecordMaker :

	public void appendValues (NumericRecord record, GPiece piece) {
		rwMaker.appendValues (record, piece);
		//~ volumeMaker.appendValues (record, piece);
		//~ massMaker.appendValues (record, piece);
		PieceVolumeRecord volRecord =
				(PieceVolumeRecord) volumeMaker.makeRecord (piece);
		record.append (volRecord.getValues ());
		PieceMassRecord masRecord =
				(PieceMassRecord) massMaker.makeRecord (piece);
		record.append (masRecord.getValues ());
		if (withPrices) {
			double v_m3 = volRecord.getVolume_m3 ();
			priceMaker.appendValues (record,
					new PiecePriceRecord.Data (piece, v_m3));
		}
		if (withYields) {
			double vw_m3 = volRecord.getWoodVolume_m3 ();
			double mw_kg = masRecord.getWoodMass_kg ();
			yieldMaker.appendValues (record,
					new PieceYieldRecord.Data (piece, vw_m3, mw_kg));
		}
	}

	public void finaliseSumRecord (NumericRecord sumRecord, int firstIndex) {
		int index = firstIndex;
		rwMaker.finaliseSumRecord (sumRecord, index);
		index += rwMaker.getNbValues ();

		volumeMaker.finaliseSumRecord (sumRecord, index);
		index += volumeMaker.getNbValues ();

		massMaker.finaliseSumRecord (sumRecord, index);
		index += massMaker.getNbValues ();

		priceMaker.finaliseSumRecord (sumRecord, index);
		index += priceMaker.getNbValues ();

		yieldMaker.finaliseSumRecord (sumRecord, index);
		index += yieldMaker.getNbValues ();

	}

	public String makeTitle (String sep) {
		return rwMaker.makeTitle (sep)
			+ volumeMaker.makeTitle (sep)
			+ massMaker.makeTitle (sep)
			+ priceMaker.makeTitle (sep)
			+ yieldMaker.makeTitle (sep)
		;
	}

	public int getNbValues () {
		return rwMaker.getNbValues ()
			+ volumeMaker.getNbValues ()
			+ massMaker.getNbValues ()
			+ priceMaker.getNbValues ()
			+ yieldMaker.getNbValues ()
		;
	}

}
