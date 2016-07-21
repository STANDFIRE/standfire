package capsis.lib.fire.fuelitem.function;

import java.util.StringTokenizer;

/**
 * Allometric relationship
 * 
 * @author F. Pimont - June 2015
 */
public class AH extends Allom2Function {

	private double a;
	
	/**
	 * Constructor, parses a String, e.g. function1(0.996;2.403;13.086)
	 */
	public AH (String str) throws Exception {
		try {
			String s = str.replace ("aH(", "");
			s = s.replace (")", "");
			s = s.trim ();
			StringTokenizer st = new StringTokenizer (s, ";");
			a = Double.parseDouble (st.nextToken ().trim ());
		} catch (Exception e) {
			throw new Exception ("AH: could not parse this function: " + str, e);
		}
	}

	@Override
	/**
	 * 
	 */
	public double f(double dbh, double h) {
			return  h * a;
	}

	public String toString () {
		return "aH(" + a + ")";
	}


}
