package capsis.extension.intervener.simcopintervener.picker.sizeclass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import capsis.extension.intervener.simcopintervener.SizeClassGenericEnums.SizeClassType;
import capsis.extension.intervener.simcopintervener.picker.PickerTree;
import capsis.extension.intervener.simcopintervener.picker.TargetNumberOfTreesNotReached;
import capsis.extension.intervener.simcopintervener.utilities.DoubleList;
import capsis.extension.intervener.simcopintervener.utilities.InvalidProbabilitiesException;
import capsis.extension.intervener.simcopintervener.utilities.RandomNotFoundException;
import capsis.extension.intervener.simcopintervener.utilities.Tools;

/**
 * A list of size classes.
 * 
 * @author thomas.bronner@gmail.com public static HashMap<Integer, List<SimcopTree>>
 *         getSizeClasses(Collection<SimcopTree> treeList, int i, SizeClassType sizeClassType,
 *         boolean b) { throw new UnsupportedOperationException("Not supported yet."); //To change
 *         body of generated methods, choose Tools | Templates. }
 */
public class PickerSizeClasses {

	private SizeClassType type;
	private List<PickerSizeClass> sizeClasses;

	public void clearPickedTrees () {
		for (PickerSizeClass sizeClass : sizeClasses) {
			sizeClass.clearPickedTrees ();
		}
	}

	public PickerSizeClasses (SizeClassType type, Collection<PickerTree> treeList, List<Double> sizeClassWheights,
			List<Double> distanceFactors, boolean integerBounds) throws Exception {
		this.type = type;
		HashMap<Integer,List<PickerTree>> map = getSizeClasses (treeList, sizeClassWheights.size (), type, integerBounds);
		ArrayList<PickerSizeClass> classes = new ArrayList<PickerSizeClass> ();
		for (Integer classN : map.keySet ()) {

			// fc+mp-4.7.2014 if !scene.isSpatialized () (e.g. Gymnos), distanceFactors size may be
			// different than sizeClassWheights size
			double distanceFactor = 0;
			if (classN < distanceFactors.size ()) {
				distanceFactor = distanceFactors.get (classN);
			}

			classes.add (new PickerSizeClass (map.get (classN), sizeClassWheights.get (classN), distanceFactor)); // fc+mp-4.7.2014
			// classes.add(new PickerSizeClass(map.get(classN), sizeClassWheights.get(classN),
			// distanceFactors.get(classN))); // fc+mp-4.7.2014 buggy if !scene.isSpatialized ()
			
		}
		this.sizeClasses = classes;
		normalizeWheights ();
	}

	public int size () {
		return sizeClasses.size ();
	}

	public List<Double> getDistanceFactors () {
		ArrayList<Double> result = new ArrayList<Double> ();
		for (PickerSizeClass sizeClass : sizeClasses) {
			result.add (sizeClass.getDistanceFactor ());
		}
		return result;
	}

	public List<Double> getWheights () {
		ArrayList<Double> result = new ArrayList<Double> ();
		for (PickerSizeClass sizeClass : sizeClasses) {
			result.add (sizeClass.getWheight ());
		}
		return result;
	}

