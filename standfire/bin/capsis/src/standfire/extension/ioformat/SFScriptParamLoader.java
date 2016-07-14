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

import java.io.File;
import java.util.Iterator;

import jeeb.lib.util.Import;
import jeeb.lib.util.Record;
import jeeb.lib.util.RecordSet;
import jeeb.lib.util.Translator;
import capsis.lib.fire.exporter.wfds.WFDSParam;
import capsis.util.NativeBinaryInputStream;

/**
 * SFScriptParamLoad contains the parameter for the script method "add" to avoid overwriting of the
 * scene
 * 
 * @author F. Pimont - Nov 2013
 */
public class SFScriptParamLoader extends RecordSet {

	static {
		Translator.addBundle ("standfire.extension.ioformat.SFScriptParamLoader");
	}


	
	@Import
	static public class StringRecord extends Record {

		public StringRecord () {
			super ();
		}

		public StringRecord (String line) throws Exception {
			super (line);
		}

		public String getSeparator () {
			return "=";
		} // to change default "\t" separator

		public String name;
		public String value;
	}
	public String path;
	public String speciesFile;
	public String svsBaseFile;
	public String additionalPropertyFile;
	public boolean extendFVSSample =  true;
	public int extendFVSSampleSpatialOption =  0; //0: homogeneous, 1:GibbsPattern, 2: Hardcode process
	public double xFVSSampleBegin =  100d;
	public double sceneOriginX = 0d;
	public double sceneOriginY = 0d;
	public double sceneSizeX = 200d;
	public double sceneSizeY = 200d;
	public boolean respace = false;
	public double respaceDistance = 0d;
	public boolean prune = false;
	public double pruneHeight = 0d;
	public boolean saveProject = false;
	public String projectName = "temp";
	public boolean show3Dview = false;
	public int modelChoice;
	public boolean includeLitter = true;
	public boolean includeLeaveLive = true;
	public boolean includeLeaveDead = true;
	public boolean includeTwig1Live = true;
	public boolean includeTwig1Dead = true;
	public boolean includeTwig2Live = true;
	public boolean includeTwig2Dead = true;
	public boolean includeTwig3Live = true;
	public boolean includeTwig3Dead = true;
	
	public boolean verboseExport = false;
	public WFDSParam wfdsparam = new WFDSParam(); 
	public String format = NativeBinaryInputStream.X86; // littleEndian
	public double firetec_dx = 2d;
	public double firetec_dy = 2d;
	public double firetec_dz = 15d;
	public int firetec_nz = 41;
	public double firetec_aa1 = 0.1;
	public String firetecOutDir;
	
	/**
	 * Constructor. Only to ask for extension properties (authorName, version...).
	 */
	public SFScriptParamLoader () {
	}


	/**
	 * Direct constructor
	 */

