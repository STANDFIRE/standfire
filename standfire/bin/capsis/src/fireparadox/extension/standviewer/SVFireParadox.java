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

package fireparadox.extension.standviewer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import jeeb.lib.defaulttype.SimpleCrownDescription;
import jeeb.lib.defaulttype.TreeWithCrownProfile;
import jeeb.lib.sketch.scene.item.Polygon;
import jeeb.lib.util.Log;
import jeeb.lib.util.RGBManager;
import jeeb.lib.util.Spatialized;
import jeeb.lib.util.Translator;
import jeeb.lib.util.Vertex3d;
import capsis.commongui.projectmanager.StepButton;
import capsis.commongui.util.Tools;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeList;
import capsis.extension.standviewer.SVSimple;
import capsis.extension.standviewer.SVSimpleSettings;
import capsis.kernel.Cell;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Plot;
import capsis.kernel.Step;
import capsis.lib.fire.fuelitem.FiLayerSet;
import capsis.lib.fire.fuelitem.FiPlant;
import capsis.util.Grouper;
import capsis.util.GrouperManager;
import fireparadox.model.FmInitialParameters;
import fireparadox.model.FmModel;
import fireparadox.model.FmPlot;
import fireparadox.model.FmStand;
import fireparadox.model.plant.FmPlant;

/**	SVFireParadox is a cartography simple viewer for the FireParadox module.
*	It's based on SVSimple.
*	@author F. de Coligny - september 2008
*/
public class SVFireParadox extends SVSimple {
	static public String AUTHOR = "F. de Coligny";
	static public String VERSION = "1.1";
	
	public static final int MARGIN = 20;

	static {
		Translator.addBundle("fireparadox.extension.standviewer.SVFireParadox");
	}

	// FiLayerSets and Polygons are stored here for selection
	// Key is the polygon shape, value is the polygon / FiLayerSet
	private Map<Shape,Polygon> polygons;


	/** Init function */
	@Override
	public void init(GModel model, Step s, StepButton but) throws Exception {
		super.init (model, s, but);
	
		polygons = new HashMap<Shape,Polygon> ();

	}

