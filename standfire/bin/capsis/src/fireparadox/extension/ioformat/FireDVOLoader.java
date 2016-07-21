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

// This extension is for sapin model.
// It may become usable by other models: implement other constructors
// ex: public FireDVOLoader (MobyDickStand stand) throws Exception {
// with: import mobydick.model.*;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import jeeb.lib.sketch.scene.item.Polygon;
import jeeb.lib.util.AmapTools;
import jeeb.lib.util.Import;
import jeeb.lib.util.Log;
import jeeb.lib.util.Record;
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

/**	FireDVOLoader contains records description for tree collection input
*	scene file.
*
*	@author O. Vigy, E. Rigaud - september 2006
*/
public class FireDVOLoader extends StandRecordSet {

	static {
		Translator.addBundle ("capsis.extension.ioformat.FireDVOLoader");
	}

	// Generic keyword record is described in superclass: key = value

	// Fire Paradox Terrain record is described here - a Rectangle with an altitude
	@Import
	static public class TerrainRecord extends Record {
		public TerrainRecord () {super ();}
		public TerrainRecord (String line) throws Exception {super (line);}
		//public String getSeparator () {return ";";}	// to change default "\t" separator
		public String name;
		public double cellWidth;
		public double altitude;
		public double xMin;
		public double yMin;
		public double xMax;
		public double yMax;
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

	// Fire Paradox species record is described here
	//~ static public class SpeciesRecord extends Record {
		//~ public SpeciesRecord () {super ();}
		//~ public SpeciesRecord (String line) throws Exception {super (line);}
		//~ //public String getSeparator () {return ";";}	// to change default "\t" separator
		//~ public int id;
		//~ public String code;
		//~ public String name;
	//~ }

	// Fire Paradox tree record is described here
	@Import
	static public class TreeRecord extends Record {
		public TreeRecord () {super ();}
		public TreeRecord (String line) throws Exception {super (line);}
		//public String getSeparator () {return ";";}	// to change default "\t" separator
		public int fileId;
		public String speciesName;	// ex: Pinus halepensis
		public double x;
		public double y;
		public double z;
		//~ public String dbFuelId;
		public double height;
		public double crownBaseHeight;
		public double crownDiameter;
		//public double crownDiameterHeight;
		public boolean openess;
	}


	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public FireDVOLoader () {}

	

	/**	Direct constructor
	*/
	public FireDVOLoader (String fileName) throws Exception {createRecordSet (fileName);}	// for direct use for Import

