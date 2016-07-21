/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2001  Francois de Coligny
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package capsis.extension.intervener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import jeeb.lib.util.Log;
import jeeb.lib.util.StatusDispatcher;
import jeeb.lib.util.Translator;
import capsis.defaulttype.Numberable;
import capsis.defaulttype.SpatializedTree;
import capsis.defaulttype.TreeCollection;
import capsis.defaulttype.TreeList;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.Intervener;
import capsis.lib.fire.FiModel;
import capsis.lib.fire.FiStand;
import capsis.lib.fire.fuelitem.FiParticle;
import capsis.lib.fire.fuelitem.FiPlant;
import capsis.lib.fire.intervener.FiIntervenerWithRetention;
import capsis.util.Group;
import capsis.util.GroupableIntervener;
import capsis.util.TreeDbhComparator;
import capsis.util.methodprovider.CrownDiameterProvider;
import capsis.util.methodprovider.CrownRadiusProvider;

/**
 * Create an NrgThinner2. For interactive mode, use constructor with
 * ExtensionStarter (containing stand to thin and mode CUT/MARK trees). A dialog
 * box is showed to get user choices. For console mode, use the other
 * constructor with specific paramater NrgThinner2Starter.
 * 
 * @author Ph. Dreyfus - february 2008 version 2.0 : more generic (GMaddTree) //
 *         PhD 2008-09-11 version 2.1 : keep big trees (as much as possible) or
 *         NOT // PhD 2008-09-16 version 2.2 : can be now applied to a group of
 *         trees // fc + FP - 1.2.2010 Version 3.0: fp added simulated
 *         annealing, exhaustive method and forester method
 */
public class NrgThinner2 extends FiIntervenerWithRetention  implements GroupableIntervener, Intervener {

	public static final String NAME = "NrgThinner2";
	public static final String VERSION = "3.0";
	public static final String AUTHOR =  "Ph. Dreyfus";
	public static final String DESCRIPTION = "NrgThinner2.description";
	public static String SUBTYPE = "SelectiveThinner";

	public static Random random = new Random ();

	static {
		Translator.addBundle ("capsis.extension.intervener.NrgThinner2");
	}

	private boolean constructionCompleted = false;		// if cancel in interactive mode, false
	private int mode;				// CUT or MARK
	private GScene stand;			// Reference stand: will be altered by apply ()
	private GModel model;			// Associated model

	private int distCriterion;
	private double minDist;
	// private boolean KeepBigTrees;
	private int thinningCriterion;  // 0 randomWalk; 1 keep big trees; 2 forester; 3 simulated annealing; 4 optimal
	private double martellingDist;

	/**	Default constructor
	 */
	public NrgThinner2 () {
	}

	/**	Build an individual thinner in console mode.
	 *	It can then be executed by init () and apply ().
	 *	@param distCriterion: 0:stem, 1:crown
	 *	@param minDist: distance between stems or crown depending on distCriterion
	 *	@param thinningCriterion: 0:randomWalk, 1:keepBigTrees, 2:foresterLike, 3:simulatedAnnealing, 4:optimal
	 *	@param martellingDist: martelling distance if thinningCriterion = foresterLike
	 */
	public NrgThinner2 (int distCriterion, double minDist, 
			int thinningCriterion, double martellingDist) {

		this.minDist = minDist;
		this.distCriterion = distCriterion;
		// this.KeepBigTrees = keepBigTrees;
		this.thinningCriterion = thinningCriterion;
		this.martellingDist = martellingDist;
		constructionCompleted = true;

		// now, call init () and apply ()
	}

