package capsis.lib.fire.fuelitem.function;

import capsis.lib.fire.fuelitem.FiPlant;

/**
 * A mass profile function, see FiSpecies.
 * 
 * @author F. Pimont, F. de Coligny - September 2013
 */
public class ConstantAboveCBH extends MassProfileFunction {

	
	/**
	 * Constructor, parses a String, e.g. Constant
	 */
	public ConstantAboveCBH (String str) throws Exception {
		try {
//			String s = str.replace ("function1(", "");
//			s = s.replace (")", "");
//			s = s.trim ();
//			StringTokenizer st = new StringTokenizer (s, ";");
//			a = Double.parseDouble (st.nextToken ().trim ());
//			b = Double.parseDouble (st.nextToken ().trim ());
//			c = Double.parseDouble (st.nextToken ().trim ());
		} catch (Exception e) {
			throw new Exception ("ConstantAboveCBH: could not parse this function: " + str, e);
		}
	}

	@Override
	public double f (double hrel, FiPlant plant) {
		double z = hrel * plant.getHeight ();
		if (z < plant.getCrownBaseHeight () || z > plant.getHeight ()) {
			return 0d;
		} else {
			return 1d / (1d - plant.getCrownBaseHeightBeforePruning () / plant.getHeight ());
		}
	}

	public String toString () {
		return "constantAboveCBH()";
	}

}
