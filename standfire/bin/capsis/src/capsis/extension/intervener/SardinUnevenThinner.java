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


// modified by B.Courbaud 2010-03-14

package capsis.extension.intervener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import jeeb.lib.util.AmapTools;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import jeeb.lib.util.annotation.Ignore;
import capsis.defaulttype.Numberable;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeList;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.kernel.Step;
import capsis.kernel.automation.Automatable;
import capsis.kernel.extensiontype.Intervener;
import capsis.util.TreeDbhThenIdComparator;
import capsis.util.Group;
import capsis.util.GroupableIntervener;
import capsis.util.methodprovider.GProvider;


/**	SardinUnevenThinner cuts trees according to thresholds
*	on their sizes.
*	@author B. Courbaud, F. de Coligny - february 2010
*/
public class SardinUnevenThinner implements GroupableIntervener, Automatable, Intervener {

	public static final String NAME = "SardinUnevenThinner";
	public static final String VERSION = "1.0";
	public static final String AUTHOR =  "B. Courbaud, F. de Coligny";
	public static final String DESCRIPTION = "SardinUnevenThinner.description";
	static public String SUBTYPE = "SelectiveThinner";

	static {
		Translator.addBundle ("capsis.extension.intervener.SardinUnevenThinner");
	}

	@Ignore
	private boolean constructionCompleted = false;		// false if cancel occurs in interactive mode
	@Ignore
	private GProvider gProvider;
	@Ignore
	private Random random;
	@Ignore
	private StringBuffer report;
	@Ignore
	private double coef_ha;		// to calculate basal area per hectare

	@Ignore
	private TreeList stand;			// reference stand: will be altered by apply ()

	// Parameters (with default values)

	protected double dHarvest = 52.5;		// min Diameter-limit for harvest (cm)
	protected double dThin = 27.5;		// min Diameter-limit for thinning (cm)
	protected double gCutMax = 10;			// max cutting basal area (m2/ha)
	protected double gCutStandard = 7.5;	// standard cutting basal area (m2/ha)
	protected double gCutMin = 5;			// min cutting basal area (m2/ha)
	protected double harvestRatioMax = 0.5;	// max proportion of basal area higher than dHarvest harvested
	protected double thinRatioMax= 0.1;		// max proportion of basal area between dThin and dHarvest thinned
//	protected double gStockMin= 0;		// min stock basal area above dThin to allow cutting


	// Parameters

	// The intervener is applied to these trees only
	@Ignore
	private Collection<Tree> concernedTrees;
    @Ignore
	private SortedSet<Tree> treeSetHarvest;	// trees with dbh > dHarvest
	@Ignore
	private SortedSet<Tree> treeSetThin;	// trees with dthin < dbh < dHarvest


	public SardinUnevenThinner () {}



	/**	Direct constructor (for script mode).
	*	The stand parameter is the stand that will be thinned.
	*	the concernedTrees parameter is ignored if null,
	*	else the thinning process will be restricted to these trees.
	*/
	public SardinUnevenThinner (TreeList stand, GProvider gProvider, Collection<Tree> concernedTrees,
			double dHarvest, double dThin, double gCutMax, double gCutStandard, double gCutMin,
			double harvestRatioMax, double thinRatioMax) {

		// The stand to be thinned
		this.stand = stand;

		// Do we need to restrict to only some trees ?
		this.concernedTrees = (concernedTrees == null)
				? ((Collection<Tree>) ((TreeList) stand).getTrees ())  // All trees are considered
				: (concernedTrees);  // Only some trees (the other trees will be ignored)

		this.gProvider = gProvider;

		this.dHarvest = dHarvest;
		this.dThin = dThin;
		this.gCutMax = gCutMax;
		this.gCutStandard = gCutStandard;
		this.gCutMin = gCutMin;
		this.harvestRatioMax = harvestRatioMax;
		this.thinRatioMax = thinRatioMax;
//		this.gStockMin = gStockMin;

		random = new Random ();

		constructionCompleted = true;

	}

	@Override
	public void init(GModel m, Step s, GScene scene, Collection c) {
		stand = (TreeList) scene;

		// The method to calculate basal area
		GModel model = m;
		gProvider = (GProvider) model.getMethodProvider ();

		// Do we need to restrict to only some trees ?
		concernedTrees = (c == null)
				? ((Collection<Tree>) ((TreeList) stand).getTrees ())  // All trees are considered
				: c;  // Only some trees (the other trees will be ignored)

		random = new Random ();
		constructionCompleted = true;

	}


