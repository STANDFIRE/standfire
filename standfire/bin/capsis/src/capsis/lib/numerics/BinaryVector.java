package capsis.lib.numerics;


/**
	Name			BinaryVector
	Author			Alain Franc
	Date			May, 10th, 2002
	Version			1.1
	Last Revision	August, 15th, 2002
*/

public class BinaryVector{

	byte[]	vec		;
	int		dim		;


	//	---------------------- Constructors --------------------------------------------------------


	/**	Constructor with dimension dim and all values equal to zeros
	*/
	public BinaryVector(int dim){
		this.dim	= dim			;
		vec			= new byte[dim]	;
	}

	public BinaryVector(int dim, byte[] vec){
		this.dim	= dim	;
		this.vec	= vec	;
	}
	//	============================================================================================
	//
	//							Public methods
	//
	//	============================================================================================


	//	----------------------- GetSet	------------------------------------------------------------
	//
	/** gets dimension
	*/
	public int getDim(){return dim;}
	//
	/** gets value at given location
	*/
	public byte getValueAt(int i){return vec[i];}
	//
	/**	sets all values to zero
	*/
	private void setZeros(){
		int	i	;
		for (i=0; i < dim; i++){vec[i] = 0	;}
	}
	//
	/**	sets all values to one
	*/
	private void setOnes(){
		int	i	;
		for (i=0; i < dim; i++){vec[i] = 1	;}
	}
	//
	/** sets ones at random locations with density p
	*/
	public void setAlea(double p){
		int		i	;
		double	x	;
		for(i=0; i < dim; i++){
			x	= Math.random()	;
			if (x < p)	{vec[i]	= 1	 ;}
			else		{vec[i]	= 0	 ;}
		}
	}
	/** Vector with random values
	*/
	public void setAlea(){
		int 	i	;
		double	x	;
		for (i=0; i<dim; i++){
			x	= Math.random()	;
			if (x < 0.5){vec[i]=1;}
			else		{vec[i]=0;}
		}
	}
	/** Set a value at a given location
	*/
	public void setValueAt(byte b, int i){vec[i]=b;}

	//
	//	---------------- Statistics	----------------------------------------------------------------------------
	//

	/** Number of non null items
	*/
	public int sum(){
		int	i, s	;
		s	= 0		;
		for (i=0; i<dim; i++){s = s + vec[i]	;}
		return s	;
	}
	/**	Number of pairs
	*/
	public int[] pairNumbers(){
		byte	b1, b2				;
		int		i					;
		int		n00, n01, n10, n11	;
		int[]	pairs				;
		//
		pairs	= new int[4]		;
		n00		= 0					;
		n01		= 0					;
		n10		= 0					;
		n11		= 0					;
		//
		for (i=0; i<=dim-2;i++){
			b1	= getValueAt(i)					;
			b2	= getValueAt(i+1)				;
			if (b1==0 & b2==0)	{n00 = n00 +1	;}
			if (b1==0 & b2==1)	{n01 = n01 +1	;}
			if (b1==1 & b2==0)	{n10 = n10 +1	;}
			if (b1==1 & b2==1)	{n11 = n11 +1	;}
		}
		b1	= getValueAt(dim-1)				;
		b2	= getValueAt(0)					;
		if (b1==0 & b2==0)	{n00 = n00 +1	;}
		if (b1==0 & b2==1)	{n01 = n01 +1	;}
		if (b1==1 & b2==0)	{n10 = n10 +1	;}
		if (b1==1 & b2==1)	{n11 = n11 +1	;}
		//
		pairs[0]	= n00	;
		pairs[1]	= n01	;
		pairs[2]	= n10	;
		pairs[3]	= n11	;
		//
		return	pairs		;
	}

	/**	Mutation at coordinate i
	*/
	public void mutateAt(int i){
		vec[i]= (byte)~vec[i]	;
	}

	/** Mutation at random location
	*/
	public void mutateAtRandom(){
		int 	i							;
		int		v							;
		double	x							;
		x		= Math.random()				;
		i		= (int)Math.floor(dim*x)	;
		v		= 1-vec[i]					;
		vec[i]	= (byte)v					;
	}

