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


import java.util.Collection;
import java.util.Iterator;

import jeeb.lib.util.Import;
import jeeb.lib.util.Record;
import jeeb.lib.util.RecordSet;
import jeeb.lib.util.Translator;
import jeeb.lib.util.Vertex2d;
import capsis.app.CapsisExtensionManager;
import fireparadox.model.layerSet.FmLocalLayerModels;
import fireparadox.model.layerSet.FmLocalLayerSetModels;

/**	Contains some information on the layerModels used locally that are loaded in a textfile "filename"
 *  A model like that is automatically loaded by the FiModel when the module is loaded
 *  fileName= dirName+"/data/fireparadox/fuelModels/layerModels";
 *  
 *  These layerModels can be added from a loader or from the 3D editor			
* 
*	@author F. Pimont - october 2009
*/
public class FiLocalLayerModelLoader extends RecordSet {

	static {
		Translator.addBundle ("capsis.extension.ioformat.FiLocalLayerModelLoader");
	}
	FmLocalLayerSetModels temp;
	
	
	
	/** Layers record
	 *  contains, speciesName, mvr, svr, shape (isACylinder), and
	 *  alive and dead bulk density collection (that contains (height, bulk density),
	 *   giving the density at a given height
	 *  
	 * @author pimont
	 *
	 */
	@Import
	static public class LayerRecord extends Record {
		public LayerRecord () {super ();}
		public LayerRecord (String line) throws Exception {super (line);}
		//public String getSeparator () {return ";";}	// to change default "\t" separator
		public String speciesName;
		public double mvr; // kg/m3
		public double svr; // m2/m3
		public boolean isACylinder; //shape: cylinder or flat leave
		public Collection aliveBulkDensityCollection; // collection of (height, bulkdensity couple)
		public Collection deadBulkDensityCollection;
	}
	
	/**	Direct constructor
	*/
	public FiLocalLayerModelLoader (String fileName) throws Exception {
		super ();
		createRecordSet (fileName);
	}

	@Override
	public void createRecordSet (String fileName) throws Exception {super.createRecordSet (fileName);}

	/**
	 * RecordSet -> FiLocalLayerModels
	 */
	public FmLocalLayerModels load () throws Exception {
		System.out.println ("FiLocalLayerModels :");
		FmLocalLayerModels layerModels=new FmLocalLayerModels();
		for (Iterator i = this.iterator (); i.hasNext ();) {
			Record record = (Record) i.next ();
			if (record instanceof FiLocalLayerModelLoader.LayerRecord) {
				FiLocalLayerModelLoader.LayerRecord r = (FiLocalLayerModelLoader.LayerRecord) record;	// cast to precise type
				System.out.println ("	"+r.speciesName);
				double [][] aliveBulkDensityArray = convertCollectionInArray(r.aliveBulkDensityCollection,r.speciesName);
				double [][] deadBulkDensityArray = convertCollectionInArray(r.deadBulkDensityCollection,r.speciesName);
				layerModels.add(r.speciesName, r.mvr,r.svr,r.isACylinder,aliveBulkDensityArray, deadBulkDensityArray);
				
			}
		}
		return layerModels;
	}
	
	private double [][] convertCollectionInArray(Collection col, String speciesName) throws Exception {
		int size = col.size();
		double [][] result;
		if (size==0) {
			result = new double[2][1];
			result[0][0]=0.0;
			result[1][0]=0.0;
			return result;
		}
		result = new double[2][size];
		int count=0;
		for (Iterator i = col.iterator (); i.hasNext ();) {
			Vertex2d v2 = (Vertex2d) i.next ();
			result[0][count] = v2.x;
			if (count>0 && (result[0][count]<=result[0][count-1])) {
				throw new Exception ("In LocalLayerModel of "+speciesName+", height should strictly increase:"+result[0][count]+"<="+result[0][count-1]);
			}
			result[1][count] = v2.y;
			count++;
		}
		return result;
	}

//	Interpret
	//	Returns the little file name
	//
/*	public void interpret (CytSettings settings) throws Exception {
		CytClimateDay[] tab = new CytClimateDay[this.size ()];
		int k = 0;
		
		for (Iterator i = this.iterator (); i.hasNext ();) {
			Object record = i.next ();

			if (record instanceof ClimateRecord) {
				ClimateRecord r = (ClimateRecord) record;
				
				CytClimateDay d = new CytClimateDay (
						r.year, 
						r.day, 
						r.temperature, 
						r.solarRadiation, 
						r.temperatureSum); 
				tab[k++] = d;
				
			} else {
				throw new Exception ("wrong format in "+completeFileName+" near record "+record);
			}
		}
		CytClimate climate = new CytClimate (completeFileName, tab);
		String littleFileName = climate.getLittleFileName ();
		
		//~ settings.addClimate (littleFileName, climate);
		//~ return littleFileName;
		settings.climate = climate;
	}
*/
/*
	////////////////////////////////////////////////// Extension stuff
	/**
	 * From Extension interface.
	 */
	public String getName () {return Translator.swap ("FiLocalLayerModelLoader");}

	/**
	 * From Extension interface.
	 */
	public String getVersion () {return VERSION;}
	public static final String VERSION = "1.0";

	/**
	 * From Extension interface.
	 */
	public String getAuthor () {return "F. Pimont";}

	/**
	 * From Extension interface.
	 */
	public String getDescription () {return Translator.swap ("FiLocalLayerSetLoader.description");}


	////////////////////////////////////////////////// IOFormat stuff
	public boolean isImport () {return true;}
	public boolean isExport () {return true;}
	//////////////////////////////////////////////////Extension stuff
	/**
	 * From Extension interface.
	 */
	public String getType () {
		return CapsisExtensionManager.IO_FORMAT;
	}

	/**
	 * From Extension interface.
	 */
	public String getClassName () {
		return this.getClass ().getName ();
	}

	/**
	 * From Extension interface.
	 * May be redefined by subclasses. Called after constructor
	 * at extension creation (ex : for view2D zoomAll ()).
	 */
	public void activate () {}

}
