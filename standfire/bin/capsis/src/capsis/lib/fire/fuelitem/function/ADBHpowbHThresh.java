package capsis.lib.fire.fuelitem.function;

import java.util.StringTokenizer;

/**
 * Allometric relationship
 * 
 * @author F. Pimont - June 2015
 */
public class ADBHpowbHThresh extends Allom2Function {

	private double a;
	private double b;
	private double c;
	private double d;
	private double e;
	
	/**
	 * Constructor, parses a String, e.g. function1(0.996;2.403;13.086)
	 */
	public ADBHpowbHThresh (String str) throws Exception {
		try {
			String s = str.replace ("aDBHpowbcDBHpowdHThresh(", "");
			s = s.replace (")", "");
			s = s.trim ();
			StringTokenizer st = new StringTokenizer (s, ";");
			a = Double.parseDouble (st.nextToken ().trim ());
			b = Double.parseDouble (st.nextToken ().trim ());
			c = Double.parseDouble (st.nextToken ().trim ());
			d = Double.parseDouble (st.nextToken ().trim ());
			e = Double.parseDouble (st.nextToken ().trim ());
		} catch (Exception e) {
			throw new Exception ("ADBHpowbHThresh: could not parse this function: " + str, e);
		}
	}

	@Override
	/**
	 * 
	 */
	public double f (double dbh,double h) {
		if(h <= e) {
			return a * Math.pow (dbh, b);
		} else {
			return c * Math.pow (dbh, d);
		}
	}

	public String toString () {
		return "aDBHpowbcDBHpowdHThresh(" + a + ";" + b + ";" + c + ";" + d + ";" + e +")";
	}

}
