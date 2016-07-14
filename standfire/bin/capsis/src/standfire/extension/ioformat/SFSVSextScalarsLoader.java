/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 2000-2003 Francois de Coligny
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */

package standfire.extension.ioformat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jeeb.lib.util.Import;
import jeeb.lib.util.Log;
import jeeb.lib.util.Record;
import jeeb.lib.util.RecordSet;
import jeeb.lib.util.Translator;
import standfire.model.SFModel;
import capsis.lib.fire.fuelitem.FiLayer;

/**
 * SFSVSextTreesLoader loads scalars files from the SVS extended format. This StandRecordSet has a
 * method "add" to avoid overwriting of the scene
 * 
 * @author F. Pimont, R. Parsons - June 2013
 */
public class SFSVSextScalarsLoader extends RecordSet {

	static {
		Translator.addBundle ("standfire.model.SFSVSextScalarsLoader");
	}


	// Standfire scalar record from SVS is described here
	@Import
	static public class ScalarRecord extends Record {

		public ScalarRecord () {
			super ();
		}

		public ScalarRecord (String line) throws Exception {
			super (line);
		}

		// SVS files columns separators are commas (this changes from the default tab)
		public String getSeparator () {
			return ",";
		} // to change default "\t" separator

		public double shrubload; // shrub  tons/acre
		public double herbload; // herb  tons/acre
		public double litterload; // litter tons/acre
		public double duffload; // duff  tons/acre


	}


	/**
	 * Constructor. Only to ask for extension properties (authorName, version...).
	 */
	public SFSVSextScalarsLoader () {
		addAdditionalCommentMark ("\"");
	}


	/**
	 * Direct constructor
	 */

	public SFSVSextScalarsLoader (String fileName) throws Exception {
		this (); // fc - sep 2010
		createRecordSet (fileName);
	} // for direct use for Import

	/**
	 * Extension dynamic compatibility mechanism. This matchwith method checks if the extension can
	 * deal (i.e. is compatible) with the referent.
	 */

	static public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof SFModel)) { return false; }

		} catch (Exception e) {
			Log.println (Log.ERROR, "SFLoader.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
		return true;
	}

	@Override
	public void createRecordSet (String fileName) throws Exception {

		RecordSet.commentMark = "_"; // temporary change

		// Unknown lines will not result in a "wrong format" error
		// e.g. #UNITS ENGLISH (unused)
		setMemorizeWrongLines (true);

		super.createRecordSet (fileName);

		RecordSet.commentMark = "#"; // restore standard comment mark
	}

	/**
	 * return the 4 value of loads for shrub, herb, litter and duff
	 * 
	 * @return
	 * @throws Exception
	 */
	public Map<String,Double> getLoads ()
	throws Exception {
		Map<String,Double> loads = new HashMap<String,Double> ();
		System.out.println ("SFSVSextScalarsLoader.load () : # of records : " + size ());
		
		for (Iterator i = this.iterator (); i.hasNext ();) {
			Record record = (Record) i.next ();

			if (record instanceof SFSVSextScalarsLoader.ScalarRecord) {
				SFSVSextScalarsLoader.ScalarRecord r = (SFSVSextScalarsLoader.ScalarRecord) record;

				double shrubload = r.shrubload * 1000.0 / 4046.85642; // tons/acre -> kg/m2
				double herbload = r.herbload * 1000.0 / 4046.85642; // tons/acre -> kg/m2
				double litterload = r.litterload * 1000.0 / 4046.85642; // tons/acre -> kg/m2
				double duffload = r.duffload * 1000.0 / 4046.85642; // tons/acre -> kg/m2
				System.out.println ("	loads in kg/m2:" + shrubload + "," + herbload + "," + litterload + "," + duffload);

				loads.put (FiLayer.SHRUB, shrubload);
				loads.put (FiLayer.HERB, herbload);
				loads.put (FiLayer.LITTER, litterload);
				// loads.put (FiLayer.DUFF, duffload);

			} else {
				throw new Exception ("SFSVSextScalarsLoader, unknown line: " + record);
			}
		}
		return loads;
	}


	// //////////////////////////////////////////////// Extension stuff
	/**
	 * From Extension interface.
	 */
	public String getName () {
		return Translator.swap ("SFSVSextScalarsLoader");
	}

	/**
	 * From Extension interface.
	 */
	public String getVersion () {
		return VERSION;
	}

	public static final String VERSION = "1.0";

	/**
	 * From Extension interface.
	 */
	public String getAuthor () {
		return "F. Pimont";
	}

	/**
	 * From Extension interface.
	 */
	public String getDescription () {
		return Translator.swap ("SFSVSextTreesLoader.description");
	}

	// //////////////////////////////////////////////// IOFormat stuff
	public boolean isImport () {
		return true;
	}

	public boolean isExport () {
		return true;
	}

}