	// returns trees by size classes of equal width
	public static HashMap<Integer,List<PickerTree>> getSizeClasses (Collection<PickerTree> treeList,
			int numberOfClasses, SizeClassType sizeClassType, boolean integerBounds) throws Exception {
		final String methodName = "SizeClasses.getSizeClasses()";
		// get size by id
		double maxSize = 0d, minSize = Double.MAX_VALUE;
		HashMap<Integer,List<PickerTree>> idSizeClassMap = new HashMap<Integer,List<PickerTree>> ();// sizeClass#=>trees
		// let -1 be the no size class initialization
		idSizeClassMap.put (-1, new ArrayList<PickerTree> ());
		for (Iterator<PickerTree> it = treeList.iterator (); it.hasNext ();) {

			PickerTree stree = it.next ();
			idSizeClassMap.get (-1).add (stree);
			if (sizeClassType == SizeClassType.VolumeClass) {
				if (stree.getVolume () > maxSize) {
					maxSize = stree.getVolume ();
				}
				if (stree.getVolume () < minSize) {
					minSize = stree.getVolume ();
				}
			} else if (sizeClassType == SizeClassType.DiameterClass) {
				if (stree.getDbh () > maxSize) {
					maxSize = stree.getDbh ();
				}
				if (stree.getDbh () < minSize) {
					minSize = stree.getDbh ();
				}
			}
		}
		if (maxSize == 0d) {
			// everything = 0 => only one size class
			idSizeClassMap.put (0, idSizeClassMap.get (-1));
			idSizeClassMap.remove (-1);
			return idSizeClassMap;
		}
		if (integerBounds) {
			// round the max and min to nearest integer
			minSize = Math.floor (minSize);
			maxSize = Math.ceil (maxSize);
		}
		// construct size classes
		double sizeClassMins[] = new double[numberOfClasses];
		double sizeClassMax[] = new double[numberOfClasses];
		double sizeClassesWidth = (maxSize - minSize) / numberOfClasses;
		for (int i = 0; i < numberOfClasses; i++) {
			sizeClassMins[i] = i * sizeClassesWidth + minSize;
			sizeClassMax[i] = sizeClassMins[i] + sizeClassesWidth;
			idSizeClassMap.put (i, new ArrayList<PickerTree> ());
		}
		// fill size classes
		int sizeClassNumber = 0;
		for (Iterator<PickerTree> it = idSizeClassMap.get (-1).iterator (); it.hasNext ();) {
			PickerTree tree = it.next ();
			sizeClassNumber = -1;
			// find volume class
			for (int i = 0; i < numberOfClasses; i++) {
				// last class higher bound is inclusive
				if (sizeClassType == SizeClassType.VolumeClass) {
					if ((i == (numberOfClasses - 1)) && tree.getVolume () >= sizeClassMins[i]
							&& tree.getVolume () <= (sizeClassMax[i] + 0.0000001d)) {// due to
						// double
						// precision
						// floating
						// point
						// implementation,
						// there can
						// sometime
						// be a loss
						// of
						// precision
						sizeClassNumber = i;
						break;
					} else if ((tree.getVolume () >= sizeClassMins[i] && tree.getVolume () < sizeClassMax[i])) {
						sizeClassNumber = i;
						break;
					}
				} else if (sizeClassType == SizeClassType.DiameterClass) {
					if ((i == (numberOfClasses - 1)) && tree.getDbh () >= sizeClassMins[i]
							&& tree.getDbh () <= (sizeClassMax[i] + 0.0000001d)) {// due to double
						// precision
						// floating
						// point
						// implementation,
						// there can
						// sometime be a
						// loss of
						// precision
						sizeClassNumber = i;
						break;
					} else if ((tree.getDbh () >= sizeClassMins[i] && tree.getDbh () < sizeClassMax[i])) {
						sizeClassNumber = i;
						break;
					}
				}
			}
			if (sizeClassNumber != -1) {
				// size class found
				idSizeClassMap.get (sizeClassNumber).add (tree);
				it.remove ();
			} else {
				throw new Exception (methodName + ": cannot find size class for Tree#" + tree.getId () + sizeClassType
						+ " =" + tree.getVolume ());
			}
		}
		// simple DEBUG check
		if (!idSizeClassMap.get (-1).isEmpty ()) {
			throw new Exception (methodName + ": trees not sorted");
		} else {
			idSizeClassMap.remove (-1);
		}
		return idSizeClassMap;
	}

	public SizeClassType getType () {
		return type;
	}

	public void setType (SizeClassType type) {
		this.type = type;
	}

	public List<PickerSizeClass> getSizeClasses () {
		return sizeClasses;
	}

	public void setClasses (List<PickerSizeClass> classes) {
		this.sizeClasses = classes;
	}

	public Collection<PickerTree> getPickedTrees () {
		HashSet<PickerTree> result = new HashSet<PickerTree> ();
		for (PickerSizeClass sizeClass : sizeClasses) {
			result.addAll (sizeClass.getPickedTrees ());
		}
		return result;
	}

	public Collection<PickerTree> getNonPickedTrees () {
		HashSet<PickerTree> result = new HashSet<PickerTree> ();
		for (PickerSizeClass sizeClass : sizeClasses) {
			result.addAll (sizeClass.getNonPickedTrees ());
		}
		return result;
	}

