/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2003 Francois de Coligny
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */

package capsis.extension.intervener;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.defaulttype.Numberable;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeCollection;
import capsis.defaulttype.TreeList;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.Intervener;
import capsis.util.Group;
import capsis.util.GroupableIntervener;
import capsis.util.diagram2d.MaidHisto;
import capsis.util.methodprovider.GProvider;
import capsis.util.methodprovider.StandDensityIndexInterface;
import capsis.util.methodprovider.StumpTreatmentAvailabilityProvider; // add cm tl 02 05 2013 Fompine

/**
 * Create an HistoThinner to cut trees with an interactive dbh/girth histogram.
 *
 * @author F. de Coligny - april 2003
 */
public class HistoThinner implements Intervener, GroupableIntervener {
	// fc-5.7.2012 replaced TreeList by GScene AND TreeCollection for Jackpine
	// compatibility

	static {
		Translator.addBundle("capsis.extension.intervener.HistoThinner");
	}

	public static final String NAME = "HistoThinner";
	public static final String VERSION = "1.1";
	public static final String AUTHOR = "F. de Coligny";
	public static final String DESCRIPTION = "HistoThinner.description";
	static public String SUBTYPE = "SelectiveThinner";

	private Random random;

	protected GModel model;

	private HistoThinnerCutJob[] cutJobs;

	private boolean constructionCompleted = false; // if cancel in interactive
													// mode, false
	private int mode; // CUT or MARK

	private GScene stand1; // Reference stand: will be altered by apply ()
	private TreeCollection stand2; // Reference stand: will be altered by apply
									// ()

	private Collection<Integer> treeIds;
	private Collection<? extends Tree> concernedTrees;

	/**
	 * Default constructor.
	 */
	public HistoThinner() {
	}

	/**
	 * This constructor must be used to create a usable instance of
	 * HistoThinner. Then, init () and apply () must be called.
	 */
	public HistoThinner(HistoThinnerCutJob[] cutJobs) throws Exception {

		this.cutJobs = cutJobs;
		constructionCompleted = true;

	}

	@Override
	public void init(GModel m, Step s, GScene scene, Collection c) {

		random = new Random();

		this.model = m;

		// This is referentStand.getInterventionBase ();
		stand1 = (GScene) scene;
		stand2 = (TreeCollection) scene;

		if (c == null) {
			concernedTrees = stand2.getTrees();
		} else {
			concernedTrees = c;
		}

		// Save ids for future use
		treeIds = new HashSet<Integer>();
		for (Object o : concernedTrees) {
			Tree t = (Tree) o;
			treeIds.add(t.getId());
		}

		// Define cutting mode : ask model
		mode = (m.isMarkModel()) ? MARK : CUT;

		constructionCompleted = true;

	}

	/**
	 * Can be called after init () and before apply () in script mode. Creates
	 * the cutJobs for the given parameters. Throws an exception in case of
	 * trouble.
	 */
	public HistoThinnerCutJob[] createCutJobs(double targetSDI)
			throws Exception {

		// Get the model's method providers
		MethodProvider mp = model.getMethodProvider();
		StandDensityIndexInterface sdiInterface = null;
		GProvider basalAreaProvider = null;


		if (mp instanceof StandDensityIndexInterface)
			sdiInterface = (StandDensityIndexInterface) mp;
		if (mp instanceof GProvider)
			basalAreaProvider = (GProvider) mp;
		if (sdiInterface == null || basalAreaProvider == null)
			throw new Exception(
					"the model's method provider must be instance of StandDensityIndexInterface AND GProvider, aborted. "
							+ mp);


		// Get the initial values
		MaidHisto histo = new MaidHisto(concernedTrees);
		// If Numberable trees, the maidHisto built one bar for each tree
		// If individual based model only, aggregate:
		Tree t1 = concernedTrees.iterator().next();
		if (!(t1 instanceof Numberable))
			histo.update(); // with default config (classWidth = 5...)

		// Calculate nBefore thinning
		double nb = 0d;
		for (Object o : concernedTrees) {
			if (o instanceof Numberable) {
				Numberable t = (Numberable) o;
				nb += t.getNumber();
			} else {
				nb++;
			}
		}
		int nBefore = (int) Math.round(nb);

		// SDI -> nToCut
		double Gbefore = basalAreaProvider.getG(stand1, concernedTrees);
		int Nafter = sdiInterface.getNafter(targetSDI, Gbefore, nBefore);
		int nToCut = nBefore - Nafter;

		// Apply alder's algorithm in the histo to reach nToCut
		int[] initialValues = histo.getIntValues();
		int[] newValues = getAlderValues(initialValues, nToCut);

		// Get the cutJobs
		this.cutJobs = buildCutJobs(initialValues, newValues, histo, false, 1);

		return cutJobs;

	}