	@Override
	public boolean initGUI() throws Exception {
		// Open the dialog to get the parameters
		SardinUnevenThinnerDialog dlg = new SardinUnevenThinnerDialog (this);
		constructionCompleted = false;
		// Valid means that ok was hit and all values were checked
		if (dlg.isValidDialog ()) {
			try {
				dHarvest = dlg.get_dHarvest ();
				dThin = dlg.get_dThin ();

				gCutMax = dlg.get_gCutMax ();
				gCutStandard = dlg.get_gCutStandard ();
				gCutMin = dlg.get_gCutMin ();

				harvestRatioMax = dlg.get_harvestRatioMax ();
				thinRatioMax = dlg.get_thinRatioMax ();

//				gStockMin = dlg.get_gStockMin ();

				constructionCompleted = true;

			} catch (Exception e) {
				dlg.dispose ();
				Log.println (Log.ERROR, "SardinUnevenThinner.initGUI ()", "Could not get parameters from the Dialog box", e);
				throw e;
			}
		}

		dlg.dispose ();
		return constructionCompleted;
	}


	@Override
	public void activate() {
		// TODO Auto-generated method stub

	}




	/**	Extension dynamic compatibility mechanism.
	*	This matchwith method checks if the extension can deal
	*	(i.e. is compatible) with the referent.
	*/
	static public boolean matchWith (Object referent) {

		try {
			if (!(referent instanceof GModel)) {return false;}
			GModel m = (GModel) referent;
			GScene s = ((Step) m.getProject ().getRoot ()).getScene ();

			// TreeList...
			if (!(s instanceof TreeList)) {return false;}

			// ...trees are not numberable...
			Collection list = ((TreeList) s).getTrees ();
			list = AmapTools.getRepresentatives (list);
			for (Object o : list) {
				if (o instanceof Numberable) {return false;}
			}

			// ...and GProvider
			MethodProvider mp = m.getMethodProvider ();
			return (mp instanceof GProvider);

		} catch (Exception e) {
			Log.println (Log.ERROR, "SardinUnevenThinner.matchWith ()",
					"Error in matchWith () (returned false)", e);
			return false;
		}

	}


	/**	GroupableIntervener interface. This intervener acts on trees,
	*	tree groups can be processed.
	*/
	public String getGrouperType () {return Group.TREE;}


	/**	These assertions are checked at the beginning of apply ()
	*	(Interactive or script mode).
	*/
	private boolean assertionsAreOk () {

		if (stand == null) {
			Log.println (Log.ERROR, "SardinUnevenThinner.assertionsAreOk ()",
				"stand is null. SardinUnevenThinner is not appliable.");
			return false;
		}
		if (dHarvest < 0) {
			Log.println (Log.ERROR, "SardinUnevenThinner.assertionsAreOk ()",
					"dHarvest ("+dHarvest+") < 0. SardinUnevenThinner is not appliable.");
			return false;
		}
		if (dThin < 0) {
			Log.println (Log.ERROR, "SardinUnevenThinner.assertionsAreOk ()",
					"dThin ("+dThin+") < 0. SardinUnevenThinner is not appliable.");
			return false;
		}
		if (gCutMax < 0) {
			Log.println (Log.ERROR, "SardinUnevenThinner.assertionsAreOk ()",
					"gCutmax ("+gCutMax+") < 0. SardinUnevenThinner is not appliable.");
			return false;
		}
		if (gCutStandard > gCutMax) {
			Log.println (Log.ERROR, "SardinUnevenThinner.assertionsAreOk ()",
					"gCutStandard ("+gCutStandard+") >= gCutMax ("+gCutMax+"). SardinUnevenThinner is not appliable.");
			return false;
		}
		if (gCutMin > gCutStandard) {
			Log.println (Log.ERROR, "SardinUnevenThinner.assertionsAreOk ()",
					"gCutMin ("+gCutMin+") >= gCutStandard ("+gCutStandard+"). SardinUnevenThinner is not appliable.");
			return false;
		}

		return true;
	}


	/**	From Intervener.
	*	Control input parameters.
	*/
	public boolean isReadyToApply () {
		// If interactive dialog was canceled, constructionCompleted == false
		if (constructionCompleted && assertionsAreOk ()) {return true;}
		return false;
	}


		/**	Return the list of the biggest trees which cumulated basal area reaches
		*	target_G (target basal area)
		*/
		private List<Tree> getBiggestTrees (SortedSet<Tree> bigTrees, double target_G) {

			List<Tree> biggestTrees = new ArrayList<Tree> ();
			double g = 0;
			for (Tree t : bigTrees) {
				biggestTrees.add (t);
				g += gProvider.getG (stand, Arrays.asList (t)) * coef_ha;
				// Stop if target_G is reached
				if (g >= target_G) {break;}
			}
			return biggestTrees;

		}


