/* 
 * The Standfire model.
 *
 * Copyright (C) September 2013: F. Pimont (INRA URFM).
 * 
 * This file is part of the Standfire model and is NOT free software.
 * It is the property of its authors and must not be copied without their 
 * permission. 
 * It can be shared by the modellers of the Capsis co-development community 
 * in agreement with the Capsis charter (http://capsis.cirad.fr/capsis/charter).
 * See the license.txt file in the Capsis installation directory 
 * for further information about licenses in Capsis.
 */

package standfire.model;

import jeeb.lib.util.Log;
import standfire.extension.ioformat.SFScriptParamLoader;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.InitialParameters;
import capsis.kernel.automation.Automatable;
import capsis.lib.fire.FiInitialParameters;
import capsis.lib.fire.fuelitem.FiSpecies;

/**
 * SFInitialParameters are the initial settings for Standfire. They are set at
 * the project initialization stage and then stay unchanged during all the
 * simulations within this project.
 * 
 * @author F. Pimont - September 2013
 */
//@AutoUI(title = "Standfire : initialisation", translation = "SFLabels")
public class SFInitialParameters extends FiInitialParameters implements InitialParameters, Automatable {

	// This class uses the AutoUIs annotations

	public double xFVSSampleBegin; // position of the FVS scene in the scene
	public boolean extendFVSSample;
	public int extendFVSSampleSpatialOption = 0;// 0: homogeneous,
												// 1:GibbsPattern, 2: Hardcode
												// process

	// Setting this file is enough to init the StandFire model (contains other
	// file names and config)
//	@Editor(editorClass = FilenameEditor.class)
	public String scriptFileName;

	// @Editor(group="1-SpeciesFiles", editorClass=FilenameEditor.class)
	public String speciesFileName;

	// @Editor(group="2-SVSextendedFiles", editorClass=FilenameEditor.class)
	public String treesFileName;
	// @Editor(group="2-SVSextendedFiles", editorClass=FilenameEditor.class)
	public String snagsFileName;
	// @Editor(group="2-SVSextendedFiles", editorClass=FilenameEditor.class)
	public String cwdFileName;
	// @Editor(group="2-SVSextendedFiles", editorClass=FilenameEditor.class)
	public String scalarsFileName;
	// @Editor (group = "3-SpeciesFiles", editorClass = FilenameEditor.class)
	public String additionalPropertyFileName;


//	@Ignore
	private SFScene initScene;

	/**
	 * Default constructor.
	 */
	public SFInitialParameters() throws Exception {
	}

//	/**
//	 * A constructor accepting the input file of the script SFScript.
//	 */
//	public SFInitialParameters(String scriptFileName) throws Exception { // fc-7.11.2014
//		this.scriptFileName = scriptFileName;
//		
//	}

	/**
	 * A constructor for scripts.
	 */
	public SFInitialParameters(SFScriptParamLoader p) throws Exception {
		init (p);
	} 
	
	/**
	 * Init with a script param loader.
	 */
	private void init (SFScriptParamLoader p) {
		this.sceneOriginX = p.sceneOriginX;
		this.sceneOriginY = p.sceneOriginY;
		this.sceneSizeX = p.sceneSizeX;
		this.sceneSizeY = p.sceneSizeY;
		this.speciesFileName = p.speciesFile;
		this.treesFileName = p.svsBaseFile + "_trees.csv";
		this.snagsFileName = p.svsBaseFile + "_snags.csv";
		this.cwdFileName = p.svsBaseFile + "_cwd.csv";
		this.scalarsFileName = p.svsBaseFile + "_scalars.csv";
		this.additionalPropertyFileName = p.additionalPropertyFile;
		this.xFVSSampleBegin = p.xFVSSampleBegin;
		this.extendFVSSample = p.extendFVSSample;
		this.extendFVSSampleSpatialOption = p.extendFVSSampleSpatialOption;
	}

	/**
	 * A constructor for scripts.
	 */

	public SFInitialParameters(double sceneOriginX, double sceneOriginY, double sceneSizeX, double sceneSizeY,
			String speciesFileName, String treesFileName, String snagsFileName, String cwdFileName,
			String scalarsFileName, String additionalPropertyFileName) throws Exception {
		this.sceneOriginX = sceneOriginX;
		this.sceneOriginY = sceneOriginY;
		this.sceneSizeX = sceneSizeX;
		this.sceneSizeY = sceneSizeY;
		this.speciesFileName = speciesFileName;
		this.treesFileName = treesFileName;
		this.snagsFileName = snagsFileName;
		this.cwdFileName = cwdFileName;
		this.scalarsFileName = scalarsFileName;
		this.additionalPropertyFileName = additionalPropertyFileName;

	}

	@Override
	public GScene getInitScene() {
		return initScene;
	}

	/**
	 * Builds the initial scene.
	 */
	@Override
	public void buildInitScene(GModel model) throws Exception {
		SFModel m = (SFModel) model;
		model.setSettings(this);

		// fc-7.11.2014
		if (scriptFileName != null) {

			SFScriptParamLoader p = new SFScriptParamLoader (scriptFileName);
			p.interpret ();
			init (p);
			
		}
		
		// Load the species file
		try {
			speciesMap = FiSpecies.loadSpeciesMap(speciesFileName);
		} catch (Exception e) {
			Log.println(Log.ERROR, "SFInitialParameters.buildInitScene ()", "Error during species file loading", e);
			throw new Exception("Error during species file loading, see Log file", e);
		}
		// Load the scene files
		try {
			initScene = (SFScene) m.loadInitScene(treesFileName, snagsFileName, cwdFileName, scalarsFileName,
					additionalPropertyFileName, extendFVSSample, extendFVSSampleSpatialOption, xFVSSampleBegin,
					speciesMap);
			
		} catch (Exception e) {
			Log.println(Log.ERROR, "SFInitialParameters.buildInitScene ()", "Error during FVS extended files loading",
					e);
			throw new Exception("Error during FVS extended files loading, see Log file", e);
		}
	}
}
