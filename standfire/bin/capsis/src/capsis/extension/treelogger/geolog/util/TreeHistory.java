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

package capsis.extension.treelogger.geolog.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import capsis.defaulttype.SimpleScene;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeList;
import capsis.kernel.Step;

/**	
 * TreeHistory : manages the successive stages of a tree
 * from the initial step to the reference step.
 * Intervention steps are removed.
 * @author F. Mothe - january 2006
 * @author Mathieu Fortin - November 2011 (refactoring)
 */
public class TreeHistory {

	protected Tree [] history;

	/**
	 * General constructor.
	 * @param tree the Tree instance from which is calculated this history
	 * @param stepFromRoots a Collection of Step instances that goes from the root to the current step
	 * @param interventionIncluded a boolean - true to include the intervention step or false otherwise
	 */
	public TreeHistory(Tree tree, Collection<Step> stepFromRoots, boolean interventionIncluded) {
		history = getTreeHistoryArray(tree, stepFromRoots, interventionIncluded);
	}

	
	/**
	 * Specific constructor. NOTE: The intervention step are omitted in this constructor.
	 * @param tree the Tree instance from which is calculated this history
	 * @param stepFromRoots a Collection of Step instances that goes from the root to the current step
	 */
	public TreeHistory(Tree tree, Collection<Step> stepFromRoots) {
		this(tree, stepFromRoots, false);
	}
	
	
	@Deprecated
	protected TreeHistory() {
		this.history = null;
	}

	
	/**
	 * This method returns an array of Tree instances that represents the tree history.
	 * @return an array of Tree instances
	 */
	public Tree[] getHistory() {
		return history;
	}
	
	protected Vector<Tree> getTreeHistoryVector(Tree tree, Collection <Step> stepsFromRoot, boolean includeInterventions) {
		if (stepsFromRoot == null) {
			stepsFromRoot = getStepsFromRoot (tree.getScene().getStep(), includeInterventions);
		}
		
		int treeId = tree.getId ();

		// Search for the last step (the older one) where tree was present :
		int nbStepsFromRoot = stepsFromRoot.size ();
		int nbSteps = 0;
		List <Step> list = new Vector <Step> (stepsFromRoot);
		if (nbStepsFromRoot > 0) {
			for (ListIterator <Step> stepIt = list.listIterator (nbStepsFromRoot); stepIt.hasPrevious ();) {		// iterate from the last to the first element
				Step step = stepIt.previous ();
				TreeList stand =  (TreeList) step.getScene ();
				if (stand.getTree(treeId) != null) { // After call to previous (), nextIndex () is the current index !!
					nbSteps = stepIt.nextIndex() + 1;
					break;
				}
			}
		}
		
		// at this point nbSteps indicates the last step where the tree was present in the tree list

		// Build of tree history (without intervention steps if required) :
		// (we don't know if stepsFromRoot included interventions or not)
		Vector<Tree> historyVector = new Vector<Tree>();
		Iterator<Step> it = stepsFromRoot.iterator();
		for (int n = 0; n < nbSteps; ++n) {
			Step step = it.next ();
			TreeList stand =  (TreeList) step.getScene ();
			if (includeInterventions || !stand.isInterventionResult()) {
				//~ Tree t = stand.getTree (treeId);
				//~ // The tree may be missing at the begining (if recruited later) :
				//~ if (t != null) {historyVector.add (t);}
				// History may contains null trees at the beginning (if recruited later) :
				if (stand.getTree(treeId) != null) {
					historyVector.add (stand.getTree(treeId));
				}
			}
		}

		return historyVector;
	}


	/**
	 * This method returns the history of a particular tree. It retrieves the tree during the previous steps and returns an
	 * array that contains these instances.
	 * @param tree a Tree instance
	 * @param stepsFromRoot a Collection of Step instances from the root to the current step
	 * @param includeInterventions true to include the intervention steps or false to exclude these
	 * @return an Array of Tree instances
	 */
	protected Tree[] getTreeHistoryArray(Tree tree, Collection <Step> stepsFromRoot, boolean includeInterventions) {
		Vector <Tree> historyVector = getTreeHistoryVector(tree, stepsFromRoot, includeInterventions);
		return historyVector.toArray (new Tree [0]);
	}

	
	/**
	 * This method returns the history of a particular tree. It retrieves the tree during the previous steps and returns an
	 * array that contains these instances. NOTE: The intervention steps are discarded.
	 * @param tree a Tree instance
	 * @param stepsFromRoot a Collection of Step instances from the root to the current step
	 * @return an Array of Tree instances
	 */
	protected Tree [] getTreeHistoryArray(Tree tree, Collection <Step> stepsFromRoot) {
		return getTreeHistoryArray(tree, stepsFromRoot, false);
	}

	
	/**	
	 * This method returns a Vector of Step instances from the root to the current step.
	 * @param includeInterventions true to include the intervention steps or false otherwise
	 * @return a Vector of Step instances
	 */
	public static Vector<Step> getStepsFromRoot(Step currentStep, boolean includeInterventions) {
		Vector<Step> steps = new Vector<Step>();
		for (Step step : currentStep.getProject().getStepsFromRoot(currentStep)) {
			SimpleScene stand =  (SimpleScene) step.getScene ();
			if (includeInterventions || !stand.isInterventionResult()) {
				steps.add(step);
			}
		}
		return steps;
	}

	
}
