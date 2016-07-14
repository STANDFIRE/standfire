package capsis.lib.fire.fuelitem.function;

import java.util.StringTokenizer;

/**
 * Allometric relationship
 * 
 * @author F. Pimont - June 2015
 */
public class Hover1plusExpaplusbH extends Allom2Function {

	private double a;
	private double b;

	/**
	 * Constructor, parses a String, e.g. function1(0.996;2.403;13.086)
	 */
	public Hover1plusExpaplusbH (String str) throws Exception {
		try {
			String s = str.replace ("hover1plusExpaplusbH(", "");
			s = s.replace (")", "");
			s = s.trim ();
			StringTokenizer st = new StringTokenizer (s, ";");
			a = Double.parseDouble (st.nextToken ().trim ());
			b = Double.parseDouble (st.nextToken ().trim ());
		} catch (Exception e) {
			throw new Exception ("Hover1plusExpaplusbH: could not parse this function: " + str, e);
		}
	}

	@Override
	/**
	 * 
	 */
	public double f (double dbh, double h) {
		return h / (1.0 + Math.exp(a + b * h));
	}

	public String toString () {
		return "hover1plusExpaplusbH(" + a + ";" + b + ")";
	}

}
