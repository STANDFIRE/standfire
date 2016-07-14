package capsis.extension.intervener.simcopintervener.picker.sizeclass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import capsis.extension.intervener.simcopintervener.picker.PickerTree;
import capsis.extension.intervener.simcopintervener.utilities.Tools;

/**
 * A size class, contains PickerTree instances.
 * 
 * @author thomas.bronner@gmail.com
 */
public class PickerSizeClass {

	static Random random = new Random ();
	// picked trees from this class
	private Collection<PickerTree> pickedTrees = new HashSet<PickerTree> ();
	// non picked trees in this size class
	private Collection<PickerTree> nonPickedTrees = new HashSet<PickerTree> ();
	// number of tree to pick in this class
	private int toPick = 0;
	// distance factor for tree picking
	private double distanceFactor;
	// wheight for tree picking
	private double wheight;

	public PickerSizeClass (List<PickerTree> trees, double factor, double distanceFactor) {
		this.distanceFactor = distanceFactor;
		this.nonPickedTrees = trees;
		this.wheight = factor;
	}

	public boolean nonPickedTreesContains (PickerTree tree) {
		return nonPickedTrees.contains (tree);
	}

	public boolean hasPickedTrees () {
		return !pickedTrees.isEmpty ();
	}

	public boolean hasNonPickedTrees () {
		return !nonPickedTrees.isEmpty ();
	}

	public void setTrees (Collection<PickerTree> trees) {
		this.nonPickedTrees = trees;
	}

	public double getDistanceFactor () {
		return distanceFactor;
	}

	public void setDistanceFactor (double distanceFactor) {
		this.distanceFactor = distanceFactor;
	}

	public Collection<PickerTree> getPickedTrees () {
		return pickedTrees;
	}

	public void setPickedTrees (Collection<PickerTree> pickedTrees) {
		this.pickedTrees = pickedTrees;
	}

	public void clearPickedTrees () {
		if (pickedTrees != null) {
			pickedTrees.clear ();
		}
	}

	public double getWheight () {
		return wheight;
	}

	public void setWheight (double factor) {
		this.wheight = factor;
	}

	// pick a tree at random in the list
	public PickerTree randomGetNonPickedTree (Collection exclusionList) {
		ArrayList<PickerTree> eligibleNonPickedTrees = new ArrayList<PickerTree> (nonPickedTrees);
		if (exclusionList != null) {
			eligibleNonPickedTrees.removeAll (exclusionList);
		}
		if (eligibleNonPickedTrees.isEmpty ()) { return null; }
		PickerTree nonPickedTree = null;
		int chosenPosition = random.nextInt (eligibleNonPickedTrees.size ());
		int i = 0;
		for (PickerTree t : eligibleNonPickedTrees) {
			if (i++ == chosenPosition) {
				nonPickedTree = t;
				break;
			}
		}
		return nonPickedTree;
	}

	public void pickTree (PickerTree pickedTree) {
		pickedTrees.add (pickedTree);
		nonPickedTrees.remove (pickedTree);
		toPick--;
		if (toPick < 0) {
			toPick = 0;
		}
	}

	public void unpickTree (PickerTree unpickedTree) {
		pickedTrees.remove (unpickedTree);
		nonPickedTrees.add (unpickedTree);
		toPick++;
	}

	public Collection<PickerTree> getNonPickedTrees () {
		return nonPickedTrees;
	}

	public Collection<PickerTree> getAllTrees () {
		HashSet<PickerTree> result = new HashSet<PickerTree> (nonPickedTrees);
		result.addAll (pickedTrees);
		return result;
	}

	@Override
	public String toString () {
		return "SC(df=" + Tools.twoDigitPrecisionDouble.format (distanceFactor) + ",w="
				+ Tools.twoDigitPrecisionDouble.format (wheight) + ",tp=" + toPick + ",npt="
				+ nonPickedTrees.size () + ",pt=" + pickedTrees.size () + ")";
	}

	public void pickAll () {
		pickedTrees.addAll (nonPickedTrees);
		nonPickedTrees.clear ();
		toPick -= pickedTrees.size ();
		if (toPick < 0) {
			toPick = 0;
		}
	}

	public int getNbOfTreesToPick () {
		return toPick;
	}

	public void setNbOftreesToPick (int toPick) {
		this.toPick = toPick;
	}

	public boolean isDepleted () {
		return nonPickedTrees.isEmpty ();
	}

}
