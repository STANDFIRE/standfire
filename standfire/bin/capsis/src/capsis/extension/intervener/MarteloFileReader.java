/* 
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 2000-2010  Francois de Coligny
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
package capsis.extension.intervener;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import jeeb.lib.util.AmapTools;
import jeeb.lib.util.Check;
import jeeb.lib.util.Log;

/**	A file reader for the MarteloThinner.
 * 
 * 	<pre>
 * 
 *	MarteloFileReader r = new MarteloFileReader (fileName);
 * 	r.interpret ();
 * 	
 * 	String[] columnNames = r.getColumnNames ();
 * 	// lines: col0 contains Integer ids, other cols are Strings
 * 	Object[][] lines = r.getLines ();
 * 
 * 	</pre>
 * 
 * @author F. de Coligny - september 2010
 */
public class MarteloFileReader {

	private String fileName;
	private String[] columnNames;
	private Object[][] lines;
	private String commentString;
	private String separator;  // may be a single character of a regex


	/**	Constructor
	 */
	public MarteloFileReader (String fileName) {
		this.fileName = fileName;
		commentString = "#";
		separator = ";";
	}
	
	/**	Reads the file
	 */
	public void interpret () throws Exception {
		BufferedReader r = null;
		try {
			r = new BufferedReader (new FileReader (fileName));
			
			String line = null;
			boolean foundHeader = false;
			List<String[]> data = new ArrayList<String[]> ();
			
			while ( (line = r.readLine ()) != null ) {
				if (line.startsWith (commentString)) {continue;}  // comment, ignored
				
				if (!foundHeader) {
					foundHeader = true;
					columnNames = cutLine (line);
					
				} else {
					String[] tokens = cutLine (line);
					data.add (tokens);
				}
			}
			
			if (!foundHeader) {throw new Exception ("Could not find header: column names sparated by "+separator);}
			
			this.lines = new Object [data.size ()][columnNames.length];
			int i = 0;
			for (String[] l : data) {
				
				// Check the size of the line
				if (l.length != columnNames.length) {
					String lineTrace = "";
					for (int k = 0; k < l.length; k++) {lineTrace += l[k] + ";";}
					throw new Exception ("Wrong size for this line: found "+l.length+" columns instead of "
							+columnNames.length+" columns read in the file header \n"+lineTrace);
				}
				
				for (int j = 0; j < columnNames.length; j++)  {
					
					// Values in col 0 are ints -> instantiate an Integer 
					// (better sorting in the JTable later)
					if (j == 0) {
						this.lines[i][j] = new Integer (l[j]);
					} else {
						// Other columns are Strings
						this.lines[i][j] = l[j];
					}
					
				}
				i++;
				
			}
				
		} catch (Exception e) {
			throw e;
		} finally {
			r.close ();  // close the file properly
		}
	}

	/**	Cuts the line into several Strings	
	 */
	private String[] cutLine (String line) {
		
		line = line.replace (separator, separator+" ");  // To process correctly "21;;;;" -> "21 ; ; ; ;". The extra spaces are trimmed below
		
		List<String> tokens = new ArrayList<String> ();
		for (StringTokenizer st = new StringTokenizer (line, separator); st.hasMoreTokens();) {
			String t = st.nextToken().trim ();
			tokens.add (t);
		}
		return tokens.toArray (new String[tokens.size ()]);
		
		
	}

	
	/**	Returns the column names
	 */
	public String[] getColumnNames () {
		return columnNames;
	}

	/**	Returns the other lines
	 */
	public Object[][] getLines () {
		return lines;
	}
	
}
