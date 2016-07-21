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

package capsis.extension.modeltool.amapsim2;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Log;
import jeeb.lib.util.StatusDispatcher;
import jeeb.lib.util.Translator;
import jeeb.lib.util.Vertex3d;
import amapsim.model.SimModel;
import amapsim.model.SimStand;
import amapsim.model.SimTree;
import capsis.commongui.projectmanager.Current;
import capsis.commongui.projectmanager.ProjectManager;
import capsis.kernel.Engine;
import capsis.kernel.Project;
import capsis.kernel.Step;
import capsis.lib.amapsim.AMAPsimLayer;
import capsis.lib.amapsim.AMAPsimTree;
import capsis.lib.amapsim.AMAPsimTreeStep;

/**
 * ProjectBuilder.
 * 
 * @author F. de Coligny - october 2003
 */
public class ProjectBuilder {
	
	private Mode1Response response;	// a Mode1Response or a Mode2Response
	private Random random;
	
	/**	Constructor.
	*/
	public ProjectBuilder (Mode1Response response) throws Exception {
		if (response == null) {throw new Exception ("ProjectBuilder aborted: Response is null");}
		if (response.returnCode != 0) {throw new Exception ("ProjectBuilder aborted: Response returnCode != 0: code="+response.returnCode);}
		
		this.response = response;
		random = new Random ();
	}
	
