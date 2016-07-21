/*
 * FiPattern2DPanel.java
 *
 * Created on 1 juin 2007, 11:31
 *
 *
 */
package fireparadox.gui.plantpattern;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jeeb.lib.util.Translator;
import jeeb.lib.util.Vertex2d;
import capsis.util.Drawer;
import capsis.util.Panel2D;
import fireparadox.model.plant.fmgeom.FmGeom;
import fireparadox.model.plant.fmgeom.FmGeomDiameter;

/**
 * The panel to preview the 2D shape of a FiPattern
 * @author S. Griffon - May 2007
 */
public class FmPattern2DPanel extends JPanel implements Drawer {
	
	public static final int X_MARGIN = 10;
	public static final int Y_MARGIN = 10;
	public static final int X_GRID_STEP = 5;
	public static final int Y_GRID_STEP = 5;
	public static final int CROWN_HEIGHT = 100;
	
	private Panel2D panel2D;
	
	
	FmGeom fPattern;
	
	/** Creates a new instance of FiPattern2DPanel */
	public FmPattern2DPanel (FmGeom fPattern) {
		this.fPattern = fPattern;
		
		createUI ();
	}
	
	public void reset () {
		panel2D.reset ();
		repaint ();
	}
	
	public void setPattern (FmGeom fPattern) {
		this.fPattern = fPattern;
		reset ();
	}
	
	
	
	/**
	 * From Drawer interface.
	 * This method draws in the Panel2D each time this one must be repainted.
	 * The given Rectangle is the sub-part of the object to draw (zoom) in user
	 * coordinates (i.e. meters...). It can be used in preprocesses to avoid
	 * drawing invisible parts.
	 */
	public void draw (Graphics g, Rectangle.Double r) {
		
		double xgrid = r.x;
		
		double ygrid = r.y;
		
		NumberFormat f = NumberFormat.getInstance ();
		f.setMinimumFractionDigits (0);
		f.setMaximumFractionDigits (2);
		
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor (Color.lightGray);
		
		
		//Draw the grid
		//Vertical lines
		while (xgrid < r.width) {
			g2.draw (new Line2D.Double (xgrid,r.y,xgrid,r.height));
			xgrid += X_GRID_STEP;
		}
		
		//Horizontal lines
		while (ygrid < r.height) {
			g2.draw (new Line2D.Double (r.x, ygrid, r.x + r.width, ygrid));
			ygrid += Y_GRID_STEP;
		}
		g2.setStroke (new BasicStroke (1.5f));
		
		
		if (fPattern != null)	{
			
			double heightDiamMax = fPattern.getHDMax () * CROWN_HEIGHT / 100.;
			
			//Draw the trunk
			g2.setColor (Color.BLACK);
			g2.draw (new Line2D.Double (0, -100, 0, 0));
			
			//Draw the trunk line mark
			g2.setStroke (new BasicStroke (1f));
			g2.setColor (Color.DARK_GRAY);
			g2.draw (new Line2D.Double (0, 0, 0, CROWN_HEIGHT));
			
			
			
			//background color to see the inferior and superior diameter zone
			g2.setStroke (new BasicStroke (1f));
			float transparency = 0.2f;
			//Set transparancy : alpha
			g2.setComposite (java.awt.AlphaComposite.getInstance (java.awt.AlphaComposite.SRC_OVER, Math.min (transparency, 1.0f ) )) ;
			
			g2.setColor (Color.blue.brighter ());
			RoundRectangle2D.Double infRect = new RoundRectangle2D.Double (-50d,0d,100d,heightDiamMax,10d,10d);
			g2.draw (infRect);
			g2.fill (infRect);
			
			g2.setColor (Color.red.brighter ());
			RoundRectangle2D.Double supRect = new RoundRectangle2D.Double (-50d,heightDiamMax,100d,CROWN_HEIGHT-heightDiamMax,10d, 10d);
			g2.draw (supRect);
			g2.fill (supRect);
			
			//Set no transparancy : alpha ==1
			g2.setComposite (java.awt.AlphaComposite.getInstance (java.awt.AlphaComposite.SRC_OVER, 1.0f )) ;
			//end of drawing inf and sup zone
			
			
			//An array to save the point of the crown polygon
			ArrayList <Vertex2d> pointPoly = new ArrayList <Vertex2d> ();
			GeneralPath crownPolygone = new GeneralPath ();
			
			g2.setStroke (new BasicStroke (1.f));
			
			//Draw the inferior diameters
			g2.setColor (Color.BLUE);
			g2.setFont (g2.getFont ().deriveFont (Font.PLAIN,10f));
			//Faire sur Panel2D
			FontMetrics fMetric = panel2D.getFontMetrics (g2.getFont ());
			
			g2.drawString ("0",-54f-(fMetric.stringWidth ("0")/2f),0);
			g2.drawString ("100",-54f-(fMetric.stringWidth ("100")/2f),(float)heightDiamMax);
			
			for (FmGeomDiameter diam : fPattern.getDiametersInferior ()) {
				double relativeHeight=diam.getHeight () * heightDiamMax / 100.;
				double halfWidth=diam.getWidth () / 2.;
				
				g2.draw (new Line2D.Double (-halfWidth, relativeHeight, halfWidth, relativeHeight));
				pointPoly.add (new Vertex2d (halfWidth, relativeHeight));
				String rHeight = f.format (diam.getHeight ());
				g2.drawString (rHeight,-54f-(fMetric.stringWidth (rHeight)/2f),(float)relativeHeight);
			}
			
			//draw the max diameter	: width 100
			g2.setColor (Color.RED);
			g2.draw (new Line2D.Double (-50,heightDiamMax,50,heightDiamMax));
			//Add the points for the max diameter
			pointPoly.add (new Vertex2d (50,heightDiamMax));
			
			
			//Draw the superior diameters
			g2.setColor (Color.RED);
			g2.drawString ("0",54f-(fMetric.stringWidth ("0")/2f),(float)heightDiamMax);
			g2.drawString ("100",54f-(fMetric.stringWidth ("100")/2f),CROWN_HEIGHT);
			for (FmGeomDiameter diam : fPattern.getDiametersSuperior ())	{
				double relativeHeight=diam.getHeight () * ((CROWN_HEIGHT-heightDiamMax) / 100.) + heightDiamMax;
				double halfWidth=diam.getWidth () / 2.;
				g2.draw (new Line2D.Double (-halfWidth, relativeHeight, halfWidth, relativeHeight));
				pointPoly.add (new Vertex2d (halfWidth, relativeHeight));
				String rHeight = f.format (diam.getHeight ());
				g2.drawString (rHeight,54f-(fMetric.stringWidth (rHeight)/2f),(float)relativeHeight);
			}
			
			
			g2.setColor (Color.green.brighter ());
			g2.setStroke (new BasicStroke (1f));
			transparency = 0.7f;
			g2.setComposite (java.awt.AlphaComposite.getInstance (java.awt.AlphaComposite.SRC_OVER, Math.min (transparency, 1.0f ) )) ;
			crownPolygone.moveTo (0,0);
			for (Vertex2d v : pointPoly)	{
				crownPolygone.lineTo ((float)-v.x, (float)v.y);
			}
			crownPolygone.lineTo (0,CROWN_HEIGHT);
			ListIterator<Vertex2d> it = pointPoly.listIterator (pointPoly.size ());
			while (it.hasPrevious ())	{
				Vertex2d v = it.previous ();
				crownPolygone.lineTo ((float)v.x, (float)v.y);
			}
			
			
			crownPolygone.closePath ();
			g2.draw (crownPolygone);
			g2.fill (crownPolygone);
			
			
			//Legend
			g2.setColor (Color.blue.brighter ());
			RoundRectangle2D.Double legInfRect = new RoundRectangle2D.Double (-55d,-98d,15d,10d,2d,2d);
			g2.draw (legInfRect);
			g2.fill (legInfRect);
			g2.drawString (Translator.swap ("FiPattern2DPanel.inferiorPart"),-38f,-98);
		
			g2.setColor (Color.red.brighter ());
			RoundRectangle2D.Double legSupRect = new RoundRectangle2D.Double (-55d,-83d,15,10d,2d,2d);
			g2.draw (legSupRect);
			g2.fill (legSupRect);
			g2.drawString (Translator.swap ("FiPattern2DPanel.superiorPart"),-38f,-83);
			
			
			//Draw strings
			
			g2.setFont (g2.getFont ().deriveFont (Font.BOLD,12f));
			fMetric = panel2D.getFontMetrics (g2.getFont ());
			g2.setColor (Color.black);
			String hdMaxStr = f.format(fPattern.getHDMax ());
			//g2.setFont(new Font("impact", Font.BOLD, 6));
			g2.drawString ("0",0f-(fMetric.stringWidth ("0")/2f),0);
			g2.drawString ("100",0f-(fMetric.stringWidth ("100")/2f),CROWN_HEIGHT);
			g2.drawString (hdMaxStr, 0f-(fMetric.stringWidth (hdMaxStr)/2f),(float)heightDiamMax);
		} //end pattern != null
		
		
	}
	
