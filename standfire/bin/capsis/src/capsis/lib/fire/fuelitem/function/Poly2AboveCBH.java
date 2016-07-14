package capsis.lib.fire.fuelitem.function;

import java.util.StringTokenizer;

import capsis.lib.fire.fuelitem.FiPlant;

/**
 * A mass profile function, see FiSpecies.
 * 
 * @author F. Pimont, F. de Coligny - September 2013
 */
public class Poly2AboveCBH extends MassProfileFunction {

	private double a;
	private double b;
	private double c;

	/**
	 * Constructor, parses a String, e.g. Constant
	 */
	public Poly2AboveCBH (String str) throws Exception {
		try {
			String s = str.replace ("poly2AboveCBH(", "");
			s = s.replace (")", "");
			s = s.trim ();
			StringTokenizer st = new StringTokenizer (s, ";");
			a = Double.parseDouble (st.nextToken ().trim ());
			b = Double.parseDouble (st.nextToken ().trim ());
			c = Double.parseDouble (st.nextToken ().trim ());
		} catch (Exception e) {
			throw new Exception ("polyAboveCBH: could not parse this function: " + str, e);
		}
	}

	@Override
	public double f (double hrel, FiPlant plant) {
		double z = hrel * plant.getHeight ();
		if (z < plant.getCrownBaseHeight () || z > plant.getHeight ()) {
			return 0d;
		} else {
			double ph = (z-plant.getCrownBaseHeight ())/(plant.getHeight ()-plant.getCrownBaseHeight ());
			return (a * ph *ph + b * ph + c) / (1d - plant.getCrownBaseHeightBeforePruning () / plant.getHeight ());
		}
	}

	public String toString () {
		return "poly2AboveCBH()";
	}

}