	/**	Build an individual thinner in console mode when the activityFuelRetention is an option (for FiModel)
	 *	It can then be executed by init () and apply ().
	 *	@param distCriterion: 0:stem, 1:crown
	 *	@param minDist: distance between stems or crown depending on distCriterion
	 *	@param thinningCriterion: 0:randomWalk, 1:keepBigTrees, 2:foresterLike, 3:simulatedAnnealing, 4:optimal
	 *	@param martellingDist: martelling distance if thinningCriterion = foresterLike
	 */
	public NrgThinner2 (int distCriterion, double minDist, 
			int thinningCriterion, double martellingDist,boolean activityFuelRetention, double residualFuelHeight, double residualFuelCoverFraction, double residualFuelCharacteristicSize, double residualFuelMoisture) {
		super(activityFuelRetention, residualFuelHeight, residualFuelCoverFraction, residualFuelCharacteristicSize, residualFuelMoisture);
		this.minDist = minDist;
		this.distCriterion = distCriterion;
		this.thinningCriterion = thinningCriterion;
		this.martellingDist = martellingDist;
		
		constructionCompleted = true;

		// now, call init () and apply ()
	}

	
	
	@Override
	public void init(GModel m, Step s, GScene scene, Collection c) {
		model = m;
		stand = scene;

		// 0. Define mode : ask model
		if (model.isMarkModel ()) {
			mode = MARK;
		} else {
			mode = CUT;
		}

		// GroupableIntervener
		if (c == null) {
			concernedTrees = ((TreeList) stand).getTrees ();
		} else {
			concernedTrees = c;
		}

	}

	@Override
	public boolean initGUI() throws Exception {
		NrgThinner2Dialog dlg = new NrgThinner2Dialog (stand, model);
		constructionCompleted = false;
		if (dlg.isValidDialog ()) {
			// valid -> ok was hit and check were ok
			try {
				minDist = dlg.getMinDist();
				distCriterion = dlg.getDistCriterion();
				// KeepBigTrees = dlg.getKeepBigTrees ();
				thinningCriterion = dlg.getThinningCriterion();
				martellingDist = dlg.getMartellingDistance();
				if (model instanceof FiModel) {
					this.setResidualFuelProperties(dlg);
				}
				constructionCompleted = true;
			} catch (Exception e) {
				constructionCompleted = false;
			}
		}
		dlg.dispose ();

		return constructionCompleted;
	}

	@Override
	public void activate() {
		// TODO Auto-generated method stub

	}

	/**
	 * Extension dynamic compatibility mechanism.
	 * This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof GModel)) {return false;}
			GModel m = (GModel) referent;
			GScene s = ((Step) m.getProject ().getRoot ()).getScene ();
			if (!(s instanceof TreeList)) {return false;}
			MethodProvider mp = m.getMethodProvider ();					// PhD 2008-09-11
			if (!(mp instanceof CrownRadiusProvider) && !(mp instanceof CrownDiameterProvider)) {return false;}		// PhD 2008-09-11
			TreeCollection tc = (TreeCollection) s;							// PhD 2008-09-11
			if (!(tc.getTrees ().iterator ().next () instanceof SpatializedTree)) {return false;}	// PhD 2008-09-11

			//if (tc.getTrees ().iterator ().next () instanceof Numberable) {return false;}	// PhD 2008-09-11  ~PhD 2009-04-23
			// the user should ensure all Number are = 1


		} catch (Exception e) {
			Log.println (Log.ERROR, "NrgThinner2.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}

	/**
	 * GroupableIntervener interface. This intervener acts on trees, tree groups
	 * can be processed.
	 */
	public String getGrouperType () {return Group.TREE;}


	/** These assertions must be checked before apply.
	 */
	private boolean assertionsAreOk () {
		if (mode != CUT && mode != MARK) {
			Log.println (Log.ERROR, "NrgThinner2.assertionsAreOk ()", "Wrong mode="+mode
					+", should be "+CUT+" (CUT) or "+MARK+" (MARK). NrgThinner2 is not appliable.");
			return false;
		}
		if (model == null) {
			Log.println (Log.ERROR, "NrgThinner2.assertionsAreOk ()",
			"model is null. NrgThinner2 is not appliable.");
			return false;
		}
		if (stand == null) {
			Log.println (Log.ERROR, "NrgThinner2.assertionsAreOk ()",
			"stand is null. NrgThinner2 is not appliable.");
			return false;
		}

		return true;
	}

	/**	Intervener.
	 *	Control input parameters.
	 */
	public boolean isReadyToApply () {
		// Cancel on dialog in interactive mode -> constructionCompleted = false
		if (constructionCompleted && assertionsAreOk ()) {return true;}
		return false;
	}


