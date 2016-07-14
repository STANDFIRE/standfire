package capsis.lib.forestgales;

import java.util.Collection;
import java.util.List;

/**
 * ForestGales stand-level mode: an interface for the scene in the compatible modules.
 *
 * @author B. Gardiner, K. Kamimura, C. Meredieu, T. Labbe  - August 2013 update May 2014
 */
public interface FGSLStandInterface {

	// Number of trees per hectare
	public int getNha ();

	// Dominant height, m
	public double getDominantHeight ();

	// Stand mean dbh, m
	public double getMeanDbh ();

	// Stand mean height, m
	public double getMeanHeight ();

	// Stand mean crown width, m, optional (-1)
	public double getMeanCrownWidth ();

	// Stand mean crown depth, m, optional (-1)
	public double getMeanCrownDepth ();

	// Stand mean stem volume, m3, optional (-1)
	public double getMeanStemVolume ();

	// Stand mean stem weight, kg, optional (-1)
	public double getMeanStemWeight ();

	// Stand mean crown volume, m3, optional (-1)
	public double getMeanCrownVolume ();

	// Stand mean crown weight, kg, optional (-1)
	public double getMeanCrownWeight ();

	// Returns a ForestGales species name
	public String getFGSpeciesName ();


	// Adds a ForestGales method, if run () has been called, contains results in their FGTree
	// instances.
	public void addFGMethod (FGMethod method);

	// Returns the FGMethods, if run () has been called, contains results in their FGTree
	// instances.
	public List<FGMethod> getFGMethods ();

}
