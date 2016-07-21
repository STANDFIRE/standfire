package capsis.lib.numerics;


/**
	Name			Vector of Bytes
	Author			Alain Franc
	Date			January 6th, 2003
	Version			1.0
	Last Revision	09/01/03
*/

public class VectorOfBytes{

	byte[]	vec		;
	int		dim		;

	public	VectorOfBytes(byte[] vec, int dim){
		this.dim	= dim	;
		this.vec	= vec	;
	}

	public	VectorOfBytes(byte[] vec){

		int	dim	;

		dim			= vec.length	;
		this.vec	= vec			;
		this.dim	= dim			;
	}

	public void toScreen(){

		int i	;

		for (i=0; i< dim; i++){System.out.print(vec[i] + " ")	;}
		System.out.println("")									;
	}
}