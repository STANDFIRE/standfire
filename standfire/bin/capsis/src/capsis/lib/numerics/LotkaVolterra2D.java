package capsis.lib.numerics;

//~ import general.*	;

public class LotkaVolterra2D extends FunctionN2N{
	//
	double 	a, p, b, q	;
	//
	public LotkaVolterra2D(double a, double p, double b, double q){
		this.a	= a	;
		this.p	= p	;
		this.b	= b	;
		this.q	= q	;
	}
	//
	public RealVector compute(RealVector rvX){
		double		x, y	;
		double		dx,dy	;
		RealVector	rvY		;
		//
		rvY	= new RealVector(2)	;
		x	= rvX.getValueAt(0)	;
		y	= rvX.getValueAt(1)	;
		//
		dx	= x*(a - p*y)		;
		dy	= y*(-b + q*x)		;
		//
		rvY.setValueAt(dx,0)	;
		rvY.setValueAt(dy,1)	;
		//
		return rvY				;
	}
}