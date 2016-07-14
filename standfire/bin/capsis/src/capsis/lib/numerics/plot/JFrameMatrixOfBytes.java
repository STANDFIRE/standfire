package capsis.lib.numerics.plot			;

import java.awt.*		;
import java.awt.geom.*	;
import javax.swing.*	;
import general.*		;

public class JFrameMatrixOfBytes extends JFrame{
	public static final int H	= 600	;
	public static final int W	= 600	;
	Dimension d							;
	//
	public JFrameMatrixOfBytes(String title, MatrixOfBytes mob){
		super()								;
		setTitle(title)						;
		setSize(W,H)						;
		Dimension d = new Dimension()		;
		d			= getSize()				;
		//
		JPanelMatrixOfBytes jpmob = new JPanelMatrixOfBytes(mob)	;
		jpmob.setBackground(Color.white)								;
		getContentPane().add(jpmob)									;
	}
}