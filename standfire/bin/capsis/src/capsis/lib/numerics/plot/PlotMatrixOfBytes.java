package capsis.lib.numerics.plot;

import 	general.*	;

public class PlotMatrixOfBytes{
	JFrameMatrixOfBytes	f			;
	//
	public PlotMatrixOfBytes(String title, MatrixOfBytes mob){
		f = new JFrameMatrixOfBytes(title, mob)	;
	}
	//
	public void plot()	{f.show()	;}
}