package capsis.lib.phenofit.function.util;


import capsis.lib.phenofit.function.FitlibFunction;

/**
 * The Gauzere function.
 * 
 * @author Julie Gauzere, Isabelle Chuine- August 2015
 */
public class Gauzere {

	private double a1;
	private double a2;
	private double a3;
	private double Ccrit;

	/**
	 * Constructor.
	 */
	public Gauzere(double a1, double a2, double a3, double Ccrit) {
		this.a1 = a1;
		this.a2 = a2;
		this.a3 = a3;
		this.Ccrit = Ccrit;
	}
	
	/**
	 * 
	 */
	public double execute (double x1, double x2) {
		
		Sigmoid sigmoid1 = new Sigmoid(a2, Ccrit);
		double dl50 = (12 - a1) + 2 * a1 * sigmoid1.execute(x1);
		
		Sigmoid sigmoid2 = new Sigmoid(a3, dl50);
		return sigmoid2.execute(x2);
		
	}
	
}