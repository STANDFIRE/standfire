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

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jeeb.lib.sketch.scene.item.Polygon;
import jeeb.lib.util.Alert;
import jeeb.lib.util.AmapTools;
import jeeb.lib.util.Import;
import jeeb.lib.util.Log;
import jeeb.lib.util.Question;
import jeeb.lib.util.Record;
import jeeb.lib.util.Translator;
import jeeb.lib.util.Vertex3d;
import capsis.gui.MainFrame;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.lib.fire.fuelitem.FiPlant;
import capsis.lib.fire.fuelitem.FiSpecies;
import capsis.util.CancelException;
import capsis.util.StandRecordSet;
import fireparadox.model.FmInitialParameters;
import fireparadox.model.FmModel;
import fireparadox.model.FmPlot;
import fireparadox.model.FmStand;
import fireparadox.model.database.FmPlantFromDB;
import fireparadox.model.database.FmPlantSyntheticData;

/**	FireDBFileLoader reads / writes input scene file describing fuel from the European database.
*	The file contains a terrain line and possibly many polygon / plant lines. 
*	Each plant line contains a shapeId (id of the plant in the fire fuel data base) 
*	and a location for this plant.
*	@author F. de Coligny - may 2008
*/
public class FireDBFileLoader extends StandRecordSet {
	// fc - sep 2009 - reviewed
	static {
		Translator.addBundle ("capsis.extension.ioformat.FireDBFileLoader");
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

	// Fire Paradox tree record is described here
	@Import
	static public class TreeRecord extends Record {
		public TreeRecord () {super ();}
		public TreeRecord (String line) throws Exception {super (line);}
		//public String getSeparator () {return ";";}	// to change default "\t" separator
		public long shapeId;		// shapeId in the FireParadox fuel data base
		public double x;
		public double y;
		public double z;
	}


	/**	Phantom constructor. 
	* 	Only to ask for extension properties (authorName, version...).
	*/
	public FireDBFileLoader () {}



	/**	Direct constructor
	*/
	public FireDBFileLoader (String fileName) throws Exception {createRecordSet (fileName);}	// for direct use for Import

	/**	Extension dynamic compatibility mechanism.
	*	This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	*/
	
	static public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof FmModel)) {return false;}

		} catch (Exception e) {
			Log.println (Log.ERROR, "FireDBFileLoader.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
		return true;
	}

	@Override
	public void createRecordSet (String fileName) throws Exception {super.createRecordSet (fileName);}

	/**	Export
	*	FiStand -> RecordSet.
	*	Then call save (fileName).
	*/
	@Override
	public void createRecordSet (GScene sc) throws Exception {
		FmStand stand = (FmStand) sc;
		super.createRecordSet (stand);		// deals with RecordSet's source
		try {
			FmPlot plot = stand.getPlot ();
			FmModel model = (FmModel) stand.getStep ().getProject ().getModel ();
			FmInitialParameters sets = model.getSettings ();
			
			// 1. KeyRecords (none at the moment)
			
			
			// 2. TreeRecords
			add (new EmptyRecord ());
			add (new CommentRecord ("Terrain"));
			add (new CommentRecord ("name	cellWidth(m)	altitude(m)	xMin	yMin	xMax	yMax"));
			
			TerrainRecord terrain = new TerrainRecord ();
			terrain.name = "Terrain";
			terrain.cellWidth = sets.cellWidth;
			terrain.altitude = plot.getAltitude ();
			terrain.xMin = stand.getOrigin ().x;
			terrain.yMin = stand.getOrigin ().y;
			terrain.xMax = stand.getOrigin ().x + stand.getXSize ();
			terrain.yMax = stand.getOrigin ().y + stand.getYSize ();
			add (terrain);
			
			add (new EmptyRecord ());
			add (new CommentRecord ("Polygons (optionnal)"));
			add (new CommentRecord ("fileId	{(x1,y1);(x2,y2)...}"));
			int polygonId = 1;
			//~ for (FirePolygon p : plot.getPolygons ()) {
			for (Polygon p : plot.getPolygons ()) {
				PolygonRecord polygon = new PolygonRecord ();
				polygon.id = polygonId++;
				polygon.vertices = p.getVertices ();
				if (polygon.vertices != null) {
					add (polygon);
				} else {
					add (new CommentRecord ("Found a polygon (fire.model.FirePolygon) with null vertices, not writen"));
				}
			}
			
			
			Collection<Integer> wrongTrees = new ArrayList<Integer> ();
			
			add (new EmptyRecord ());
			add (new CommentRecord ("Trees"));
			add (new CommentRecord ("shapeId	x	y	z"));
			for (Iterator i = stand.getTrees ().iterator (); i.hasNext ();) {
				FiPlant t = (FiPlant) i.next ();
				
				TreeRecord r = new TreeRecord ();
				
				if (t instanceof FmPlantFromDB) {
					r.shapeId = (int) ((FmPlantFromDB) t).getShapeId();
				} else {
					r.shapeId = -1;
				}
				
				r.x = t.getX ();
				r.y = t.getY ();
				r.z = t.getZ ();
				
				if (r.shapeId == 0) {
					wrongTrees.add (t.getId ());
				} else {
					add (r);
				}
			}
			
			// Trees without fuelIds (=zero): prompt user
			if (!wrongTrees.isEmpty ()) {
				String title = Translator.swap ("FireDBFileLoader.warning");
				String question = Translator.swap ("FireDBFileLoader.theFollowingTreesCannotBeExportedBecauseTheyDoNotHaveAFuelId")
						+"\n"+wrongTrees
						+"\n"+Translator.swap ("FireDBFileLoader.continueWithoutTheseTrees")
						+" (n="+wrongTrees.size ()+") ?";
				if (!Question.ask (MainFrame.getInstance (), title, question)) {
					throw new CancelException ();	// abort
				}
			}
			
		} catch (CancelException e) {
			throw e;
		} catch (Exception e) {
			Log.println (Log.ERROR, "FireDBFileLoader.createRecordSet (FiStand)", 
					"Could not export file due to an error", e);
			Alert.print (Translator.swap ("FireDBFileLoader.couldNotExportFileDueToAnErrorSeeLog"), e);
		}
	}

	/**	Import
	*	RecordSet -> FiStand
	*/
	@Override
	public GScene load (GModel model) throws Exception {
		
		FmModel m = (FmModel) model;
		FmInitialParameters settings = m.getSettings ();	
		
		// Initializations
		FmStand initStand = new FmStand (m);
		initStand.setSourceName (source);		// generally fileName
		initStand.setDate (0);
		double standWidth = 0;
		double standHeight = 0;
		Vertex3d standOrigin = new Vertex3d (0, 0, 0);
		
		Map speciesMap = new HashMap ();
		int speciesId = 1;
		FiSpecies firstSpecies = null;
		FmPlot plot = null;
		
		// Get the plant synthetic map
		Map<Long,FmPlantSyntheticData> plantSyntheticMap = m.getPlantSyntheticMap ();
		
System.out.println ("FireDBFileLoader.load () : # of records : "+size ());
		for (Iterator i = this.iterator (); i.hasNext ();) {
			Record record = (Record) i.next ();
			
			// fc - 14.5.2007
			if (record instanceof FireDBFileLoader.TerrainRecord) {
				FireDBFileLoader.TerrainRecord r = (FireDBFileLoader.TerrainRecord) record;	// cast to precise type
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
				
			} else if (record instanceof FireDBFileLoader.PolygonRecord) {
				FireDBFileLoader.PolygonRecord r = (FireDBFileLoader.PolygonRecord) record;	// cast to precise type
				
				if (plot == null) {
						throw new Exception ("Can not process a Polygon record before a Terrain record");}
				
				//~ FirePolygon p = new FirePolygon (r.vertices);
				// fc - 21.11.2008
				//~ Polygon p = new Polygon (Tools.toVertex3dCollection (r.vertices));
				Polygon p = new Polygon (new ArrayList<Vertex3d> (AmapTools.toVertex3dCollection (r.vertices)));
				
				plot.add (p);
				
			} else if (record instanceof FireDBFileLoader.TreeRecord) {
				FireDBFileLoader.TreeRecord r = (FireDBFileLoader.TreeRecord) record;	// cast to precise type
				
				FmPlantSyntheticData data = plantSyntheticMap.get (r.shapeId);
				if (data == null) {
						throw new Exception ("Wrong shapeId: "+r.shapeId+", unknown in the fire fuel data base");}
				
				Color crownColor = Color.BLUE;
						
				FiSpecies species = m.getSpecies (data.getSpeciesName ());
				if (species == null) {
						throw new Exception ("Wrong speciesName, shapeId="+r.shapeId+" speciesName="+data.getSpeciesName ());}
				
				FmPlantFromDB tree = new FmPlantFromDB(data, crownColor,
						species, m);
				tree.setId (m.getTreeIdDispenser ().next ());		// set the tree id (unique in the stand)
				tree.setXYZ (r.x, r.y, r.z);
				
				// add tree in stand
				initStand.addTree (tree);
				
			} else {
				throw new Exception ("Unrecognized record: "+record);	// automatic toString () (or null)
			}	
		}
		
		if (initStand.getPlot () == null) {
				throw new Exception ("missing Terrain in file, could not create plot; aborted");}
		
		return initStand;
	}	
	


	////////////////////////////////////////////////// Extension stuff
	/** 
	 * From Extension interface.
	 */
	public String getName () {return Translator.swap ("FireDBFileLoader");}

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
	public String getDescription () {return Translator.swap ("FireDBFileLoader.description");}


	////////////////////////////////////////////////// IOFormat stuff
	public boolean isImport () {return true;}
	public boolean isExport () {return true;}

}
