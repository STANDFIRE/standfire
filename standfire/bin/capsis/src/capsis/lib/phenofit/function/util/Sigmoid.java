package capsis.lib.phenofit.function.util;

/**
 * A Sigmoid function.
 * 
 * @author Isabelle Chuine, Yassine Motie - January 2015
 */
public class Sigmoid {

	private double d;
	private double e;

	/**
	 * Constructor.
	 */
	public Sigmoid(double d, double e) {
		this.d = d;
		this.e = e;
	}

	public double execute(double x) {
		double y = d * (x - e);
		try {
			return 1d / (1d + Math.exp(y));
		} catch (Exception e) {
			return 0;
		}
	}

}