package capsis.lib.numerics;

public class Logistics extends Function{
	//
	double r, K	;
	//
	public Logistics(double r, double K){
		this.r	= r	;
		this.K	= K	;
	}
	//
	public double compute(double x){
		double	y			;
		y	= r*x*(1-(x/K))	;
		//
		return	y			;
	}
}