	/**
	 * Alder's thinning algo: given the initial numbers in the classes and the
	 * number of trees to cut, calculate the new class numbers.
	 */
	public int[] getAlderValues(int[] initialValues, int nToCut) {

		int n = initialValues.length;
		int[] cumNj = new int[n];

		// Note: nBefore_equ is the sum of the initialValues in the equalizer
		// these values are integers -> nBefore_equ may be slightly different
		// than the accurate
		// nBefore

		int nBefore_equ = 0;
		for (int i = 0; i < n; i++) {
			nBefore_equ += initialValues[i];
			int prevValue = (i == 0) ? 0 : cumNj[i - 1];
			cumNj[i] = prevValue + initialValues[i];
		}

		double L = ((double) nBefore_equ - nToCut) / nBefore_equ;

		int[] Nafter = new int[n];
		for (int i = 0; i < n; i++) {

			double prevCumNj = (i == 0) ? 0d : (double) cumNj[i - 1];

			double newValue = nBefore_equ
					* L
					* (Math.pow(((double) cumNj[i]) / nBefore_equ, 1d / L) - Math
							.pow(prevCumNj / nBefore_equ, 1d / L));

			Nafter[i] = (int) Math.round(newValue);
		}

		return Nafter;
	}

	/**
	 * Build the cut jobs from the equalizer
	 */
	public HistoThinnerCutJob[] buildCutJobs(int[] initialValues,
			int[] targetValues, MaidHisto histo, boolean perHectare,
			double hectareCoefficient) throws Exception {

		int n = initialValues.length;

		HistoThinnerCutJob[] cutJobs = new HistoThinnerCutJob[n];

		for (int i = 0; i < n; i++) {
			int targetValue = targetValues[i];
			int initialValue = initialValues[i];
			if (targetValue != initialValue) {
				Collection trees = histo.getUTrees(i);

				// If we specified thinning per hectare : reset to stand area
				if (perHectare) {
					initialValue /= hectareCoefficient;
					targetValue /= hectareCoefficient;
				}
				HistoThinnerCutJob job = new HistoThinnerCutJob(initialValue,
						targetValue, trees);
				cutJobs[i] = job;
			}
		}
		return cutJobs;
	}

	@Override
	public boolean initGUI() throws Exception {
		// Interactive start
		HistoThinnerDialog dlg = new HistoThinnerDialog(this, model, stand1,
				concernedTrees);
		constructionCompleted = false;
		if (dlg.isValidDialog()) {
			// Valid -> ok was hit and checks were ok
			try {
				cutJobs = dlg.getCutJobs();
				constructionCompleted = true;

			} catch (Exception e) {
				constructionCompleted = false;
				Log.println(
						Log.ERROR,
						"HistoThinner ()",
						"Could not get parameters in HistoThinnerDialog due to ",
						e);
				throw e;
			}
		}
		dlg.dispose();

		//add cm tl 2 05 2013 form FomPine model
		// to change one property of Fomstump after thinning
		StumpTreatmentAvailabilityProvider stumpTreatmentAvailabilityProvider = null ;
		MethodProvider mp = model.getMethodProvider();
		if (mp instanceof StumpTreatmentAvailabilityProvider) {
			stumpTreatmentAvailabilityProvider =(StumpTreatmentAvailabilityProvider) mp;
			stumpTreatmentAvailabilityProvider.setStumpTraitment (dlg.stumpTreatment.isSelected (), model);
			stumpTreatmentAvailabilityProvider.setProbSporeInfection (Double.parseDouble (dlg.probSporeInfection.getText ()), model);
			stumpTreatmentAvailabilityProvider.setTreatedFailure (Double.parseDouble (dlg.treatedFailure.getText ()), model);

		}

		return constructionCompleted;

	}

