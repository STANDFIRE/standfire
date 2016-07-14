package capsis.lib.forestgales;


/**
 * The spatialized tree description for ForestGales.
 *
 * @author B. Gardiner, K. Kamimura - August 2013
 */
public class FGSpatializedTree extends FGTree {

	private double x;
	private double y;
	private double z;


	/**
	 * Constructor.
	 */
	public FGSpatializedTree (double dbh, double height, double crownWidth, double crownDepth, double stemVolume,
			double stemWeight, double crownVolume, double crownWeight, double [] diam, double [] h, double [] mass, String speciesName, double x, double y, double z) {
		super (dbh, height, crownWidth, crownDepth, stemVolume, stemWeight, crownVolume, crownWeight,diam, h, mass, speciesName);
		this.x = x;
		this.y = y;
		this.z = z;
	}


	public double getX () {
		return x;
	}


	public double getY () {
		return y;
	}


	public double getZ () {
		return z;
	}



}