	public Collection<PickerTree> getPickedTreesWithSpatialRule () {
		HashSet<PickerTree> result = new HashSet<PickerTree> ();
		for (PickerSizeClass sizeClass : sizeClasses) {
			if (sizeClass.getDistanceFactor () > 0d) {
				result.addAll (sizeClass.getPickedTrees ());
			}
		}
		return result;
	}

	public Collection<PickerTree> getPickedTreesWithoutSpatialRule () {
		HashSet<PickerTree> result = new HashSet<PickerTree> ();
		for (PickerSizeClass sizeClass : sizeClasses) {
			if (sizeClass.getDistanceFactor () == 0d) {
				result.addAll (sizeClass.getPickedTrees ());
			}
		}
		return result;
	}

	public PickerSizeClass getSizeClass (int volumeClassNumber) {
		return sizeClasses.get (volumeClassNumber);
	}

	public PickerSizeClass getSizeClass (PickerTree tree) {
		for (PickerSizeClass sizeClass : sizeClasses) {
			if (sizeClass.getAllTrees ().contains (tree)) { return sizeClass; }
		}
		return null;
	}

	public void wheightByFrequencies () throws InvalidProbabilitiesException {
		Double[] temp = new Double[sizeClasses.size ()];
		// get total number of trees
		double totalTreeNumber = getTotalNumberOfTrees ();
		int i = 0;
		for (PickerSizeClass sizeClass : sizeClasses) {
			temp[i++] = ((double) sizeClass.getAllTrees ().size ()) / totalTreeNumber;
		}
		// adjust weights
		i = 0;
		for (PickerSizeClass sizeClass : sizeClasses) {
			sizeClass.setWheight (temp[i++] * sizeClass.getWheight ());
		}
		normalizeWheights ();
	}

	private int getTotalNumberOfTrees () {
		int result = 0;
		for (PickerSizeClass sizeClass : sizeClasses) {
			result += sizeClass.getAllTrees ().size ();
		}
		return result;
	}

	// make sure sum(wheights) = 1
	public void normalizeWheights () throws InvalidProbabilitiesException {
		double sum = 0d;
		for (PickerSizeClass sizeClass : sizeClasses) {
			sum += sizeClass.getWheight ();
		}
		if (sum != 0d) {
			for (PickerSizeClass sizeClass : sizeClasses) {
				sizeClass.setWheight (sizeClass.getWheight () / sum);
			}
		} else {
			throw new InvalidProbabilitiesException ("Sum of normalized wheights = 0");
		}
		if (!PickerSizeClasses.isSumOne (getWheights ())) { throw new InvalidProbabilitiesException (
				"Sum of normalized wheights != 1"); }
	}

	// get a size class randomly but according to its wheight
	public PickerSizeClass getRandomSizeClass () throws InvalidProbabilitiesException, RandomNotFoundException {
		int volumeClassNumber = PickerSizeClasses.pickIndex (getWheights ());
		PickerSizeClass sizeClass = getSizeClass (volumeClassNumber);
		return sizeClass;
	}

	// dispatch total number of tree to pick in each size class according to wheight
	public void wheightNumberOfTreesToPickPerClass (int totalNumberOfTreesToPick) throws TargetNumberOfTreesNotReached,
			Exception {
		DoubleList temp = new DoubleList ();
		for (PickerSizeClass sizeClass : getSizeClasses ()) {
			if (sizeClass.getWheight () < 0d) { throw new Exception ("Wrong wheight : " + sizeClass.getWheight ()
					+ " in size class" + sizeClass); }
			temp.add (sizeClass.getWheight () * totalNumberOfTreesToPick);
		}
		temp.roundAndPreserveSumBD ();
		int j = 0;
		for (Double toPick : temp) {
			getSizeClass (j++).setNbOftreesToPick (toPick.intValue ());
		}
	}

