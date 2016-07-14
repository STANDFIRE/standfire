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
import java.util.HashMap;
import java.util.Vector;

import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeList;
import capsis.kernel.Step;

/**	TreeStatusMaker : computes the living status of a tree
*	considering the current stand before and after intervention (if any)
*	and the stand one step after
*
*	@author F. Mothe - spetember 2006
*/

public class TreeStatusMaker {
	public enum Status {ALIVE, CUT, DEAD};

	Vector <TreeList> standsBefore;		// stand before intervention
	Vector <TreeList> standsAfter;		// null or stand after intervention
	HashMap <Integer, Integer> dates;	// <date, index in stands tables>

	public TreeStatusMaker (Collection stepsFromRoot) {
		initialise (stepsFromRoot);
	}
	public TreeStatusMaker (TreeList lastStand) {
//		initialise (TreeUtil.getStepsFromRoot (lastStand.getStep ()));
		Step lastStep = lastStand.getStep();
		initialise (lastStep.getProject().getStepsFromRoot(lastStep));
	}

	private void initialise (Collection stepsFromRoot) {
		standsBefore = new Vector <TreeList> ();
		standsAfter = new Vector <TreeList> ();
		dates = new HashMap <Integer, Integer> ();
		int index = 0;
		//for (Step step : stepsFromRoot) {
		for (Object o : stepsFromRoot) {
			Step step = (Step) o;
			TreeList s =  (TreeList) step.getScene ();
			if (s.isInterventionResult ()) {
				standsAfter.set (index-1, s);
			} else {
				standsBefore.add (s);
				standsAfter.add (null);
				dates.put (s.getDate (), index);
				++ index;
			}
		}
	}

	public Collection <TreeList> getStandsBeforeInterventions () {
		return standsBefore;
	}

	public Status getStatus (Tree tree) {
		return getStatus (tree, (TreeList) tree.getScene ());
	}

	private Status getStatus (Tree tree, TreeList stand) {
		int index = dates.get (stand.getDate ());
		return getStatus (tree, index);
	}

	private Status getStatus (Tree tree, int index) {
		return getStatus (tree, standsBefore.get (index), standsAfter.get (index),
			index+1 < standsBefore.size () ? standsBefore.get (index+1) : null);
	}

	// A null sAfter means no intervention at this step
	// A null sNext means it is the last step
	private static Status getStatus (Tree tree, TreeList sBefore, TreeList sAfter, TreeList sNext) {
		// Tree exists :
		// before  after   next   status
		// intervention    year
		// ==============================================
		// true    true    true   alive   intervention step
		// true    true    false  dead
		// true    false   false  cut
		// ----------------------------------------------
		// true    true    null   alive   last step
		// true    false   null   cut
		// ==============================================
		// true    null    true   alive   no intervention
		// true    null    false  dead
		// ----------------------------------------------
		// true    null    null   alive   last step
		// ----------------------------------------------
		//
		// if (sAfter == null || tree exists After)
		// 	if (sNext==null || tree exists Next)
		// 		alive
		// 	else : dead
		// else : cut


		Status status;
		// if (sAfter == null || TreeUtil.containsTree (sAfter, tree.getId ()) {
		if (sAfter == null || sAfter.getTree (tree.getId ()) != null) {
			if (sNext==null || sNext.getTree (tree.getId ()) != null) {
				status = Status.ALIVE;
			} else {
				status = Status.DEAD;
			}
		} else {
			status = Status.CUT;
		}
		return status;
	}
}

