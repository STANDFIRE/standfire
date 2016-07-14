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

package capsis.extension.treelogger.log2job;

import java.awt.Container;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import repicea.simulation.treelogger.TreeLoggerParameters;

/**	A Starter for Log2Job. It contains everything to run
*	the job without dialog.
*
*	@author F. de Coligny - december 2005
*/
public class Log2JobTreeLoggerParameters extends TreeLoggerParameters<Log2JobLogCategory> {

	protected static final String ANY = "Any";
	
	// example, there may be several other properties here
	public int numberOfLogsInTheTree = 1;
	// other properties here...
	
	private Log2JobTreeLoggerParametersDialog guiInterface;

	public Log2JobTreeLoggerParameters() {
		super(Log2Job.class);
	}
	
	/**	
	 * This method returns true if the current starter is correct
	 * a good execution of Log2Job.
	 */
	@Override
	public boolean isCorrect () {
		if (numberOfLogsInTheTree < 1) {return false;}
		// other tests here...
		return true;	// if all the tests were passed
	}


	@Override
	public void initializeDefaultLogCategories() {
		getLogCategories().clear();
		List<Log2JobLogCategory> logCategories = new ArrayList<Log2JobLogCategory>();
		getLogCategories().put(ANY, logCategories);
		logCategories.add(new Log2JobLogCategory());
	}


	@Override
	public Log2JobTreeLoggerParametersDialog getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new Log2JobTreeLoggerParametersDialog((Window) parent, this);
		}
		return guiInterface;
	}

	
	public static void main(String[] args) {
		Log2JobTreeLoggerParameters params = new Log2JobTreeLoggerParameters();
		params.initializeDefaultLogCategories();
		params.showUI(null);
		System.exit(0);
	}
	
	
}
