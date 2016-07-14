package capsis.lib.fire.fuelitem.function;

import java.util.StringTokenizer;

/**
 * Allometric relationship
 * 
 * @author F. Pimont - June 2015
 */
public class ADBHpowb extends AllomFunction {

	private double a;
	private double b;

	/**
	 * Constructor, parses a String, e.g. function1(0.996;2.403;13.086)
	 */
	public ADBHpowb (String str) throws Exception {
		try {
			String s = str.replace ("aDBHpowb(", "");
			s = s.replace (")", "");
			s = s.trim ();
			StringTokenizer st = new StringTokenizer (s, ";");
			a = Double.parseDouble (st.nextToken ().trim ());
			b = Double.parseDouble (st.nextToken ().trim ());
		} catch (Exception e) {
			throw new Exception ("ADBHpowb: could not parse this function: " + str, e);
		}
	}

	@Override
	/**
	 * 
	 */
	public double f (double dbh) {
		return a * Math.pow (dbh, b);
	}

	public String toString () {
		return "aDBHpowb(" + a + ";" + b + ")";
	}

}
