package capsis.lib.fire.fuelitem.function;

import java.util.StringTokenizer;

import capsis.lib.fire.fuelitem.FiPlant;

/**
 * A mass profile function, see FiSpecies.
 * 
 * @author F. Pimont, F. de Coligny - September 2013
 */
public class MPaover1plusExpbminusc1minusHRELFromGround extends MassProfileFunction {

	private double a;
	private double b;
	private double c;

	/**
	 * Constructor, parses a String, e.g. function1(0.996;2.403;13.086)
	 */
	public MPaover1plusExpbminusc1minusHRELFromGround (String str) throws Exception {
		try {
			String s = str.replace ("aover1plusExpbminusc1minusHRELFromGround(", "");
			s = s.replace (")", "");
			s = s.trim ();
			StringTokenizer st = new StringTokenizer (s, ";");
			a = Double.parseDouble (st.nextToken ().trim ());
			b = Double.parseDouble (st.nextToken ().trim ());
			c = Double.parseDouble (st.nextToken ().trim ());
		} catch (Exception e) {
			throw new Exception ("MPaover1plusExpbminusc1minusHRELFromGround: could not parse this function: " + str, e);
		}
	}

	@Override
	/**
	 * From cumulative distribuion of alexander et al 2004 a/(1+exp((b-c(1-hrel)))
	 * NB: Alexander relationships start from apex !!!!!!!!!
	 */
	public double f (double hrel, FiPlant plant) {
		return a * c * Math.exp (b - c * (1-hrel)) / Math.pow (1 + Math.exp (b - c * (1-hrel)), 2d);
	}

	public String toString () {
		return "aover1plusExpbminusc1minusHRELFromGround(" + a + ";" + b + ";" + c + ")";
	}

}
