package capsis.lib.forestgales;

import java.io.Serializable;


/**
 * The rooting depth classification.
 * 
 * @author B. Gardiner, K. Kamimura - August 2013
 */
public class FGRootingDepth implements Serializable {
	
	public static FGRootingDepth SHALLOW = new FGRootingDepth (0, "Shallow < 40cm");
	public static FGRootingDepth MEDIUM = new FGRootingDepth (1, "Medium 40-80cm");
	public static FGRootingDepth DEEP = new FGRootingDepth (2, "Deep > 80cm");
	
	private int id;
	private String name; 
	
	/**
	 * Constructor.
	 */
	public FGRootingDepth (int id, String name) {
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
