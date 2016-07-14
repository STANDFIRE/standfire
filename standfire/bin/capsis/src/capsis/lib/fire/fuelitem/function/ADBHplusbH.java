package capsis.lib.fire.fuelitem.function;

import java.util.StringTokenizer;

/**
 * Allometric relationship
 * 
 * @author F. Pimont - June 2015
 */
public class ADBHplusbH extends Allom2Function {

	private double a;
	private double b;

	/**
	 * Constructor, parses a String, e.g. function1(0.996;2.403;13.086)
	 */
	public ADBHplusbH (String str) throws Exception {
		try {
			String s = str.replace ("aDBHplusb(", "");
			s = s.replace (")", "");
			s = s.trim ();
			StringTokenizer st = new StringTokenizer (s, ";");
			a = Double.parseDouble (st.nextToken ().trim ());
			b = Double.parseDouble (st.nextToken ().trim ());
		} catch (Exception e) {
			throw new Exception ("ADBHplusbH: could not parse this function: " + str, e);
		}
	}

	@Override
	/**
	 * 
	 */
	public double f (double dbh, double h) {
		return a * dbh + b;
	}

	public String toString () {
		return "aDBHplusb(" + a + ";" + b + ")";
	}

}
