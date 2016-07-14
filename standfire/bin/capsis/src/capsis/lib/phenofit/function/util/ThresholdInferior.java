package capsis.lib.phenofit.function.util;


/**
 * The ThresholdInferior function.
 * 
 * @author Isabelle Chuine, Julie Gauzère - August 2015
 */
public class ThresholdInferior {

	private double Vb;

	/**
	 * Constructor.
	 */
	public ThresholdInferior(double Vb) {
		this.Vb = Vb;
	}
	
	public double execute (double x) {
		if (x < Vb) {
			return 1;
		} else {
			return 0;
		}
	}
	
}