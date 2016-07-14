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

package capsis.extension.treelogger.geolog.species;

import capsis.extension.treelogger.geolog.util.TreeHistory;

/**	
 * FgTreeHistory : specialisation of TreeHistory for Fagacees trees
 * generating fake trees at missing years
 * @author F. Mothe - january 2006
 * @author M. Fortin - November 2011 (refactoring)
 */
@Deprecated
public class FgTreeHistory extends TreeHistory {


//	//	Constructor only appliable to FgTree
//	//	(stepsFromRoot may be null)
////	public FgTreeHistory(Tree tree, Collection <Step> stepsFromRoot, boolean full, boolean startFirstYear) {
//	public FgTreeHistory(Tree tree, Collection <Step> stepsFromRoot, boolean full, boolean startFirstYear) {
//		this.history = full
//				? getFullTreeHistory (tree, stepsFromRoot, startFirstYear)
//				: getTreeHistoryArray(tree, stepsFromRoot);
//	}
//
//	//	Static methods called by the non static methods
//
//	//	Return the full history of a FgTree in the scenario from root to reference step,
//	//	including the missing trees between each step.
//	//
//	//	If startFirstYear is false, the missing trees are interpolated except from
//	//	year 1 to the first step.
//	//
//	//	If startFirstYear is true, all the missing trees are interpolated since year 1.
//	//	WARNING : as the current version of Fagacées cannot generate trees below
//	//	1.5 m height, startFirstYear should always be false !!
//	//
//	//	(stepsFromRoot is calculated by caller for time optimisation)
//	//
//	private Tree [] getFullTreeHistory (Tree inputTree, Collection <Step> stepsFromRoot, boolean startFirstYear) {
//
//		FgTree tree = (FgTree) inputTree;
//		FgSpecies species = tree.getOfficialSpeciesForSimulation();
//		
////		int treeId = tree.getId ();
////		Tree [] partialHistory = TreeUtil.getTreeHistory (tree, stepsFromRoot, false);
//		Tree [] partialHistory = new TreeHistory(tree, stepsFromRoot).getHistory();
//
//		int cpt = 0;
//		int nbTrees;
//		Tree t1;
//		if (startFirstYear) {
//			// From year 1 to tree.getAge ()
//			nbTrees = tree.getAge ();
//			// Temporary tree of age 0 (not kept into the history) :
//			if (species == FgSpecies.OAK) {
//				t1 = new FgOakTree (tree.getId (), tree.getScene (), 0, 0.0, 0.0, 0.0, tree.isMarked ());
//			} else {
//				t1 = new FgBeechTree (tree.getId (), tree.getScene (), 0, 0.0, 0.0, 0.0, tree.isMarked ());
//			}
//		} else {
//			// From age of tree at step 1 to tree.getAge ()
//			nbTrees = tree.getAge () - partialHistory [0].getAge () + 1;
//			t1 = partialHistory [0];
//		}
//
//		Tree [] history = new Tree [nbTrees];
//		if (! startFirstYear) {
//			history [cpt++] = t1;
//		}
//		for (int i=1; i<partialHistory.length; i++) {
//			Tree t2 = partialHistory [i];
//			for (int age = t1.getAge ()+1; age < t2.getAge (); age++) {
//				// Interpolation of the missing trees :
//				double r2 = ((double) age - t1.getAge ()) / (t2.getAge () - t1.getAge ());
//				double r1 = 1.0 - r2;
//
//				double dbh = r1 * t1.getDbh () + r2 * t2.getDbh ();
//				double height = r1 * t1.getHeight () + r2 * t2.getHeight ();
//				double crownBaseHeight = r1 * ((FgTree) t1).getCrownBaseHeight () +
//						r2 * ((FgTree) t2).getCrownBaseHeight ();
//				
//				Tree t;
//				if (species == FgSpecies.OAK) {
//					t = new FgOakTree (tree.getId (), tree.getScene (), age, dbh, height, crownBaseHeight, tree.isMarked ());
//				} else {
//					t = new FgBeechTree (tree.getId (), tree.getScene (), age, dbh, height, crownBaseHeight, tree.isMarked ());
//				}
//				history [cpt++] = t;
//			}
//			history [cpt++] = t2;
//			t1 = t2;
//		}
//		return history;
//	}

}