	/**	Intervener.
	 *	Makes the action: thinning.
	 */
	public Object apply () throws Exception {
		// 0. Check if apply possible (should have been done before : security)
		if (!isReadyToApply ()) {
			throw new Exception ("NrgThinner2.apply () - Wrong input parameters, see Log");
		}

		stand.setInterventionResult (true);
		if (model instanceof FiModel) {
			if (activityFuelRetention) {
				buildLayerSet();
			}
		}
		
		Collection trees = concernedTrees;

		// 1. Iterate and cut
		Object[] vctI_; // original collection
		vctI_ = trees.toArray ();
		Object[] vctI;   // collection used (sorted or randomized) for thinning
		vctI = trees.toArray (); // at initialization, is the same as original collection
		int max, rank;
		// here we just resort trees for thinning criterion 0 ,1 2, 3
		// in order to examine trees in the appropriate order
		if (thinningCriterion == 0) { // random
			// randomize
			max = trees.size();
			rank = 0;
			for (int i = 0; i < trees.size(); i++) {
				rank = random.nextInt(max);
				vctI[i] = vctI_[rank];
				vctI_[rank] = vctI_[max - 1];
				max = max - 1;
			}
		} else if (thinningCriterion == 1 || thinningCriterion == 3) { // keep
			// big
			// trees
			Arrays.sort(vctI, new TreeDbhComparator(false)); // sort Dbh in
			// descending
			// order
		} else if (thinningCriterion == 2) {// foresterLike
			// reordering trees in 10 m cells
			Arrays.sort(vctI, new TreeDbhComparator(false)); // sort Dbh in
			// descending in order to keep the 10 % bigger

			double largeTreeKeptP = 0d; // 10%
			int largeTreeKeptNumber = (int) Math.floor(trees.size()
					* largeTreeKeptP * 0.01);
			double cellSize = martellingDist;

			double minX = Double.MAX_VALUE;
			double maxX = Double.MIN_VALUE;
			double minY = Double.MAX_VALUE;
			double maxY = Double.MIN_VALUE;


			for (int i = 0; i < trees.size(); i++) {
				SpatializedTree t = (SpatializedTree) vctI_[i];
				minX = Math.min(t.getX(), minX);
				maxX = Math.max(t.getX(), maxX);
				minY = Math.min(t.getY(), minY);
				maxY = Math.max(t.getY(), maxY);
			}
			int nx = (int) Math.ceil((maxX - minX) / cellSize);
			int ny = (int) Math.ceil((maxY - minY) / cellSize);

			// Collection[][] treeLocation = new Collection[nx][ny];
			List[][] treeLocation = new List[nx][ny];
			//System.out.println("nx=" + nx + ", ny=" + ny);
			for (int i = largeTreeKeptNumber; i < trees.size(); i++) {
				SpatializedTree t = (SpatializedTree) vctI_[i];
				double x = t.getX();
				double y = t.getY();
				int ix = (int) Math.floor((x - minX) / (maxX - minX + 1e-3)
						* nx);
				int iy = (int) Math.floor((y - minY) / (maxY - minY + 1e-3)
						* ny);
				if (treeLocation[ix][iy] == null) {
					treeLocation[ix][iy] = new ArrayList();
				}
				treeLocation[ix][iy].add(t);
			}
			int newIndex = largeTreeKeptNumber;
			for (int ix = 0; ix < nx; ix++) {
				for (int iy = 0; iy < ny; iy++) {
					if (treeLocation[ix][iy] != null) {
						Collections.sort(treeLocation[ix][iy],
								new TreeDbhComparator(false)); // sort Dbh in
						// Arrays.sort(treeLocation[ix][iy], new
						// TreeDbhComparator(false)); // sort Dbh in
						// descending
						// order

						for (Iterator it = treeLocation[ix][iy].iterator(); it
						.hasNext();) {

							// int j = (Integer) it.next();
							// System.out.println("tree  :ix=" + ix
							// + ", iy=" + iy + ", new index is "
							// + newIndex);
							vctI[newIndex] = it.next();
							// System.out.println("newIndex="
							// + newIndex
							// + "; id="
							// + ((SpatializedTree) vctI[newIndex])
							// .getId());
							newIndex++;
						}
					}


				}
			}


		}
		// trees are now ordered correctly and can be examine to remove the
		// other
		if (this.thinningCriterion <= 3) {
			for (int i = 0; i < vctI.length; i++) {
				SpatializedTree t = (SpatializedTree) vctI[i];
				if (!t.isMarked()) {
					for (int j = i + 1; j < vctI.length; j++) {
						SpatializedTree t2 = (SpatializedTree) vctI[j];
						if (!t2.isMarked() && computeDistance(t, t2) < minDist) {
							// distance mini entre tiges
							t2.setMarked(true);
						}
					}
				}
			}
			if (this.thinningCriterion == 3) { // =3:simulated annealing
				// thinning
				// this method need to be initialize with a solution (here keep
				// big trees)
				this.computeOptimalWithSimulatedAnnealing(vctI);
			}

		} else if (this.thinningCriterion == 4) { // =4:optimal thinning
			// this method should not be applied for more than 25 trees (growth
			// exponentially)
			System.out.println("compute optimal thinning on " + vctI.length
					+ " trees");
			computeOptimalThinning(vctI);
		}




		// check method result
		boolean thinningCorrectlyDone = true;
		for (int i = 0; i < vctI.length; i++) {
			SpatializedTree t1 = (SpatializedTree) vctI[i];
			if (!t1.isMarked()) {
				for (int j = i + 1; j < vctI.length; j++) {
					SpatializedTree t2 = (SpatializedTree) vctI[j];
					if (!t2.isMarked() && computeDistance(t1, t2) < minDist) { // distance
						// mini
						// entre
						// tiges
						thinningCorrectlyDone = false;
					}
				}
			}
		}


		System.out.println("thinning correctly done = " + thinningCorrectlyDone);


		// Removing the selected trees :
		Iterator ite2 = trees.iterator ();
		while (ite2.hasNext ()) {
			SpatializedTree t2 = (SpatializedTree) ite2.next ();
			if (t2.isMarked () ) {
				if (mode == CUT) {
					if (activityFuelRetention) {
						FiPlant plant = (FiPlant) t2;
						for (FiParticle fp : plant.getParticles()) {
							if (plant.biomass.containsKey(fp.name)) {
							  double load = plant.getBiomass(fp.name)/layerSet.getPolygonArea();
							  layer.addFiLayerParticleFromLoad(load, fp);
							}
						}
					}
					ite2.remove ();
					((TreeCollection) stand).removeTree (t2);
				} else if (mode == MARK) {
					t2.setMarked (true);
				}
				// fc - 18.3.2004
				if (!(t2 instanceof Numberable)) {
					((TreeList) stand).storeStatus (t2, "cut");
				} else {
					Numberable n = (Numberable) t2;
					((TreeList) stand).storeStatus (n, "cut", n.getNumber ());	// fc + tl - 7.3.2006 - "cut all this tree"
				}
			}
		}
		if (activityFuelRetention) {
			layerSet.addLayer(layer);
			((FiStand) stand).addLayerSet(layerSet);
		}
		return stand;
	}