	public SFScriptParamLoader (String fileName) throws Exception {
		this (); 
		createRecordSet (fileName);
	} 

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public void interpret ()
	throws Exception {
		// System.out.println ("SFScriptParamLoader.load () : # of records : " + size ());
		for (Iterator i = this.iterator (); i.hasNext ();) {
			Record record = (Record) i.next ();
			// System.out.println ("record=" + record + ",class=" + record.getClass ());
			if (record instanceof StringRecord) {
				StringRecord r = (StringRecord) record;
				if (r.name.equals ("path")) {
					path = r.value;
				} else if (r.name.equals ("speciesFile")) {
					speciesFile = path+File.separator+r.value;
				} else if (r.name.equals ("svsBaseFile")) {
					svsBaseFile = path+File.separator+r.value;
				} else if (r.name.equals ("additionalPropertyFile")) {
					additionalPropertyFile = path+File.separator+r.value;
				} else if (r.name.equals ("respace")) {
					respace = Boolean.parseBoolean (r.value);
				} else if (r.name.equals ("respaceDistance")) {
					respaceDistance = Double.parseDouble (r.value);
				} else if (r.name.equals ("prune")) {
					prune = Boolean.parseBoolean (r.value);
				} else if (r.name.equals ("pruneHeight")) {
					pruneHeight = Double.parseDouble (r.value);
				} else if (r.name.equals ("saveProject")) {
					saveProject = Boolean.parseBoolean (r.value);
				} else if (r.name.equals ("projectName")) {
					projectName = path+File.separator+r.value;
				} else if (r.name.equals ("includeLitter")) {
					includeLitter = Boolean.parseBoolean (r.value);
				} else if (r.name.equals ("includeLeaveLive")) {
					includeLeaveLive = Boolean.parseBoolean (r.value);
				} else if (r.name.equals ("includeLeaveDead")) {
					includeLeaveDead = Boolean.parseBoolean (r.value);
				} else if (r.name.equals ("includeTwig1Live")) {
					includeTwig1Live = Boolean.parseBoolean (r.value);
				} else if (r.name.equals ("includeTwig1Dead")) {
					includeTwig1Dead = Boolean.parseBoolean (r.value);
				} else if (r.name.equals ("includeTwig2Live")) {
					includeTwig2Live = Boolean.parseBoolean (r.value);
				} else if (r.name.equals ("includeTwig2Dead")) {
					includeTwig2Dead = Boolean.parseBoolean (r.value);
				} else if (r.name.equals ("includeTwig3Live")) {
					includeTwig3Live = Boolean.parseBoolean (r.value);
				} else if (r.name.equals ("includeTwig3Dead")) {
					includeTwig3Dead = Boolean.parseBoolean (r.value);
				} else if (r.name.equals ("canopyFuelRepresentation")) {
					if (!(r.value.equals(WFDSParam.RECTANGLE)||r.value.equals(WFDSParam.CYLINDER)||
							r.value.equals(WFDSParam.HET_RECTANGLE_TEXT)||r.value.equals(WFDSParam.HET_RECTANGLE_BIN))) {
					throw new Exception ("SFScriptParamLoader, wrong keyword for canopyFuelRepresentation = "+r.value);}
					wfdsparam.canopyFuelRepresentation = r.value;
				} else if (r.name.equals ("bulkDensityAccuracy")) {
					wfdsparam.bulkDensityAccuracy = Double.parseDouble (r.value);
				} else if (r.name.equals ("wfdsFirstGridFile")) {
					wfdsparam.firstGridFile = path+File.separator+r.value;
				} else if (r.name.equals ("wfdsGridNumber")) {
					wfdsparam.gridNumber = Integer.parseInt (r.value);
				} else if (r.name.equals ("wfdsOutDir")) {
					wfdsparam.outDir = path+File.separator+r.value;
				} else if (r.name.equals ("wfdsFileName")) {
					wfdsparam.fileName = r.value;
				} else if (r.name.equals ("firetecOutDir")) {
					firetecOutDir = path+File.separator+r.value;
				} else if (r.name.equals ("format")) {
					format = r.value;
					wfdsparam.format = r.value;
				} else if (r.name.equals ("vegetation_cdrag")) {
					wfdsparam.vegetation_cdrag = Double.parseDouble (r.value);
				} else if (r.name.equals ("vegetation_char_fraction")) {
					wfdsparam.vegetation_char_fraction = Double.parseDouble (r.value);
				} else if (r.name.equals ("emissivity")) {
					wfdsparam.emissivity = Double.parseDouble (r.value);
				} else if (r.name.equals ("vegetation_arrhenius_degrad")) {
					wfdsparam.vegetation_arrhenius_degrad = Boolean.parseBoolean (r.value);
				} else if (r.name.equals ("fireline_mlr_max")) {
					wfdsparam.fireline_mlr_max = Double.parseDouble (r.value);
				} else if (r.name.equals ("veg_initial_temperature")) {
					wfdsparam.veg_initial_temperature = Double.parseDouble (r.value);
				} else if (r.name.equals ("veg_char_fraction")) {
					wfdsparam.veg_char_fraction = Double.parseDouble (r.value);
				} else if (r.name.equals ("veg_drag_coefficient")) {
					wfdsparam.veg_drag_coefficient = Double.parseDouble (r.value);
				} else if (r.name.equals ("veg_burning_rate_max")) {
					wfdsparam.veg_burning_rate_max = Double.parseDouble (r.value);
				} else if (r.name.equals ("veg_dehydratation_rate_max")) {
					wfdsparam.veg_dehydratation_rate_max = Double.parseDouble (r.value);
				} else if (r.name.equals ("veg_remove_charred")) {
					wfdsparam.veg_remove_charred = Boolean.parseBoolean (r.value);
				} else if (r.name.equals ("extendFVSSample")) {
					extendFVSSample = Boolean.parseBoolean (r.value);
				} else if (r.name.equals ("extendFVSSampleSpatialOption")) {
					extendFVSSampleSpatialOption = Integer.parseInt (r.value);
				} else if (r.name.equals ("xFVSSampleBegin")) {
					xFVSSampleBegin = Double.parseDouble (r.value);
				} else if (r.name.equals ("sceneOriginX")) {
					sceneOriginX = Double.parseDouble (r.value);
				} else if (r.name.equals ("sceneOriginY")) {
					sceneOriginY = Double.parseDouble (r.value);
				} else if (r.name.equals ("sceneSizeX")) {
					sceneSizeX = Double.parseDouble (r.value);
				} else if (r.name.equals ("sceneSizeY")) {
					sceneSizeY = Double.parseDouble (r.value);
				} else if (r.name.equals ("show3Dview")) {
					show3Dview = Boolean.parseBoolean (r.value);
				// System.out.println ("   " + r.name + "=" + r.value + "," + sceneSizeY);
				} else if (r.name.equals ("firetec_dx")) {
					firetec_dx = Double.parseDouble (r.value);
				} else if (r.name.equals ("firetec_dy")) {
					firetec_dy = Double.parseDouble (r.value);
				} else if (r.name.equals ("firetec_dz")) {
					firetec_dz = Double.parseDouble (r.value);
				} else if (r.name.equals ("firetec_nz")) {
					firetec_nz = Integer.parseInt (r.value);
				} else if (r.name.equals ("firetec_aa1")) {
					firetec_aa1 = Double.parseDouble (r.value);
				} else if (r.name.equals ("modelChoice")) {
					modelChoice = Integer.parseInt (r.value);
				} else if (r.name.equals ("verboseExport")) {
					verboseExport = Boolean.parseBoolean (r.value);
				} else {
					throw new Exception ("SFScriptParamLoader, unknown keyword "+r.name+" line " + record);	
				}
			} else {
				throw new Exception ("SFScriptParamLoader, unknown line: " + record);
			}
		}
	}
}