	/**
	 * Extension dynamic compatibility mechanism. This matchwith method checks
	 * if the extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith(Object referent) {
		try {
			if (!(referent instanceof GModel)) {
				return false;
			}
			GModel m = (GModel) referent;
			GScene s = ((Step) m.getProject().getRoot()).getScene();

			if (!(s instanceof GScene && s instanceof TreeCollection)) {
				return false;
			}
			TreeCollection l = (TreeCollection) s;
			if (l.getTrees().isEmpty()) {
				return true;
			} // compatible with empty stands

		} catch (Exception e) {
			Log.println(Log.ERROR, "HistoThinner.matchWith ()",
					"Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}

	/**
	 * GroupableIntervener interface. This intervener acts on trees, tree groups
	 * can be processed.
	 */
	public String getGrouperType() {
		return Group.TREE;
	}

	/**
	 * These assertions are checked at the beginning of apply ().
	 */
	private boolean assertionsAreOk() {
		if (mode != CUT && mode != MARK) {
			Log.println(Log.ERROR, "HistoThinner.assertionsAreOk ()",
					"Wrong mode=" + mode + ", should be " + CUT + " (CUT) or "
							+ MARK + " (MARK). HistoThinner is not appliable.");
			return false;
		}
		if (stand1 == null) {
			Log.println(Log.ERROR, "HistoThinner.assertionsAreOk ()",
					"stand is null. HistoThinner is not appliable.");
			return false;
		}

		// To be completed
		//
		// ~ if (min < 0) {
		// ~ Log.println (Log.ERROR, "HistoThinner.assertionsAreOk ()",
		// ~ "min < 0. HistoThinner is not appliable.");
		// ~ return false;
		// ~ }
		// ~ if (max < min) {
		// ~ Log.println (Log.ERROR, "HistoThinner.assertionsAreOk ()",
		// ~ "max < min. HistoThinner is not appliable.");
		// ~ return false;
		// ~ }

		return true;
	}

	/**
	 * From Intervener. Control input parameters.
	 */
	public boolean isReadyToApply() {
		// Cancel on dialog in interactive mode -> constructionCompleted = false
		if (constructionCompleted && assertionsAreOk()) {
			return true;
		}
		return false;
	}

