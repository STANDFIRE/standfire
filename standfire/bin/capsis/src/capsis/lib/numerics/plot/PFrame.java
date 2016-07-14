package capsis.lib.numerics.plot;
//
import java.awt.*		;
import java.awt.geom.*	;
import javax.swing.*	;
import general.*		;
/*
	Used by JPanelPlot
	Gives coordintates of the frame in which the plot is drawn

	AF, Kourou, 24/11/02
*/
public class PFrame{
	double		h							;
    double		top, left, width, height	;
    double		minX, minY, ampX, ampY		;
    //
    public PFrame(int b, int h, int w){
		//
		int		iw, ih				;
		//
		this.h	= (double)h			;
		iw		= w - 3*b			;
		ih		= h - 5*b	 		;
		//
		top		= (double) b		;
		left	= (double) b		;
		width	= (double) iw		;
		height	= (double) ih		;
    }
    //
    public void setBoundValues(double minX, double minY, double ampX, double ampY){
		this.minX	= minX	;
		this.minY	= minY	;
		this.ampX	= ampX	;
		this.ampY	= ampY	;
	}
	//
    public	void addTo(Graphics2D g2){
		Rectangle2D	rect											;
		rect	= new Rectangle2D.Double(left, top, width, height)	;
		g2.setPaint(Color.black)									;
		g2.draw(rect)												;
	}
	//
	public Point2D getPoint(double x, double y){
		double	xf, yf			;
		double	xp, yp			;
		Point2D	p				;
		//
		xf	= (x - minX)/ampX			;
		yf	= (y - minY)/ampY			;
		xp	= left + width*xf			;
		yp	= h - 4*top - height*yf		;
		//
		p	= new Point2D.Double(xp,yp)	;
		return	p						;
	}
}