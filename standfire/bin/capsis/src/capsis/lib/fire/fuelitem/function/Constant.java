package capsis.lib.fire.fuelitem.function;

import capsis.lib.fire.fuelitem.FiPlant;

/**
 * A mass profile function, see FiSpecies.
 * 
 * @author F. Pimont, F. de Coligny - September 2013
 */
public class Constant extends MassProfileFunction {

	
	/**
	 * Constructor, parses a String, e.g. Constant
	 */
	public Constant (String str) throws Exception {
		try {
//			String s = str.replace ("function1(", "");
//			s = s.replace (")", "");
//			s = s.trim ();
//			StringTokenizer st = new StringTokenizer (s, ";");
//			a = Double.parseDouble (st.nextToken ().trim ());
//			b = Double.parseDouble (st.nextToken ().trim ());
//			c = Double.parseDouble (st.nextToken ().trim ());
		} catch (Exception e) {
			throw new Exception ("function1: could not parse this function: " + str, e);
		}
	}

	@Override
	public double f (double hrel, FiPlant plant) {
		return 1d;
	}

	public String toString () {
		return "constant()";
	}

}
