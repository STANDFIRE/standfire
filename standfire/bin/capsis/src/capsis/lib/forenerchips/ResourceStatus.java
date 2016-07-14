package capsis.lib.forenerchips;

/**
 * The list of all possible status in Forenerchips
 * 
 * @author N. Bilot - February 2013
 */
public class ResourceStatus {

	public final static String STANDING_TREE = "STANDING_TREE"; // arbre sur pied
	public final static String FALLEN_TREE = "FALLEN_TREE"; // arbre abattu
	public final static String LOG = "LOG"; // billon
	public final static String RESIDUAL = "RESIDUAL"; // menus bois
	public final static String BUNDLE = "BUNDLE"; // fagot
	public final static String BRANCH = "BRANCH"; // branches, foisonnant
	public final static String CHIP = "CHIP"; // plaquettes

	/**
	 * Helper method. Used when reading scenario from .txt files.
	 */
	public static String getStatus (String name) throws Exception {
		if (name.equals ("STANDING_TREE"))
			return ResourceStatus.STANDING_TREE;
		else if (name.equals ("FALLEN_TREE"))
			return ResourceStatus.FALLEN_TREE;
		else if (name.equals ("LOG"))
			return ResourceStatus.LOG;
		else if (name.equals ("RESIDUAL"))
			return ResourceStatus.RESIDUAL;
		else if (name.equals ("BUNDLE"))
			return ResourceStatus.BUNDLE;
		else if (name.equals ("BRANCH"))
			return ResourceStatus.BRANCH;
		else if (name.equals ("CHIP"))
			return ResourceStatus.CHIP;
		else 
			throw new Exception ("ResourceStatus: could not find a status for name: "+name);
	}
}
