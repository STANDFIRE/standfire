/** 
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 1999-2010 INRA 
 * 
 * Authors: F. de Coligny, S. Dufour-Kowalski, 
 * 
 * This file is part of Capsis
 * Capsis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * Capsis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU lesser General Public License
 * along with Capsis.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package capsis.script;

import java.util.Collection;

import jeeb.lib.util.Log;
import jeeb.lib.util.StatusDispatcher;
import jeeb.lib.util.extensionmanager.ExtensionManager;
import capsis.kernel.Engine;
import capsis.kernel.EvolutionParameters;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.InitialParameters;
import capsis.kernel.PathManager;
import capsis.kernel.Project;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.Intervener;
import capsis.kernel.extensiontype.Memorizer;
import capsis.kernel.extensiontype.OFormat;

/**	GScript is a superclass for all Capsis scripts. 
 * 	New release, trying to get simplier.
 * 
 * 	See the script doc on the Capsis web site documentation pages.
 * 
 *	@author F. de Coligny - september 2002
 */
public abstract class GScript {
	
	protected GModel model;
	protected Project project;
	protected Step currentStep;
	
	// public GScript (String[] args) {}	// subclasses should have a constructor of this form
	
	public abstract void run () throws Exception;	// subclasses should have a run () method

	/**	Loads a model from its package name (e.g. "mountain" or ).
	 */
	public GModel loadModel (String modelPackageName) {
		
		// - Important note -
		// If you have a "Design error..." problem here while in a script 
		// with a main(String[]) method, consider extending C4Script (deals 
		// with capsis.kernel.Engine initialisation in a static initializer) 
		// If you are not in Capsis, extend the matching class (SimeoScript...)
 
		model = null;
		try {
			model = Engine.getInstance().loadModel (modelPackageName, "script");
		} catch (Exception e) {
			e.printStackTrace (System.out);
		}
		return model;
	}

	/**	Creates a project with the given model and InitialParameters.
	 *	The root step is created and associated to the initial scene found in InitialParameters.
	 *	e.g. : Project p1 = createProject ("a", "maddmodule", ip);
	 */
	public Project createProject (String projectName, GModel model, InitialParameters ip) throws Exception {
		resetCurrentStep();
		
		// Create Project
		project = Engine.getInstance ().processNewProject (projectName, model);
		project.initialize (ip);
		
		return project;
	}

	/**	Set a memorizer to the given project	
	 */
	public void setMemorizer (Project project, Memorizer memorizer) throws Exception {
		boolean verbose = false ;
		try {
			project.setMemorizer (memorizer);
			if (verbose) {
				StatusDispatcher.print ("Memorizer "+memorizer.getClass ().getName ()
					+ " was correctly set for project "+project);
			}
		} catch (Exception e) {
			StatusDispatcher.print ("Error while setting memorizer "+e);
			e.printStackTrace (System.out);
			throw new Exception ("Error while setting memorizer");
		}
	}

	/**	Triggers evolution from the current step.
	 */
	public Step evolve (EvolutionParameters ep) {
		if (currentStep == null) {currentStep = (Step) project.getRoot();}
		return evolve (currentStep, ep);
	}
	
	/**	Triggers evolution from the given step according to the given evolution parameters.
	 *	Return the last created step.
	 */
	public Step evolve (Step referentStep, EvolutionParameters ep) {
		
		try {
			currentStep = referentStep.getProject ().evolve (referentStep, ep);
			return currentStep;
			
		} catch (Exception e) {
			Log.println (Log.ERROR, "GScript.evolve ()", "Exception ", e);
			e.printStackTrace();
		}
		return null;
	}
	
	/**	Close the current project	
	 */
	public void closeProject () {
		closeProject (project);
	}
		
	/**	Close the given project	
	 */
	public void closeProject (Project p) {
		Engine.getInstance ().processCloseProject (p);
		project = null;
	}
	
	/** Run an intervener on the given step.
	 * @param intervener: the intervener to be run (must have been parametrized with a 
	 * constructor and must be ready to apply after init ())
	 * @param p: the current project
	 * @param model: the current model
	 * @param step: the step carrying the scene (not changed by intervention) on which the 
	 * intervention must be run (the scene is copied before intervention)
	 * @param concernedTrees: a list of trees in the scene on which the intervention should 
	 * be restricted or null for considering all trees
	 * @return a new step carrying the new scene resulting of the intervention
	 */
	public Step runIntervener (Intervener intervener, Step step, Collection concernedTrees)  {
		
		// We run the intervener on a copy of the scene under the given step
		GScene sceneCopy = (GScene) step.getScene ().getInterventionBase ();
		
		// Init the intervener
		intervener.init (model, step, sceneCopy, concernedTrees);
		
		// Check compatibility
 		String className = intervener.getClass().getName();
		if(! ExtensionManager.matchWith(className, model))  {
			StatusDispatcher.print ("GScript.runIntervener (): Could not use intervener " + className
					+ ": intervener is not compatible. ");
			System.exit (0);
		}
		
		// Check intervener parameters
		if (!intervener.isReadyToApply ()) {
			StatusDispatcher.print ("GScript.runIntervener (): Could not apply intervener " + className
					+" on step "+step+": intervener was not ready to apply. Check log. ");
			System.exit (0);
		}
		
		// Run the intervener
		try {
			StatusDispatcher.print ("Processing Intervention...");
			
			if (project == null) project = step.getProject (); // Added this on 25.4.2012 -bug, script, PhD
			
			Step newStep = project.intervention (step, intervener, true);
			
			currentStep = newStep;
			
			return newStep;
			
		} catch (Exception e) {
			StatusDispatcher.print ("GScript.runIntervener (): Could not apply intervener "+intervener
					+" on step "+step+" due to "+e);
			e.printStackTrace (System.out);
			System.exit (0);
			return null;
		}
		
	}
	
	/**	Shortcut to run an intervener on all the trees of the scene	
	 */
	public Step runIntervener (Intervener intervener, Step step) {
		return runIntervener(intervener, step, (Collection) null);
	}
	
	/**	Run an output format on the given step, write to the given fileName.
	 */
	public void runOFormat (OFormat export, Step s, String fileName) {
		
		try {
			export.initExport (model, s);
			project.export (s, export, fileName);
			
		} catch (Exception e) {
			Log.println (Log.ERROR, "GScript.runOFormat ()", "Exception ", e);
			StatusDispatcher.print ("GScript.runOFormat (): Could not run oformat " + export
					+" due to exception "+e+". Check log. ");
			System.exit (0);
		}
		
	}
	
	/**	This method makes sure the current step is set to null
	 */
	protected void resetCurrentStep (){
		if (currentStep != null) {
			currentStep = null;
		}
	}
	
	/** Returns the model 
	 */
	public GModel getModel() {
		return model;
	}

	/** Returns the project 
	 */
	public Project getProject() {
		return project;
	}

	/** Returns the project's root step 
	 */
	public Step getRoot () {
		try {
			return (Step) project.getRoot ();
		} catch (Exception e) {
			return null;
		}
	}

	/** Return Capsis data directory 
	 */
	public String getDataDir () {
		return PathManager.getDir ("data");
	}
	
	/** Return Capsis root directory 
	 */
	public String getRootDir() {
		return PathManager.getInstallDir();
	}
	
	
}


