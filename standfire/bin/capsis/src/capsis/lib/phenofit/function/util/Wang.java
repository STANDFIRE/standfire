package capsis.lib.phenofit.function.util;

import capsis.lib.phenofit.function.FitlibFunction;

/**
 * The Chuine function.
 * 
 * @author Isabelle Chuine, Yassine Motie - January 2015
 */
public class Wang {

	private double Topt;
	private double Tmin;
	private double Tmax;

	/**
	 * Constructor.
	 */
	public Wang(double Topt, double Tmin, double Tmax) {
		this.Topt = Topt;
		this.Tmin = Tmin;
		this.Tmax = Tmax;
	}

	public double execute(double x) { //
		if (x <= Tmin) {
			return 0;
		}
		if ((x <= Tmax) && (Tmax > (Tmin + 1)) && (Topt > (Tmin + 0.5)) && (Topt < (Tmax - 0.5))) {

			double alfa = Math.log(2) / Math.log((Tmax - Tmin) / (Topt - Tmin));
			double numerator = 2 * Math.pow((x - Tmin), alfa) * Math.pow((Topt - Tmin), alfa)
					- Math.pow((x - Tmin), 2 * alfa);
			double denominator = Math.pow((Topt - Tmin), 2 * alfa);

			// try {
			double result = numerator / denominator;
			return result;
		}// } catch (Exception e) {
		return 0;

	}
}
