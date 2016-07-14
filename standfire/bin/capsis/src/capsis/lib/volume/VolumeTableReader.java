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

package capsis.lib.volume;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import jeeb.lib.util.Import;
import jeeb.lib.util.Record;
import jeeb.lib.util.RecordSet;

/**
 * A format description to read a file containing a list of interventions
 * CAUTION : all years should be present for all species in the file, and correctly sorted
 *
 * @author G. Lagarrigues - December 2013
 */
public class VolumeTableReader extends RecordSet {

	/**
	 * One CommandLine per tree
	 */
	@Import
	static public class CommandLine extends Record {
		public CommandLine() {
			super();
		}

		public CommandLine(String line) throws Exception {
			super(line);
		}

		// The fields below are in columns separated by tabs
		public String volumeTableId;
		public double diameter;
		public double volume;

	}

	public String volumeTableName; // the script reads this file (passed as a parameter)
	private TreeMap<String,TreeMap<Double,Double>> formatedTable = new TreeMap<String,TreeMap<Double,Double>>(); // the script reads this file (passed as a parameter)

	/**
	 * Default constructor.
	 */
	public VolumeTableReader (String volumeTableName) throws Exception {
		super();
		this.volumeTableName = volumeTableName;
		createRecordSet(volumeTableName);
		formatTable();
	}


	/**
	 * A convenient accessor to get the command lines in a list.
	 */
	public ArrayList<CommandLine> getCommandLines() {
		return new ArrayList<CommandLine>((Collection) this);
	}

	/**
	 * Format table into a map
	 */
	private void formatTable() {
		ArrayList<CommandLine> lines = getCommandLines();
		TreeMap<Double,Double> subTable;

		for (CommandLine line : lines) {
			if(!formatedTable.containsKey(line.volumeTableId)) {
				subTable = new TreeMap<Double,Double>();
				subTable.put(line.diameter,line.volume);
				formatedTable.put(line.volumeTableId,new TreeMap<Double,Double>(subTable));
			} else {
				subTable = formatedTable.get(line.volumeTableId);
				subTable.put(line.diameter,line.volume);
			}
		}
	}

	/**
	 * Get the a volume table adressing its name
	 */
	public TreeMap<Double,Double> getTable(String volumeTableName) {
		return formatedTable.get(volumeTableName);
	}

	/**
	 * Get a set of the table names
	 */
	public Set<String> getVolumeTableNames() {
		return formatedTable.keySet();
	}


}
