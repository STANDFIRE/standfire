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

package capsis.extension.treelogger.geolog.simplelogging;

import java.util.ArrayList;
import java.util.List;

import repicea.simulation.treelogger.TreeLoggerParameters;
import jeeb.lib.util.Translator;
import capsis.extension.treelogger.GPiece;
import capsis.extension.treelogger.geolog.GeoLog;
import capsis.extension.treelogger.geolog.GeoLogTreeLoggerParameters;
import capsis.extension.treelogger.geolog.logcategories.FirewoodLogCategory;
import capsis.extension.treelogger.geolog.logcategories.GenericLogCategory;
import capsis.extension.treelogger.geolog.logcategories.GeoLogLogCategory;
import capsis.extension.treelogger.geolog.logcategories.ParticleBoardLogCategory;
import capsis.extension.treelogger.geolog.logcategories.StumpLogCategory;
import capsis.extension.treelogger.geolog.logcategories.TopLogCategory;
import capsis.extension.treelogger.geolog.util.PieceRingWidthRecord;
import capsis.extension.treelogger.geolog.util.PieceVolumeRecord;
import capsis.extension.treelogger.geolog.util.RecordMaker;

/**	SimpleLoggingStarter : simple starter for GeoLog
*
*	@author F. Mothe - marsh 2006
*/
public class SimpleLoggingTreeLoggerParameters extends GeoLogTreeLoggerParameters {

//	private static final long serialVersionUID = 20060325L;	// avoid java warning

	static {
		// Necessary since starter is created within woodqualityworkshop.TreeTab
		Translator.addBundle("capsis.extension.treelogger.geolog.simplelogging.SimpleLogging");
	}


//	static Data data = new Data ("SimpleLogging");

	// No constructor needed.
	// The initialisation is performed by createProducts () called by
	// the constructor of GeoLogStarter.

//	/**	This method returns true if the current starter is correct
//	*	(may be overrided in child classes)
//	*/
//	public boolean isCorrect () {
//		return super.isCorrect ();
//	}

	// Abstracts methods of GeoLogStarter :

//	//	Returns the local Data instance
//	public Data getData () {
//		return data;
//	}

	//	Add the products by increasing priority order
	@Override
	protected void setLogCategories() {
		// System.out.println ("SimpleLoggingStarter.createProducts ()");
		int id = 1;
		List<GeoLogLogCategory> categories = new ArrayList<GeoLogLogCategory>();
		getLogCategories().put(TreeLoggerParameters.ANY_SPECIES, categories);
		categories.add(new StumpLogCategory(id++, TreeLoggerParameters.ANY_SPECIES));
		//addProduct (new BasicProduct (id++, "SimpleLoggingStarter.P1.name", -1, false, 2.0, 4.0, 30, 0.0, true));
		//addProduct (new BasicProduct (id++, "SimpleLoggingStarter.P2.name",-1, true, 2.5, 3.5, 20, 1.0, true));
		categories.add(new GenericLogCategory(id++, Translator.swap("SimpleLoggingStarter.P1.name"),
				-1, false, 2.0, 4.0, 40, 1.0, true, -1, -1, -1));
		categories.add(new GenericLogCategory(id++, Translator.swap("SimpleLoggingStarter.P2.name"),
				-1, true, 2.0, 4.0, 30, 1.0, true, -1, -1, -1));
		categories.add(new GenericLogCategory(id++, Translator.swap("SimpleLoggingStarter.P3.name"),
				-1, true, 2.0, 2.0, 20, 1.0, true, -1, -1, -1));
		categories.add(new GenericLogCategory(id++, Translator.swap("SimpleLoggingStarter.P4.name"),
				-1, true, 2.0, 2.0, 10, 1.0, true, -1, -1, -1));
		categories.add(new TopLogCategory(id++, TreeLoggerParameters.ANY_SPECIES));

		// The user will have to change the priorities to get these ones :
		categories.add(new ParticleBoardLogCategory(id++, TreeLoggerParameters.ANY_SPECIES));
		categories.add(new FirewoodLogCategory (id++, TreeLoggerParameters.ANY_SPECIES));
	}

	//	Returns a factory of GPiece records for the export file
	//	(called by GeoLog)
	public RecordMaker <GPiece> makeRecordMaker (
			//~ MethodProvider mp)
			GeoLog.SpecialCase oldestTreeSpecial)
	{
		// Save only volume :
		// return new PieceVolumeRecord.Maker ();
		// Save rw + volume :
		return RecordMaker.merge (
			new PieceRingWidthRecord.Maker (),
			new PieceVolumeRecord.Maker ()
		);
	}

	public static void main(String[] args) {
		SimpleLoggingTreeLoggerParameters params = new SimpleLoggingTreeLoggerParameters();
		params.initializeDefaultLogCategories();
		params.showUI(null);
		System.exit(0);
	}
	
}
