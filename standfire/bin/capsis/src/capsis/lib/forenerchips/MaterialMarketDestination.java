package capsis.lib.forenerchips;


/**
 * Possible market for the processed material.
 * 
 * @author N. Bilot - February 2013
 */
public class MaterialMarketDestination {
	
	public static final String ND = "ND";
	public static final String LUMBER = "LUMBER";
	public static final String INDUSTRY = "INDUSTRY";
	public static final String ENERGY = "ENERGY";
	
  /**
	 * Helper method. Used when reading scenario from .txt files.
	 */
	public static String getMarketDestination (String name) throws Exception {
		if (name.equals ("ND"))
			return MaterialMarketDestination.ND;
		else if (name.equals ("LUMBER"))
			return MaterialMarketDestination.LUMBER;
		else if (name.equals ("INDUSTRY"))
			return MaterialMarketDestination.INDUSTRY;
		else if (name.equals ("ENERGY"))
			return MaterialMarketDestination.ENERGY;
		else 
			throw new Exception ("MaterialMarketDestination: could not find a market for name: "+name);
	}
}
