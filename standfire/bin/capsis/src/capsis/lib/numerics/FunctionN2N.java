package capsis.lib.numerics;

//~ import general.*;

/**	Define a generic function from a vector to a vector
	we have rvY = functionN2N(rvX)
*/
public abstract class FunctionN2N{
	//
	public abstract RealVector compute(RealVector rvX)		;
	//

	/**	rmCompute : return a matrix rmY, such as each line of rmY is the image by 'functionN2N' of the
		corresponding line of rmX.
	*/
	public RealMatrix 	rmCompute(RealMatrix rmX){
		int 		i				;
		int			nl,nc			;
		RealVector	rvX, rvY		;
		RealMatrix	rmY				;
		//
		nl		= rmX.getNLine()		;
		nc		= rmX.getNCol()			;
		rmY		= new RealMatrix(nl,nc)	;
		//
		for (i=0; i<nl; i++){
			rvX	= rmX.getLine(i)			;
			rvY	= compute(rvX)				;
			rmY.setRealVectorAtLine(rvY,i)	;
		}
		//
		return rmY	;
	}
}