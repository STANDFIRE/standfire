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

import java.util.Iterator;
import java.util.Map;

import jeeb.lib.util.Import;
import jeeb.lib.util.Log;
import jeeb.lib.util.Record;
import jeeb.lib.util.RecordSet;
import jeeb.lib.util.Translator;
import jeeb.lib.util.Vertex3d;
import standfire.model.SFInitialParameters;
import standfire.model.SFModel;
import standfire.model.SFScene;
import standfire.model.SFTree;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.lib.fire.fuelitem.FiSpecies;
import capsis.util.StandRecordSet;

/**
 * SFSVSextTreesLoader loads trees files from the SVS extended format.
 * 
 * @author F. Pimont, R. Parsons - June 2013
 */
public class SFSVSextTreesLoader extends StandRecordSet {

	static {
		Translator.addBundle ("standfire.model.SFSVSextTreesLoader");
	}


	// Standfire tree record from SVS is described here
	@Import
	static public class TreeRecord extends Record {

		public TreeRecord () {
			super ();
		}

		public TreeRecord (String line) throws Exception {
			super (line);
		}

		// SVS files columns separators are commas (this changes from the default tab)
		public String getSeparator () {
			return ",";
		} // to change default "\t" separator

		public double xloc; // feet
		public double yloc; // feet
		public String species; // 4 letter species code
		public double dbh; // inches
		public double ht; // feet
		public double crwdth; // diameter in feet
		public double cratio; // % of tree crown height over tree height
		public double crownwt0; // foliage wt (lbs)
		public double crownwt1; // <0.25 inch in diameter wt (lbs)
		public double crownwt2; // 0.25< <1 inch in diameter wt (lbs)
		public double crownwt3; // 1< <3 inch in diameter wt (lbs)
	}



	private Map<String,FiSpecies> speciesMap;

	/**
	 * Constructor. Only to ask for extension properties (authorName, version...).
	 */
	public SFSVSextTreesLoader () {
		addAdditionalCommentMark ("\"");
	}

	/**
	 * Direct constructor
	 */
	
	public SFSVSextTreesLoader (String fileName, Map<String,FiSpecies> speciesMap) throws Exception {
		this (); // fc - sep 2010
		this.speciesMap = speciesMap;
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
	 * RecordSet -> FiStand Implementation here. Was initialy described in FiModel.loadinitScene ()
	 * To load a stand for another model, recognize real type of model : if (model instanceof
	 * FiModel) -> this code if (model instanceof MobyDickModel) -> other code...
	 */
	@Override
	public GScene load (GModel model) throws Exception {

		SFModel m = (SFModel) model;
		SFInitialParameters settings = m.getSettings ();

		// Scene Initializations
		SFScene initScene = new SFScene ();
		initScene.rnd = m.rnd;
		initScene.setSourceName (source); // generally fileName
		initScene.setDate (0);
		double sceneOriginX = settings.sceneOriginX;
		double sceneOriginY = settings.sceneOriginY;
		double sceneSizeX = settings.sceneSizeX;
		double sceneSizeY = settings.sceneSizeY;
		initScene.setOrigin (new Vertex3d (sceneOriginX, sceneOriginY, 0));
		initScene.setXSize (sceneSizeX);
		initScene.setYSize (sceneSizeY);
		initScene.setArea (initScene.getXSize () * initScene.getYSize ());
		// int speciesId = 1;
//		SFPlot plot = null;

		// trees
		System.out.println ("SFSVSextTreesLoader.load () : # of records : " + size ());
		for (Iterator i = this.iterator (); i.hasNext ();) {
			Record record = (Record) i.next ();
			// System.out.println ("SFSVSextTreesLoader record: " + record);

			if (record instanceof SFSVSextTreesLoader.TreeRecord) {
				SFSVSextTreesLoader.TreeRecord r = (SFSVSextTreesLoader.TreeRecord) record;
				
				initScene.maxId++;
				double x_m = r.xloc * 0.3048;
				double y_m = r.yloc * 0.3048;
				double dbh_cm = r.dbh * 2.54; // inch -> cm
				double height_m = r.ht * 0.3048; // foot -> m
				double crownBaseHeight_m = (1d - 0.01 * r.cratio) * height_m;
				double crownDiameter_m = r.crwdth * 0.3048;
				double crownwt0_kg = r.crownwt0 * 0.4536; // lbs -> kg
				double crownwt1_kg = r.crownwt1 * 0.4536; // lbs -> kg
				double crownwt2_kg = r.crownwt2 * 0.4536; // lbs -> kg
				double crownwt3_kg = r.crownwt3 * 0.4536; // lbs -> kg
				
				String speciesName = r.species.replace ('"', ' ');
				speciesName = speciesName.trim ();
				// System.out.println ("SFSVSextTreesLoader: species.trim (): "+speciesName);

				if (!speciesMap.containsKey (speciesName)) { throw new Exception (
						"SFSVSextTreesLoader, unknown species: " + speciesName); }
				FiSpecies sp = speciesMap.get (speciesName);
				
				SFTree t = new SFTree (initScene.maxId, 
						initScene, 
						m, // fc-2.2.2015
						x_m, 
						y_m, 
						0, // z 
						dbh_cm, 
						height_m, 
						crownBaseHeight_m, 
						crownDiameter_m, 
						sp, 
						
						crownwt0_kg, 
						crownwt1_kg, 
						crownwt2_kg, 
						crownwt3_kg);

				initScene.addTree (t);
						//System.out.println(" scene extention "+initScene.getOrigin().x+","+initScene.getOrigin().y+","+initScene.getXSize()+","+initScene.getYSize());	
			} else {
				throw new Exception ("SFSVSextTreesLoader, unknown line: "+record);
			}
		}

		// Deal with the scene
//		double standWidth = plotSizeX - plotOriginX;
//		double standHeight = plotSizeY - plotOriginY;
//		standOrigin = new Vertex3d (plotOriginX, plotOriginY, 0);
//		Rectangle.Double rectangle = new Rectangle.Double (plotOriginX, plotOriginY, standWidth, standHeight);
//		plot = new FiPlot (initScene, "SVS plot", 10, 0, rectangle, settings); // cellWidth = 10,
//																				// maybe not useful
//																				// here, altitude =
//																				// 0
//		initScene.setPlot (plot);
//

		//		
//
//		// Test the plot
//		if (initScene.getPlot () == null) { throw new Exception (
//				"missing Terrain in file, could not create plot; aborted"); }
//
//		// Init treeIdDispenser (to get new ids for regeneration)
//		m.getTreeIdDispenser ().setCurrentValue (maxId);
//
//		// All trees added in stand : plot creation (not for all models)
//		// ~ initScene.createPlot (m, 10);

		return initScene;
	}
		
	// //////////////////////////////////////////////// Extension stuff
	/**
	 * From Extension interface.
	 */
	public String getName () {
		return Translator.swap ("SFSVSextTreesLoader");
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
		return "F. Pimont, R. Parsons";
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