	/**
	 * this method is the simulated annealing (recuit simul�), based on
	 * maximisation of remaining basal area a new thinning plan is defined from
	 * the previous one (see below) and this new thinning plan becomes the new
	 * thiining if the basal area increases with probaility 1 or if it decreases
	 * witha probability that decrease with number of iteration
	 * 
	 * A new state is obtained from the previous on by picking a tree, switch
	 * his status (if mark, then unmark and vice versa) and updating
	 * neighbooring trees (mark the ones that should be or unmark the ones that
	 * could be (to maximise the basal area, using computeOptimalThinning)
	 * 
	 * @param vctI
	 * 
	 * 
	 */
	private void computeOptimalWithSimulatedAnnealing(Object[] vctI) {

		for (int it = 0; it < 20 * vctI.length; it++) {
			// // TEMP
			// double numberOfUnmarkedTrees = 0d;
			// for (int i = 0; i < vctI.length; i++) {
			// if (!((SpatializedTree) vctI[i]).isMarked()) {
			// numberOfUnmarkedTrees++;
			// }
			// }
			// System.out
			// .println("numberOfUnmarkedTrees=" + numberOfUnmarkedTrees);
			// / TEMP
			double basalAreaDelta = 0d;
			Collection<Integer> treeToBeSwaped = new ArrayList<Integer>();
			int rank = random.nextInt(vctI.length);
			SpatializedTree t = (SpatializedTree) vctI[rank];
			// let's pick a tree
			if (t.isMarked()) {
				// lets try to "demark" it: basal area will increase with it
				// but other trees (the one close to it should be marked
				for (int i = 0; i < vctI.length; i++) {
					SpatializedTree t2 = (SpatializedTree) vctI[i];
					double dbh2 = t2.getDbh() * t2.getDbh();
					if (i == rank) {
						basalAreaDelta += dbh2;
						treeToBeSwaped.add(i);
						// System.out.println("tree added is " + i + " diam="
						// + t2.getDbh());
					} else if (!t2.isMarked()
							&& computeDistance(t, t2) < minDist) {
						basalAreaDelta -= dbh2;
						treeToBeSwaped.add(i);
						// System.out.println("tree removed is " + i + " diam="
						// + t2.getDbh());
					}
				}
			} else { // t is not marked
				// let s try to mark it, basal area will decrease with it
				// but other trees might be demarked
				Map<Integer, Object> treePotentiallyDemarked = new HashMap<Integer, Object>();
				for (int i = 0; i < vctI.length; i++) {
					SpatializedTree t2 = (SpatializedTree) vctI[i];

					if (computeDistance(t, t2) < minDist) {
						if (i == rank) {
							basalAreaDelta -= t2.getDbh() * t2.getDbh();
							treeToBeSwaped.add(i);
							// System.out.println("i=irank; " + basalAreaDelta);
						} else if (t2.isMarked()) {
							// can this tree be demarked?
							// we look at the not marked tree in its
							// neighboorhood, put them in the collection 
							// and then choose the biggest trees that satisfy the relashionship
							boolean t2mightBeDemarked = true;
							for (int j = 0; j < vctI.length; j++) {
								if (j != rank && j != i) {
									SpatializedTree t3 = (SpatializedTree) vctI[j];
									if ((!t3.isMarked())
											&& computeDistance(t2, t3) < minDist) {
										t2mightBeDemarked = false;
									}
								}
							}

							if (t2mightBeDemarked) {
								treePotentiallyDemarked.put(i, vctI[i]);
							}

						}
					}
				}
				// we have to check amongs the potentially demarked trees, the biggest that satisfied the distance property
				if (treePotentiallyDemarked.size() <= 10) {
					// exhaustive search on this small set
					// be careful: thise method really unmark the trees so we
					// have to
					// remark them (see lines below)
					// before testing the value of basalAreaDelta and swap them

					basalAreaDelta += computeOptimalThinning(treePotentiallyDemarked
							.values()
							.toArray());

				} else {
					Object[] temp = treePotentiallyDemarked.values().toArray();
					Arrays.sort(temp, new TreeDbhComparator(false)); // sort
					// Dbh
					// in
					for (int i = 0; i < temp.length; i++) {
						SpatializedTree t3 = (SpatializedTree) temp[i];
						for (int j = i + 1; j < temp.length; j++) {
							SpatializedTree t2 = (SpatializedTree) temp[j];
							if (t2.isMarked())
								continue;
							if (computeDistance(t3, t2) < minDist) { // distance
								// mini
								// entre
								// tiges
								t2.setMarked(true);
							}
						}
					}

				}
				for (int i : treePotentiallyDemarked.keySet()) {
					SpatializedTree t3 = (SpatializedTree) treePotentiallyDemarked
					.get(i);
					if (!t3.isMarked()) {
						t3.setMarked(true);
						treeToBeSwaped.add(i);
					}
				}
			}
			// // TEMP
			// numberOfUnmarkedTrees = 0d;
			// for (int i = 0; i < vctI.length; i++) {
			// if (!((SpatializedTree) vctI[i]).isMarked()) {
			// numberOfUnmarkedTrees++;
			// }
			// }
			// System.out.println("numberOfUnmarkedTrees="+numberOfUnmarkedTrees);
			// / TEMP

			double rand = Math.random(); // between 0 and 1
			double p = Math.exp(0.01 * (basalAreaDelta) / 200d * it);
			if (rand < p) {
				// System.out.println("iteration " + it + ": basalAreaDelta="
				// + basalAreaDelta + "	tree was marked=" + t.isMarked());
				for (int i : treeToBeSwaped) {
					SpatializedTree t2 = (SpatializedTree) vctI[i];
					t2.setMarked(!t2.isMarked());
				}
			}
		}
	}

