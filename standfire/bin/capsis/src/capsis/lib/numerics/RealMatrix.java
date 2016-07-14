package capsis.lib.numerics;

/**
	Name			RealMatrix
	Author			Alain Franc
	Date			May 1st, 2002
	Version			1.2
	Last Revision	Februray, 27th, 2003
*/

public class RealMatrix{
	double[][]	mat		;
	int			nLine	;
	int			nCol	;

	/**	Constructor with dimension nLine and nCol and all values equal to zeros
	*/
	public RealMatrix(int nLine, int nCol){
		this.nLine	= nLine						;
		this.nCol	= nCol						;
		mat			= new double[nLine][nCol]	;
	}
	/**	Constructor with dimension dim and all values equal to zeros
	*/
	public RealMatrix(int dim){
		nLine	= dim						;
		nCol	= dim						;
		mat		= new double[nLine][nCol]	;
	}

	/** Gets the number of lines
	*/
	public int getNLine(){
		int 	nl		;
		nl		= nLine	;
		return nl		;
	}

	/** Gets the number of columns
	*/
	public int getNCol(){
		int		nc		;
		nc		= nCol	;
		return	nc		;
	}

	/** Gets a specified line
	*/
	public RealVector getLine(int i){
		int 		j				;
		RealVector	rvX				;
		rvX	= new RealVector(nCol)	;
		//
		for (j=0; j< nCol; j++){rvX.setValueAt(mat[i][j],j);}
		//
		return rvX					;
	}

	/** Gets a specified column
	*/
	public RealVector getColumn(int j){
		int			i				;
		RealVector	rvY				;
		rvY	= new RealVector(nLine)	;
		//
		for (i=0; i< nLine; i++){rvY.setValueAt(mat[i][j],i);}
		//
		return rvY					;
	}

	/**
	*/
	public void setRealVectorAtLine(RealVector rvX, int i){
		int j, d	;
		double	x	;
		//
		d	= rvX.getDim()	;
		if (d != nCol){
			System.out.println("!======================================================!");
			System.out.println("! Affectation error in RealMatrix.setRealVectorAtLine()!");
			System.out.println("! Dimensions do not fit                                !");
			System.out.println("!======================================================!");
		}
		else{
			for (j=0; j<nCol; j++){
				x			= rvX.getValueAt(j)	;
				mat[i][j]	= x					;
			}
		}
	}

	/**
	*/
	public void setVectorAtLine(double[] vecX, int i){
		int j	;
		for (j=0; j<nCol; j++){mat[i][j]	= vecX[j]	;}
	}

	/**
	*/
	public void setValueAt(double x, int i, int j){mat[i][j]=x;}

	/**
	*/
	public void toScreen(){
		int		i, j	;
		double	x		;
		for (i=0; i < nLine ; i++){
			for (j=0; j < nCol; j++){
				x	= mat[i][j]	;
				System.out.print(x + " ! ")	;
			}
			System.out.println("");
		}
	}

	/** Gets minimum and maximum values in a matrix
		all lines and columns together
	*/
	/** Minimum and Maximum value
	*/
	public double[] minMax(){
		int			i,j				;
		double 		num, nMin, nMax ;
		double[]	mM				;
		nMin		= mat[0][0]		;
		nMax		= mat[0][0]		;
		mM			= new double[2]	;
		//
		for (i=0; i < nLine; i++){
			for (j=0; j<nCol; j++){
				num	= mat[i][j]	;
				if (num >= nMax){nMax = num	;}
				if (num <= nMin){nMin = num	;}
			}
		}
		//
		mM[0]	= nMin	;
		mM[1]	= nMax	;
		//
		return mM		;
	}

}