package capsis.lib.fire.fuelitem.function;

import java.io.Serializable;

/**
 * An interface for mass functions, for allometric relationships.
 * 
 * @author F. Pimon - June 2015
 */
public abstract class Allom2Function implements Serializable {

	/**
	 * Returns a dimension, biomass, etc. value for a given plant.
	 * 
	 */
	public abstract double f (double dbh,double h);

	/**
	 * Turns the given encoded string into a ready-to-use mass profile function.
	 */
	static public Allom2Function getFunction (String s) throws Exception {
		
		if (s.startsWith ("aHThresh")) return new AHThresh (s);
		if (s.startsWith ("aH")) return new AH (s);
		if (s.startsWith ("aDBHpowbcDBHpowdHThreshSum")) return new ADBHpowbHThreshSum(s);
		if (s.startsWith ("aDBHpowbcDBHpowdHThresh")) return new ADBHpowbHThresh(s);
		if (s.startsWith ("aDBHpowbplusc")) return new ADBHpowbpluscH(s);
		if (s.startsWith ("aDBHpowb")) return new ADBHpowbH (s);
		if (s.startsWith ("aDBHplusb")) return new ADBHplusbH (s);
		if (s.startsWith ("hover1plusExpaplusbH")) return new Hover1plusExpaplusbH(s);
		throw new Exception ("AllomFunction.getFunction (): could not get a function for: " + s);		
	}

}