	// compute opt mal thinning (maximazing basal area) (might be long) (return
	// the basal area)
	private double computeOptimalThinning(Object[] vctI) {
		int n = vctI.length;

		boolean[] tempMarkedTrees = new boolean[n];
		boolean[] markedTrees = new boolean[n];
		for (int tn = 0; tn < n; tn++) {
			tempMarkedTrees[tn] = false;
			markedTrees[tn] = false;
		}
		double tempBasalArea = 0d;
		double basalArea = 0d;

		boolean allTested = false;
		int numberOfCaseTested = 0;
		while (!allTested && n > 0) {
			StatusDispatcher
			.print(Translator
					.swap("NrgThinner2.investigating all scenarios:")
					+ " "
					+ (int) (100d * (1d + numberOfCaseTested) / Math
							.pow(2d, n)) + " % done");
			// System.out.println("Number of configuration tested "
			// + numberOfCaseTested);
			numberOfCaseTested++;
			// generation of a new combination of thinned trees (from none of
			// them marked to all of them)
			tempBasalArea = 0d;
			boolean firstMarkTreeReached = false;
			int tn = 0;
			while (!firstMarkTreeReached && tn < n) {
				if (!tempMarkedTrees[tn]) {
					tempMarkedTrees[tn] = true;
					firstMarkTreeReached = true;
					for (int tn2 = 0; tn2 < tn; tn2++) {
						tempMarkedTrees[tn2] = false;
					}
					// System.out.println("firstMarkTree index " + tn);
				} else if (tn == n - 1) {
					allTested = true;
				}
				tn++;
			}
			// compute basal area of current combination
			for (tn = 0; tn < n; tn++) {
				if (!tempMarkedTrees[tn]) {
					SpatializedTree t2 = (SpatializedTree) vctI[tn];
					tempBasalArea += t2.getDbh() * t2.getDbh();
				}
			}
			// check if the current combination (in terms of distance between
			// trees)
			boolean thinningCorrectlyDone = true;
			for (int i = 0; i < n; i++) {
				SpatializedTree t1 = (SpatializedTree) vctI[i];
				if (!tempMarkedTrees[i]) {
					for (int j = i + 1; j < vctI.length; j++) {
						SpatializedTree t2 = (SpatializedTree) vctI[j];
						if (!tempMarkedTrees[j]
						                     && computeDistance(t1, t2) < minDist) {
							thinningCorrectlyDone = false;
						}
					}
				}
			}
			if (!thinningCorrectlyDone) {
				tempBasalArea = 0d;
			}
			// /System.out.println("tempBasalArea " + tempBasalArea);
			if (tempBasalArea > basalArea) {
				basalArea = tempBasalArea;
				for (tn = 0; tn < n; tn++) {
					markedTrees[tn] = tempMarkedTrees[tn];
				}
			}

			for (tn = 0; tn < n; tn++) {
				SpatializedTree t2 = (SpatializedTree) vctI[tn];
				t2.setMarked(markedTrees[tn]);
			}
		}
		return basalArea;
	}