	/** Mutation with a rate tMut
	*/
	public void mutate(double tMut){
		byte	b		;
		int		i, bi	;
		double	x		;
		//
		for (i=0; i<dim; i++){
			x	= Math.random()	;
			if (x < tMut){
				b		= vec[i]	;
				bi		= 1-b		;
				b		= (byte)bi	;
				vec[i]	= b			;
			}
		}
	}

	/** to a String
	*/

	public String toString(){
		String	s		;
		int		i		;
		s		= ""	;
		for (i=0;i<dim;i++){s=s+vec[i];}
		return s		;
	}

	/** Displays to Screen
	*/
	public void toScreen(){
		String s				;
		s	= toString()		;
		System.out.println(s)	;
	}

	/** Clone
	*/
	public BinaryVector toClone(){
		int				d							;
		byte[]			bv							;
		BinaryVector	v							;
		//
		bv				= new byte[dim]				;
		bv				= (byte[])vec.clone()		;
		//
		v				= new BinaryVector(dim, bv)	;
		//
		return			v	;
	}
	public byte[] neighborhood(){
		//
		int		i				;
		byte[]	N				;
		N		= new byte[dim]	;
		//
		if	(vec[dim-2]==0 & vec[dim-1]==0 & vec[0]==0)	{N[dim-1]=0;}
		if	(vec[dim-2]==0 & vec[dim-1]==0 & vec[0]==1)	{N[dim-1]=1;}
		if	(vec[dim-2]==0 & vec[dim-1]==1 & vec[0]==0)	{N[dim-1]=2;}
		if	(vec[dim-2]==0 & vec[dim-1]==1 & vec[0]==1)	{N[dim-1]=3;}
		if	(vec[dim-2]==1 & vec[dim-1]==0 & vec[0]==0)	{N[dim-1]=4;}
		if	(vec[dim-2]==1 & vec[dim-1]==0 & vec[0]==1)	{N[dim-1]=5;}
		if	(vec[dim-2]==1 & vec[dim-1]==1 & vec[0]==0)	{N[dim-1]=6;}
		if	(vec[dim-2]==1 & vec[dim-1]==1 & vec[0]==1)	{N[dim-1]=7;}
		//
		if	(vec[dim-1]==0 & vec[0]==0 & vec[1]==0)	{N[0]=0;}
		if	(vec[dim-1]==0 & vec[0]==0 & vec[1]==1)	{N[0]=1;}
		if	(vec[dim-1]==0 & vec[0]==1 & vec[1]==0)	{N[0]=2;}
		if	(vec[dim-1]==0 & vec[0]==1 & vec[1]==1)	{N[0]=3;}
		if	(vec[dim-1]==1 & vec[0]==0 & vec[1]==0)	{N[0]=4;}
		if	(vec[dim-1]==1 & vec[0]==0 & vec[1]==1)	{N[0]=5;}
		if	(vec[dim-1]==1 & vec[0]==1 & vec[1]==0)	{N[0]=6;}
		if	(vec[dim-1]==1 & vec[0]==1 & vec[1]==1)	{N[0]=7;}
		//
		for (i=1;i<=dim-2; i++){
			if	(vec[i-1]==0 & vec[i]==0 & vec[i+1]==0)	{N[i]=0;}
			if	(vec[i-1]==0 & vec[i]==0 & vec[i+1]==1)	{N[i]=1;}
			if	(vec[i-1]==0 & vec[i]==1 & vec[i+1]==0)	{N[i]=2;}
			if	(vec[i-1]==0 & vec[i]==1 & vec[i+1]==1)	{N[i]=3;}
			if	(vec[i-1]==1 & vec[i]==0 & vec[i+1]==0)	{N[i]=4;}
			if	(vec[i-1]==1 & vec[i]==0 & vec[i+1]==1)	{N[i]=5;}
			if	(vec[i-1]==1 & vec[i]==1 & vec[i+1]==0)	{N[i]=6;}
			if	(vec[i-1]==1 & vec[i]==1 & vec[i+1]==1)	{N[i]=7;}
		}
		return	N	;
	}
}