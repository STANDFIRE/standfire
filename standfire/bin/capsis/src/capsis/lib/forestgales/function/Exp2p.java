package capsis.lib.forestgales.function;

import java.util.StringTokenizer;


/**
 * An exponential function with 2 parameters.
 * 
 * @author B. Gardiner, K. Kamimura - August 2013
 */
public class Exp2p extends Function {

	private double a;
	private double b;


	/**
	 * Constructor, parses a String, e.g. exp2p(0.125;0.683)
	 */
	public Exp2p (String str) throws Exception {
		try {
			String s = str.replace ("exp2p(", "");
			s = s.replace (")", "");
			s = s.trim ();
			StringTokenizer st = new StringTokenizer (s, ";");
			a = Double.parseDouble (st.nextToken ().trim ());
			b = Double.parseDouble (st.nextToken ().trim ());
		} catch (Exception e) {
			throw new Exception ("exp2p: could not parse this function: " + str, e);
		}
	}

	public double f (double x) {
		return a * Math.exp (b * x);
	}

	public String toString () {
		return "exp2p(" + a + ";" + b + ")";
	}

	

}
