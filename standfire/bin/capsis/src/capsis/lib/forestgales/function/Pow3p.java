package capsis.lib.forestgales.function;

import java.util.StringTokenizer;


/**
 * A power function with 3 parameters.
 * 
 * @author B. Gardiner, K. Kamimura - August 2013
 */
public class Pow3p extends Function {

	private double a;
	private double b;
	private double c;


	/**
	 * Constructor, parses a String, e.g. pow3p(0.125;0.683;0.5)
	 */
	public Pow3p (String str) throws Exception {
		try {
			String s = str.replace ("pow3p(", "");
			s = s.replace (")", "");
			s = s.trim ();
			StringTokenizer st = new StringTokenizer (s, ";");
			a = Double.parseDouble (st.nextToken ().trim ());
			b = Double.parseDouble (st.nextToken ().trim ());
			c = Double.parseDouble (st.nextToken ().trim ());
		} catch (Exception e) {
			throw new Exception ("pow3p: could not parse this function: " + str, e);
		}
	}

	public double f (double x) {
		return a * Math.pow (x, b) + c;
	}

	public String toString () {
		return "pow3p(" + a + ";" + b + ";" + c + ")";
	}

	
}
