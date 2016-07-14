package capsis.lib.numerics.plot;

import java.awt.*		;
import java.awt.geom.*	;
import javax.swing.*	;
import general.*		;
//
public class JPanelImageMob extends JPanel{
	private	int					nRow, nCol			;
	private	double				top, left, a, l, c	;
	private	MatrixOfBytes		mob					;
	private	Image				picture				;
	//
	public JPanelImageMob(MatrixOfBytes mob){
		super()							;
		this.mob	= mob			;
		nRow		= mob.getNRow()	;
		nCol		= mob.getNCol()	;
		//
		top			= 10			;	// margin on top
		left		= 10			;	// margin on left
		a			= 512/nRow		;	// size of a cell, in pixels
		l			= 0				;
		c			= 0				;
	}
	//
	public void paintComponent (Graphics graphics){
		//
		Rectangle	bounds		= getBounds()								;
		picture					= createImage(bounds.width, bounds.height)	;
		Graphics	g			= picture.getGraphics()						;
		Graphics2D	g2			= (Graphics2D)g								;
		//
		int 	i,j	;
		byte	b	;
		//
		for (i=0; i<nRow;i++){
			l	= top + i*a					;
			for (j=0; j<nCol; j++){
				c					= left + j*a							;
				b					= mob.getValueAt(i,j)					;
				Rectangle2D rect	= new Rectangle2D.Double(c, l, a, a)	;
				//
				if (b==0)	{g2.setPaint(Color.blue)	;}
				if (b==1)	{g2.setPaint(Color.red)		;}
				g2.fill(rect)		;
			}
		}
		graphics.drawImage(picture,0,0,null)	;
	}
}