package capsis.lib.forestgales.function;

import java.util.StringTokenizer;


/**
 * A linear function with 2 parameters.
 *
 * @author B. Gardiner, K. Kamimura - August 2013
 */
public class Linear2p extends Function {

	private double a;
	private double b;


	/**
	 * Constructor, parses a String, e.g. linear2p(0.125;0.683)
	 */
	public Linear2p (String str) throws Exception {
		try {
			String s = str.replace ("linear2p(", "");
			s = s.replace (")", "");
			s = s.trim ();
			StringTokenizer st = new StringTokenizer (s, ";");
			a = Double.parseDouble (st.nextToken ().trim ());
			b = Double.parseDouble (st.nextToken ().trim ());
		} catch (Exception e) {
			throw new Exception ("linear2p: could not parse this function: " + str, e);
		}
	}

	public double f (double x) {
		return a * x  + b;
	}

	public String toString () {
		return "linear2p(" + a + ";" + b + ")";
	}


}