		/**	Return the list of the medium trees to be cut, which cumulated basal area
		*	reaches target_G (target basal area) with a specific algorithm.
		*	The biggest tree has 99% chances to be cut and the smallest one has 0% chances to be cut.
		*/
		private List<Tree> selectMediumTrees (SortedSet<Tree> mediumTrees, double target_G) {

			LinkedList<Tree> in = new LinkedList<Tree> (mediumTrees);	// also sorted, and get (i) is available

			List<Tree> selectedTrees = new ArrayList<Tree> ();
			double g = 0;

			//~ report.append ("selecting medium trees (loop) total treeSetM = "+mediumTrees.size ()+"..."+"\n");

			// Stop the loop if target is reached or all trees were tested
			while (g < target_G && in.size () > 0) {

				// Select a tree at random
				int N = in.size ();
				int i = random.nextInt (N);			// i is in [0, N-1]

				Tree t = in.get (i);
				in.remove (i);		// consider the tree only once

				// Decide if 'cut'
				double a = random.nextDouble ();	// a is in [0, 1[
				if (a < (((double) N) - (i+1)) / N) {			// i+1 is in [1, N]
					// Select the tree for cutting
					selectedTrees.add (t);
					g += gProvider.getG (stand, Arrays.asList (t)) * coef_ha;
				}

			}

			//~ report.append ("...selecting medium trees done, selected = "+selectedTrees.size ()+"\n"
					//~ +"cumulating a basal area of "+gProvider.getG (stand, selectedTrees) * coef_ha+"\n");

			return selectedTrees;

		}


