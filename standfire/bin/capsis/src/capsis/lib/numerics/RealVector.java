package capsis.lib.numerics	;

//~ import 	plot.*	;

/**
	Name			RealVector
	Author			Alain Franc
	Date			April 2nd, 2002
	Version			1.1
	Last Revision	May, 4th, 2002
	//
	Fields			double[]	vec
					int			dim

	Constructors	RealVector(int dim)
					RealVector(double first, double step, double last)
					RealVector(int dim, double value)

	Public methods	int 		getDim()
					double[] 	getVec()
					double 		getValueAt(int i)
					void 		setValueAt(double x, int i)
					void 		toScreen()
					double 		min()
					double 		max()
					double[] 	minMax()
					double 		amplitude()
					double 		sum()
					double 		mean()
					void		toFrequence()
					RealVector 	toFrequenceTo()

*/

public class RealVector{

	double[]	vec		;
	int			dim		;


	//	---------------------- Constructors --------------------------------------------------------


	/**	Constructor with dimension dim and all values equal to zeros
	*/
	public RealVector(int dim){
		this.dim	= dim				;
		vec			= new double[dim]	;
	}


	/**	Constructor for a regular vector
		yields an arithmetic progression with first term 'first', step 'step' until value 'last'
		has been reached
	*/
	public RealVector(double first, double step, double last){
		int		i,d	;
		double	x	;
		x	= (last - first)/step	;
		d	= (int)Math.floor(x)	;
		//
		dim	= d+1					;
		vec	= new double[dim]		;
		//
		for (i=0; i<dim; i++){
			vec[i]	= first + i*step	;
		}
	}

	/**	Constructor for a vector: each coordinate is equal to value
	*/
	public RealVector(int dim, double value){
		//
		int	i					;
		//
		this.dim	= dim				;
		vec			= new double[dim]	;
		//
		for(i=0; i<dim; i++)	{vec[i] = value	;}
		toScreen();
	}

	// -------------------------- Gets information --------------------------------------------------


	/** Gets the dimension
	*/
	public int getDim(){
		int		d		;
		d		= dim	;
		//
		return	d		;
	}

	/** gets the array
	*/
	public double[] getVec(){
		double[]	v					;
		v			= new double[dim]	;
		v			= vec				;
		//
		return		v					;
	}

	/**	Gets a value at a given coordinate
	*/
	public double getValueAt(int i){
		double	x			;
		x		= vec[i]	;
		//
		return	x			;
	}


	// ----------------------------  Sets values -----------------------------------------------------

	/** Sets a value at a given coordinate
	*/
	public void setValueAt(double x, int i){vec[i] = x	;}


	/**	Sets values to random numbers
	*/
	public void setAlea(){
		int		i		;
		double	x		;
		for(i=0; i < dim; i++){
			x		= Math.random()	;
			vec[i]	= x				;
		}
	}


	// ---------------------------- Exports ---------------------------------------------------------


	/** Displays on the screen
	*/
	public void toScreen(){
		int i	;
		for (i=0; i < dim; i++){
			System.out.println(vec[i])	;
		}
	}

	// --------------------------- Simple calculation --------------------------------------------------

	/** Minimum value
	*/
	public double min(){
		int		i			;
		double 	num, nMin 	;
		nMin	= vec[0]	;
		//
		for (i=0; i < dim; i++){
			num	= vec[i]	;
			if (num <= nMin){nMin = num	;}
		}
		return nMin	;
	}

	/** Maximum value
	*/
	public double max(){
		int		i			;
		double 	num, nMax 	;
		nMax	= vec[0]	;
		//
		for (i=0; i < dim; i++){
			num	= vec[i]	;
			if (num >= nMax){nMax = num	;}
		}
		return nMax	;
	}

	/** Minimum and Maximum value
	*/
	public double[] minMax(){
		int			i				;
		double 		num, nMin, nMax ;
		double[]	mM				;
		nMin		= vec[0]		;
		nMax		= vec[0]		;
		mM			= new double[2]	;
		//
		for (i=0; i < dim; i++){
			num	= vec[i]	;
			if (num >= nMax){nMax = num	;}
			if (num <= nMin){nMin = num	;}
		}
		//
		mM[0]	= nMin	;
		mM[1]	= nMax	;
		//
		return mM		;
	}
	/**
	Amplitude
	*/
	public double amplitude(){
		double		m, M	;
		double		amp		;
		double[]	mM		;
		//
		mM		= new double[2]	;
		mM		= this.minMax()	;
		m		= mM[0]			;
		M		= mM[1]			;
		amp		= M - m			;
		//
		return	amp				;
	}

	//	-------------------- Basic operations for statistics ----------------------------------------

	/**	Sum of the elements
	*/
	public double sum(){
		int		i	;
		double	s	;
		s	= 0		;
		for (i=0; i<dim; i++){s=s+vec[i];}
		return	s	;
	}

	/**	Mean of the values
	*/
	public double mean(){
		double	m		;
		m	= sum()/dim	;
		return	m		;
	}

	/**	Transforms into frequencies (divides by the sum)
	*/
	public void toFrequence(){
		int		i		;
		double	s		;
		s	= sum()	;
		for (i=0; i<dim; i++){vec[i] = vec[i]/s;}
	}

	/** Computes the frequencies (as a new RealVector)
		s	= sum_i(x_i)
		F_i	= x_i/s
	*/
	public RealVector toFrequenceTo(){
		int			i				;
		double		s,x				;
		RealVector	F				;
		s	= sum()					;
		F	= new RealVector(dim)	;
		for (i=0; i<dim; i++){
			x	= vec[i]/s			;
			F.setValueAt(x,i)		;
		}
		return F					;
	}

	//	------------------- Operations on vector -----------------------------------------------------


	/** Multiplication by a scalar on the same RealVector
		X = a*X
	*/
	public void multiplyByScalar(double a){
		int i	;
		for (i=0; i< dim; i++){vec[i] = a*vec[i] ;}
	}

	/**	Multiplication by a scalar to another RealVector
		Y = a*X
	*/
	public RealVector multiplyByScalarTo(double a){
		int			i						;
		double		x						;
		RealVector	rvY						;
		rvY			= new RealVector(dim)	;
		for (i=0; i< dim; i++){
			x	= a*vec[i] 					;
			rvY.setValueAt(x,i)				;
		}
		return rvY							;
	}

	/** Sum with a Realvector on the same RealVector
		X = X+A
	*/
	public void sumWithRealVector(RealVector rvA){
		int i		;
		double a	;
		for (i=0; i<dim; i++){
			a	= rvA.getValueAt(i)	;
			vec[i]	= vec[i] + a	;
		}
	}

	/** Sum with a Realvector to another vector
		Y = X + A
	*/
	public RealVector sumWithRealVectorTo(RealVector rvA){
		int i, d		;
		double y, a		;
		RealVector	rvY				;
		d	= rvA.getDim()			;
		rvY	= new RealVector(dim)	;
		//
		for (i=0; i<dim; i++){
			a	= rvA.getValueAt(i)	;
			y 	= vec[i] + a		;
			rvY.setValueAt(y,i)		;
		}
		//
		return rvY					;
	}
}