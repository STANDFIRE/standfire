package capsis.lib.phenofit.function.util;


import capsis.lib.phenofit.function.FitlibFunction;

/**
 * The Chuine function.
 * 
 * @author Isabelle Chuine, Yassine Motie - January 2015
 */
public class Chuine {

	private double a;
	private double b;
	private double c;

	/**
	 * Constructor.
	 */
	public Chuine(double a, double b, double c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
	public double execute (double x) {
		double y = a * (x - c) * (x - c) + b * (x - c);
		try {
			return 1d / (1d + Math.exp(y));
		} catch (Exception e) {
			return 0;
		}
	}
	
}