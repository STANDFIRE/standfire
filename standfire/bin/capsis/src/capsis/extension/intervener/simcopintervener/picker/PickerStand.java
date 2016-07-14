package capsis.extension.intervener.simcopintervener.picker;

import java.util.Collection;

/**
 * An interface for stands compatible with the SimcopSizeClassSpatialIntervener. 
 * 
 * @author F. de Coligny, M. Pulkkinen - June 2014
 */
public interface PickerStand {

	/**
	 * The intervener will use this method on the stand resulting of the intervention.
	 */
	public void setInterventionResult (boolean v);

	/**
	 * Returns true if the model is spatialized, i.e. if trees have x and y coordinates.
	 */
	public boolean isSpatialized ();

	/**
	 * Returns the dominant height of the stand (m.)
	 */
	public double getHdom (Collection treeList);

	/**
	 * Returns the trees in this scene.
	 */
	public Collection<PickerTree> getPickerTrees ();
	
	/**
	 * Returns the area of the stand (m2).
	 */
	public double getArea ();
	
	/**
	 * Returns a unique caption for this stand, e.g. *15a
	 */
	public String getCaption ();
	
	/**
	 * Removes a tree from the stand.
	 */
	public void removeTree (PickerTree t);
	
}
