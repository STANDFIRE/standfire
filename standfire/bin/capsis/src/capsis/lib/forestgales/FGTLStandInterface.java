package capsis.lib.forestgales;

import java.util.Collection;
import java.util.List;


/**
 * ForestGales tree-level mode: an interface for the scene in the compatible modules.
 *
 * @author B. Gardiner, K. Kamimura - August 2013
 */
public interface FGTLStandInterface {

	// Number of trees per hectare
	public int getNha ();

	// Dominant height, m
	public double getDominantHeight ();

	// A list of trees instanceof FGTree
	public Collection<FGTree> getFGTrees ();




	// Adds a ForestGales method, if run () has been called, contains results in their FGTree
	// instances.
	public void addFGMethod (FGMethod method);

	// Returns the FGMethods, if run () has been called, contains results in their FGTree
	// instances.
	public List<FGMethod> getFGMethods ();

}
