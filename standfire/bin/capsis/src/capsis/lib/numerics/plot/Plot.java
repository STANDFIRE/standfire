package capsis.lib.numerics.plot;

import 	general.*	;

/*
**/
public class Plot{
	JFramePlot	f			;
	//
	public Plot(String title, RealVector rvX, RealVector rvY, char col, char symb){
		f = new JFramePlot(title, rvX, rvY, col, symb)	;
 	}
	public Plot(String title, RealVector rvX, RealVector rvY, char col){
		f = new JFramePlot(title, rvX, rvY, col)	;
	}
	//
	public Plot(String title, RealVector rvY, char col, char symb){
		int			i,	dim					;
		double		xi						;
		RealVector	rvX						;
		dim			= rvY.getDim()			;
		rvX			= new RealVector(dim)	;
		//
		for (i=0;i<dim;i++){
			xi	= (double)i			;
			rvX.setValueAt(xi,i)	;
		}
		//
		f = new JFramePlot(title, rvX, rvY,col, symb)	;
	}
	//
	public Plot(String title, RealVector rvY, char col){
		int			i,	dim					;
		double		xi						;
		RealVector	rvX						;
		dim			= rvY.getDim()			;
		rvX			= new RealVector(dim)	;
		//
		for (i=0;i<dim;i++){
			xi	= (double)i			;
			rvX.setValueAt(xi,i)	;
		}
		//
		f = new JFramePlot(title, rvX, rvY,col)	;
	}
	public void draw()	{f.show()	;}
}