	/**	Intervener interface.
	*	Makes the thinning action. Algo version 2, April 10 2010 (BC)
	*/
	public Object apply () throws Exception {
		// Check construction and parameters
		if (!isReadyToApply ()) {
			throw new Exception ("SardinUnevenThinner.apply () - Wrong input parameters, see Log");
		}

		// Multiply basal area per coef_ha to have basal area per hectare
		coef_ha = 1d / (stand.getArea () / 10000d);	// getArea () is in m, / 10000 -> ha

		report = new StringBuffer ("SardinUnevenThinner report\n");
		report.append ("was applied on "+concernedTrees.size ()+" trees\n");
		report.append ("total G =  "+gProvider.getG (stand, concernedTrees) * coef_ha+"\n");
		report.append ("\n");

		stand.setInterventionResult (true);

		Collection<Tree> treesToRemove = new ArrayList<Tree> ();

		// A comparator to sort trees in descending oreder on their dbh
		Comparator descTreeComparator = new TreeDbhThenIdComparator (false);

//		report.append ("dHarvest = "+dHarvest+" dThin = "+dThin+"\n");
//		report.append ("\n");

		int a = 0;
		int b = 0;

		// Split the trees according to their size ()
		treeSetHarvest = new TreeSet<Tree> (descTreeComparator);
		treeSetThin = new TreeSet<Tree> (descTreeComparator);
		for (Tree t : concernedTrees) {
			if (t.getDbh () >= dHarvest) {
				treeSetHarvest.add (t);
				a++;
			} else if (t.getDbh () >= dThin) {
				treeSetThin.add (t);
				b++;
			}
		}

		// Calculate basal area for each class
		double gHarvest = gProvider.getG (stand, treeSetHarvest) * coef_ha;
		double gThin = gProvider.getG (stand, treeSetThin) * coef_ha;

		report.append ("treeSetHarvest "+treeSetHarvest.size ()+" trees, gHarvest = "+gHarvest+"\n");
		report.append ("treeSetThin "+treeSetThin.size ()+" trees, gThin = "+gThin+"\n");
		report.append ("\n");

		//0.
//		if ((gHarvest+gThin) <= gStockMin) {
//			// Cut nothing
//			report.append ("case 0: gHarvest+gThin = "+ (gHarvest+gThin) +"<= gStockMin\n");
//			report.append ("case 0: cut nothing\n");
//			report.append ("\n");
//		} else

		// 1.
		if (harvestRatioMax * gHarvest >= gCutMax) {

			List<Tree> l = getBiggestTrees (treeSetHarvest, gCutMax);
			treesToRemove.addAll (l);

			report.append ("case 1: harvestRatioMax * gHarvest = "+ harvestRatioMax * gHarvest+" >= gCutMax = "+ gCutMax+"\n");
			report.append ("cut "+l.size ()+" Harvest trees, g(cut) "+gProvider.getG (stand, l) * coef_ha+"\n");
			report.append ("\n");


		// 2.
		} else if (harvestRatioMax * gHarvest + thinRatioMax * gThin >= gCutStandard) {

			// cut harvest trees
			if (harvestRatioMax * gHarvest >= gCutStandard) {
				List<Tree> l = getBiggestTrees (treeSetHarvest, gCutStandard);
				treesToRemove.addAll (l);
				double cut_G = gProvider.getG (stand, treesToRemove) * coef_ha;

				report.append ("case 2.1: harvestRatioMax * gHarvest = "+ (harvestRatioMax * gHarvest) +" >= gCutStandard = "+gCutStandard+"\n");
				report.append ("cut "+treesToRemove.size ()+" g(cut) "+gProvider.getG (stand, treesToRemove) * coef_ha+"\n");
				report.append ("\n");

			} else {
				List<Tree> l = getBiggestTrees (treeSetHarvest, harvestRatioMax * gHarvest);
				treesToRemove.addAll (l);
				double cut_G = gProvider.getG (stand, treesToRemove) * coef_ha;
				// Not enough, also cut in thin trees
				double missing_G = gCutStandard - cut_G;	// basal area still to be cut in the thin trees

				treesToRemove.addAll (selectMediumTrees (treeSetThin, missing_G));

				report.append ("case 2.2: harvestRatioMax * gHarvest + thinRatioMax * gThin = "+ (harvestRatioMax * gHarvest + thinRatioMax * gThin) +" >= gCutStandard = "+gCutStandard+"\n");
				report.append ("cut "+treesToRemove.size ()+" g(cut) "+gProvider.getG (stand, treesToRemove) * coef_ha+"\n");
				report.append ("\n");
			}

		// 3.
		} else if (harvestRatioMax * gHarvest + thinRatioMax * gThin >= gCutMin) {

			// cut harvest trees
			if (harvestRatioMax * gHarvest >= gCutMin) {
				List<Tree> l = getBiggestTrees (treeSetHarvest, gCutMin);
				treesToRemove.addAll (l);

				// Need more ? (i.e. did we reach gCutStandard or not yet)
				double cut_G = gProvider.getG (stand, treesToRemove) * coef_ha;
				report.append ("case 3: g(treeSetHarvest) = "+cut_G+"\n");

				report.append ("enough wood was cut in treeSetHarvest"+"\n");
				report.append ("case 3.1: harvestRatioMax * gHarvest = "+ (harvestRatioMax * gHarvest) +" >= gCutMin = "+gCutMin+"\n");
				report.append ("cut "+treesToRemove.size ()+" g(cut) "+gProvider.getG (stand, treesToRemove) * coef_ha+"\n");
				report.append ("\n");

			} else {

				List<Tree> l = getBiggestTrees (treeSetHarvest, harvestRatioMax * gHarvest);
				treesToRemove.addAll (l);
				double cut_G = gProvider.getG (stand, treesToRemove) * coef_ha;

				// Not enough, also cut in thin trees
				double missing_G = gCutMin - cut_G;	// basal area still to be cut in the thin trees

				treesToRemove.addAll (selectMediumTrees (treeSetThin, missing_G));

				report.append ("        missing_G to be cut in treeSetThin = "+missing_G+"\n"+"\n");
				report.append ("case 3.2: harvestRatioMax * gHarvest + thinRatioMax * gThin = "+ (harvestRatioMax * gHarvest + thinRatioMax * gThin) +" >= gCutMin = "+gCutMin+"\n");
				report.append ("cut "+treesToRemove.size ()+" g(cut) "+gProvider.getG (stand, treesToRemove) * coef_ha+"\n");
				report.append ("\n");


			}

		// 4.
		} else {

			// Cut nothing
				report.append ("case 4: cut nothing\n");
				report.append ("\n");


		}


		// Actually cut the trees to remove
		for (Tree t : treesToRemove) {
			stand.removeTree (t);
			stand.storeStatus (t, "cut");
		}

System.out.println (report.toString ());	// could be writen in a Log when debug is over

		return stand;
	}




	/**	String representation
	*/
	public String toString () {
		return "class="+getClass().getName ()
				+" name="+NAME
				+" constructionCompleted="+constructionCompleted
				+" stand="+stand
				+" dHarvest="+dHarvest
				+" dThin="+dThin
				+" gCutmax="+gCutMax
				+" gCutStandard="+gCutStandard
				+" gCutMin="+gCutMin
				+" harvestRatioMax="+harvestRatioMax
				+" thinRatioMax="+thinRatioMax;
//				+" gStockMin="+gStockMin;
	}






}