	// * this method compute distance between stem ou crown according to
	// distCriterion
	private double computeDistance(SpatializedTree t1, SpatializedTree t2)
	{
		double dist = Math.sqrt((t1.getX() - t2.getX())
				* (t1.getX() - t2.getX()) + (t1.getY() - t2.getY())
				* (t1.getY() - t2.getY()));
		if (distCriterion == 1) { // => distance between Crowns (using
			// crowndiameter only)
			dist = computeDistanceBetweenCylindricCrown(t1,t2);

		} else if (distCriterion == 2) { // => distance between Crowns using
			try { // crownGeometry
				if (t1 instanceof FiPlant && t2 instanceof FiPlant) {
					FiPlant ft1 = (FiPlant) t1;
					FiPlant ft2 = (FiPlant) t2;
					//				// TODO : the following method is not done correctly!
					//					double d1 = dist;
					//					MethodProvider mp = model.getMethodProvider();
					//					double crownr, crownr2;
					//					if (mp instanceof CrownRadiusProvider) {
					//						crownr = ((CrownRadiusProvider) mp).getCrownRadius(t1);
					//					} else if (mp instanceof CrownDiameterProvider) {
					//						crownr = 0.5 * ((CrownDiameterProvider) mp)
					//						.getCrownDiameter(t1);
					//					} else { // SNH
					//						crownr = 0;
					//					}
					//					if (mp instanceof CrownRadiusProvider) {
					//						crownr2 = ((CrownRadiusProvider) mp).getCrownRadius(t2);
					//					} else if (mp instanceof CrownDiameterProvider) {
					//						crownr2 = 0.5 * ((CrownDiameterProvider) mp)
					//						.getCrownDiameter(t2);
					//					} else { // SNH
					//						crownr2 = 0;
					//					}
					//				double d2 = d1 - (crownr + crownr2);
					dist = ft1.computeCrownDistanceWith (ft2, 0);
					//					if (d1<dist || (d2>dist & !(dist<0 && d2<0))) {
					//						System.out.println("dmin="+d2+"    dist="+dist+"   dmax="+d1);
					//					}
				} else {
					throw new Exception(
					"This mode of computation of distance is available for FiPlant only (crownProfile required) ");
				}
			} catch (Exception e) {
				Log.println(Log.ERROR, "NrgThinner2.c ()",
						"unable to compute with distCriterion=2", e);
			}


		} else if (distCriterion == 3) { // => distance between Crowns using with 20 % of variability on diameter
			try { // crownProfile
				if (t1 instanceof FiPlant && t2 instanceof FiPlant) {
					FiPlant ft1 = (FiPlant) t1;
					FiPlant ft2 = (FiPlant) t2;
					dist = ft1.computeCrownDistanceWith(ft2, 40d);
				} else {
					throw new Exception(
					"This mode of computation of distance is available for FiPlant only (crownProfile required) ");
				}
			} catch (Exception e) {
				Log.println(Log.ERROR, "NrgThinner2.c ()",
						"unable to compute with distCriterion=2", e);
			}
		}
		return dist;
	}

