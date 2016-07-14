package capsis.extension.intervener.simcopintervener.picker;

/**
 * An interface for trees in stands compatible with the SimcopSizeClassSpatialIntervener. Important
 * note: PickerTrees must extend capsis.defaulttype.Tree.
 * 
 * @author F. de Coligny, M. Pulkkinen - June 2014
 */
public interface PickerTree {

	/**
	 * A unique id for the tree in the PickerStand.
	 */
	public int getId ();

	/**
	 * Returns the age of the tree (years).
	 */
	public int getAge ();

	/**
	 * Returns the dbh of the tree (cm).
	 */
	public double getDbh ();

	/**
	 * Returns the volume of the tree (m3).
	 */
	public double getVolume ();

	/**
	 * Stem position X (m). If the trees are not spatialized, unused (e.g. return -1).
	 */
	public double getX ();

	/**
	 * Stem position Y (m). If the trees are not spatialized, unused (e.g. return -1).
	 */
	public double getY ();

	/**
	 * Returns true if the tree is not allowed to be thinned.
	 */
	public boolean isMarked ();

	/**
	 * Changes the mark property of the tree (marked generally means dead or cut).
	 */
	public void setMarked (boolean v);

}
