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

package fireparadox.extension.ioformat;


import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import jeeb.lib.sketch.scene.item.Polygon;
import jeeb.lib.util.AmapTools;
import jeeb.lib.util.Import;
import jeeb.lib.util.Log;
import jeeb.lib.util.Record;
import jeeb.lib.util.RecordSet;
import jeeb.lib.util.Translator;
import jeeb.lib.util.Vertex3d;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.lib.fire.fuelitem.FiSpecies;
import capsis.util.StandRecordSet;
import fireparadox.model.FmInitialParameters;
import fireparadox.model.FmModel;
import fireparadox.model.FmPlot;
import fireparadox.model.FmStand;
import fireparadox.model.plant.FmPlant;

/**	FireSVSLoader contains records description for .svs input
*	file.
*
*	@author K. Doyle - august 2010
*/
public class FireSVSLoader extends StandRecordSet {

	static {
		Translator.addBundle ("capsis.extension.ioformat.FireSVSLoader");
	}

	// Generic keyword record is described in superclass: key = value

	// SVS Terrain record, used to match the following lines
	// #PLOTORIGIN  0.00 0.00
	// #PLOTSIZE    100.00 100.00
	@Import
	static public class KeyWith2DoubleRecord extends Record {
		public KeyWith2DoubleRecord () {super ();}
		public KeyWith2DoubleRecord (String line) throws Exception {super (line);}

		// SVS files columns separators are blanks (this changes from the default tab)
		public String getSeparator () {return " ";}	// to change default "\t" separator
		
		public String key;
		public double a;
		public double b;
	}

	// Fire Paradox PolygonRecord record is described here
	@Import
	static public class PolygonRecord extends Record {
		public PolygonRecord () {super ();}
		public PolygonRecord (String line) throws Exception {super (line);}
		//public String getSeparator () {return ";";}	// to change default "\t" separator
		public int id;
		public Collection vertices;
	}

	// Fire Paradox tree record is described here
	@Import
	static public class TreeRecord extends Record {
		public TreeRecord () {super ();}
		public TreeRecord (String line) throws Exception {super (line);}

		// SVS files columns separators are blanks (this changes from the default tab)
		public String getSeparator () {return " ";}	// to change default "\t" separator
		
		public int species;
		public String plantID; // tree tag number (when inventory)
		public int classPlnt; // ???
		public int classCrwn; // ???
		public int treeStat; // Tree Status: 0 = dead, 1 = live, 2 = stump
		public double dbh; // in inches (should be converted to cm)
		public double height; // in feet (should be converted to m)
		public double lang; // lean angle (�) unused currently
		public double fang; // fall angle (�) unused currently
		public double crownEndDia; // top diameter of the stem (in inches), not
									// used correctly
		// SVS files accept 4 different crown ratio and crown base height
		public double crownRadius1; // radius in feet
		public double crownRatio1; // ratio between
									// (height-crownBaseHeight)/height o
		public double crownRadius2;
		public double crownRatio2;
		public double crownRadius3;
		public double crownRatio3;
		public double crownRadius4;
		public double crownRatio4;
		public double expansFactor; // should be ignored, related to number of
									// sample of this type tree per ha
		public int markCode; // unused
		public double xCoordinate; // in feet
		public double yCoordinate;
		public double zCoordinate; // zero will be used for z and topography
									// will be provided separately in an
									// additional file
		
	}
	
	// To be used to change inches and feet into cm and meters, set in the load method
	private boolean inputInInchesAndFeet = false;


	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public FireSVSLoader () {
		addAdditionalCommentMark(";");  // fc - sep 2010
	}

	

	/**	Direct constructor
	*/
	public FireSVSLoader (String fileName) throws Exception {
		this ();  // fc - sep 2010
		
		createRecordSet (fileName);
		
	}	// for direct use for Import

