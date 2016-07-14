/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2001-2003  Francois de Coligny
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package capsis.extension.standviewer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import jeeb.lib.util.Log;
import jeeb.lib.util.Spatialized;
import jeeb.lib.util.Translator;
import capsis.commongui.projectmanager.StepButton;
import capsis.defaulttype.Tree;
import capsis.kernel.GModel;
import capsis.kernel.Step;

/**
 * SVSapin is a cartography simple viewer for trees with coordinates. It
 * draws the trees within the cells. It's based on SVSimple.
 * It's compatible with sapin model.
 * 
 * @author F. de Coligny - april 2001
 */
public class SVSapin extends SVSimple {
	static public String AUTHOR = "F. De Coligny";
	static public String VERSION = "1.0";

	public static final int MARGIN = 10;

	public static final Color COLOR1 = Color.white;
	public static final Color COLOR2 = Color.lightGray;
	public static final Color COLOR3 = Color.gray;
	public static final Color COLOR4 = Color.orange;
	public static final Color COLOR5 = Color.red;


	static {
		Translator.addBundle("capsis.extension.standviewer.SVSapin");
	} 
	
	/** Init function */
	@Override
	public void init(GModel model, Step s, StepButton but) throws Exception {
		super.init (model, s, but);
	
	}

	/**
	 * Extension dynamic compatibility mechanism.
	 * This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith (Object referent) {
		try {
			if (!SVSimple.matchWith (referent)) {return false;}
			GModel m = (GModel) referent;
			//if (!(m instanceof SapModel)) {return false;}	// fc : not necessary to restrict to SapModel

		} catch (Exception e) {
			Log.println (Log.ERROR, "SVSapin.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
		return true;
	}



	/**
	 * Method to draw a GCell within this viewer.
	 */
/*	public void drawCell (Graphics2D g2, GCell gcell, Rectangle.Double r) {

		// compute gray scales
		SquareCell cell = (SquareCell) gcell;

	}
*/

	/**
	 * Method to draw a GMaddTree within this viewer.
	 */
	public void drawTree (Graphics2D g2, Tree t, Rectangle.Double r) {
		// Marked trees are considered dead by generic tools -> don't draw
		if (t.isMarked ()) {return;}
		
		Spatialized s = (Spatialized) t;	// fc - 10.4.2008

		double width = 0.1;		// 10 cm.
		double x = s.getX ();
		double y = s.getY ();
		
		//Tree tree = (SapTree) t;
		
		// 1. Draw the trunk
		if (((SVSimpleSettings) settings).showDiameters) {	// trunk diam
			width = t.getDbh ()/100;
			if (width < visibleThreshold) {width = visibleThreshold;}
			g2.setColor (getTreeColor ());
			
			Shape sh = new Ellipse2D.Double (x-width/2, y-width/2, width, width);
			Rectangle2D bBox = sh.getBounds2D ();
			if (r.intersects (bBox)) {g2.fill (sh);}
			
		} else if (r.contains (new java.awt.geom.Point2D.Double (x, y))) {	// single point
			Rectangle2D.Double p = new Rectangle2D.Double 
					(x, y, visibleThreshold, visibleThreshold);	// visibleThreshold is protected var
			g2.setColor(Color.BLACK);
			g2.fill (p);
			
		}
		
	}

}

