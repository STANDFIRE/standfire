package capsis.lib.fire.fuelitem.function;

import java.io.Serializable;

import capsis.lib.fire.fuelitem.FiPlant;

/**
 * An interface for mass profile functions, see FiSpecies.
 * 
 * @author F. Pimont, F. de Coligny - September 2013
 */
public abstract class MassProfileFunction implements Serializable {

	/**
	 * Returns a biomass distribution value at any height in the given plant.
	 * 
	 * @param drel: relative distance : can be height to the ground (m) or radius to max radius
	 * @param plant
	 * @return a distribution value of biomass at hrel
	 */
	public abstract double f (double drel, FiPlant plant);

	/**
	 * Turns the given encoded string into a ready-to-use mass profile function.
	 */
	static public MassProfileFunction getFunction (String s) throws Exception {
		if (s.startsWith ("constantAboveCBH")) return new ConstantAboveCBH (s);
		if (s.startsWith ("constant")) return new Constant (s);
		if (s.startsWith ("aover1plusExpbminusc1minusHRELFromGround")) return new MPaover1plusExpbminusc1minusHRELFromGround (s);
		if (s.startsWith ("aover1plusExpbminuscHRELFromGround")) return new MPaover1plusExpbminuscHRELFromGround(s);
		if (s.startsWith ("poly2AboveCBH")) return new Poly2AboveCBH (s);
//		if (s.startsWith ("exp2p")) return new Exp2p (s);

		throw new Exception ("MassProfileFunction.getFunction (): could not get a function for: " + s);

	}

}