	/**
	 * Extension dynamic compatibility mechanism.
	 * This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	
	static public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof FmModel)) {return false;}

		} catch (Exception e) {
			Log.println (Log.ERROR, "FireSVSLoader.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
		return true;
	}

	@Override
	public void createRecordSet (String fileName) throws Exception {
		
		RecordSet.commentMark = "_";  // temporary change
		
		// Unknown lines will not result in a "wrong format" error
		// e.g. #UNITS       ENGLISH   (unused)
		setMemorizeWrongLines (true);
		
		super.createRecordSet (fileName);
		
		RecordSet.commentMark = "#";  // restore standard comment mark
	}


	/**
	 * RecordSet -> FiStand
	 * Implementation here.
	 * Was initialy described in FiModel.loadInitStand ()
	 * To load a stand for another model, recognize real type of model :
	 * if (model instanceof FiModel) -> this code
	 * if (model instanceof MobyDickModel) -> other code...
	 */
	@Override
	public GScene load (GModel model) throws Exception {

		FmModel m = (FmModel) model;
		FmInitialParameters settings = m.getSettings ();

		// Initializations
		FmStand initStand = new FmStand (m);
		initStand.setSourceName (source);		// generally fileName
		initStand.setDate (0);
		int maxId = 0;	//fc4.0
		double plotOriginX = 0;
		double plotOriginY = 0;
		double plotSizeX = 0;
		double plotSizeY = 0;
		int counter = 1;
		Vertex3d standOrigin = new Vertex3d (0, 0, 0);

		int speciesId = 1;
		FmPlot plot = null;

		//TODO get info from DB
		System.out.println("FireSVSLoader.load () : # of records : " + size());
		for (Iterator i = this.iterator (); i.hasNext ();) {
			Record record = (Record) i.next ();
			//System.out.println("FireSVSLoader record: "+record.getClass ().getName ()+" "+record);
			
			if (record instanceof FireSVSLoader.KeyWith2DoubleRecord) {
				FireSVSLoader.KeyWith2DoubleRecord r = (FireSVSLoader.KeyWith2DoubleRecord) record;	// cast to precise type
				// Read data about the terrain extension to create a FiPlot, see below
				if (r.key.trim ().equals ("#PLOTORIGIN")) {  // trim () removes the leading blank (see correctLine ())
					plotOriginX = r.a; 
					plotOriginY = r.b; 
					// System.out.println("FireSVSLoader PLOTORIGIN "+r.a+" "+r.b);
				} else if (r.key.trim ().equals ("#PLOTSIZE")) {
					plotSizeX = r.a; 
					plotSizeY = r.b; 
					// System.out.println("FireSVSLoader PLOTSIZE "+r.a+" "+r.b);
				}
			} else if (record instanceof FireSVSLoader.PolygonRecord) {
				FireSVSLoader.PolygonRecord r = (FireSVSLoader.PolygonRecord) record;	// cast to precise type
				if (plot == null) {
						throw new Exception ("Can not process a Polygon record before a Terrain record");}

				Polygon p = new Polygon (
						new ArrayList<Vertex3d> (AmapTools.toVertex3dCollection (r.vertices)));
				plot.add (p);

			} else if (record instanceof FireSVSLoader.TreeRecord) {
				FireSVSLoader.TreeRecord r = (FireSVSLoader.TreeRecord) record;	// cast to precise type
				maxId = counter;

			
				FiSpecies s = m.getSpecies(FmModel.PINUS_PONDEROSA_USFS1);
				// temporary workaround--need tree definitions for US

				int age = -1;
				double crownBaseHeight = r.height * (1d - r.crownRatio1);
		
				FmPlant tree = new FmPlant (counter,
						initStand, 
						m, // fc-2.2.2015
						age,
						r.xCoordinate,
						r.yCoordinate,
						r.zCoordinate,
						""+counter,
						r.dbh, 			// dbh CHECK THIS WITH FRANCOIS
						r.height,
						crownBaseHeight,
						//r.height*r.ratio1,
						r.crownRadius1*2,
						s,			// species
						0, 			// pop = 0 PhD 2008-09-25
						// FP : the information about the moisture may come from your SVS file, unless you prefer to compute it later
						// I suggest to put at least default values (see below)
						100d, // liveMoisture=0
						10d, // deadMoisture=0
						80d,// liveTwigMoisture=0
						false); // not from database...
						//0d, // liveMoisture=0
						//0d, // deadMoisture=0
						//0d); // liveTwigMoisture=0
				// adds tree in stand
				initStand.addTree (tree);
				counter++;
				

			} else if (record instanceof RecordSet.UnknownRecord) {
				RecordSet.UnknownRecord r = (RecordSet.UnknownRecord) record;	// cast to precise type
				if (r.line.startsWith("#UNITS")) {
					String aux = r.line.substring(6);
					if (aux.trim ().equals("ENGLISH")) {
						// Detect if the input file uses inches / feet, to be converted into cm / m
						inputInInchesAndFeet = true;  // Kyle, Russ, you can use this boolen to convert the inches / feet values into cm / m
					}
					
				}

			} else if (record instanceof FireSVSLoader.KeyRecord) {
				FireSVSLoader.KeyRecord r = (FireSVSLoader.KeyRecord) record;	// cast to precise type

				System.out.println ("record="+r);		// Automatic toString ()

			}
		}
		
		System.out.println("inputInInchesAndFeet = "+inputInInchesAndFeet);
		
		// Deal with the plot (a flat terrain)
		double standWidth = plotSizeX - plotOriginX;
		double standHeight = plotSizeY - plotOriginY;
		standOrigin = new Vertex3d (plotOriginX, plotOriginY, 0);
		Rectangle.Double rectangle = new Rectangle.Double (plotOriginX, plotOriginY,
				standWidth, standHeight);
		plot = new FmPlot (initStand, "SVS plot",
				10, 0, rectangle, settings);  // cellWidth = 10, maybe not useful here, altitude = 0
		initStand.setPlot (plot);

		initStand.setOrigin (standOrigin);
		initStand.setXSize (standWidth);
		initStand.setYSize (standHeight);
		initStand.setArea (initStand.getXSize () * initStand.getYSize ());

		// Test the plot
		if (initStand.getPlot () == null) {
				throw new Exception ("missing Terrain in file, could not create plot; aborted");}

		// Init treeIdDispenser (to get new ids for regeneration)
		m.getTreeIdDispenser ().setCurrentValue (maxId);

		// All trees added in stand : plot creation (not for all models)
		//~ initStand.createPlot (m, 10);

		return initStand;
	}



	////////////////////////////////////////////////// Extension stuff
	/**
	 * From Extension interface.
	 */
	public String getName () {return Translator.swap ("FireSVSLoader");}

	/**
	 * From Extension interface.
	 */
	public String getVersion () {return VERSION;}
	public static final String VERSION = "1.0";

	/**
	 * From Extension interface.
	 */
	public String getAuthor () {return "K. Doyle";}

	/**
	 * From Extension interface.
	 */
	public String getDescription () {return Translator.swap ("FireSVSLoader.description");}


	////////////////////////////////////////////////// IOFormat stuff
	public boolean isImport () {return true;}
	public boolean isExport () {return true;}

}