	/**
	 * From Drawer interface.
	 * We may receive (from Panel2D) a selection rectangle (in user space i.e. meters)
	 * and return a JPanel containing information about the objects (trees) inside
	 * the rectangle.
	 * If no objects are found in the rectangle, return null.
	 */
	public JPanel select (Rectangle.Double r, boolean ctrlIsDown) {
		return null;
	}
	
	/**
	 * User interface definition.
	 */
	private void createUI () {
		this.setLayout (new BorderLayout ());
		
		
		// 1. tree drawing
		//The tree will be center in 0,0,100,200 but we need 30 unit more on each side to print some Strings
		Rectangle.Double r2 = new Rectangle.Double (-50.-X_MARGIN, -100.-Y_MARGIN, 100 + X_MARGIN + X_MARGIN, 200 + Y_MARGIN + Y_MARGIN);	// x, y, w, h
		
		panel2D = new Panel2D (this, 	// when repaint needed, panel2D will call this.draw ()
			r2,
			0,
			0,
			//~ true);
			false);
		
		panel2D.setZoomEnabled (false);
		panel2D.setMoveEnabled (false);
		
		//panel2D.setPreferredSize (new Dimension (250, 500));
		//JPanel part1 = new JPanel (new BorderLayout ());
		
		JScrollPane scrollPane = new JScrollPane (panel2D);
		scrollPane.setMinimumSize (new Dimension (100,200));
		scrollPane.setPreferredSize (new Dimension (250, 500));
		
		//part1.add (scrollPane, BorderLayout.CENTER);
		this.add (scrollPane, BorderLayout.CENTER);
		
		
		// Layout parts
		//this.add (part1, BorderLayout.CENTER);
	}
}
