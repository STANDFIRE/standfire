package capsis.lib.numerics.plot;

import java.awt.*		;
import java.awt.geom.*	;
import javax.swing.*	;
import general.*		;

/*
*/
public class JPanelPlot extends JPanel{
	//
	//	Geometry of the panel
	//
	int			h, w		;	// height and Width of the window the panel belongs to
	int			b			;	// for the margins of the frame
	char		col			;	// color for drawings
	//
	//	Objecys to draw
	//
	PFrame		pF			;	//
	PVectors	pV			;	// drawing vectors
	//
	//==========================================================================================//
	//																							//
	//									Constructors											//
	//																							//
	//==========================================================================================//
	//
	public JPanelPlot (RealVector rvX, RealVector rvY, Dimension dim, char col){
		super()									;
		this.col	= col						;
		//
		h			= dim.height				;
		w			= dim.width					;
		b			= 10						;
		//
		pF			= new PFrame(b,h,w)			;
		pV			= new PVectors(rvX, rvY)	;
	}
	//
	public JPanelPlot (RealVector rvX, RealVector rvY, Dimension dim, char col, char symb){
		super()		;
	}
	//==========================================================================================//
	//																							//
	//									PaintComponent											//
	//																							//
	//==========================================================================================//
	//
	public void paintComponent (Graphics g){
		super.paintComponent(g)			;
		Graphics2D	g2	= (Graphics2D)g	;
		//
		addFrame(g2)					;
		addVectors(g2, col)				;
		//
		int		size	;
		size	= 6		;
		pV.addTo(g2, pF, size)	;
		//
	}
	private void addFrame(Graphics2D g2){pF.addTo(g2)	;}
	//
	private void addVectors(Graphics2D g2, char col){
		selectColor(g2,col)				;
		pV.initializeFrame(pF)			;
		pV.addTo(g2, pF)				;
	}
	//
	//==========================================================================================//
	//																							//
	//									Private Methods											//
	//																							//
	//==========================================================================================//
	//
	//--------------------------------	selectColor---------------------------------------------//
	/*
	Selects the color for drawing
	*/
	//
	private void selectColor(Graphics2D g2, char col){
		switch(col){
			case 'r':
				g2.setPaint(Color.red)		;
				break						;
			case 'b':
				g2.setPaint(Color.blue)		;
				break						;
			case 'g':
				g2.setPaint(Color.green)	;
				break						;
			case 'y':
				g2.setPaint(Color.yellow)	;
				break						;
			case 'm':
				g2.setPaint(Color.magenta)	;
				break						;
			case 'c':
				g2.setPaint(Color.cyan)		;
				break						;
		}
	}
	//
}