package capsis.lib.phenofit.function.util;

public class GDD {
	private double a;

	/**
	 * Constructor.
	 */
	public GDD(double a) {
		this.a = a;
	}

	public double execute(double x) {
		if (x > a)
			return x - a;
		return 0;
	}
}
