package capsis.extension.intervener.simcopintervener.utilities;

import capsis.extension.intervener.simcopintervener.picker.PickerTree;

/**
 * Utility class to get a tree and a distance
 * 
 * @author thomas.bronner@gmail.com
 */
public class TreeDistanceCouple {

	private PickerTree tree;
	private double distance;

	public TreeDistanceCouple (PickerTree tree, double distance) {
		this.tree = tree;
		this.distance = distance;
	}

	public PickerTree getTree () {
		return tree;
	}

	public void setTree (PickerTree tree) {
		this.tree = tree;
	}

	public double getDistance () {
		return distance;
	}

	public void setDistance (double distance) {
		this.distance = distance;
	}
}
