/* 
 * The Standfire model.
 *
 * Copyright (C) 2013-2014: F. Pimont (INRA URFM).
 * 
 * This file is part of the Standfire model and is NOT free software.
 * It is the property of its authors and must not be copied without their 
 * permission. 
 * It can be shared by the modellers of the Capsis co-development community 
 * in agreement with the Capsis charter (http://capsis.cirad.fr/capsis/charter).
 * See the license.txt file in the Capsis installation directory 
 * for further information about licenses in Capsis.
 */

package standfire.myscripts;

import java.util.HashSet;

import standfire.extension.ioformat.SFScriptParamLoader;
import standfire.model.SFInitialParameters;
import standfire.model.SFModel;
import standfire.model.SFScene;
import capsis.app.C4Script;
import capsis.defaulttype.TreeList;
import capsis.extension.intervener.NrgThinner2;
import capsis.kernel.Engine;
import capsis.kernel.Step;
import capsis.lib.fire.exporter.PhysData.PhysDataOptions;
import capsis.lib.fire.exporter.PhysExporter;
import capsis.lib.fire.exporter.firetec.Firetec;
import capsis.lib.fire.exporter.wfds.WFDS;
import capsis.lib.fire.fuelitem.FiParticle;
import capsis.lib.fire.fuelitem.FuelMatrix.FuelMatrixOptions;
import capsis.lib.fire.intervener.FiIntervenerWithRetention;
import capsis.lib.fire.intervener.FiTreePruning;

/*
 * Memo: to build an installer for standfire: ant clean installer -Dmodules="standfire/**"
 */

/**
 * An example script for Standfire.
 * 
 * <pre>
 *  sh capsis.sh -p script standfire.myscripts.SFScript data/standfire/inputfiles/scriptparam.txt
 * </pre>
 * 
 * @author F. Pimont - September 2013
 */
public class SFScript {

	public static void main(String[] args) throws Throwable {

		// fc-13.11.2014 Check parameters
		if (args.length != 2)
			usage(); // stops the script

		String scriptFileName = args[1];

		C4Script s = new C4Script("standfire");

		SFScriptParamLoader p = new SFScriptParamLoader(scriptFileName);
		p.interpret();

		SFInitialParameters i = new SFInitialParameters(p);
		s.init(i); // after this statement, the project root step is available

		SFModel m = (SFModel) s.getModel();
		
		Step step = s.getRoot();
		// thinning
		if (p.respace) {
			int thinningCriterion = 2;
			int distCriterion = 3;
			double martellingDist = Math.max(Math.min(Math.sqrt(7d/ ((TreeList) step.getScene()).getTrees().size()* step.getScene().getArea()),15d),3d);
			NrgThinner2 t = new NrgThinner2(distCriterion,p.respaceDistance, thinningCriterion,martellingDist);
			step = s.runIntervener(t, step);
		}
		if (p.prune) {
			 boolean activityFuelRetention = false;
			FiTreePruning tp = new FiTreePruning(p.pruneHeight,  activityFuelRetention, FiIntervenerWithRetention.HEIGHT, FiIntervenerWithRetention.COVERFRACTION, FiIntervenerWithRetention.CHARACTERISTICSIZE, FiIntervenerWithRetention.MOISTURE);
			step = s.runIntervener(tp, step);
		}
		SFScene scene = (SFScene) step.getScene();
		

		
		// fc-13.11.2014 Show the root step to the user (3D appreciated)
		if (p.show3Dview) {
			System.out.println("Opening the 3D view...");
			StandFireFrame.showStandFireFrame(step);
			System.out.println("User closed the 3D view resuming the script...");
		}

		if (p.saveProject) {
			Engine.getInstance().processSaveAsProject(s.getProject(),p.projectName);
		}
		
		// System.exit(-2); // For tuning time only

		// System.out.println("this scene contains "+scene.getTrees
		// ().size()+" trees and "+scene.getLayerSets ().size()+" layersets");

		// EXPORT TO WFDS OR FIRETEC : OPTION DEFINITION
		FuelMatrixOptions fmo = new FuelMatrixOptions();
		fmo.fiLayerSetHorizontalDistributionDx = 1d;
		// fmo.particleNames = m.particleNames;// all particles
		fmo.particleNames = new HashSet<String>();
		if (p.includeLitter)
			fmo.particleNames.add(FiParticle.LITTER);
		if (p.includeLeaveLive)
			fmo.particleNames.add(FiParticle.makeKey(FiParticle.LEAVE, FiParticle.LIVE));
		if (p.includeTwig1Live)
			fmo.particleNames.add(FiParticle.makeKey(FiParticle.TWIG1, FiParticle.LIVE));
		if (p.includeTwig2Live)
			fmo.particleNames.add(FiParticle.makeKey(FiParticle.TWIG2, FiParticle.LIVE));
		if (p.includeTwig3Live)
			fmo.particleNames.add(FiParticle.makeKey(FiParticle.TWIG3, FiParticle.LIVE));
		if (p.includeLeaveDead)
			fmo.particleNames.add(FiParticle.makeKey(FiParticle.LEAVE, FiParticle.DEAD));
		if (p.includeTwig1Dead)
			fmo.particleNames.add(FiParticle.makeKey(FiParticle.TWIG1, FiParticle.DEAD));
		if (p.includeTwig2Dead)
			fmo.particleNames.add(FiParticle.makeKey(FiParticle.TWIG2, FiParticle.DEAD));
		if (p.includeTwig3Dead)
			fmo.particleNames.add(FiParticle.makeKey(FiParticle.TWIG3, FiParticle.DEAD));
		fmo.verbose = p.verboseExport;

		PhysDataOptions pdo = new PhysDataOptions();
		pdo.overlappingPermitted = true;
		pdo.produceTreeCrownVoxel = false;
		pdo.verbose = p.verboseExport;

		// EXPORT:
		if (p.modelChoice == 1 || p.modelChoice == 3) {
			WFDS w = new WFDS(p.wfdsparam, scene, fmo, pdo);
		}
		if (p.modelChoice == 2 || p.modelChoice == 3) {
			Firetec f = new Firetec(p.firetec_dx, p.firetec_dy, p.firetec_dz,
					p.firetec_nz, p.firetec_aa1,
					p.sceneOriginX, p.sceneOriginY, p.sceneSizeX, p.sceneSizeY, scene,
					fmo, pdo, p.firetecOutDir,
					PhysExporter.LITTLEENDIAN);
		}
		System.out.println("SimpleScript ended.");
		System.exit(0); // to make sure to finish in case of show3Dview
	}

	/**
	 * To be called in case parameters are missing: prints correct usage then
	 * stops.
	 */
	static private void usage() {
		System.out.println("The Capsis-StandFire script needs one parameter: script-file-name");
		System.out.println("e.g. under Linux or Mac: "
				+ "\nsh capsis.sh -p script standfire.myscripts.SFScript scriptparam.txt");
		System.out.println("e.g. under Windows: " + "\ncapsis -p script standfire.myscripts.SFScript scriptparam.txt");
		System.exit(-1);
	}

}