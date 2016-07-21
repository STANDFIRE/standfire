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


import java.util.Iterator;

import jeeb.lib.util.Import;
import jeeb.lib.util.Record;
import jeeb.lib.util.RecordSet;
import jeeb.lib.util.Translator;
import capsis.app.CapsisExtensionManager;
import fireparadox.model.FmModel;
import fireparadox.model.layerSet.FmLocalLayerSetModels;

/**	Contains some information on the layerSetModels used locally, 
* loaded from a text file call filename
* A model like that is automatically loaded by the FiModel when the module is loaded
 *  fileName= dirName+"/data/fireparadox/fuelModels/layerSetModels";
 *  
 *  These layerSetModels can be added from a loader or from the 3D editor			
* 
*
*	@author F. Pimont - october 2009
*/

public class FiLocalLayerSetModelLoader extends RecordSet {

	static {
		Translator.addBundle ("capsis.extension.ioformat.FiLocalLayerSetModelLoader");
	}
	FmLocalLayerSetModels temp;
	
	
	
	/**
	 *  Layer in layerSet properties, contains a name for the layer, a speciesname, a height for the layer
	 *  a percentage (cover fraction) and characteristic size of patch (inside layerSet), moisture
	 *  
	 * @author pimont
	 *
	 */
	@Import
	static public class LayerRecord extends Record {
		public LayerRecord () {super ();}
		public LayerRecord (String line) throws Exception {super (line);}
		//public String getSeparator () {return ";";}	// to change default "\t" separator
		public String layerSetName;
		public String speciesName;
		public int spatialGroup; // if spatialGroup of two species differ, they can be superposed
		public double height; //m
		public double percentage; // cover fraction in layerSet (%)
		public double characteristicSize; // patch size in layerSet (m)
		public double aliveMoisture; //(%)
		public double deadMoisture; //(%)
		public double liveBulkDensity;//kg/m3
		public double deadBulkDensity;
		public double svr;//m2/m3
		public double mvr;//kg/m3	
	}
	
	/**	Direct constructor
	*/
	public FiLocalLayerSetModelLoader (String fileName) throws Exception {
		super ();
		createRecordSet (fileName);
	}

	@Override
	public void createRecordSet (String fileName) throws Exception {super.createRecordSet (fileName);}

	/**
	 * RecordSet -> FiLocalLayerSetModels
	 */
	public FmLocalLayerSetModels load (FmModel model) throws Exception {
		System.out.println ("FiLocalLayerSetModels :");
		FmLocalLayerSetModels layerSetModels=new FmLocalLayerSetModels();
		for (Iterator i = this.iterator (); i.hasNext ();) {
			Record record = (Record) i.next ();
			if (record instanceof FiLocalLayerSetModelLoader.LayerRecord) {
				FiLocalLayerSetModelLoader.LayerRecord r = (FiLocalLayerSetModelLoader.LayerRecord) record;	// cast to precise type
				String speciesName = r.speciesName;
				double height = r.height;
				// when properties are not defined correctly, they are computed from localLayerModel
				double lbd = r.liveBulkDensity;
				if (lbd < 0) {
					lbd = model.localLayerModels.computeBulkDensity(speciesName, height, true);
				}
				double dbd = r.deadBulkDensity;
				if (dbd < 0) {
					dbd = model.localLayerModels.computeBulkDensity(speciesName, height, false);
				}
				double svr = r.svr;
				if (svr < 0) {
					svr = model.localLayerModels.getSVR(speciesName);
				}
				double mvr = r.mvr;
				if (mvr < 0) {
					mvr = model.localLayerModels.getMVR(speciesName);
				}
				layerSetModels.addLayer(r.layerSetName,  speciesName, r.spatialGroup, height,
						r.percentage, r.characteristicSize, r.aliveMoisture, r.deadMoisture, lbd, dbd, svr, mvr);
			}
		}
		for (String key : layerSetModels.map.keySet()) {
			System.out.println("	"+key);
		}
		return layerSetModels;
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
	public String getName () {return Translator.swap ("FireDVOLoader");}

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
