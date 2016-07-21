package capsis.lib.fire.fuelitem.function;

import java.io.Serializable;

/**
 * An interface for mass functions, for allometric relationships.
 * 
 * @author F. Pimon - June 2015
 */
public abstract class AllomFunction implements Serializable {

	/**
	 * Returns a dimension, biomass, etc. value for a given plant.
	 * 
	 */
	public abstract double f (double dbh);

	/**
	 * Turns the given encoded string into a ready-to-use mass profile function.
	 */
	static public AllomFunction getFunction (String s) throws Exception {
		if (s.startsWith ("aDBHpowb")) return new ADBHpowb (s);
		if (s.startsWith ("aDBHplusb")) return new ADBHplusb (s);
		if (s.startsWith ("minaDBHpowbcDBHplusd")) return new MinaDBHpowbcDBHplusd (s);
		if (s.startsWith ("aplusExpbpluscDBHpowd")) return new AplusExpbpluscDBHpowd (s);
		if (s.startsWith ("atimes1minusExpbDBHplusc")) return new Atimes1minusExpbDBHplusc (s);
		throw new Exception ("AllomFunction.getFunction (): could not get a function for: " + s);

	}

}
