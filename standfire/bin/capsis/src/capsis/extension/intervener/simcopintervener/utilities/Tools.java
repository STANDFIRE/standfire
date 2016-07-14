package capsis.extension.intervener.simcopintervener.utilities;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import capsis.extension.intervener.simcopintervener.picker.PickerTree;

/**
 * A tool class for the Simcop generic intervener.
 * 
 * @author F. de Coligny, M. Pulkkinen - June 2014
 */
public class Tools {
	
	public static DecimalFormat zeroDigitPrecisionDouble;
	public static DecimalFormat twoDigitPrecisionDouble;
	public static Random random;
    public static final String VALUES_SEPARATOR = ";,\t ";

	static {
		DecimalFormatSymbols pointDecimalSeperator = new DecimalFormatSymbols ();
		pointDecimalSeperator.setDecimalSeparator ('.');
		zeroDigitPrecisionDouble = new DecimalFormat ("#", pointDecimalSeperator);
		zeroDigitPrecisionDouble.setRoundingMode (RoundingMode.HALF_UP);
		twoDigitPrecisionDouble = new DecimalFormat ("#.##", pointDecimalSeperator);
		twoDigitPrecisionDouble.setRoundingMode (RoundingMode.HALF_UP);
		random = new Random ();
	}


	/**
	 * Distance between 2 trees.
	 */
	public static double getDistance (PickerTree aTree, PickerTree anotherTree) throws Exception {
		if (aTree == null || anotherTree == null) { throw new Exception ("Tools.getDistance (): was passed a null tree"); }
		return Math.sqrt (Math.pow (aTree.getX () - anotherTree.getX (), 2)
				+ Math.pow (aTree.getY () - anotherTree.getY (), 2));
	}

	/**
	 * Get minimum distance between a tree and a collection of trees. Do not take into account trees
	 * with the same id.
	 */
	public static TreeDistanceCouple getMinDist (PickerTree tree, Collection<PickerTree> trees) throws Exception {
		assert tree != null;
		double minDist = Double.MAX_VALUE;
		PickerTree minDistTree = null;
		double distance;
		for (PickerTree aTree : trees) {
			// Do not compare a tree with itself
			if (tree.getId () == aTree.getId ()) {
				continue;
			}
			distance = getDistance (tree, aTree);
			if (distance < minDist) {
				minDist = distance;
				minDistTree = aTree;
			}
		}
		if (minDistTree == null) {
			return null;
		} else {
			return new TreeDistanceCouple (minDistTree, minDist);
		}
	}

	/**
	 * Decodes a list of String containing numbers into a list of double.
	 */
    public static List<Double> decodeDoubleListString(String volumeClassesPonderationsString) throws NumberFormatException {
        ArrayList<Double> doubles = new ArrayList<Double>();
        StringTokenizer tokenizer = new StringTokenizer(volumeClassesPonderationsString, VALUES_SEPARATOR);
        while (tokenizer.hasMoreElements()) {
            doubles.add(Double.valueOf(tokenizer.nextToken()));
        }
        return doubles;
    }

}
