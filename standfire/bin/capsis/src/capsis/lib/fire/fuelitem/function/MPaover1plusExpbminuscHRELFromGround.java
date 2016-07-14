package capsis.lib.fire.fuelitem.function;

import java.util.StringTokenizer;

import capsis.lib.fire.fuelitem.FiPlant;

/**
 * A mass profile function, see FiSpecies.
 * 
 * @author F. Pimont, F. de Coligny - September 2013
 */
public class MPaover1plusExpbminuscHRELFromGround extends MassProfileFunction {

	private double a;
	private double b;
	private double c;

	/**
	 * Constructor, parses a String, e.g. 
	 */
	public MPaover1plusExpbminuscHRELFromGround (String str) throws Exception {
		try {
			String s = str.replace ("aover1plusExpbminuscHRELFromGround(", "");
			s = s.replace (")", "");
			s = s.trim ();
			StringTokenizer st = new StringTokenizer (s, ";");
			a = Double.parseDouble (st.nextToken ().trim ());
			b = Double.parseDouble (st.nextToken ().trim ());
			c = Double.parseDouble (st.nextToken ().trim ());
		} catch (Exception e) {
			throw new Exception ("MPaover1plusExpbminuscHRELFromGround: could not parse this function: " + str, e);
		}
	}

	@Override
	/**
	 * From cumulative distribuion : a/(1+exp((b-c(1-hrel)))
	 */
	public double f (double hrel, FiPlant plant) {
		return a * c * Math.exp (b - c * hrel) / Math.pow (1 + Math.exp (b - c * hrel), 2d);
	}

	public String toString () {
		return "aover1plusExpbminuscHREL(" + a + ";" + b + ";" + c + ")";
	}

}