	private double computeDistanceBetweenCylindricCrown(SpatializedTree t1, SpatializedTree t2) {
		double dist = Math.sqrt((t1.getX() - t2.getX())
				* (t1.getX() - t2.getX()) + (t1.getY() - t2.getY())
				* (t1.getY() - t2.getY()));

		MethodProvider mp = model.getMethodProvider();
		double crownr, crownr2;
		if (mp instanceof CrownRadiusProvider) {
			crownr = ((CrownRadiusProvider) mp).getCrownRadius(t1);
		} else if (mp instanceof CrownDiameterProvider) {
			crownr = 0.5 * ((CrownDiameterProvider) mp)
			.getCrownDiameter(t1);
		} else { // SNH
			crownr = 0;
		}
		if (mp instanceof CrownRadiusProvider) {
			crownr2 = ((CrownRadiusProvider) mp).getCrownRadius(t2);
		} else if (mp instanceof CrownDiameterProvider) {
			crownr2 = 0.5 * ((CrownDiameterProvider) mp)
			.getCrownDiameter(t2);
		} else { // SNH
			crownr2 = 0;
		}
		dist -= crownr + crownr2;
		return dist;
	}

	/**
	 * Normalized toString () method : should allow to rebuild a filter with
	 * same parameters.
	 */
	public String toString () {
		return "class="+getClass().getName ()
		+ " name=\"" + NAME + "\"";
	}

}