	/**	Builds a new project relying on the AMAPsim module with the data
	*	in the AMAPsim response.
	*/
	public void execute () {
		System.out.println ("ProjectBuilder.execute (): response="+response);
		try  {
			// TO BE TESTED ON MODE 1 ANSWER
				// TO BE TESTED ON MODE 1 ANSWER
					// TO BE TESTED ON MODE 1 ANSWER
			
			Engine e = Engine.getInstance ();
			
			// Stand Area, Width and Height
			// fc - 4.2.2004
			double standArea = response.surface;
			double w = Math.sqrt (standArea);
			double standWidth = w; 
			double standHeight = w;
			
			Map treeXs = new HashMap ();
			Map treeYs = new HashMap ();
			
			
			int numberOfTrees = response.numberOfTrees;
			System.out.println ("Number of trees in response="+numberOfTrees);
			
			String projectName = "Sim"+response.messageId;
			
			SimModel model = (SimModel) e.loadModel("amapsim");
			Project sce = e.processNewProject (projectName, model);
			model.setProject (sce);
		
			// Find max size of history
			//
			StatusDispatcher.print (Translator.swap ("ProjectBuilder.searchingMaxSizeOfHistory"));		
			int maxStepNumber = 0;
			double maxCrownDiameter = 1d;	// min 1 meter
			for (Iterator i = response.trees.iterator (); i.hasNext ();) {
				AMAPsimTree tree = (AMAPsimTree) i.next ();
				if (tree.numberOfTreeSteps > maxStepNumber) {maxStepNumber = tree.numberOfTreeSteps;}
				
					// fc - 4.2.2004 - memo random coordinates for each tree (see below)
					double x = random.nextDouble () * standWidth;
					double y = random.nextDouble () * standHeight;
					treeXs.put (new Integer (tree.treeId), new Double (x));
					treeYs.put (new Integer (tree.treeId), new Double (y));
				
				// Calculate maxCrownDiameter to space nicely the trees on their line
				//
				for (Iterator steps = tree.treeSteps.iterator (); steps.hasNext ();) {
					AMAPsimTreeStep step = (AMAPsimTreeStep) steps.next ();
					if (step.layers != null) {
						for (Iterator layers = step.layers.iterator (); layers.hasNext ();) {
							AMAPsimLayer layer = (AMAPsimLayer) layers.next ();
							maxCrownDiameter = Math.max (maxCrownDiameter, (double) layer.layerDiameter);
						}
					}
				}
			}
			System.out.println ("History max size="+maxStepNumber+" maxCrownDiameter="+maxCrownDiameter);
			
			// fc - 11.2.2004
			// restore numberOfTreesInStand : idem as in request
			// by setting a number or represented trees to each tree
			int n = (int) response.numberOfTreesInStand / numberOfTrees;
			int remain = response.numberOfTreesInStand - numberOfTrees * n;
			System.out.println ("numberOfTreesInStand "+response.numberOfTreesInStand+" numberOfTrees "+numberOfTrees+" n "+n+" remain "+remain);

			// Intermediate data structure
			// One line per tree
			// One col per step (each line do not have same col number)
			//
			SimTree[][] simTrees = new SimTree[numberOfTrees][maxStepNumber];
			
			int treeNumber = 0;
			for (Iterator i = response.trees.iterator (); i.hasNext ();) {
				AMAPsimTree tree = (AMAPsimTree) i.next ();
				int numberOfSteps = tree.numberOfTreeSteps;
				
				StatusDispatcher.print (Translator.swap ("ProjectBuilder.buildingTree")+" "+tree.treeId);		
				System.out.println ("treeNumber="+treeNumber+" treeId="+tree.treeId+" numberOfSteps="+numberOfSteps);			
				// We consider that an history is returned by AMAPsim for the tree
				//
				int historyIndex = 0;
				for (Iterator history = tree.treeSteps.iterator (); history.hasNext ();) {
					AMAPsimTreeStep h = (AMAPsimTreeStep) history.next ();
					
					double x = ((Double) treeXs.get (new Integer (tree.treeId))).doubleValue ();
					double y = ((Double) treeYs.get (new Integer (tree.treeId))).doubleValue ();
					
					SimTree t = new SimTree (tree.treeId, 
							null, 				// stand is null, will be set later by addTree ()
							tree.fileName, 
							h, 					// AMAPsimTreeStep
							x, y, 0d); 			// x, y, z -> for trees
					
					if (treeNumber == 0) {
						t.setNumber (n+remain);	// fc - 11.2.2004
					} else {
						t.setNumber (n);		// fc - 11.2.2004
					}
					
					simTrees[treeNumber][maxStepNumber - numberOfSteps + historyIndex] = t;
					historyIndex++;
				}
				
				treeNumber++;
			}
			
			
			System.out.println ();
			System.out.println ("Intermediate data structure : ");
			System.out.println ("NumberOfTrees="+simTrees.length);
			for (int i = 0; i < simTrees.length; i++) {
				System.out.println ("tree="+simTrees[i][0].getId ()+" steps="+simTrees[i].length);
			}
			System.out.println ();
			
			// Read simTrees "in reverse order"
			// last column = root step -> first column = last step
			// For each col, create a step, add trees present in the col, create a step
			//
			Step stp = null;
			for (int col = 0; col < simTrees[0].length; col++) {
				StatusDispatcher.print (Translator.swap ("ProjectBuilder.buildingStep")+" "+col);		
				SimStand stand = new SimStand ();
				
				for (int line = 0; line < numberOfTrees; line++) {
					if (simTrees[line][col] != null) {	// do we have history for this tree at this step ?
						stand.addTree (simTrees[line][col]);
					}
				}
				
				// Stand properties completion
				//
				Vertex3d origin = new Vertex3d (0, 0, 0);
				stand.setOrigin (origin); 
				stand.setDate (col+1);		// begin at 1
				
				//~ stand.setWidth (numberOfTrees); 	// trees are put on a line (TO BE MODIFIED)
				//~ stand.setHeight (10);	// 10 m.
				//~ stand.setArea (stand.getWidth ()*stand.getHeight ());	// DEFAULT VALUE, TO BE DISCUSSED
				
				stand.setXSize (standWidth); 
				stand.setYSize (standHeight);
				stand.setArea (standArea);
				
				stand.setSourceName (Translator.swap ("ProjectBuilder.fromAMAPsim"));
				
				stp = sce.processNewStep (stp, stand, "Built by ProjectBuilder");
				
			}
			
			
			// Update gui ScenarioManager
//			ProjectManager scenarioManager = ProjectManager.getInstance ();
//			scenarioManager.processCreate (stp);
			Current.getInstance ().setStep (stp);
			
			MessageDialog.print (this, Translator.swap ("ProjectBuilder.projectCreationWasCorrect")
					+": "+projectName);
		} catch (Exception e){
			Log.println (Log.ERROR, "ProjectBuilder.execute ()", "Exception", e);
			MessageDialog.print (this, "An error occurred, see Log for further details");
			
		}
	}

}


