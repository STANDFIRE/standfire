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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeList;
import capsis.kernel.GModel;
import capsis.kernel.MethodProvider;
import capsis.kernel.Step;
import capsis.lib.amapsim.AMAPsimRequestableTree;
import capsis.util.methodprovider.FertilityProvider;
import capsis.util.methodprovider.GProvider;
import capsis.util.methodprovider.NProvider;

/**
 * Mode2 request for AMAPsim client/server connection.
 * 
 * @author F. de Coligny - november 2002
 */
public class Mode2 extends Mode1 {
	
	/**	Constructor.
	*/
	public Mode2 (Object params, Step step, String messageId) throws Exception {
		this.step = step;
		this.messageId = messageId;		// messageId is now given - fc - 30.1.2004
		
		// New request or consider a model request
		//
		if (params != null) {
			// fc - 13.10.2003 - open dialog with model request
			init ((Mode2) params);
		} else {
			setDefaultValues ();
		}
		Mode2Dialog dlg = new Mode2Dialog (this, step);
		dlg.dispose ();
		dataLength = getLength ();	// does not include dataLength field
	}
	
	
	/**	String representation.
	*/
	public String toString () {
		return "Mode2: dataLength="+dataLength+" messageId="+messageId
				+" requestType="+requestType+" (...)";
	}
	
	
	// Init request from model request before dialog opening.
	// fc - 13.10.2003
	//
	protected void init (Mode2 modelRequest) {
		TreeList stand = (TreeList) step.getScene ();
		GModel model = step.getProject ().getModel ();
		MethodProvider mp = model.getMethodProvider ();
		
		dataLength = 0;				// MUST BE RECALCULATED LATER
		//~ messageId = ProtocolManager.nextMessageId ();	// NEW MESSAGEID
		requestType = modelRequest.requestType;
		
		species = modelRequest.species;
		
		storeLineTree = modelRequest.storeLineTree;
		storeMtg = modelRequest.storeMtg;
		storeBranches = modelRequest.storeBranches;
		storeCrownLayers = modelRequest.storeCrownLayers;
		storePolycyclism = modelRequest.storePolycyclism;
		
			storeTrunkShape = modelRequest.storeTrunkShape;				// 19.1.2004
			initialSimulationAge = modelRequest.initialSimulationAge;	// 20.1.2004
		
		surface = modelRequest.surface;
		numberOfTrees = modelRequest.numberOfTrees;
		basalArea = modelRequest.basalArea;
		fertilityHDom = modelRequest.fertilityHDom;
		fertilityAge = modelRequest.fertilityAge;
		coeffAge = modelRequest.coeffAge;
		
		useAge = modelRequest.useAge;
		ageMean = modelRequest.ageMean;
		ageg = modelRequest.ageg;
		ageDom = modelRequest.ageDom;
		ageStandardDeviation = modelRequest.ageStandardDeviation;
		ageMin = modelRequest.ageMin;
		ageMax = modelRequest.ageMax;
		
		useH = modelRequest.useH;
		HMean = modelRequest.HMean;
		Hg = modelRequest.Hg;
		HDom = modelRequest.HDom;
		HStandardDeviation = modelRequest.HStandardDeviation;
		HMin = modelRequest.HMin;
		HMax = modelRequest.HMax;
		
		useD = modelRequest.useD;
		DMean = modelRequest.DMean;
		Dg = modelRequest.Dg;
		DDom = modelRequest.DDom;
		DStandardDeviation = modelRequest.DStandardDeviation;
		DMin = modelRequest.DMin;
		DMax = modelRequest.DMax;
		
			useCrown = modelRequest.useCrown;					// added on 19.1.2004
			crownBaseHeight = modelRequest.crownBaseHeight;		// added on 19.1.2004
			crownMaxDiameter = modelRequest.crownMaxDiameter;	// added on 19.1.2004
		
		numberOfTreesToBeSimulated = modelRequest.numberOfTreesToBeSimulated;
			
		// Individual data only in Mode 2
		//
		// In mode 2 : model request trees are replaced by trees under 
		// the reference step (Y. Caraglio - 13.10.2003)
		//
		numberOfTrees = stand.getTrees ().size ();	// this is the number of trees we want from AMAPsim in mode 2
		trees = new ArrayList ();
		Step refStep = step;	// this name is clearer : the step on which the extension has been called
		
		for (Iterator i = stand.getTrees ().iterator (); i.hasNext ();) {
			Tree t = (Tree) i.next ();
			
			TreeDesc tree = new TreeDesc ();
			trees.add (tree);
			
			tree.treeId = t.getId ();
			tree.treeSpecies = "pinmar";
			if (modelRequest.useCrown) {
				tree.finalCrownBaseHeight = (float) ((AMAPsimRequestableTree) t).getCrownBaseHeight ();
				tree.finalCrownDiameter = (float) ((AMAPsimRequestableTree) t).getCrownDiameter ();
			} else {
				tree.finalCrownBaseHeight = -1f;
				tree.finalCrownDiameter = -1f;
			}
			
			tree.numberOfTreeSteps = 0;
			tree.treeSteps = new ArrayList ();
			
			Step stp = refStep;
			boolean beforeBirth = false;
			
			do {	// begins on refStep and goes back in time until before tree birth or project root
				TreeList std = (TreeList) stp.getScene ();
				Tree t1 = std.getTree (tree.treeId);	// tree at present time, then same tree in the past
				if (t1 == null) {
					beforeBirth = true;
				} else {
					TreeStep s = new TreeStep ();
					tree.treeSteps.add (s);
					tree.numberOfTreeSteps += 1;
					
					//~ s.age = (float) t1.getAge ();
						s.age = t1.getAge ();			// 20.1.2004
					s.height = (float) t1.getHeight ();
					s.dbh = (float) t1.getDbh ();
				}
			} while (!beforeBirth && ((stp = (Step) stp.getFather ()) != null));
		}
		
	}
	
	
	// Set default values for every request property.
	//
	protected void setDefaultValues () {
		
		TreeList stand = (TreeList) step.getScene ();
		Collection wTrees = stand.getTrees ();	// fc - 9.4.2004 - for Method Providers (29.4.2004: wTrees)
		
		GModel model = step.getProject ().getModel ();
		MethodProvider mp = model.getMethodProvider ();
		
		dataLength = 0;		// deferred calculation
		//~ messageId = ProtocolManager.nextMessageId ();
		requestType = 2;
		
		species = "";
		
		//numberOfTreesToBeSimulated = see below
			
		storeLineTree = false;
		storeMtg = false;
		storeBranches = false;
		storeCrownLayers = false;
		storePolycyclism = false;
		
			storeTrunkShape = false;		// 19.1.2004
			initialSimulationAge = 0;		// 20.1.2004
		
		surface = (float) stand.getArea ();
		
		//~ numberOfTreesInStand = stand.getTrees ().size ();
		try {numberOfTreesInStand = (int) ((NProvider) mp).getN (stand, wTrees);	// fc - 22.8.2006 - Numberable is double
				} catch (Exception e) {numberOfTreesInStand = 0;}
		
		try {basalArea = (float) ((GProvider) mp).getG (stand, wTrees);
				} catch (Exception e) {basalArea = 0;}
		
		try {
			// double[] contains a couple (Hdom, age)
			double[] fertility = ((FertilityProvider) mp).getFertility (stand);
			fertilityHDom = (float) fertility[0];
			fertilityAge = (int) fertility[1];
		} catch (Exception e) {
			fertilityHDom = 0;
			fertilityAge = 0;
		}
		
		coeffAge = 1;	// default value
		
		//---------------------------------------------------- Age
		//
		useAge = false;
		
		ageMean = 0;	// unused in mode 2
		ageg = 0;
		ageDom = 0;
		ageStandardDeviation = 0;
		ageMin = 0;
		ageMax = 0;
		
		//---------------------------------------------------- H
		//
		useH = false;
		
		HMean = 0;	// unused in mode 2
		Hg = 0;
		HDom = 0;
		HStandardDeviation = 0;
		HMin = 0;
		HMax = 0;
		
		//---------------------------------------------------- D
		//
		useD = false;
		
		DMean = 0;	// unused in mode 2
		Dg = 0;
		DDom = 0;
		DStandardDeviation = 0;
		DMin = 0;
		DMax = 0;
		
			//---------------------------------------------------- Crown	// added on 19.1.2004
			//
			useCrown = false;
			crownBaseHeight = 0f;
			crownMaxDiameter = 0f;
		
		// Individual data only in Mode 2
		//
		numberOfTrees = stand.getTrees ().size ();	// this is the number of trees we want from AMAPsim in mode 2
		
		numberOfTreesToBeSimulated = numberOfTrees;	// forced : 1 for 1
		
		trees = new ArrayList ();
		Step refStep = step;	// this name is clearer : the step on which the extension has been called
		
		for (Iterator i = stand.getTrees ().iterator (); i.hasNext ();) {
			Tree t = (Tree) i.next ();
			
			TreeDesc tree = new TreeDesc ();
			trees.add (tree);
			
			tree.treeId = t.getId ();
			tree.treeSpecies = "pinmar";
			tree.finalCrownBaseHeight = (float) ((AMAPsimRequestableTree) t).getCrownBaseHeight ();
			tree.finalCrownDiameter = (float) ((AMAPsimRequestableTree) t).getCrownDiameter ();
			
			tree.numberOfTreeSteps = 0;
			tree.treeSteps = new ArrayList ();
			
			Step stp = refStep;
			boolean beforeBirth = false;
			
			do {	// begins on refStep and goes back in time until before tree birth or project root
				TreeList std = (TreeList) stp.getScene ();
				Tree t1 = std.getTree (tree.treeId);	// tree at present time, then same tree in the past
				if (t1 == null) {
					beforeBirth = true;
				} else {
					TreeStep s = new TreeStep ();
					tree.treeSteps.add (s);
					tree.numberOfTreeSteps += 1;
					
					//~ s.age = (float) t1.getAge ();
						s.age = t1.getAge ();			// 20.1.2004
					s.height = (float) t1.getHeight ();
					s.dbh = (float) t1.getDbh ();
				}
			} while (!beforeBirth && ((stp = (Step) stp.getFather ()) != null));
		}
		
	}
	
}
	

