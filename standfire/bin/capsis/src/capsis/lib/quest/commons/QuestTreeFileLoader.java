package capsis.lib.quest.commons;

import java.util.List;

import jeeb.lib.util.Record;
import jeeb.lib.util.fileloader.FileLoader;

/**
 * A file loader for a tree input files.
 * 
 * @author F. de Coligny - March 2015
 */
public class QuestTreeFileLoader extends FileLoader {

	public int treeId;
	public String species;

	public List<DbhHeightRecord> dbhHeightRecords;

	@Override
	protected void checks() throws Exception {

	}

	// REMOVED treeId is now a public instance variable, no need for an accessor
//	/**
//	 * Returns the treeId extracted from the first dbhHeightRecord, or -1 if
//	 * trouble.
//	 */
//	public int getTreeId() {
//		try {
//			return treeId;
//		} catch (Exception e) {
//			return -1;
//		}
//	}

	/**
	 * A line in the input file
	 */
	static public class DbhHeightRecord extends Record {
		
		public double dbh_cm;
		public double height_m;

		/**
		 * Constructor (super constructor is automatic, based on introspection).
		 * 
		 * @param line
		 *            : the line to be turned into a DbhHeightRecord object
		 * @throws Exception
		 *             : if the line format does not match a DbhHeightRecord
		 */
		public DbhHeightRecord(String line) throws Exception {
			super(line);
		}

		@Override
		public String getSeparator() {
			// Fields are separated by blanks
			return " ";
		}
	}

}
