package capsis.lib.fire.fuelitem.function;

import java.util.StringTokenizer;

/**
 * Allometric relationship
 * 
 * @author F. Pimont - June 2015
 */
public class ADBHpowbpluscH extends Allom2Function {

	private double a;
	private double b;
	private double c;

	/**
	 * Constructor, parses a String, e.g. function1(0.996;2.403;13.086)
	 */
	public ADBHpowbpluscH (String str) throws Exception {
		try {
			String s = str.replace ("aDBHpowbplusc(", "");
			s = s.replace (")", "");
			s = s.trim ();
			StringTokenizer st = new StringTokenizer (s, ";");
			a = Double.parseDouble (st.nextToken ().trim ());
			b = Double.parseDouble (st.nextToken ().trim ());
			c = Double.parseDouble (st.nextToken ().trim ());
		} catch (Exception e) {
			throw new Exception ("ADBHpowbpluscH: could not parse this function: " + str, e);
		}
	}

	@Override
	/**
	 * 
	 */
	public double f (double dbh,double h) {
		return a * Math.pow (dbh, b)+c;
	}

	public String toString () {
		return "aDBHpowbplusc(" + a + ";" + b + ";" + c +")";
	}

}