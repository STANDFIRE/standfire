package capsis.lib.forestgales;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import jeeb.lib.util.Import;
import jeeb.lib.util.Record;
import jeeb.lib.util.RecordSet;

/**
 * A tool to load a ForestGales stand file.
 *
 * @author T.Labbe, C.Meredieu - May 2014
 */
public class FGStandLoader extends RecordSet {

	private String fileName;
	protected Collection<StandRecord> standRecords;
	protected Collection<String> unknownLines;


	// Species line format
	@Import
	static public class StandRecord extends Record {

		public StandRecord () {
			super ();
		}

		public StandRecord (String line) throws Exception {
			super (line);
		}

		public String id;
		public int soilType;
		public int rootingDepth;
		public double nha;
		public double hdom;
		public String species;
		public double meanDbh_m;
		public double meanHeight;
		public double meanCrownWidth;
		public double meanCrownDepth;
		public double meanStemVolume;
		public double meanStemWeight;
		public double meanCrownVolume;
		public double meanCrownWeight;

	}

	/**
	 * Constructor 1: reads the given file
	 */
	public FGStandLoader (String fileName) throws Exception {
		super ();
		this.fileName = fileName;
		createRecordSet (fileName);

	}
	//	ForestGales stand level file Interpretation.
	//	Returns an exit code.
	//	0: Ok, use getStandRecords () to get the records
	//	1: some record were not recognized, use getStandRecords () to get the records
	//		AND use getUnknownStandLines () to get the unknown records (as Strings)
	//
	public int load () throws Exception {
		standRecords = new ArrayList<StandRecord> ();
		unknownLines = new ArrayList<String> ();
		int exitCode = 0;

		for (Iterator i = this.iterator (); i.hasNext ();) {
			Object record = i.next ();

			if (record instanceof StandRecord) {
				StandRecord r = (StandRecord) record;	// cast to precise type
				standRecords.add (r);
			} else if (record instanceof UnknownRecord){	// UnknownRecords
				exitCode = 1;
				//~ System.out.println (""+record);
				unknownLines.add (record.toString ());
			} else {
				// should never happen due to setMemorizeWrongLines (true) in constructor
				throw new Exception ("Wrong format in input file, NOT considered as an UnknownRecord...");
			}
		}
		return exitCode;
	}

	public Collection<StandRecord> getRecords () {return standRecords;}
	public Collection<String> getUnknownLines () {return unknownLines;}

}
