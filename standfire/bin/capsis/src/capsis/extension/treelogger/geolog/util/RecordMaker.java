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

import java.util.Collection;
import java.util.Vector;

/**	RecordMaker : abstract factory of numeric records
*	created using a data object of class D
*
*	@author F. Mothe - august 2006
*/

public abstract class RecordMaker <D> {
	// Returns the variable names (beginning with a separator) :
	abstract
	public String makeTitle (String sep);

	// Returns the number of variables
	abstract
	public int getNbValues ();

	// TODO : should be protected !
	// Appends the variables to record (as new values)
	abstract
	public void appendValues (NumericRecord record, D data);

	// Make the final treatment of sumRecord (e.g. computes the weighted averages)
	// The concerned variables are in the range [ firstIndex, firstIndex+getNbValues () [
	abstract
	public void finaliseSumRecord (NumericRecord sumRecord, int firstIndex);

	// Returns a new initialised record
	// (may be overrided to return a specialised class)
	public NumericRecord makeRecord (D data) {
		NumericRecord record = new NumericRecord ();
		appendValues (record, data);
		return record;
	}

	// Appends the variable names to string
	public void appendTitle (String string, String sep) {
		string += makeTitle (sep);
	}

	// Make the final treatment of sumRecord (e.g. computes the weighted averages)
	public void finaliseSumRecord (NumericRecord sumRecord) {
		finaliseSumRecord (sumRecord, 0);
	}

	// Some static utility methods and classes :

	static
	public String makeTitle (String [] names, String sep) {
		String s = "";
		for (String n : names) {
			s += sep + n;
		}
		return s;
	}

	// Computes the weighted averages of sumRecord  for the variables
	// in the range [ firstIndex, firstIndex+getNbValues () [
	static
	public void averageSumRecord (NumericRecord sumRecord,
			int firstIndex, int nbValues)
	{
		Vector <Double> values = sumRecord.getValues ();
		double weight = sumRecord.getWeight ();
		if (weight > 0) {
			for (int nval=0; nval<nbValues; ++nval) {
				double average = values.get (firstIndex + nval) / weight;
				values.set (firstIndex + nval, average);
			}
		}
	}

	// Empty record maker class
	// Usage :	RecordMaker <GPiece> maker = (withVolumes) ?
	//		new PieceVolumeRecord.Maker () : new RecordMaker.Null <GPiece> ()
	static
	public class Null <D> extends RecordMaker <D> {
		// Abstract methods of RecordMaker :
		public void appendValues (NumericRecord record, D data) {}
		public void finaliseSumRecord (NumericRecord sumRecord,
				int firstIndex) {}
		public String makeTitle (String sep) {
			// Not beginning with a sep !
			return "";
		}
		public int getNbValues () {
			return 0;
		}
	}

	// Return a RecordMaker merging 2 RecordMakers
	// Usage :	maker = RecordMaker.merge (
	//			new PieceVolumeRecord.Maker (),
	//			new PieceRingWidthRecord.Maker ()
	//		);
	static
	public <D> RecordMaker <D> merge (final RecordMaker <D> maker1,
			final RecordMaker <D> maker2)
	{
		/*
		if (maker1 == null)
			return maker2;
		else if (maker2 == null)
			return maker1;
		else return new RecordMaker <D> () {
		*/
		return new RecordMaker <D> () {
			public String makeTitle (String sep) {
				return maker1.makeTitle (sep) + maker2.makeTitle (sep);
			}
			public int getNbValues () {
				return maker1.getNbValues () + maker2.getNbValues ();
			}
			public void appendValues (NumericRecord record, D data) {
				maker1.appendValues (record, data);
				maker2.appendValues (record, data);
			}
			public void finaliseSumRecord (NumericRecord sumRecord, int firstIndex) {
				maker1.finaliseSumRecord (sumRecord, firstIndex);
				maker2.finaliseSumRecord (sumRecord,
						maker1.getNbValues () + firstIndex);
			}
		};
	}

	// Return a RecordMaker merging a collection of RecordMakers
	static
	public <D> RecordMaker <D> merge (
			Collection <RecordMaker <D> > makers)
	{
		// RecordMaker <D> merged = null;
		RecordMaker <D> merged = new Null <D> ();
		for (RecordMaker <D> maker : makers) {
			merged = merge (merged, maker);
		}
		return merged;
	}

}
