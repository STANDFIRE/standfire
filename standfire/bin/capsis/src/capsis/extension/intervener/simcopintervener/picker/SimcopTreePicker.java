package capsis.extension.intervener.simcopintervener.picker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import capsis.extension.intervener.simcopintervener.SizeClassGenericEnums.AlgorithmType;
import capsis.extension.intervener.simcopintervener.SizeClassGenericEnums.AverageDistanceComputationMethod;
import capsis.extension.intervener.simcopintervener.SizeClassGenericEnums.SizeClassType;
import capsis.extension.intervener.simcopintervener.picker.sizeclass.PickerSizeClass;
import capsis.extension.intervener.simcopintervener.picker.sizeclass.PickerSizeClasses;
import capsis.extension.intervener.simcopintervener.utilities.InvalidProbabilitiesException;
import capsis.extension.intervener.simcopintervener.utilities.RandomNotFoundException;
import capsis.extension.intervener.simcopintervener.utilities.Tools;
import capsis.extension.intervener.simcopintervener.utilities.TreeDistanceCouple;

public class SimcopTreePicker {

	private static final Random random = new Random ();
	private Collection<PickerTree> treeList;
	private PickerStand scene;
	private int totalNumberOfTreesToPick;
	private int numberOfTreesInScene;
	private double averageDistanceBetweenPickedTrees;
	private PickerSizeClasses sizeClasses;
	private AverageDistanceComputationMethod averageDistanceComputationMethod = AverageDistanceComputationMethod.HexMesh;
	private final ArrayList<String> messages = new ArrayList<String> ();
	private AlgorithmType algoType;

	public SimcopTreePicker (Collection<PickerTree> treeList, PickerStand scene, int nbTreesToPickPerha,
			AverageDistanceComputationMethod method) {
		this.treeList = treeList;
		this.scene = scene;
		this.numberOfTreesInScene = scene.getPickerTrees ().size ();
		double ha = scene.getArea () / 10000d;
		this.totalNumberOfTreesToPick = (int) Math.round (nbTreesToPickPerha * ha);
		this.averageDistanceBetweenPickedTrees = getAverageDistance (nbTreesToPickPerha);
		this.averageDistanceComputationMethod = method;
		setupLog ();
	}

	public SimcopTreePicker (Collection<PickerTree> treeList, PickerStand scene, double targetDensity,
			AverageDistanceComputationMethod method) {
		this.treeList = treeList;
		this.scene = scene;
		double ha = scene.getArea () / 10000d;
		this.numberOfTreesInScene = scene.getPickerTrees ().size ();
		double currentDensity = ((double) numberOfTreesInScene) / ha;
		this.totalNumberOfTreesToPick = (int) Math.round ((currentDensity - targetDensity) * ha);
		this.averageDistanceBetweenPickedTrees = getAverageDistance (totalNumberOfTreesToPick);
		this.averageDistanceComputationMethod = method;
		setupLog ();
	}

	public void setupLog () {
		Handler logHandler = new Handler () {

			@Override
			public void publish (LogRecord record) {
				messages.add (record.getMessage ());
			}

			@Override
			public void flush () {}

			@Override
			public void close () throws SecurityException {}
		};
		Logger pickerLog = Logger.getLogger (SimcopTreePicker.class.getName ());
		pickerLog.addHandler (logHandler);
		Logger pickerSubLog = Logger.getLogger (PickerSizeClasses.class.getName ());
		pickerSubLog.addHandler (logHandler);
	}

	public String getLogMessages () {
		if (!messages.isEmpty ()) {
			StringBuilder msgSB = new StringBuilder ();
			for (Iterator<String> it = messages.iterator (); it.hasNext ();) {
				String logLine = it.next ();
				msgSB.append (logLine);
				if (it.hasNext ()) {
					msgSB.append ("\n");
				}
			}
			return msgSB.toString ();

		}
		return null;
	}

	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder ("Tree picking at");
		double age = 0;
		double nbTrees = 0;
		for (PickerTree t : treeList) {
			age += t.getAge ();
			nbTrees++;
		}
		age /= nbTrees;
		if (age % 1d > -0.00001d && age % 1d < 0.00001d) {
			sb.append (" age ").append ((int) age);
		} else {
			sb.append (" average age ").append (Tools.twoDigitPrecisionDouble.format (age))
					.append (" ( THIS IS A BUG since all trees should have the same age ! ) ");
		}

