// TODO : add snags, cwd and surface fuels

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

import java.util.Map;
import java.util.Random;

import jeeb.lib.sketch.kernel.SketchLinkable;
import jeeb.lib.sketch.kernel.SketchLinker;
import standfire.extension.ioformat.SFAdditionalPropertiesLoader;
import standfire.extension.ioformat.SFSVSextScalarsLoader;
import standfire.extension.ioformat.SFSVSextTreesLoader;
import standfire.sketch.SFSketchLinker;
import capsis.kernel.EvolutionParameters;
import capsis.kernel.GScene;
import capsis.kernel.InitialParameters;
import capsis.kernel.Step;
import capsis.lib.fire.FiModel;
import capsis.lib.fire.fuelitem.FiParticle;
import capsis.lib.fire.fuelitem.FiSpecies;

/**
 * The main class for Standfire. It usually contains methods to create the
 * initial scene in the project. This can be done by loading a file or by
 * specific generation methods. It contains an initializeModel
 * (InitialParameters p) method that is run at project initialisation time. It
 * also contains the main processEvolution (Step s, EvolutionParameters p)
 * method to calculate the evolution of the scene over time, i.e. the growth or
 * dynamics model.
 * 
 * @author F. Pimont - September 2013
 */
public class SFModel extends FiModel implements SketchLinkable {


	/**
	 * Constructor
	 */
	public SFModel() throws Exception {
		super();
		this.rnd =  new Random(0);// standfire is deterministic
		initParticleNames();
		setSettings(new SFInitialParameters());

	}

	/**
	 * Init the particule names in this method, will be called if a project is
	 * reopened (else, an error occurs).
	 */
	private void initParticleNames() {
		//particleNames = new HashSet<String>();
		particleNames.add(FiParticle.LITTER);
		particleNames.add(FiParticle.makeKey(FiParticle.LEAVE, FiParticle.LIVE));
		particleNames.add(FiParticle.makeKey(FiParticle.TWIG1, FiParticle.LIVE));
		particleNames.add(FiParticle.makeKey(FiParticle.TWIG2, FiParticle.LIVE));
		particleNames.add(FiParticle.makeKey(FiParticle.TWIG3, FiParticle.LIVE));
		particleNames.add(FiParticle.makeKey(FiParticle.LEAVE, FiParticle.DEAD));
		particleNames.add(FiParticle.makeKey(FiParticle.TWIG1, FiParticle.DEAD));
		particleNames.add(FiParticle.makeKey(FiParticle.TWIG2, FiParticle.DEAD));
		particleNames.add(FiParticle.makeKey(FiParticle.TWIG3, FiParticle.DEAD));
		System.out.println("SFModel particles :"+particleNames);
	}
	
//	/**
//	 * Creates a MethodProvider for the module.
//	 */
//	protected MethodProvider createMethodProvider() {
//		return new SFMethodProvider();
//	}

	/**
	 * Convenient method.
	 */
	public SFInitialParameters getSettings() {
		return (SFInitialParameters) settings;
	}

	/**
	 * Loads the FVS extended files. Creates SFTree instances and adds them into
	 * the initial scene. layerHeights, layerBaseHeights, layerCoverFractions,
	 * layerCharacteristicSizes are the properties of the shrub, herb, litter
	 * and duff to be specified
	 */

	public SFScene loadInitScene(String treesFileName, String snagsFileName, String cwdFileName,
			String scalarsFileName, String additionalPropertiesFileName, boolean extendFVSSample,
			int extendFVSSampleSpatialOption, double xFVSSampleBegin, Map<String, FiSpecies> speciesMap)
			throws Exception {

		SFSVSextTreesLoader l1 = new SFSVSextTreesLoader(treesFileName, speciesMap);
		SFScene initScene = (SFScene) l1.load(this);
		if (extendFVSSample) {
			initScene.extendInitialTreeSet(extendFVSSampleSpatialOption, xFVSSampleBegin);// populate
		} else { 		// resizing scene to parameters (when no extention is done only)
			initScene.reduceSceneTo(getSettings().sceneOriginX, getSettings().sceneOriginY, getSettings().sceneSizeX, getSettings().sceneSizeY);			
		}
		SFSVSextScalarsLoader l2 = new SFSVSextScalarsLoader(scalarsFileName);
		Map<String, Double> layerLoads = l2.getLoads();
		SFAdditionalPropertiesLoader l3 = new SFAdditionalPropertiesLoader(additionalPropertiesFileName, speciesMap,
				layerLoads);
		initScene = l3.addLayersAndUpdateTreeMoisture(this, initScene);
		
		System.out.println("The scene contains " + initScene.getTrees().size() + " trees, "+ initScene.getLayerSets().size() + " layersets");

		
		return initScene;

	}

	/**
	 * This method is called for the first scene of the project at project
	 * creation time.
	 */
	@Override
	public Step initializeModel(InitialParameters p) {
		// Optional process at project creation time
		// -> nothing here

		return p.getInitScene().getStep();
	}

	/**
	 * This method is called when a project is loaded from disk.
	 */
	@Override
	protected void projectJustOpened() {
		// fc-19.1.2015 this method needs to be recalled on project opening
		initParticleNames();
	}

	/**
	 * Evolution loop.
	 */
	@Override
	public Step processEvolution(Step step, EvolutionParameters p) throws Exception {

		// SFEvolutionParameters ep = (SFEvolutionParameters) p;
		//
		// int originYear = step.getScene ().getDate ();
		// int numberOfYears = ep.getNumberOfYears ();
		//
		// ProgressDispatcher.setMinMax (0, numberOfYears);
		//
		// for (int k = 1; k <= numberOfYears; k++) {
		// int newYear = originYear + k;
		// StatusDispatcher.print (Translator.swap ("SFModel.evolutionForYear")
		// + " " + newYear);
		// ProgressDispatcher.setValue (k);
		//
		// // Create the new scene by partial copy
		// SFScene newScene = (SFScene) step.getScene ().getEvolutionBase ();
		// newScene.setDate (newYear);
		//
		// // Make all the trees grow, add them in newScene
		// Collection trees = ((SFScene) step.getScene ()).getTrees ();
		// for (Iterator i = trees.iterator (); i.hasNext ();) {
		// SFTree t = (SFTree) i.next ();
		//
		// SFTree newTree = t.processGrowth (getSettings ().growthP1,
		// getSettings ().growthP2,
		// getSettings ().growthP3,
		// getSettings ().growthP4);
		// newScene.addTree (newTree);
		// }
		//
		// String reason = "Evolution to year " + newYear; // a free String
		//
		// Step newStep = step.getProject ().processNewStep (step, newScene,
		// reason);
		// step = newStep;
		// }
		// StatusDispatcher.print (Translator.swap ("SFModel.evolutionIsOver"));
		// ProgressDispatcher.stop ();
		//
		// // Return the last Step
		// return step;
		return null;
	}

	/**
	 * Post intervention processing
	 */
	@Override
	public void processPostIntervention(GScene newScene, GScene oldStand) {
		// Add here if needed an optional process to be run after intervention
		// -> nothing here
	}

	/**
	 * This method makes it possible to use a 3D viewer in an 'interactive
	 * script', see SFScript and StandFireFrame for example.
	 */
	@Override
	public SketchLinker getSketchLinker() { // fc-13.11.2014 added this for StandFireFrame
		return new SFSketchLinker(this);
	}

}
