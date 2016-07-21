package capsis.lib.fire.fuelitem;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This simple subclass contain the 3 properties of a fuel particle (mvr, svr,
 * moisture)
 * 
 * @author pimont
 * 
 */
public class FiParticle implements Serializable {

	public String name; // TYPE_STATUS
	public double mvr;
	public double svr;
	public double moisture;
	public String speciesName;

	// particle status
	public static final String DEAD = "Dead";
	public static final String LIVE = "Live";
	// public static final String UNKNOWN = "Unknown";

	// particle type
	public static final String TWIG3 = "Twig3";
	public static final String TWIG2 = "Twig2";
	public static final String TWIG1 = "Twig1";
	public static final String LEAVE = "Leave";
	public static final String LITTER = "Litter";
	// public static final String MIX = "Mix";

	// particle property keyword
	public static final String SVR = "SVR"; // surface to volume ratio
	public static final String MVR = "MVR"; // mass to volume ratio
	public static final String MOISTURE = "Moisture"; // moisture

	//
	// public static final String SURFACE_FUEL = "SurfaceFuel";

	// public static final String LIVE_AND_DEAD = "Live and dead";
	// keyword when live and dead are wanted for a method

	// this set contains all types
	public static final Set<String> TYPES;
	static {
		// Set<String> s = new HashSet<String> (Arrays.asList (LEAVE, TWIG1,
		// TWIG2, TWIG3, MIX));
		Set<String> s = new HashSet<String>(Arrays.asList(LEAVE, TWIG1, TWIG2, TWIG3));
		TYPES = Collections.unmodifiableSet(s);
	}
	// this set contains all status
	public static final Set<String> STATUS;
	static {
		// Set<String> s = new HashSet<String> (Arrays.asList (LIVE, DEAD,
		// UNKNOWN));
		Set<String> s = new HashSet<String>(Arrays.asList(LIVE, DEAD));
		STATUS = Collections.unmodifiableSet(s);
	}
	// this set ALL contains the keyword "All" to get access to properties of
	// all particles
	public static final Set<String> ALL;
	static {
		Set<String> s = new HashSet<String>();
		for (String a : TYPES) {
			for (String b : STATUS) {
				try {
					s.add(FiParticle.makeKey(a, b));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		// s.add (SURFACE_FUEL);
		s.add(LITTER);
		// s.add (MIX);
		ALL = Collections.unmodifiableSet(s);
	}

	/**
	 * constructor
	 * 
	 * @param name
	 * @param mvr
	 * @param svr
	 * @param moisture
	 * @throws Exception
	 */
	public FiParticle(String name, double mvr, double svr, double moisture, String speciesName) throws Exception {
		if (!checkName(name)) {
			throw new Exception("FiParticle has invalid name " + name);
		}
		this.name = name;
		this.speciesName = speciesName;
		this.mvr = mvr;
		this.svr = svr;
		this.moisture = moisture;
	}

	/**
	 * constructor 2
	 * 
	 * @param type
	 * @param status
	 * @param mvr
	 * @param svr
	 * @param moisture
	 * @throws Exception
	 */
	public FiParticle(String type, String status, double mvr, double svr, double moisture, String speciesName)
			throws Exception {
		this.name = FiParticle.makeKeyCheck(type, status);
		this.speciesName = speciesName;
		this.mvr = mvr;
		this.svr = svr;
		this.moisture = moisture;
	}

	/**
	 * check if the name is SURFACE_FUEL or TYPE_STATUS
	 * 
	 * @param name
	 * @return
	 */
	static public boolean checkName(String name) {
		return ALL.contains(name);
		// if (name.equals (SURFACE_FUEL)) return true;
		// int index = name.indexOf ("_");
		// //System.out.println(" index="+index);
		// if (index<0) return false;
		// String tp = name.substring (0, index);
		// String st = name.substring (index+1);
		// //System.out.println(tp+" "+st);
		// return (TYPES.contains (tp) && STATUS.contains (st));
	}

	/**
	 * make a key
	 * 
	 * @param a
	 * @param b
	 * @return
	 * 
	 */
	static public String makeKey(String a, String b) {
		return a + "_" + b;
	}

	static public String getType(String name) throws Exception {
		if (!checkName(name)) {
			throw new Exception("FiParticle.getType has a wrong name: " + name);
		}
		return name.substring(0, name.indexOf("_"));
	}

	static public String getStatus(String name) throws Exception {
		if (!checkName(name)) {
			throw new Exception("FiParticle.getType has a wrong name: " + name);
		}
		return name.substring(name.indexOf("_") + 1);
	}

	/**
	 * make a key and check validity
	 * 
	 * @param a
	 * @param b
	 * @return
	 * @throws Exception
	 */
	static public String makeKeyCheck(String a, String b) throws Exception {
		if (TYPES.contains(a) && STATUS.contains(b)) {
			return makeKey(a, b);
		} else {
			throw new Exception("FiParticle.makeKey has invalid type or status: " + a + "," + b);
		}
	}

	/**
	 * copy
	 * 
	 * @return
	 * @throws Exception
	 */
	public FiParticle copy() throws Exception {
		return new FiParticle(this.name, this.mvr, this.svr, this.moisture, this.speciesName);
	}

	/**
	 * set the value of the property (MVR, SVR, Moisture)
	 * 
	 * @param property
	 * @param value
	 * @throws Exception
	 */
	public void setValue(String property, double value) throws Exception {
		if (property.equals(MVR)) {
			mvr = value;
		} else if (property.equals(SVR)) {
			svr = value;
		} else if (property.equals(MOISTURE)) {
			moisture = value;
		} else {
			throw new Exception("FiParticle.setValue: unkown property " + property);
		}
	}

	/**
	 * Used in the fuel manager for importation of particle as an array
	 * 
	 * @param i
	 * @return
	 */
	static public String getTypeName(int i) {
		if (i == 0) {
			return LEAVE;
		} else if (i == 1) {
			return TWIG1;
		} else if (i == 2) {
			return TWIG2;
		} else {
			return TWIG3;
		}
	}

	/**
	 * getFullName return a string "species_name"
	 */
	public String getFullName() {
		return this.makeKey(this.speciesName, this.name);
	}

}
