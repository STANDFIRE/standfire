package capsis.lib.numerics;

//~ import general.*;

public abstract class Function{
	//
	public abstract double 		compute(double x)		;
	//
	public RealVector 	rvCompute(RealVector rvX){
		int 		i	;
		int			d	;
		double		x,y	;
		RealVector	rvY	;
		//
		d			= rvX.getDim()		;
		rvY			= new RealVector(d)	;
		//
		for (i=0; i<d; i++){
			x	= rvX.getValueAt(i)	;
			y	= compute(x)		;
			rvY.setValueAt(y,i)		;
		}
		//
		return rvY	;
	}
}