	public void redispatch (boolean smallestClassFirst) throws Exception {
		// get class log
		Logger log = Logger.getLogger (this.getClass ().getName ());
		int noInfiniteLoop;
		// prepare double arrays
		int nbClasses = getSizeClasses ().size ();
		double[] capacity = new double[nbClasses];
		double[] toPick = new double[nbClasses];
		double[] delta = new double[nbClasses];
		double[] todispatch = new double[nbClasses];
		double[] among = new double[nbClasses];
		double[] rate = new double[nbClasses];
		double[] dispatched = new double[nbClasses];
		for (int i = smallestClassFirst ? 0 : (sizeClasses.size () - 1); smallestClassFirst ? (i < sizeClasses.size ())
				: (i >= 0); i += smallestClassFirst ? 1 : -1) {
			PickerSizeClass sizeClass = getSizeClass (i);
			capacity[i] = sizeClass.getNonPickedTrees ().size ();
			toPick[i] = sizeClass.getNbOfTreesToPick ();
		}
		boolean ok = false;
		noInfiniteLoop = 0;
		while (!ok) {
			noInfiniteLoop++;
			for (int i = smallestClassFirst ? 0 : (capacity.length - 1); smallestClassFirst ? (i < capacity.length)
					: (i >= 0); i += smallestClassFirst ? 1 : -1) {
				for (int j = smallestClassFirst ? 0 : (capacity.length - 1); smallestClassFirst ? (j < capacity.length)
						: (j >= 0); j += smallestClassFirst ? 1 : -1) {
					delta[j] = toPick[j] > 0 ? (capacity[j] - toPick[j]) : 0;
					todispatch[j] = delta[j] < 0 ? (-delta[j]) : 0;
					among[j] = delta[j] > 0 ? delta[j] : 0;
				}
				if (todispatch[i] > 0) {
					// log.info("Size class #" + (i + 1) + " requires " +
					// SimcopUtilities.zeroDigitPrecisionDouble.format(toPick[i]) +
					// " trees but only have " +
					// SimcopUtilities.zeroDigitPrecisionDouble.format(capacity[i]) +
					// ". Trying to dispatch " +
					// SimcopUtilities.zeroDigitPrecisionDouble.format(todispatch[i]) +
					// " trees in other classes...");
					toPick[i] -= todispatch[i];
					double total = 0d;
					for (int j = smallestClassFirst ? 0 : (capacity.length - 1); smallestClassFirst ? (j < capacity.length)
							: (j >= 0); j += smallestClassFirst ? 1 : -1) {
						total += among[j];
					}
					if (total == 0d) {
						String message = "Impossible to reallocate "
								+ Tools.zeroDigitPrecisionDouble.format (todispatch[i])
								+ " trees : no space left in other classes. Thinning cancelled !";
						Exception ex = new Exception (message);
						log.log (Level.SEVERE, message, ex);
						throw ex;
					}
					for (int j = smallestClassFirst ? 0 : (capacity.length - 1); smallestClassFirst ? (j < capacity.length)
							: (j >= 0); j += smallestClassFirst ? 1 : -1) {
						rate[j] = among[j] / total;
					}
					for (int j = smallestClassFirst ? 0 : (capacity.length - 1); smallestClassFirst ? (j < capacity.length)
							: (j >= 0); j += smallestClassFirst ? 1 : -1) {
						dispatched[j] = todispatch[i] * rate[j];
					}
					for (int j = smallestClassFirst ? 0 : (capacity.length - 1); smallestClassFirst ? (j < capacity.length)
							: (j >= 0); j += smallestClassFirst ? 1 : -1) {
						toPick[j] += dispatched[j];
					}
				}
			}
			ok = true;
			for (int i = smallestClassFirst ? 0 : (capacity.length - 1); smallestClassFirst ? (i < capacity.length)
					: (i >= 0); i += smallestClassFirst ? 1 : -1) {
				if (toPick[i] > capacity[i]) {
					ok = false;
					break;
				}
			}
			if (noInfiniteLoop > capacity.length * 10) {// this is arbitrary
				String message = "Max number of iterations reached when trying to redispatch trees to pick";
				Exception ex = new Exception (message);
				log.log (Level.SEVERE, message, ex);
				throw ex;
			}
		}

		// make sure dispatch is correctly rounded
		DoubleList redispatched = new DoubleList (toPick);

		redispatched.roundAndPreserveSumBD ();

		// reflect dispatch result and pick all tree in depleted class
		// log.info( "Trees to pick per class after redispatch :");
		for (int i = 0; i < toPick.length; i++) {
			PickerSizeClass sizeClass = getSizeClass (i);
			sizeClass.setNbOftreesToPick (redispatched.get (i).intValue ());
			// log.info("Size class #" + (i + 1) + " : " + sizeClass.getNbOfTreesToPick());
			if (sizeClass.getNbOfTreesToPick () > sizeClass.getNonPickedTrees ().size ()) {
				log.log (Level.SEVERE, "Size class #" + (i + 1) + " has still too many tree to be picked : "
						+ sizeClass.getNbOfTreesToPick () + " , only " + sizeClass.getNonPickedTrees ().size ()
						+ " trees left");
			} else if (sizeClass.getNbOfTreesToPick () == sizeClass.getNonPickedTrees ().size ()) {
				log.log (Level.INFO, "Size class #" + (i + 1)
						+ " : picking all tree in class without distance constraint");
				sizeClass.pickAll ();
			}
		}
	}

