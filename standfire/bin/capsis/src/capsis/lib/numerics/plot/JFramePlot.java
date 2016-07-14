package capsis.lib.numerics.plot;

import java.awt.*		;
import java.awt.geom.*	;
import javax.swing.*	;
import general.*		;

public class JFramePlot extends JFrame{
	public static final int H	= 500	;
	public static final int W	= 800	;
	Dimension d							;
	//
	public JFramePlot(String title, RealVector rvX, RealVector rvY, char col, char symb){
		super()								;
		setTitle(title)						;
		setSize(W,H)						;
		Dimension d = new Dimension()		;
		d			= getSize()				;
		//
		JPanelPlot jpPlot = new JPanelPlot(rvX, rvY, d, col, symb)		;
		jpPlot.setBackground(Color.white)								;
		getContentPane().add(jpPlot)									;
	}
	public JFramePlot(String title, RealVector rvX, RealVector rvY, char col){
		super()								;
		setTitle(title)						;
		setSize(W,H)						;
		Dimension d = new Dimension()		;
		d			= getSize()				;
		//
		JPanelPlot jpPlot = new JPanelPlot(rvX, rvY, d, col)		;
		jpPlot.setBackground(Color.white)								;
		getContentPane().add(jpPlot)									;
	}
}