	/**	Extension dynamic compatibility mechanism.
	*	This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	*/
	static public boolean matchWith (Object referent) {
		try {
			if (!SVSimple.matchWith (referent)) {return false;}
			GModel m = (GModel) referent;
			if (!(m instanceof FmModel)) {return false;}

		} catch (Exception e) {
			Log.println (Log.ERROR, "SVFireParadox.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
		return true;
	}




	/**	From Drawer interface.
	*	This method draws in the Panel2D each time the latter must be
	*	repainted.
	*	The given Rectangle is the sub-part (zoom) of the stand to draw in user
	*	coordinates (i.e. meters...). It can be used in preprocesses to avoid
	*	drawing invisible trees or cells.
	*/
	@Override
	public void draw (Graphics g, Rectangle.Double r) {

		Graphics2D g2 = (Graphics2D) g;

		// 1. Cast settings for ease
		SVSimpleSettings set = settings;
		polygons.clear ();

		// 1.1. Optionally draw before
		drawBefore (g2, r, stand);	// fc - 7.4.2005

		// 2. Choose a pixel detailThreshold, compute it in meters with current scale
		// if dbh in m. >= detailThreshold -> detailled level is reached
		visibleThreshold = 1.1 / panel2D.getCurrentScale ().x;	// 1 pixel in in meters

		// 3. Consider optional status and grouper
		//
		TreeList gtcstand = (TreeList) stand;
		Collection treesInStatus = gtcstand.getTrees ();
		if (statusSelection != null) {		// fc - 23.4.2004
			treesInStatus = gtcstand.getTrees (statusSelection);
		}
		HashSet treesInGroup = null;
		if (set.grouperMode) {
			String name = set.grouperName;
			GrouperManager gm = GrouperManager.getInstance ();
			Grouper gr = gm.getGrouper (name);	// return null if not found
			Collection aux = gr.apply (treesInStatus, name.toLowerCase ().startsWith ("not "));
			treesInGroup = new HashSet (aux);
		} else {
			treesInGroup = new HashSet (treesInStatus);
		}


		// 4.1 Draw the cells
		if (stand.hasPlot ()) {
			Plot plot = stand.getPlot ();
			Collection<Cell> level1 = plot.getCellsAtLevel (1);
			for (Cell c: level1) {
				drawCell (g2, c, r);
			}
		}
		
		// 4.2 Draw the layerSets before the trees
		// sort in ascending height
		FmStand std = (FmStand) stand;
		if (std.getLayerSets () != null) {
			List<FiLayerSet> copy = new ArrayList<FiLayerSet> (std.getLayerSets ());
			Collections.sort (copy, new Comparator<FiLayerSet> () {
				public int compare (FiLayerSet o1, FiLayerSet o2) {
					if (o1.getHeight () < o2.getHeight ()) {
						return -1;
					} else if (o1.getHeight () > o2.getHeight ()) {
						return 1;
					} else {
						return 0;
					}
				}
			});
			for (FiLayerSet ls : copy) {
				Color color = new Color (164, 227, 164);
				if (effectiveSelection != null && effectiveSelection.contains (ls)) {color = Color.RED;}

				Shape shape = drawPolygon (g2, r, ls, color, true);
				if (shape == null) {continue;}

				// Memo the shape + polygon (for selection)
				polygons.put (shape, ls);
			}
		}
		
		
		
		
		
		
		// 5. Plants
		Object[] trees = treesInGroup.toArray ();

		// 5.1 Prepare label drawing strategy
		if (settings.maxLabelNumber <= 0) {
			labelCounter = 1;
			labelFrequency = Integer.MAX_VALUE;
		} else {
			labelCounter = 0;
			labelFrequency = Math.max (1, trees.length / Math.max (1, settings.maxLabelNumber));
		}

		// 5.2 Draw the plants
		boolean enableMagnifyFactor = true;	// fc - 4.3.2005

		for (int i = 0; i < trees.length; i++) {
			Tree t = (Tree) trees[i];
			Spatialized s = (Spatialized) t;

			double dbh = t.getDbh ();

			if (t instanceof SimpleCrownDescription) {enableMagnifyFactor = false;}	// fc - 4.3.2005

			drawTree (g2, t, r);
            selectTreeIfNeeded (g2, t, r);    // fc - 28.5.2003

			// 5.3 May draw tree label according to frequency - fc - 22.12.2003
			// showSomeLabels () : if return false, no labels this way
			// settings.showLabels : user asks for labels
			//
			float x = (float) s.getX ();
			float y = (float) s.getY ();
			if (showSomeLabels () && settings.showLabels
					&& r.contains (new Point.Double (x, y))) {
				drawLabel (g2, String.valueOf (t.getId ()), x, y);
			}
		}

		// If some trees implement SimpleCrownDescription, disable MagnifyFactor
		try {	// fMagnifyFactor may not exist -> null possible -> ignore exception
			fMagnifyFactor.setEnabled (enableMagnifyFactor);	// fc - 4.3.2005
		} catch (Exception e) {}

		// 6. Optionally draw more
		drawMore (g2, r, stand);	// fc + bc - 6.2.2003

		// 7. Ensure title is ok
		defineTitle ();

		// 8. Relative to filtering (see FilteringPanel)
		updateToolTipText ();
	}




	/**	Method to draw a FiPlant.
	*	Only rectangle r is visible (user coordinates) -> do not draw if outside.
	*	If beetle attack, beetle status colors are shown.
	*/
	public void drawTree (Graphics2D g2, Tree tree, Rectangle.Double r) {
		// 1. Marked trees are considered dead by generic tools -> don't draw
		if (tree.isMarked ()) {return;}

		FmPlant p = (FmPlant) tree;

		// 2. Tree location
		double x = p.getX ();
		double y = p.getY ();
		double width = p.getDbh () / 100;	// in m. (we draw in Graphics in m.)
		int mf = magnifyFactor;

		// 3. A detailed view is requested
		if (settings.showDiameters) {

			// 3.1 In some cases, a tree crown can be drawn
			Color crownColor = Tools.getCrownColor (tree);
			
			FmStand stand = (FmStand) p.getScene();
			if (stand.isBeetleAttacked()) {
				crownColor = getBeetleColor (p);
			}
			
			double crownRadius = 0;
			
			if (tree instanceof SimpleCrownDescription) {
				mf = 1;	// when crown is drawn, trunk is not magnified
				SimpleCrownDescription data = (SimpleCrownDescription) tree;
				crownRadius = data.getCrownRadius ();
			}
			if (tree instanceof TreeWithCrownProfile) {
				mf = 1;	// when crown is drawn, trunk is not magnified
				TreeWithCrownProfile data = (TreeWithCrownProfile) tree;
				crownRadius = data.getCrownRadius ();
			}

			double d = crownRadius*2;	// crown diameter

			// 3.1.1 Crown diameter is less than 1 pixel : draw 1 pixel
			if (d <= visibleThreshold) {
				if (r.contains (new java.awt.geom.Point2D.Double (x, y))) {
					Rectangle2D.Double k = new Rectangle2D.Double
							(x, y, visibleThreshold, visibleThreshold);	// fc - 15.12.2003 (bug by PhD)
					g2.setColor (crownColor);
					g2.fill (k);
				}

			// 3.1.2 Bigger than 1 pixel : fill a circle
			//
			} else {
				Shape sh = new Ellipse2D.Double (x-crownRadius, y-crownRadius, d, d);
				Rectangle2D bBox = sh.getBounds2D ();
				if (r.intersects (bBox)) {
					g2.setColor (crownColor);
					g2.fill (sh);
					g2.setColor (crownColor.darker());
					g2.draw (sh);
				}
			}
				
		}

		width = width * mf;	// in m. (we draw in Graphics in m.)
		width = Math.max (visibleThreshold, width);	// fc - 18.11.2004

		// Draw the trunk : always and after the crown : visible
		if (r.contains (new java.awt.geom.Point2D.Double (x, y))) {
			Shape s = new Ellipse2D.Double (
					x-width/2, y-width/2, width, width);	// fc - 18.11.2004
			g2.setColor (getTreeColor ());
			g2.fill (s);
		}
	}

	/**	Beetle status / R. Parsons
	 *	0: no attack, green, 1: attacked, yellow 50% moisture, 2: red, all of the
	 *	fuel 15% moisture 3: orange, half of the fuel 15% moisture 4: brown,
	 *	dead, no fuel
	 */
	private Color getBeetleColor (FmPlant p) {
		Color c = null;
		if (p.getBeetleStatus() == 0) {
			c = new Color (59, 187, 71);
		} else if (p.getBeetleStatus() == 1) {
			c = Color.YELLOW;
		} else if (p.getBeetleStatus() == 2) {
			c = Color.RED;
		} else if (p.getBeetleStatus() == 3) {
			c = new Color (205, 135, 41);
		} else if (p.getBeetleStatus() == 4) {
			c = new Color (149, 129, 97);
		} else {
			c = Color.BLACK;  // trouble
		}
		
		// Trick: write the color in the plant, some other viewers may use it
		int[] rgb = RGBManager.toRGB(c);
		p.setRGB(rgb);
		
		return c;
	}

////////


	/**	Draw more: scale, polygons...
	*/
	@Override
	protected void drawMore (Graphics2D g2, Rectangle.Double r, GScene stand) {

		// Draw scale
		Vertex3d origin = stand.getOrigin ();
		FmInitialParameters s = (FmInitialParameters) stand.getStep ().getProject ().getModel ().getSettings ();
		double cellWidth = s.cellWidth;
		double x = origin.x;
		//~ double y = origin.y - cellWidth;	// scale under the drawing
		double y = origin.y + stand.getYSize () + cellWidth/3;	// scale above the drawing (better) - fc
		double x1 = x+cellWidth;
		double y1 = y;
		g2.setColor (Color.BLACK);
		Shape scale = new Line2D.Double (x, y, x1, y1);
		double h = cellWidth / 10d;
		double y2 = y+h/2d;
		double y3 = y-h/2d;
		Shape border1 = new Line2D.Double (x, y2, x, y3);
		Shape border2 = new Line2D.Double (x1, y2, x1, y3);
		g2.draw (scale);
		g2.draw (border1);
		g2.draw (border2);
		g2.drawString (""+cellWidth+" m", (float)x, (float)y);

		// Draw the polygons in plot
		FmPlot plot = (FmPlot) stand.getPlot ();
		for (Polygon p : plot.getPolygons ()) {
			Color color = Color.GRAY;
			if (effectiveSelection != null && effectiveSelection.contains (p)) {color = Color.RED;}

			Shape shape = drawPolygon (g2, r, p, color, false);
			if (shape == null) {continue;}

			// Memo the shape + polygon (for selection)
			polygons.put (shape, p);
		}

	}

	// Draws a polygon with the given color
	// If trouble, returns null
	private Shape drawPolygon (Graphics2D g2, Rectangle.Double r, Polygon p, Color color, boolean filled) {
		if (p == null) {return null;}

		//~ System.out.println ("SVFireParadox, "+p.toString ());

		Collection<Vertex3d> v3s = p.getVertices ();
		if (v3s == null || v3s.size () < 3) {return null;}

		Color memo = g2.getColor ();
		g2.setColor (color);


		Vertex3d vFirst = null;
		GeneralPath path = new GeneralPath ();
		for (Vertex3d v3 : v3s) {
			if (vFirst == null) {
				vFirst = v3;	// memo first vertex
				path.moveTo (v3.x, v3.y);
			} else {
				path.lineTo (v3.x, v3.y);
			}
		}
		if (p.isClosed ()) {
			path.lineTo (vFirst.x, vFirst.y);
		}

		Rectangle2D bBox = path.getBounds2D ();
		if (!r.intersects (bBox)) {return null;}	// polygon is not visible (maybe zoom...)

		if (filled) {
			g2.fill (path);
			g2.setColor (color.darker ());
			g2.draw (path);
		} else {
			g2.draw (path);
		}

		g2.setColor (memo);

		return path;
	}


	/**	Here, we can select things other then trees and cells :
	*	add them to the currentSelection (which was prepared by select () :
	*	already possibly contains trees and cells).
	*	Note: do not alter currentSelection, just add things to it.
	*	We add polygons or FiLayerSets if they intersect the given rectangle.
	*/
	@Override
	protected void selectMore (Rectangle.Double r, boolean ctrlIsDown, Collection currentSelection) {
		for (Shape shape : polygons.keySet ()) {
			if (shape.intersects (r)) {
				currentSelection.add (polygons.get (shape));
			}
		}
//~ System.out.println ("SVFiParadox: currentSelection="+Tools.toString (currentSelection));
	}


}