	public List<Integer> getNbOfTreesToPick () {
		ArrayList<Integer> result = new ArrayList<Integer> ();
		for (PickerSizeClass sizeClass : sizeClasses) {
			result.add (sizeClass.getNbOfTreesToPick ());
		}
		return result;
	}

	public String getPickedTreesString () {
		StringBuilder sb = new StringBuilder ("[");
		if (sizeClasses != null) {
			for (Iterator<PickerSizeClass> it = sizeClasses.iterator (); it.hasNext ();) {
				PickerSizeClass sizeClass = it.next ();
				sb.append (sizeClass.getPickedTrees ().size ());
				if (it.hasNext ()) {
					sb.append (", ");
				}
			}
		}
		sb.append ("]");
		return sb.toString ();
	}

	public String getNbOfTreesToPickString () {
		StringBuilder sb = new StringBuilder ("[");
		if (sizeClasses != null) {
			for (Iterator<PickerSizeClass> it = sizeClasses.iterator (); it.hasNext ();) {
				PickerSizeClass sizeClass = it.next ();
				sb.append (sizeClass.getNbOfTreesToPick ());
				if (it.hasNext ()) {
					sb.append (", ");
				}
			}
		}
		sb.append ("]");
		return sb.toString ();
	}

	public Object getNonPickedTreesString () {
		StringBuilder sb = new StringBuilder ("[");
		if (sizeClasses != null) {
			for (Iterator<PickerSizeClass> it = sizeClasses.iterator (); it.hasNext ();) {
				PickerSizeClass sizeClass = it.next ();
				sb.append (sizeClass.getNonPickedTrees ().size ());
				if (it.hasNext ()) {
					sb.append (", ");
				}
			}
		}
		sb.append ("]");
		return sb.toString ();
	}

	public static boolean isSumOne (Collection<Double> probas) {
		double sum = 0d;
		for (double proba : probas) {
			sum += proba;
		}
		if (sum > 0.99999d && sum < 1.000001d) { return true; }
		return false;
	}

	// pick an index in provided collection depending on probabilites. Sum is supposer to = 1
	public static int pickIndex (Collection<Double> pickProbabilties) throws InvalidProbabilitiesException,
			RandomNotFoundException {
		ArrayList<Double> sortedProbas = new ArrayList<Double> (pickProbabilties);
		Collections.sort (sortedProbas);
		double rand = Tools.random.nextDouble ();
		Double weightsSum = 0d;
		// check provided proba collections
		if (!PickerSizeClasses.isSumOne (sortedProbas)) { throw new InvalidProbabilitiesException ("Sum of probas != 1"); }
		// compare the random number to the cumulative proba
		Double foundProba = null;
		for (Double proba : sortedProbas) {
			weightsSum += proba;
			if (rand <= weightsSum) {
				foundProba = proba;
				break;
			}
		}
		if (foundProba == null) { throw new RandomNotFoundException ("Random not found in cumulative proba"); }
		// get the corresponding index
		int index = 0;
		for (Double proba : pickProbabilties) {
			if (proba == foundProba) {
				return index;
			} else {
				index++;
			}
		}
		return 0;
	}

}
