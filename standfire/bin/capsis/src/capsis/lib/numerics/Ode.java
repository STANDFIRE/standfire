package capsis.lib.numerics;

//~ import general.*	;
//~ import functions.*	;

public class Ode{
	public Ode(){super();}
	//


	//	---------------------- Euler's method ----------------------------------------------------

	/**	Euler method for a unidimensional function
	*/
	public static RealVector euler(Function f, double x0, double t0, double tf, double h){
		int			i, d						;
		double		x, t, dx					;
		RealVector 	rvT, rvX					;
		//
		rvT			= new RealVector(t0,h,tf)	;
		d			= rvT.getDim()				;
		rvX			= new RealVector(d)			;
		x			= x0						;
		rvX.setValueAt(x,0)						;
		//
		for (i=1; i<d; i++){
			t	= t0 + i*h						;
			dx	= f.compute(x)					; // value of dx/dt at x as dx/dt = f(x)
			x	= x + h*dx						;
			rvX.setValueAt(x,i)					;
		}
		return		rvX							;
	}

	/**	Euler method for a multidimensional function
	*/
	public static RealMatrix euler(FunctionN2N f, RealVector x0, double t0, double tf, double h){
		int			i, d, n						;
		double		t							;
		RealVector	x, dx						;
		RealVector 	rvT							;
		RealMatrix	rmX							;
		//
		rvT			= new RealVector(t0,h,tf)	;
		d			= rvT.getDim()				;
		n			= x0.getDim()				;
		rmX			= new RealMatrix(d,n)		;
		x			= new RealVector(n)			;
		x			= x0						;
		rmX.setRealVectorAtLine(x,0)			;
		//
		for (i=1; i<d; i++){
			t	= t0 + i*h						;
			dx	= f.compute(x)					; // value of dx/dt at x as dx/dt = f(x)
			dx.multiplyByScalar(h)				;
			x.sumWithRealVector(dx)				;
			rmX.setRealVectorAtLine(x,i)		;
		}
		return		rmX							;
	}


	//	---------------------- Euler's method --------------------------------------------------------

	/**	Improved Polygonal Euler method for a unidimensional function
		Schwarz, p. 380
	*/
	public static RealVector eulerIP(Function f, double x0, double t0, double tf, double h){
		int			i, d						;
		double		x, x2, t 					;
		double		k1, k2						;
		RealVector 	rvT, rvX					;
		//
		rvT			= new RealVector(t0,h,tf)	;
		d			= rvT.getDim()				;
		rvX			= new RealVector(d)			;
		x			= x0						;
		rvX.setValueAt(x,0)						;
		//
		for (i=1; i<d; i++){
			t	= t0 + i*h						;
			k1	= f.compute(x)					; // value of dx/dt at x as dx/dt = f(x)
			x2	= x + h*k1/2					;
			k2	= f.compute(x2)					;
			x	= x + h*k2						;
			rvX.setValueAt(x,i)					;
		}
		return		rvX							;
	}

	/**	Improved Polygonal Euler method for a multidimensional function
		Schwarz, p. 380
	*/
	public static RealMatrix eulerIP(FunctionN2N f, RealVector x0, double t0, double tf, double h){
		int			i, d, n						;
		double		t							;
		RealVector	x, x2						;
		RealVector	k1, k2						;
		RealVector 	rvT							;
		RealMatrix	rmX							;
		//
		rvT			= new RealVector(t0,h,tf)	;
		d			= rvT.getDim()				;
		n			= x0.getDim()				;
		rmX			= new RealMatrix(d,n)		;
		x			= new RealVector(n)			;
		x2			= new RealVector(n)			;
		k1			= new RealVector(n)			;
		k2			= new RealVector(n)			;
		x			= x0						;
		rmX.setRealVectorAtLine(x,0)			;
		//
		for (i=1; i<d; i++){
			t	= t0 + i*h						;
			k1	= f.compute(x)					; 	// 	k1 	= f(x)
			k1.multiplyByScalar(h*0.5)			;	//	k1	= h*k1/2
			x2	= x.sumWithRealVectorTo(k1)		;	//	x2	= x + k1
			k2	= f.compute(x2)					;	//	k2	= f(x2)
			k2.multiplyByScalar(h)				;	//	k2	= h*k2
			x.sumWithRealVector(k2)				;	//	x	= x + k2
			rmX.setRealVectorAtLine(x,i)		;
		}
		return		rmX							;
	}

	//	---------------------- Heun's method ----------------------------------------------------

	//	---------------------- Runge-Kutta methods ----------------------------------------------

	/**	Runge-Kutta Third order method for multidimensional function -
		Schwarz, p. 387
	*/
	public static RealMatrix RungeKutta3(FunctionN2N f, RealVector x0, double t0, double tf, double h){
		int			i, d, n						;
		double		t							;
		RealVector	x							;
		RealVector	k1, k1b, k2, k2b, k3		;
		RealVector 	rvT							;
		RealMatrix	rmX							;
		//
		rvT			= new RealVector(t0,h,tf)	;
		d			= rvT.getDim()				;
		n			= x0.getDim()				;
		rmX			= new RealMatrix(d,n)		;
		x			= new RealVector(n)			;		;
		k1			= new RealVector(n)			;
		k1b			= new RealVector(n)			;
		k2			= new RealVector(n)			;
		k2b			= new RealVector(n)			;
		k3			= new RealVector(n)			;
		//
		x			= x0						;
		rmX.setRealVectorAtLine(x,0)			;
		//
		for (i=1; i<d; i++){
			t	= t0 + i*h						;
			k1	= f.compute(x)					; 	// 	k1 	= f(x)
			k1b	= k1.multiplyByScalarTo(h/3)	;	//	k1b	= h*k1/3
			k1b	= x.sumWithRealVectorTo(k1b)	;	//	k1b	= x + h*k1/3
			k2	= f.compute(k1b)				;	//	k2	= f(k1b)		= f(x+h*k1/3)
			k2.multiplyByScalar(2*h/3)			;	//	k2	= 2*h*k2/3
			k2.sumWithRealVector(x)				;	//	k2	= x + k2b		= x + 2*h*k2/3
			k3	= f.compute(k2)					;	//	k3	= f(k2)			= f(x+2*h*k2/3)
			k3.multiplyByScalar(3)				;	//	k3	= 3*k3
			k3.sumWithRealVector(k1)			;	//	k3	= k1 + k3
			k3.multiplyByScalar(h/4)			;	// 	k3	= h*k3/4
			x.sumWithRealVector(k3)				;	//	x	= x + k3		= x + (h/4)*(k1 + 3*k3)
			//
			rmX.setRealVectorAtLine(x,i)		;
		}
		return		rmX							;
	}


}