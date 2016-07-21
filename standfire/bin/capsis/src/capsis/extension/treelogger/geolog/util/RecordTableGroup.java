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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**	RecordTableGroup : a table of groups of NumericRecords
*	with fixed nb of records by group
*
*	@author F. Mothe - august 2006
*/

public class RecordTableGroup {

	// Table of records :
	class RecordTable {
		private Vector <NumericRecord> records;
		public RecordTable (int nbRecords, int nbValues) {
			records = new Vector <NumericRecord> ();
			for (int p=0; p<nbRecords; p++) {
				records.add (new NumericRecord (nbValues));
			}
		}
		public NumericRecord get (int n) {
			return records.get (n);
		}
	}

	private Map <Integer, RecordTable> groups;
	private int nbRecordsByGroup;
	private int nbVariables;

	public RecordTableGroup (int nbRecordsByGroup, int nbVariables) {
		this.nbRecordsByGroup = nbRecordsByGroup;
		this.nbVariables = nbVariables;
		groups = new HashMap <Integer, RecordTable> ();
	}

	public boolean classExists (int groupId) {
		return groups.containsKey (groupId);
	}

	// Returns the corresponding RecordTable
	// creates it if it does not exist
	public RecordTable getGroup (int groupId) {
		if (!classExists (groupId)) {
			groups.put (groupId,
					new RecordTable (nbRecordsByGroup, nbVariables));
		}
		return groups.get (groupId);
	}

	// Returns the corresponding NumericRecord
	// creates the RecordTable if it does not exist
	public NumericRecord getRecord (int nRecord, int groupId) {
		return getGroup (groupId).get (nRecord);
	}

	// returns the corresponding NumericRecord (which *must* exist !)
	public NumericRecord getExistingRecord (int nRecord, int groupId) {
		return groups.get (groupId).get (nRecord);
	}

	public int getNbGroups () {
		return groups.size ();
	}

	public int getnbRecordsByGroup () {
		return nbRecordsByGroup;
	}

	public Set <Integer> getGroupsId () {
		return groups.keySet ();
	}

}
