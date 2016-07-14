package capsis.lib.phenofit.function.util;


import capsis.lib.phenofit.function.FitlibFunction;

/**
 * The DayLength function.
 * 
 * @author Isabelle DayLength, Yassine Motie - January 2015
 */
public class DayLength {

	/**
	 * Constructor.
	 */
	public DayLength() {
	}
	
	public double execute (double latitude, int d) {
		
		if (d < 1)
			d += 365;
		
		double DEC = -23.45 * Math.PI / 180d * Math.cos (2d * Math.PI * (d + 10) / 365d);
		double a = -Math.tan(latitude * Math.PI / 180d) * Math.tan(DEC);
		if (a > 1) {
			return 0;
		} else if (a < -1) {
			return 24;
		} else {
			return 24d / Math.PI * Math.acos(a);
		}
		
	}
	
}