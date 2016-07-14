package capsis.lib.fire.fuelitem.function;

import java.util.StringTokenizer;

/**
 * Allometric relationship
 * 
 * @author F. Pimont - June 2015
 */
public class AplusExpbpluscDBHpowd extends AllomFunction {

	private double a;
	private double b;
	private double c;
	private double d;

	/**
	 * Constructor, parses a String, e.g. function1(0.996;2.403;13.086)
	 */
	public AplusExpbpluscDBHpowd (String str) throws Exception {
		try {
			String s = str.replace ("aplusExpbpluscDBHpowd(", "");
			s = s.replace (")", "");
			s = s.trim ();
			StringTokenizer st = new StringTokenizer (s, ";");
			a = Double.parseDouble (st.nextToken ().trim ());
			b = Double.parseDouble (st.nextToken ().trim ());
			c = Double.parseDouble (st.nextToken ().trim ());
			d = Double.parseDouble (st.nextToken ().trim ());
		} catch (Exception e) {
			throw new Exception ("AplusExpbpluscDBHpowd: could not parse this function: " + str, e);
		}
	}

	@Override
	/**
	 * 
	 */
	public double f (double dbh) {
		return a + Math.exp(b + c * Math.pow(dbh, d));
	}

	public String toString () {
		return "aplusExpbpluscDBHpowd(" + a + ";" + b + ";" + c + ";" + d + ")";
	}

}