	/**
	 * Extension dynamic compatibility mechanism.
	 * This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	
	static public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof FmModel)) {return false;}

		} catch (Exception e) {
			Log.println (Log.ERROR, "FireDVOLoader.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
		return true;
	}

	@Override
	public void createRecordSet (String fileName) throws Exception {super.createRecordSet (fileName);}


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
		double standWidth = 0;
		double standHeight = 0;
		Vertex3d standOrigin = new Vertex3d (0, 0, 0);

		int speciesId = 1;
		FmPlot plot = null;

		//TODO get info from DB
System.out.println ("FireDVOLoader.load () : # of records : "+size ());
		for (Iterator i = this.iterator (); i.hasNext ();) {
			Record record = (Record) i.next ();

			// fc - 14.5.2007
			if (record instanceof FireDVOLoader.TerrainRecord) {
				FireDVOLoader.TerrainRecord r = (FireDVOLoader.TerrainRecord) record;	// cast to precise type
				standWidth = r.xMax-r.xMin;
				standHeight = r.yMax-r.yMin;
				standOrigin = new Vertex3d (r.xMin, r.yMin, r.altitude);
				Rectangle.Double rectangle = new Rectangle.Double (r.xMin, r.yMin,
						standWidth, standHeight);
				plot = new FmPlot (initStand, r.name,
						r.cellWidth, r.altitude, rectangle, settings);
				initStand.setPlot (plot);

				initStand.setOrigin (standOrigin);
				initStand.setXSize (standWidth);
				initStand.setYSize (standHeight);
				initStand.setArea (initStand.getXSize () * initStand.getYSize ());


			//~ } else if (record instanceof FireDVOLoader.SpeciesRecord) {
				//~ FireDVOLoader.SpeciesRecord r = (FireDVOLoader.SpeciesRecord) record;	// cast to precise type

		//~ // file should contain only species name.
		//~ // species id (value) and short name (code) should be read in fueldb
		//~ // fc + ov - 10.7.2007 - to be done

				//~ // fc + ov - 25.9.2007
				//~ int speciesValue = FiSpecies.getValue (r.name);

				//~ FiSpecies s = new FiSpecies (speciesValue, r.code, r.name, FiSpecies.getSpecimen (),
						//~ Translator.swap ("FiSpecies.species"), FiSpecies.TRAIT_RESINEOUS, FiSpecies.SPECIES_TAXON_LEVEL);
				//~ speciesMap.put (r.id, s);


			} else if (record instanceof FireDVOLoader.PolygonRecord) {
				FireDVOLoader.PolygonRecord r = (FireDVOLoader.PolygonRecord) record;	// cast to precise type

				if (plot == null) {
						throw new Exception ("Can not process a Polygon record before a Terrain record");}

				//~ FirePolygon p = new FirePolygon (r.vertices);
				Polygon p = new Polygon (
						new ArrayList<Vertex3d> (AmapTools.toVertex3dCollection (r.vertices)));
				plot.add (p);

			} else if (record instanceof FireDVOLoader.TreeRecord) {
				FireDVOLoader.TreeRecord r = (FireDVOLoader.TreeRecord) record;	// cast to precise type
				if (r.fileId > maxId) {maxId = r.fileId;}

				FiSpecies s = m.getSpecies (r.speciesName);	// il 16/09/09

				int age = -1;
				FmPlant tree = new FmPlant (r.fileId,
						initStand,
						m, 
						age,
						r.x,
						r.y,
						r.z,
						""+r.fileId,
						0, 			// dbh
						r.height,
						r.crownBaseHeight,
						r.crownDiameter,
			//			r.crownDiameterHeight,
						s,			// species
						0, 			// pop = 0 PhD 2008-09-25
						0d, // liveMoisture=0
						0d, // deadMoisture=0
						0d,false); // liveTwigMoisture=0
				// adds tree in stand
				initStand.addTree (tree);


			} else if (record instanceof FireDVOLoader.KeyRecord) {
				FireDVOLoader.KeyRecord r = (FireDVOLoader.KeyRecord) record;	// cast to precise type

				// DEPRECATED, replaced by Terrain record (see upper) - fc - 14.5.2007
				//~ if (r.key.equals ("maxX")) {
					//~ standWidth = r.getIntValue ();
				//~ } else if (r.key.equals ("maxY")) {
					//~ standHeight = r.getIntValue ();
				//~ } else {
					//~ throw new Exception ("Unrecognized key: "+record);
				//~ }

				System.out.println ("record="+r);		// Automatic toString ()

			} else {
				throw new Exception ("Unrecognized record: "+record);	// automatic toString () (or null)
			}
		}

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
	public String getName () {return Translator.swap ("FireDVOLoader");}

	/**
	 * From Extension interface.
	 */
	public String getVersion () {return VERSION;}
	public static final String VERSION = "1.0";

	/**
	 * From Extension interface.
	 */
	public String getAuthor () {return "F. de Coligny";}

	/**
	 * From Extension interface.
	 */
	public String getDescription () {return Translator.swap ("FireDVOLoader.description");}


	////////////////////////////////////////////////// IOFormat stuff
	public boolean isImport () {return true;}
	public boolean isExport () {return true;}





	// For test only
/*	public static void main (String [] args) {
		try {
			RecordSet records = new FireDVOLoader ("c:/fafa/java/capsis4/data/benoit/bc2001/fafa.inv");

			System.out.println ("# of records : "+records.size ());
			for (Iterator i = records.iterator (); i.hasNext ();) {
				Record r = (Record) i.next ();
				System.out.println (r);
			}

		} catch (Exception e) {
			System.out.println ("FireDVOLoader.main () - exception caught : "+e.toString ());
		}
	}
*/

}