		// fc+mp-4.6.2014 SimcopSizeClassSpatialIntervener now generic
		double hdom = ((PickerStand) scene).getHdom (treeList);
		// double hdom = new SimcopMethodProvider().getHdom(scene, treeList);

		sb.append (" HDom=").append (Tools.twoDigitPrecisionDouble.format (hdom)).append (". ");
		if (algoType != null) {
			sb.append ("Picker algorithm \"").append (algoType.toString ());
		}
		sb.append ("\" on scene ").append (scene.getCaption ()).append (".");
		if (totalNumberOfTreesToPick > 0) {
			sb.append (" Target = ").append (totalNumberOfTreesToPick).append (" trees");
		}

		return sb.toString ();
	}

	public static double squareMeshAverageDistance (double plotArea, int numberOfTrees) {
		return Math.sqrt (plotArea / numberOfTrees);
	}

	public static double hexMeshAverageDistance (double plotArea, int numberOfTrees) {
		return Math.sqrt ((2 * plotArea) / (numberOfTrees * Math.sqrt (3)));
	}

	private double getAverageDistance (int nbTree) {
		switch (averageDistanceComputationMethod) {
		case HexMesh:
			// hex mesh average distance computation
			return hexMeshAverageDistance (scene.getArea (), nbTree);
		case SquareMesh:
			// square mesh average distance computation
			return squareMeshAverageDistance (scene.getArea (), nbTree);
		}
		return 0d;
	}

	// JM Ottorini volume classes ponderation algorithm with spatial constraints to select trees
	public void methodO1 () throws TargetNumberOfTreesUnreachable, TargetNumberOfTreesNotReached,
			InvalidProbabilitiesException, RandomNotFoundException, Exception {
		PickerTree pickedTree;
		TreeDistanceCouple minDistTreeFromPickedTree;
		boolean distanceConstraintOK;
		int loopSecurity = 0;
		// prepare exclusion list = list of tree whose distance constraint has already been checked
		HashMap<PickerSizeClass,HashSet<PickerTree>> exclusionList = new HashMap<PickerSizeClass,HashSet<PickerTree>> ();
		for (PickerSizeClass sizeClass : sizeClasses.getSizeClasses ()) {
			exclusionList.put (sizeClass, new HashSet<PickerTree> ());
		}
		// begin trees picking
		while (sizeClasses.getPickedTrees ().size () < totalNumberOfTreesToPick) {
			// choose a size class
			PickerSizeClass sizeClass = sizeClasses.getRandomSizeClass ();
			assert sizeClass != null;
			HashSet<PickerTree> currentExclusionList = exclusionList.get (sizeClass);
			// pick a tree in this size class
			do {
				pickedTree = sizeClass.randomGetNonPickedTree (currentExclusionList);
				if (pickedTree == null) {
					// no tree left to pick in this class
					break;
				}

				// fc+mp-6.6.2014 e.g. Gymnos stand is not spatialized
				if (!scene.isSpatialized ()) {
					distanceConstraintOK = true;
				} else {

					// make sure this tree wont be checked several times
					currentExclusionList.add (pickedTree);
					// get min distance from this tree to the trees already picked (in all classes)
					minDistTreeFromPickedTree = Tools.getMinDist (pickedTree, sizeClasses.getPickedTrees ());
					// if there was no other picked tree, the result is null and the distance
					// constraint
					// is not applicable
					if (minDistTreeFromPickedTree == null) {
						distanceConstraintOK = true;
					} else {
						// check the distance rules both from the picked tree class distance factor
						// and
						// the other tree class distance factor
						// we know that picked tree is in current size class
						distanceConstraintOK = minDistTreeFromPickedTree.getDistance () > (sizeClass
								.getDistanceFactor () * averageDistanceBetweenPickedTrees);
						if (distanceConstraintOK) { // the other tree distance factor depends from
													// its
													// size class which we must found
							PickerTree otherTree = minDistTreeFromPickedTree.getTree ();
							PickerSizeClass otherSizeClass = sizeClasses.getSizeClass (otherTree);
							if (otherSizeClass == null) { throw new TargetNumberOfTreesUnreachable (otherTree
									+ " was not found in any size class !!!"); }
							distanceConstraintOK &= minDistTreeFromPickedTree.getDistance () > (otherSizeClass
									.getDistanceFactor () * averageDistanceBetweenPickedTrees);
						}
					}
				}
			} while (!distanceConstraintOK);
			if (pickedTree != null) {
				sizeClass.pickTree (pickedTree);
			} else {
				// if no tree in this size class could be picked, exclude this class from future
				// choices
				sizeClass.setWheight (0d);
				try {
					sizeClasses.normalizeWheights ();
				} catch (InvalidProbabilitiesException e) {
					// nothing left to pick
					throw new TargetNumberOfTreesNotReached (totalNumberOfTreesToPick, sizeClasses.getPickedTrees ()
							.size ());
				}
			}
			// avoid infinite loop.
			loopSecurity++;
			if (loopSecurity > totalNumberOfTreesToPick * 10) {// this is arbitrary..
				throw new TargetNumberOfTreesNotReached ();
			}
		}
	}

	// Minna Pulkkinen modifications to JM Ottorini volume class ponderation algorithm
	private void methodMP1 (boolean smallestClassFirst) throws TargetNumberOfTreesUnreachable,
			TargetNumberOfTreesNotReached, Exception {
		// begin with either smallest or largest class
		PickerTree pickedTree;
		TreeDistanceCouple minDistTreeFromPickedTree;
		HashSet<PickerTree> exclusionList = new HashSet<PickerTree> ();
		for (int classNumber = smallestClassFirst ? 0 : (sizeClasses.size () - 1); smallestClassFirst ? (classNumber < sizeClasses
				.size ()) : (classNumber >= 0); classNumber += smallestClassFirst ? 1 : -1) {
			PickerSizeClass sizeClass = sizeClasses.getSizeClass (classNumber);
			exclusionList.clear ();
			while (sizeClass.getNbOfTreesToPick () > 0) {
				boolean distanceConstraintOK = false;
				do {
					// pick a tree
					pickedTree = sizeClass.randomGetNonPickedTree (exclusionList);
					if (pickedTree == null) {
						// class exhausted
						break;
					}

					// fc+mp-6.6.2014 e.g. Gymnos stand is not spatialized
					if (!scene.isSpatialized ()) {
						distanceConstraintOK = true;
					} else {

						// make sure this tree wont be checked several times
						exclusionList.add (pickedTree);
						// find the distance from this tree to the nearest picked
						minDistTreeFromPickedTree = Tools.getMinDist (pickedTree, sizeClasses.getPickedTrees ());
						// if there was no other picked tree, the result is null and the distance
						// constraint is not applicable
						if (minDistTreeFromPickedTree == null) {
							distanceConstraintOK = true;
						} else {
							// check the distance rules both from the picked tree class distance
							// factor
							// and the other tree class distance factor
							// we know that picked tree is in current size class
							distanceConstraintOK = minDistTreeFromPickedTree.getDistance () > (sizeClass
									.getDistanceFactor () * averageDistanceBetweenPickedTrees);
							if (distanceConstraintOK) {
								// the other tree distance factor depends from its size class wich
								// we
								// must find
								PickerTree otherTree = minDistTreeFromPickedTree.getTree ();
								PickerSizeClass otherSizeClass = sizeClasses.getSizeClass (otherTree);
								if (otherSizeClass == null) { throw new TargetNumberOfTreesUnreachable (otherTree
										+ " was not found in any size class !!!"); }
								distanceConstraintOK &= minDistTreeFromPickedTree.getDistance () > (otherSizeClass
										.getDistanceFactor () * averageDistanceBetweenPickedTrees);
							}
						}
					}

				} while (!distanceConstraintOK);
				if (pickedTree == null) {
					break;
				} else {
					sizeClass.pickTree (pickedTree);
				}
			}
		}
		int nbPickedTrees = sizeClasses.getPickedTrees ().size ();
		if (nbPickedTrees != totalNumberOfTreesToPick) {
			TargetNumberOfTreesNotReached ex = new TargetNumberOfTreesNotReached (totalNumberOfTreesToPick,
					nbPickedTrees);
			throw ex;
		}
	}

	// TB 2014 02
	// Merge JMO's and MP's pick algorithm
	public void methodJMOMPMerge (AlgorithmType algoType, List<Double> sizeClassesWheights,
			List<Double> sizeClassDistanceFactors, SizeClassType sizeClassType, boolean integerBounds,
			boolean wheightByFrequencies, boolean secondPass) throws TargetNumberOfTreesUnreachable, Exception {
		this.algoType = algoType;
		Logger logger = Logger.getLogger (SimcopTreePicker.class.getName ());
		// put information about this thinning in the log
		logger.log (Level.INFO, toString ());
		if (sizeClassType == null) {
			Exception ex = new Exception ("Size class type ommited !");
			String message = ex.getMessage ();
			logger.log (Level.SEVERE, message, ex);
			throw ex;
		}
		if (scene.isSpatialized ()) {
			if (sizeClassesWheights == null || sizeClassDistanceFactors == null
					|| sizeClassesWheights.size () != sizeClassDistanceFactors.size ()) {
				Exception ex = new Exception ("Size class weights and distance factors length differs !");
				String message = ex.getMessage ();
				logger.log (Level.SEVERE, message, ex);
				throw ex;
			}
		}
		if (treeList == null || treeList.isEmpty ()) {
			Exception ex = new Exception ("Tree list provided is null or empty");
			String message = ex.getMessage ();
			logger.log (Level.SEVERE, message, ex);
			throw ex;
		}
		if (scene == null) {
			Exception ex = new Exception ("No PickerStand provided");
			String message = ex.getMessage ();
			logger.log (Level.SEVERE, message, ex);
			throw ex;
		}
		if (totalNumberOfTreesToPick < 1) {
			Exception ex = new Exception ("Target tree selection size < 1 : " + totalNumberOfTreesToPick
					+ " (target density could be > than current density) ");
			String message = ex.getMessage ();
			logger.log (Level.SEVERE, message, ex);
			throw ex;
		}
		if (numberOfTreesInScene < totalNumberOfTreesToPick) {
			Exception ex = new TargetNumberOfTreesUnreachable (totalNumberOfTreesToPick, numberOfTreesInScene);
			String message = ex.getMessage ();
			logger.log (Level.SEVERE, message, ex);
			throw ex;
		}
		// get size classes
		sizeClasses = new PickerSizeClasses (sizeClassType, treeList, sizeClassesWheights, sizeClassDistanceFactors,
				integerBounds);
		// wheights factors with relative frequencies
		if (wheightByFrequencies) {
			sizeClasses.wheightByFrequencies ();
		}
		logger.info ("Size classes : " + sizeClasses.getNonPickedTreesString ());
		boolean MP = false, smallestClassFirst = false;
		if (algoType == AlgorithmType.MPSequentialSizeClassPickFromSmallest) {
			MP = true;
			smallestClassFirst = true;
		} else if (algoType == AlgorithmType.MPSequentialSizeClassPickFromLargest) {
			MP = true;
			smallestClassFirst = false;
		}
		if (MP) {
			// compute the number of tree to pick in each class corresponding to the wheights
			sizeClasses.wheightNumberOfTreesToPickPerClass (totalNumberOfTreesToPick);
			logger.info ("Target by classes : " + sizeClasses.getNbOfTreesToPickString ());
			// compare available number of tree in each class to the number of tree to pick and
			// redispatch if needed
			sizeClasses.redispatch (smallestClassFirst);
			logger.info ("Target by classes after redispatch: " + sizeClasses.getNbOfTreesToPickString ());
		}
		// backup stuff
		List<Double> dfList = sizeClasses.getDistanceFactors ();
		List<Double> wList = sizeClasses.getWheights ();
		Double[] dfArray = sizeClasses.getDistanceFactors ().toArray (new Double[dfList.size ()]);
		boolean retry = false;
		do {
			try {
				if (algoType == AlgorithmType.JMORandomSizeClassPick) {
					// use JM Ottorini method
					methodO1 ();
				} else if (algoType == AlgorithmType.MPSequentialSizeClassPickFromSmallest) {
					// use M Pulkkinen Method
					methodMP1 (true);
				} else if (algoType == AlgorithmType.MPSequentialSizeClassPickFromLargest) {
					// use M Pulkkinen Method
					methodMP1 (false);
				}
				retry = false;
			} catch (TargetNumberOfTreesNotReached e) {
				// if failure, check distance factors
				double sum = 0d;
				for (Double df : sizeClasses.getDistanceFactors ()) {
					sum += df;
				}
				if (sum == 0d) {
					// failure in this case, where factors where already all = 0 is unexpected !
					Exception ex = new TargetNumberOfTreesUnreachable ("Unexpected failure : Impossible to pick "
							+ totalNumberOfTreesToPick + " trees, only " + sizeClasses.getPickedTrees ().size ()
							+ " trees  were picked.");
					String message = ex.getMessage ();
					logger.log (Level.SEVERE, message, ex);
					throw ex;
				} else {
					StringBuilder newDF = new StringBuilder ("[");
					// retry with lower distance factors
					for (int i = 0; i < dfArray.length; i++) {
						dfArray[i] -= 0.1d;
						if (dfArray[i] < 0d) {
							dfArray[i] = 0d;
						}
						newDF.append (Tools.twoDigitPrecisionDouble.format (dfArray[i]));
						if (i < dfArray.length - 1) {
							newDF.append (", ");
						}
					}
					newDF.append ("]");
					logger.log (Level.INFO, e.getMessage () + ", distance factors reduced to " + newDF.toString (), e);
					int i = 0;
					for (PickerSizeClass sizeClass : sizeClasses.getSizeClasses ()) {
						sizeClass.setDistanceFactor (dfArray[i]);
						// also restore wheights for JMO algo
						sizeClass.setWheight (wList.get (i));
						i++;
					}
					retry = true;
				}
			}
		} while (retry);
		if (secondPass && scene.isSpatialized ()) {
			secondPass ();
		}
		logger.log (Level.INFO, "Picking finished  : " + sizeClasses.getPickedTreesString () + " trees picked");
	}

	/*----------------*/
	// second pass paradigm
	// Lorsque l'on a etabli par cet algorithme la liste des arbres a eclaircir, pour chaque
	// arbre de l'eclaircie, on peut essayer de le remplacer par un arbre de la meme classe de
	// volume, verifant  ou pas, ou plus ou moins ...  les meme contraintes d'espacement
	// avec les autres arbres de l'eclaircie, mais plus proche d'un arbre laisse sur pied.
	// Certaines tentatives de remplacement peuvent etre infructueuses.
	/*----------------*/
	private void secondPass () throws Exception {
		int substitutions = 0;
		// iterate picked trees
		for (PickerSizeClass sizeClass : sizeClasses.getSizeClasses ()) {
			ArrayList<PickerTree> pt = new ArrayList<PickerTree> (sizeClass.getPickedTrees ());
			for (PickerTree pickedTree : pt) {
				// record the current distance from this picked tree to the nearest non picked
				TreeDistanceCouple titularMinDistFromNonPickedTree = Tools.getMinDist (pickedTree, sizeClasses
						.getNonPickedTrees ());
				double titularDistanceFromNearestNonPicked = titularMinDistFromNonPickedTree.getDistance ();
				// unpick the tree
				sizeClass.unpickTree (pickedTree);
				HashMap<Double,PickerTree> candidates = new HashMap<Double,PickerTree> ();
				for (PickerTree candidate : sizeClass.getNonPickedTrees ()) {
					if (pickedTree.equals (candidate)) {
						continue;
					}
					// try another tree in the same size class
					boolean distanceConstraintOK = false;
					// find the distance from this tree to the nearest picked
					TreeDistanceCouple minDistTreeFromPickedTree = Tools.getMinDist (candidate, sizeClasses
							.getPickedTrees ());
					// if there was no other picked tree, the result is null and the distance
					// constraint is not applicable
					if (minDistTreeFromPickedTree == null) {
						distanceConstraintOK = true;
					} else {
						// check the distance rules both from the picked tree class distance factor
						// and the other tree class distance factor
						// we know that picked tree is in current size class
						distanceConstraintOK = minDistTreeFromPickedTree.getDistance () > (sizeClass
								.getDistanceFactor () * averageDistanceBetweenPickedTrees);
						if (distanceConstraintOK) {
							// the other tree distance factor depends from its size class wich we
							// must find
							PickerTree otherTree = minDistTreeFromPickedTree.getTree ();
							PickerSizeClass otherSizeClass = sizeClasses.getSizeClass (otherTree);
							if (otherSizeClass == null) { throw new TargetNumberOfTreesUnreachable (otherTree
									+ " was not found in any size class !!!"); }
							distanceConstraintOK &= minDistTreeFromPickedTree.getDistance () > (otherSizeClass
									.getDistanceFactor () * averageDistanceBetweenPickedTrees);
						}
					}
					if (distanceConstraintOK) {
						// record this candidate score
						TreeDistanceCouple candidateMinDistFromNonPickedTree = Tools.getMinDist (candidate, sizeClasses
								.getNonPickedTrees ());
						double candidateDistanceFromNearestNonPicked = candidateMinDistFromNonPickedTree.getDistance ();
						candidates.put (candidateDistanceFromNearestNonPicked, candidate);
					}
				}
				if (candidates.isEmpty ()) {
					// reinstate titular
					sizeClass.pickTree (pickedTree);
				} else {
					// pick best candidate
					ArrayList<Double> sortedDistances = new ArrayList<Double> (candidates.keySet ());
					Collections.sort (sortedDistances);
					Double bestCandidateDistanceFromNearestNonPicked = sortedDistances.get (0);
					// evaluate candidate
					if (bestCandidateDistanceFromNearestNonPicked < titularDistanceFromNearestNonPicked) {
						// replace titular by candidate
						PickerTree candidate = candidates.get (bestCandidateDistanceFromNearestNonPicked);
						sizeClass.pickTree (candidate);
						substitutions++;
					} else {
						// reinstate titular
						sizeClass.pickTree (pickedTree);
					}
				}
			}
		}
		Logger.getLogger (SimcopTreePicker.class.getName ()).log (Level.INFO, substitutions
				+ " subtitutions done in second pass");
	}

	// experiment
	private void evaluate () throws Exception {
		System.out.println ("Average Distance Between Picked Trees = avgDst ="
				+ Tools.twoDigitPrecisionDouble.format (averageDistanceBetweenPickedTrees));
		for (PickerSizeClass sizeClass : sizeClasses.getSizeClasses ()) {
			System.out.println (sizeClass.toString ()
					+ " Min dist = f*avgDst = "
					+ Tools.twoDigitPrecisionDouble.format (sizeClass.getDistanceFactor ())
					+ " * "
					+ Tools.twoDigitPrecisionDouble.format (averageDistanceBetweenPickedTrees)
					+ " = "
					+ Tools.twoDigitPrecisionDouble.format (sizeClass.getDistanceFactor ()
							* averageDistanceBetweenPickedTrees));
			Collection<PickerTree> pt = sizeClasses.getPickedTrees ();
			Collection<PickerTree> npt = sizeClasses.getNonPickedTrees ();
			for (PickerTree pickedTree : sizeClass.getPickedTrees ()) {
				TreeDistanceCouple minDistTreeFromPickedTree = Tools.getMinDist (pickedTree, pt);
				TreeDistanceCouple minDistTreeFromNonPickedTree = Tools.getMinDist (pickedTree, npt);
				double mdpt = minDistTreeFromPickedTree.getDistance ();
				double mdnpt = minDistTreeFromNonPickedTree.getDistance ();
				PickerTree otherPickedTree = minDistTreeFromPickedTree.getTree ();
				PickerTree nonPickedTree = minDistTreeFromNonPickedTree.getTree ();
				PickerSizeClass otherSizeClass = sizeClasses.getSizeClass (otherPickedTree);
				System.out.println (pickedTree.toString () + " min dist from picked trees = "
						+ Tools.twoDigitPrecisionDouble.format (mdpt) + " , from " + otherPickedTree.toString ()
						+ " (df=" + Tools.twoDigitPrecisionDouble.format (otherSizeClass.getDistanceFactor ()) + ")"
						+ " min dist from non picked trees = " + Tools.twoDigitPrecisionDouble.format (mdnpt)
						+ " , from " + nonPickedTree.toString ());
			}
		}
		assert false;
	}

	// compute thin factor K
	public static double getK (Collection<PickerTree> allTrees, Collection<PickerTree> thinnedTrees) {
		double v = 0d, ve = 0;
		for (PickerTree thinned : thinnedTrees) {
			ve += (double) thinned.getVolume ();
		}
		ve /= thinnedTrees.size ();
		for (PickerTree tree : allTrees) {
			v += tree.getVolume ();
		}
		v /= (double) allTrees.size ();
		return ve / v;
	}

	public Collection getPickedTrees () {
		return sizeClasses.getPickedTrees ();
	}

}
