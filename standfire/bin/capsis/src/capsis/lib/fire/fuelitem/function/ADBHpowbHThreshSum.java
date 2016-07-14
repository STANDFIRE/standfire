package capsis.lib.fire.fuelitem.function;

import java.util.StringTokenizer;

/**
 * Allometric relationship
 * 
 * @author F. Pimont - June 2015
 */
public class ADBHpowbHThreshSum extends Allom2Function {

	private double a;
	private double b;
	private double c;
	private double d;
	private double e;
	private double f;
	private double g;
	private double h;
	private double i;
	
	/**
	 * Constructor, parses a String, e.g. function1(0.996;2.403;13.086)
	 */
	public ADBHpowbHThreshSum (String str) throws Exception {
		try {
			String s = str.replace ("aDBHpowbcDBHpowdHThreshSum(", "");
			s = s.replace (")", "");
			s = s.trim ();
			StringTokenizer st = new StringTokenizer (s, ";");
			a = Double.parseDouble (st.nextToken ().trim ());
			b = Double.parseDouble (st.nextToken ().trim ());
			c = Double.parseDouble (st.nextToken ().trim ());
			d = Double.parseDouble (st.nextToken ().trim ());
			e = Double.parseDouble (st.nextToken ().trim ());
			f = Double.parseDouble (st.nextToken ().trim ());
			g = Double.parseDouble (st.nextToken ().trim ());
			h = Double.parseDouble (st.nextToken ().trim ());
			i = Double.parseDouble (st.nextToken ().trim ());
		} catch (Exception e) {
			throw new Exception ("ADBHpowbHThreshSum: could not parse this function: " + str, e);
		}
	}

	@Override
	/**
	 * 
	 */
	public double f (double dbh,double h) {
		if(h <= i) {
			return a * Math.pow (dbh, b) + c * Math.pow (dbh, d);
		} else {
			return e * Math.pow (dbh, f) + g * Math.pow (dbh, h);
		}
	}

	public String toString () {
		return "aDBHpowbcDBHpowdHThreshSum(" + a + ";" + b + ";" + c + ";" + d + ";" + e +";" + f + ";" + g + ";" + h + ";" + i +")";
	}

}