	/**
	 * From Intervener. Makes the action : thinning.
	 */
	public Object apply() throws Exception {

		// 0. Check if apply possible (should have been done before : security)
		if (!isReadyToApply()) {
			throw new Exception(
					"HistoThinner.apply () - Wrong input parameters, see Log");
		}

		stand1.setInterventionResult(true);

		// 1. Process cutJobs
		for (int i = 0; i < cutJobs.length; i++) {

			HistoThinnerCutJob cutJob = cutJobs[i];
			if (cutJob == null) {
				continue;
			} // For some classes, nothing to cut -> null cutJob

			int totalNumber = cutJob.getTotalNumber();
			int targetNumber = cutJob.getTargetNumber();
			int nToCut = totalNumber - targetNumber; // number to cut
			int nCut = 0;
			Collection trees = cutJob.getTrees();
			int n = trees.size();
			if (targetNumber == 0) {
				nToCut = n;            // tl - 02.04.2015 - cut all existing trees in this range if targetNumber is null. tl 2015
			}


			if (n == 0) {
				continue;
			} // no trees to consider

			Tree representative = (Tree) trees.iterator().next();

			// Numberables -> change their number
			if (representative instanceof Numberable) {

				boolean allZero = false;

				// fc - 18.11.2004 - trouble with luberon : Numberable but not
				// GMaidTree ->
				// correction
				Numberable[] maidTree = (Numberable[]) trees
						.toArray(new Numberable[n]);

				// Status preparation: keep the initial number for each tree of
				// the cutJob - fc -
				// 5.7.2005
				double[] memoNumber = new double[maidTree.length]; // fc -
																	// 22.8.2006
																	// -
																	// Numberable
																	// returns
																	// double
				for (int k = 0; k < maidTree.length; k++) {
					memoNumber[k] = maidTree[k].getNumber();
				}

				// Special case, set all tree numbers to zero - fc - 1.9.2005
				if (targetNumber == 0) {
					for (int k = 0; k < maidTree.length; k++) {
						maidTree[k].setNumber(0);
					}

					// Normal case, decrement tree numbers to reach the target
					// number - fc -
					// 1.9.2005
				} else {

					while (!allZero && nCut < nToCut) {

						// Find candidate
						int k = random.nextInt(n); // [0, n[
						int k0 = k;
						while (!allZero && maidTree[k].getNumber() == 0) {
							k = (k + 1) % n;
							if (k == k0) {
								allZero = true;
							}
						}
						if (allZero) {
							continue;
						} // Stop the loop

						// Decrement candidate's number
						maidTree[k].setNumber(maidTree[k].getNumber() - 1);
						nCut++;

					}

				}

				// Status for the trees in the just processed cutJob - fc -
				// 5.7.2005
				for (int k = 0; k < maidTree.length; k++) {
					double n0 = memoNumber[k]; // fc - 22.8.2006 - Numberable
												// returns double
					double n1 = maidTree[k].getNumber(); // fc - 22.8.2006 -
															// Numberable
															// returns
															// double
					double c = n0 - n1; // fc - 22.8.2006 - Numberable returns
										// double

					if (stand1 instanceof TreeList) {
						if (c > 0) {
							((TreeList) stand1).storeStatus(maidTree[k], "cut",
									c);
						}
					}
				}

				// ~ System.out.println ("HistoThinner: Cut for GMaidTree");
				// ~ System.out.println ("nToCut="+nToCut+" nCut="+nCut);
				// ~ for (Iterator j = trees.iterator (); j.hasNext ();) {
				// ~ GMaidTree t = (GMaidTree) j.next ();
				// ~ System.out.println (" t_"+t.getId ()+"("+t.getNumber
				// ()+")");
				// ~ }
				// ~ System.out.println ("done");

				// Individual trees -> remove some of them
			} else {

				Tree[] maddTree = (Tree[]) trees.toArray(new Tree[n]);
				Set treesToCut = new HashSet();

				int nCandidates = 0;
				while (nCandidates != nToCut) {
					int k = random.nextInt(n); // [0, n[
					Tree candidate = maddTree[k];
					if (!treesToCut.contains(candidate)) {
						treesToCut.add(candidate);
						nCandidates++;
					}
				}
				for (Iterator l = treesToCut.iterator(); l.hasNext();) {
					Tree t = (Tree) l.next();
					stand2.removeTree(t);
					nCut++;

					// fc - 18.3.2004
					if (stand1 instanceof TreeList) {
						if (!(t instanceof Numberable)) {
							((TreeList) stand1).storeStatus(t, "cut");
						}
					}

				}

				// ~ System.out.println ("nToCut="+nToCut+" nCut="+nCut);
				// ~ for (Iterator j = trees.iterator (); j.hasNext ();) {
				// ~ GTree t = (GTree) j.next ();
				// ~ if (treesToCut.contains (t)) {continue;}
				// ~ System.out.println (" t_"+t.getId ());
				// ~ }
				// ~ System.out.println ("done");

			}

		}



		return stand1;
	}

	/**
	 * toString () method.
	 */
	public String toString() {

		StringBuffer b = new StringBuffer();
		for (HistoThinnerCutJob cj : cutJobs) {
			if (cj == null)
				continue; // no trace for null cut jobs
			b.append("\n");
			b.append(cj);
		}

		return getClass().getSimpleName() + "\n" + "stand: " + stand1 + "\n"
				+ "Cut jobs: " + b.toString();
	}

	@Override
	public void activate() {
		// TODO Auto-generated method stub

	}

}
