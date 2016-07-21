package capsis.lib.forestgales;

import java.io.Serializable;

/**
 * The soil classification.
 * 
 * @author B. Gardiner, K. Kamimura - August 2013
 */
public class FGSoilType implements Serializable {
	
	public static final FGSoilType SOIL_TYPE_A = new FGSoilType (0, "A - Free-draining mineral soils");
	public static final FGSoilType SOIL_TYPE_B = new FGSoilType (1, "B - Gleyed mineral soils");
	public static final FGSoilType SOIL_TYPE_C = new FGSoilType (2, "C - Peaty mineral soils");
	public static final FGSoilType SOIL_TYPE_D = new FGSoilType (3, "D - Deep peats");

	private int id; 
	private String name; 
	
	/**
	 * Constructor.
	 */
	public FGSoilType (int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	
	public int getId () {
		return id;
	}

	
	public String getName () {
		return name;
	}

	public String toString () {
		return name;
	}
	
}

