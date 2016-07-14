package capsis.lib.fire.fuelitem.function;

import java.util.StringTokenizer;

/**
 * Allometric relationship
 * 
 * @author F. Pimont - June 2015
 */
public class Atimes1minusExpbDBHplusc extends AllomFunction {

	private double a;
	private double b;
	private double c;

	/**
	 * Constructor, parses a String, e.g. function1(0.996;2.403;13.086)
	 */
	public Atimes1minusExpbDBHplusc (String str) throws Exception {
		try {
			String s = str.replace ("atimes1minusExpbDBHplusc(", "");
			s = s.replace (")", "");
			s = s.trim ();
			StringTokenizer st = new StringTokenizer (s, ";");
			a = Double.parseDouble (st.nextToken ().trim ());
			b = Double.parseDouble (st.nextToken ().trim ());
			c = Double.parseDouble (st.nextToken ().trim ());
		} catch (Exception e) {
			throw new Exception ("atimes1minusExpbDBHplusc: could not parse this function: " + str, e);
		}
	}

	@Override
	/**
	 * 
	 */
	public double f (double dbh) {
		return 	a * (1.0 - Math.exp(b * dbh)) + c;
	}

	public String toString () {
		return "atimes1minusExpbDBHplusc(" + a + ";" + b + ";" + c + ")";
	}

}
