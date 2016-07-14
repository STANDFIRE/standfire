package capsis.lib.numerics.plot;

import java.awt.*			;
import java.awt.geom.*		;
import javax.swing.*		;
import general.*			;

public class PVectors{
	int	d					;
	RealVector	rvX	, rvY	;
	//
	public PVectors (RealVector rvX, RealVector rvY){
		//
		this.rvX	= rvX			;
		this.rvY	= rvY			;
		d			= rvX.getDim()	;
	}
	//
	public void initializeFrame(PFrame pF){
		//
		double 	minX, minY, ampX, ampY	;
		//
		minX	= rvX.min()				;
		minY	= rvY.min()				;
		ampX	= rvX.amplitude()		;
		ampY	= rvY.amplitude()		;
		//
		pF.setBoundValues(minX, minY, ampX, ampY)	;
	}
	//
	public void addTo(Graphics2D g2, PFrame pF){
		int			i					;
		double		x1, y1, x2, y2		;
		Point2D		p1, p2				;
		//
		x1	= rvX.getValueAt(0)			;
		y1	= rvY.getValueAt(0)			;
		p1	= pF.getPoint(x1, y1)		;
		//
		for (i=1; i< d; i++){
			x2	= rvX.getValueAt(i)				;
			y2	= rvY.getValueAt(i)				;
			p2	= pF.getPoint(x2, y2)			;
			g2.draw(new Line2D.Double(p1,p2))	;
			//
			p1	= p2							;
		}
	}
	//
	public void addTo(Graphics2D g2, PFrame pF, double size){
		int 		i				;
		double		x, y			;
		double		xp, yp			;
		double 		x1, y1, x2, y2	;
		double		s				;
		Point2D		p, p1, p2		;
		//
		s	= size/2	;
		//
		for (i=0; i<d; i++){
			x	= rvX.getValueAt(i)		;
			y	= rvY.getValueAt(i)		;
			p	= pF.getPoint(x, y)		;
			//
			xp	= p.getX()				;
			yp	= p.getY()				;
			//
			x1	= xp - s				;
			y1  = yp					;
			x2	= xp + s				;
			y2	= yp					;
			//
			p1	= new Point2D.Double(x1, y1)	;
			p2	= new Point2D.Double(x2, y2)	;
			g2.draw(new Line2D.Double(p1,p2))	;
			//
			x1	= xp							;
			y1  = yp -s							;
			x2	= xp							;
			y2	= yp + s						;
			//
			p1	= new Point2D.Double(x1, y1)	;
			p2	= new Point2D.Double(x2, y2)	;
			g2.draw(new Line2D.Double(p1,p2))	;
		}
	}
}