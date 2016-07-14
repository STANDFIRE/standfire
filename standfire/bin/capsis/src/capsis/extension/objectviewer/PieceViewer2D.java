/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2001  Francois de Coligny
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package capsis.extension.objectviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jeeb.lib.util.Disposable;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import repicea.simulation.treelogger.LoggableTree;
import capsis.commongui.util.Helper;
import capsis.commongui.util.Tools;
import capsis.defaulttype.Tree;
import capsis.extension.AbstractObjectViewer;
import capsis.extension.treelogger.GPiece;
import capsis.extension.treelogger.GPieceDisc;
import capsis.extension.treelogger.geolog.GeoLog;
import capsis.kernel.MethodProvider;
import capsis.kernel.Project;
import capsis.kernel.Step;
import capsis.util.Drawer;
import capsis.util.Panel2D;
import capsis.util.methodprovider.TreeRadius_cmProvider;

/**
 * PieceViewer2D : simple 2D GPiece viewer. Also matches with GTree (displayed
 * using a fake GPiece) (modified from FgTreeTaper)
 * 
 * @author F. Mothe - march 2006
 */

// TODO : remove special treatment for Pp3 after correction of
// Pp3MethodProvider.getTreeRadius_cm () which returns NaN if h=0

public class PieceViewer2D extends AbstractObjectViewer implements Drawer, ActionListener, Disposable {

	static {
		Translator.addBundle ("capsis.extension.objectviewer.PieceViewer2D");
	}
	static public final String NAME = Translator.swap ("PieceViewer2D");
	static public final String DESCRIPTION = Translator.swap ("PieceViewer2D.description");
	static public final String AUTHOR = "F. Mothe";
	static public final String VERSION = "1.1";

	private static final long serialVersionUID = 20060318L; // avoid java
															// warning

	public static final int X_MARGIN_IN_PIXELS = 20;
	public static final int Y_MARGIN_IN_PIXELS = 20;

	private double userWidth;
	private double userHeight;
	private double pixelWidth;
	private double pixelHeight;

	private static final boolean hasSapWood = true; // always drawn
	private boolean hasHeartWood;
	private boolean hasBark;
	private boolean hasKnottyCore;
	private boolean hasFirstDeadBranch;
	private boolean hasJuvenileWood;

	// Default options :
	// (used only for initilisation, updated when the user changes the
	// checkboxes)
	private static boolean withPieceNameDefault = true;
	private static boolean withLegendDefault = true;

	private JButton help;
	private JCheckBox withPieceName;
	private JCheckBox withLegend;

	private Collection pieces;
	private boolean isFakePiece;
	private double yBase;

	// private static final Color colorBark = Color.black;
	private static final Color colorDiscs = Color.black;
	private static final Color colorSapWood = Color.yellow;
	private static final Color colorHeartWood = Color.orange;
	private static final Color colorWoodRings = colorHeartWood.darker ();
	private static final Color colorBark = colorWoodRings.darker ();
	private static final Color colorKnottyCore = Color.red;
	private static final Color colorFirstDeadBranch = Color.blue;
	private static final Color colorJuvenileWood = Color.green;
	// private static final Color colorJuvenileWood = new Color (0.0f, 1.0f,
	// 0.0f, 0.5f);

	// ~ private SelectionSource source; // fc - 8.2.2008

	private JScrollPane scroll; // fc - 8.2.2008 - panel2D is inside
	private Panel2D panel2D;

	/**
	 * Default constructor. 
	 */
	public PieceViewer2D () {
	}

	public void init (Collection s) throws Exception {

		try {
			// fc - 8.2.2008 - Selection listeners
			// ~ source = s.getSelectionSource ();
			// ~ source.addSelectionListener (this);
			// fc - 8.2.2008 - Selection listeners

			hasHeartWood = false;
			hasBark = false;
			hasKnottyCore = false;
			hasFirstDeadBranch = false;
			hasJuvenileWood = false;
			// Not usable before draw () :
			pixelWidth = 0;
			pixelHeight = 0;
			isFakePiece = false;

			createUI ();

			// fc - 12.9.2008
			show (s);

		} catch (Exception exc) {
			Log.println (Log.ERROR, "PieceViewer2D ()", exc.toString (), exc);
			// System.out.println ("Erreur dans PieceViewer2D () : " +
			// exc.toString () + exc);
			throw exc; // fc - 4.11.2003 - object viewers may throw exception
		}
	}

	/**
	 * Extension dynamic compatibility mechanism. This matchwith method checks
	 * if the extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith (Object referent) {
		try {
			// referent is now always a Collection - fc - 8.2.2008
			Collection c = (Collection) referent;
			if (c.isEmpty ()) { return false; }

			Collection reps = Tools.getRepresentatives (c); // reps: one
															// instance of each
															// class in subjects
			if (reps.size () != 1) { return false; } // several classes or null

			Object obj = reps.iterator ().next ();
			if (obj instanceof GPiece) {
				return true;
			} else if (obj instanceof Tree && obj instanceof LoggableTree) {
				Tree tree = (Tree) obj;
				MethodProvider mp = tree.getScene ().getStep ().getProject ().getModel ().getMethodProvider ();
				if (mp instanceof TreeRadius_cmProvider) { return true; }
			}
		} catch (Exception e) {
			Log.println (Log.WARNING, "PieceViewer2D.matchWith ()", "Error in matchWith () (returned false)", e);
		}
		return false;

	}

	// Calculate the size extension of the panel2D to view the complete
	// selected scene
	// Needs initializeScale () to be called before for userWidth and userHeight
	// - fc - 8.2.2008
	//
	private void calculatePanel2D (Collection pieces) {
		// 1. tree drawing
		Rectangle.Double r2 = new Rectangle.Double (0, 0, userWidth, userHeight); // x,
																					// y,
																					// w,
																					// h

		panel2D = new Panel2D (this, // when repaint needed, panel2D will call
										// this.draw ()
				r2, X_MARGIN_IN_PIXELS, Y_MARGIN_IN_PIXELS, true);
		scroll.getViewport ().setView (panel2D);
	}

	// fc - 8.2.2008 - OVs now always manage a Collection
	private Collection extractPieces (Collection subjects) throws Exception {
		Collection result = null; // a Collection of pieces
		Collection reps = Tools.getRepresentatives (subjects); // reps: one
																// instance of
																// each class in
																// subjects

		// [Note : matchWith () was not called with subjects]

		// Only instances of GPiece in the collection: return all of them
		if (reps.size () == 1 && reps.iterator ().next () instanceof GPiece) {
			result = subjects;

			// Only GTree(s) in the collection: make a GPiece with the first one
		} else if (subjects.size () >= 1 && subjects.iterator ().next () instanceof Tree) {
			isFakePiece = true;
			Tree tree = (Tree) subjects.iterator ().next ();
			
			// ~ GPiece piece = GeoLog.makeFakePiece (0, tree, 0, tree.getHeight
			// (), .01);
			// Tempo for Pp3 base :
			double hmin = 0.0;
			try {
				if (tree instanceof pp3.model.Pp3Tree) {
					hmin = 0.01;
				}
			} catch (Error e) {}
			
			Step refStep = ((Tree) tree).getScene().getStep();
			Project scenario = refStep.getProject();
			Collection <Step> stepsFromRoot = scenario.getStepsFromRoot(refStep);
			TreeRadius_cmProvider mp = (TreeRadius_cmProvider) scenario.getModel().getMethodProvider();
			GPiece piece = new GeoLog().makeFakePiece((LoggableTree) tree, 
					0, 
					stepsFromRoot, 
					mp, 
					null,
					hmin, 
					tree.getHeight(), 
					.01);
//			GPiece piece = new GeoLog().makeFakePiece ((LoggableTree) tree, 0, hmin, tree.getHeight (), .01);
			ArrayList<GPiece> array = new ArrayList<GPiece> ();
			array.add (piece);
			result = array;
		} else {
			// should not occur:
			throw new Exception (Translator.swap ("PieceViewer2D.DoesNotMatch"));
		}
		return result;
	}

	private void resetPanel2D () {
		if (panel2D != null) {
			panel2D.reset ();
			panel2D.repaint ();
		}
	}

	/**
	 * Disposable
	 */
	public void dispose () {
		// ~ try {
		// ~ source.removeSelectionListener (this);
		// ~ } catch (Exception e) {} // does not matter very much
	}

	/**
	 * From ActionListener interface.
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		} else if (evt.getSource ().equals (withPieceName) || evt.getSource ().equals (withLegend)) {
			updateDefaultOptionsAndRedraw ();
		}
	}

	private void updateDefaultOptionsAndRedraw () {
		withPieceNameDefault = withPieceName.isSelected ();
		withLegendDefault = withLegend.isSelected ();
		panel2D.reset ();
		panel2D.repaint ();
	}

	private void initialiseScale () {
		double h0Min_m = 0;
		double h1Max_m = 0;
		double ryMax_mm = 0;
		boolean first = true;
		for (Object o : pieces) {
			GPiece piece = (GPiece) o;
			GPieceDisc disc0 = piece.getBottomDisc();
			GPieceDisc disc1 = piece.getTopDisc();

			double h0_m = disc0.getHeight_m(); //PieceUtil.getHeight_m (disc0);
			double h1_m = disc1.getHeight_m(); //PieceUtil.getHeight_m (disc1);
			double ry0_mm = disc0.getRadius_mm (true); // overbark
			if (Double.isNaN (ry0_mm)) {
				ry0_mm = 0;
			} // Tempo for Pp3 base

			if (first) {
				first = false;
				h0Min_m = h0_m;
				h1Max_m = h1_m;
				ryMax_mm = ry0_mm;
			} else {
				if (h0_m < h0Min_m) h0Min_m = h0_m;
				if (h1_m > h1Max_m) h1Max_m = h1_m;
				// if (ry0_mm > ryMax_mm)
				// ryMax_mm = ry0_mm;
			}

			// Search for the largest radius (may be not the lowest one)
//			for (GPieceDisc disc : PieceUtil.getDiscs (piece)) {
			for (GPieceDisc disc : piece.getDiscs()) {
				double ry = disc.getRadius_mm(true); // overbark
				if (Double.isNaN (ry)) { // Tempo for Pp3 base
					System.out.println ("PieceViewer2D.initialiseScale () : Rayon infini � h = "
							+ disc.getHeight_m() + "!");
//							+ PieceUtil.getHeight_m (disc) + "!");
					ry = 0;
				}
				if (ry > ryMax_mm) {
					ryMax_mm = ry;
				}
			}

		}
		double lg_m = h1Max_m - h0Min_m;
		userWidth = ryMax_mm * 1.2;
		// userHeight = lg_m * 1.2;
		userHeight = lg_m * 1.05;
		if (userWidth == 0.0) userWidth = 10.0;
		if (userHeight == 0.0) userHeight = 5.0;
		yBase = h0Min_m;

	}

	/**
	 * From Drawer interface. This method draws in the Panel2D each time this
	 * one must be repainted. The given Rectangle is the sub-part of the object
	 * to draw (zoom) in user coordinates (i.e. meters...). It can be used in
	 * preprocesses to avoid drawing invisible parts.
	 */
	public void draw (Graphics g, Rectangle.Double r) {

		pixelWidth = panel2D.getUserWidth (1);
		pixelHeight = panel2D.getUserHeight (1);

		Graphics2D g2 = (Graphics2D) g;
		// g2.setStroke (new BasicStroke (pixelHeight * 3));
		for (Object o : pieces) {
			GPiece piece = (GPiece) o;
			drawPiece (g2, piece);
		}
		drawAxis (g2);
		if (withLegend.isSelected ()) {
			drawLegend (g2);
		}

	}

	private void drawPiece (Graphics2D g2, GPiece piece) {
		if (piece.hasHeartWood()) {
			hasHeartWood = true;
			fillHeartWood (g2, piece);
			fillSapWood (g2, piece);
			// drawHeartWoodLimit (g2, piece);
		} else {
			fillWood (g2, piece);
		}
		if (piece.isWithBark()) {
			hasBark = true;
			fillBark (g2, piece);
		}
		drawWoodRings (g2, piece);
		if (piece.hasJuvenileWood()) {
			hasJuvenileWood = true;
			// fillJuvenileWood (g2, piece);
			drawJuvenileWoodLimit (g2, piece);
		}
		if (piece.hasFirstDeadBranch()) {
			// To be drawn before knotty core !
			hasFirstDeadBranch = true;
			drawFirstDeadBranchLimit (g2, piece);
		}
		if (piece.hasKnottyCore()) {
			hasKnottyCore = true;
			drawKnottyCoreLimit (g2, piece);
		}
		drawTopBottomDiscs (g2, piece);
	}

	// Draws a polygon (or polyline) :
	// (because Polygon class does not work with double)
	public static void drawPolygon (Graphics2D g2, Vector<Point2D.Double> points, boolean drawLines, boolean fillPoly) {
		int nbPts = points.size ();
		// System.out.println ("drawPolygon : " + points);

		if (nbPts > 0) { // VERIFY : is 1 valid ?
			// Polygon poly = new Polygon (); need integer coordinates !
			GeneralPath poly = new GeneralPath ();
			if (drawLines) {
				boolean first = true;
				for (Point2D.Double p : points) {
					if (first) {
						first = false;
						poly.moveTo (p.x, p.y);
					} else {
						poly.lineTo (p.x, p.y);
					}
				}
			} else {
				for (Point2D.Double p : points) {
					poly.moveTo (p.x, p.y);
					poly.lineTo (p.x, p.y);
				}
			}
			if (fillPoly) {
				g2.fill (poly);
			} else {
				g2.draw (poly);
			}
		}
	}

	// Draw and fill a polygon :
	public static void fillPolygon (Graphics2D g2, Vector<Point2D.Double> points) {
		drawPolygon (g2, points, true, true);
	}

	// Draw an empty polygon :
	public static void drawPolyLine (Graphics2D g2, Vector<Point2D.Double> points) {
		drawPolygon (g2, points, true, false);
	}

	// Draw vertices of a polygon :
	public static void drawPolyPoints (Graphics2D g2, Vector<Point2D.Double> points) {
		drawPolygon (g2, points, false, false);
	}

	// Generic method for reversing elements in vector from
	// firstIndex to last index :
	//
	// NOTE - fc - 12.9.2008 - have a look at Collections.reverseOrder ()
	//
	public static <T> void reverseEnd (Vector<T> vector, int firstIndex) {
		int size = vector.size ();
		for (int i = firstIndex; i < size; i++) {
			int j = size - 1 - i + firstIndex;
			if (i >= j) break;
			Collections.swap (vector, i, j);
		}
	}

	private void addHeartWoodLimit (GPiece piece, Vector<Point2D.Double> points, boolean reverse) {
		int index = points.size ();
//		for (GPieceDisc disc : PieceUtil.getDiscs (piece)) {
		for (GPieceDisc disc : piece.getDiscs()) {
			double x = disc.getHeartWoodRadius_mm();
//			double y = PieceUtil.getHeight_m (disc) - yBase;
			double y = disc.getHeight_m() - yBase;
			points.add (new Point2D.Double (x, y));
		}
		if (reverse) {
			reverseEnd (points, index);
		}
	}

	private void addKnottyCoreLimit (GPiece piece, Vector<Point2D.Double> points, boolean reverse) {
		int index = points.size ();
//		for (GPieceDisc disc : PieceUtil.getDiscs (piece)) {
		for (GPieceDisc disc : piece.getDiscs()) {
			double x = disc.getKnottyCoreRadius_mm();
//			double y = PieceUtil.getHeight_m (disc) - yBase;
			double y = disc.getHeight_m() - yBase;
			points.add (new Point2D.Double (x, y));
		}
		if (reverse) {
			reverseEnd (points, index);
		}
	}

	private void addFirstDeadBranchLimit (GPiece piece, Vector<Point2D.Double> points, boolean reverse) {
		int index = points.size ();
//		for (GPieceDisc disc : PieceUtil.getDiscs (piece)) {
		for (GPieceDisc disc : piece.getDiscs()) {
			double x = disc.getFirstDeadBranchRadius_mm();
//			double y = PieceUtil.getHeight_m (disc) - yBase;
			double y = disc.getHeight_m() - yBase;
			points.add (new Point2D.Double (x, y));
		}
		if (reverse) {
			reverseEnd (points, index);
		}
	}

	private void addJuvenileWoodLimit (GPiece piece, Vector<Point2D.Double> points, boolean reverse) {
		int index = points.size ();
//		for (GPieceDisc disc : PieceUtil.getDiscs (piece)) {
		for (GPieceDisc disc : piece.getDiscs()) {
			double x = disc.getJuvenileWoodRadius_mm();
//			double y = PieceUtil.getHeight_m (disc) - yBase;
			double y = disc.getHeight_m() - yBase;
			points.add (new Point2D.Double (x, y));
		}
		if (reverse) {
			reverseEnd (points, index);
		}
	}

	private void addPithLimit (GPiece piece, Vector<Point2D.Double> points, boolean reverse) {
		int index = points.size ();
//		for (GPieceDisc disc : PieceUtil.getDiscs (piece)) {
		for (GPieceDisc disc : piece.getDiscs()) {
			double x = disc.getPithWidth_mm();
			double y = disc.getHeight_m() - yBase;
//			double y = PieceUtil.getHeight_m (disc) - yBase;
			points.add (new Point2D.Double (x, y));
		}
		if (reverse) {
			reverseEnd (points, index);
		}
	}

	private void addRingLimit (GPiece piece, Vector<Point2D.Double> points, int cambialAge, boolean reverse) {
		int index = points.size ();
//		for (GPieceDisc disc : PieceUtil.getDiscs (piece)) {
		for (GPieceDisc disc : piece.getDiscs()) {
			boolean end = cambialAge > disc.getNbWoodRings();
			double x = end ? 0.0 : disc.getExtRadius_mm (cambialAge);
			double y = disc.getHeight_m() - yBase;
//			double y = PieceUtil.getHeight_m (disc) - yBase;
			points.add (new Point2D.Double (x, y));
		}
		if (reverse) {
			reverseEnd (points, index);
		}
	}

	private void fillHeartWood (Graphics2D g2, GPiece piece) {
		g2.setColor (colorHeartWood);

		int nbDiscs = piece.getNumberOfDiscs();
		Vector<Point2D.Double> points = new Vector<Point2D.Double> (2 * nbDiscs); // reserve
																					// memory
																					// only
		addHeartWoodLimit (piece, points, false);
		addPithLimit (piece, points, true);
		fillPolygon (g2, points);
	}

	private void fillSapWood (Graphics2D g2, GPiece piece) {
		g2.setColor (colorSapWood);

		int nbDiscs = piece.getNumberOfDiscs();
		Vector<Point2D.Double> points = new Vector<Point2D.Double> (2 * nbDiscs); // reserve
																					// memory
																					// only
		addHeartWoodLimit (piece, points, false);
		addRingLimit (piece, points, 1, true);
		fillPolygon (g2, points);
	}

	private void fillWood (Graphics2D g2, GPiece piece) {
		g2.setColor (colorSapWood);

		int nbDiscs = piece.getNumberOfDiscs();
		Vector<Point2D.Double> points = new Vector<Point2D.Double> (2 * nbDiscs); // reserve
																					// memory
																					// only
		addRingLimit (piece, points, 1, false);
		addPithLimit (piece, points, true);
		fillPolygon (g2, points);
	}

	private void fillJuvenileWood (Graphics2D g2, GPiece piece) {
		g2.setColor (colorJuvenileWood);

		int nbDiscs = piece.getNumberOfDiscs();
		Vector<Point2D.Double> points = new Vector<Point2D.Double> (2 * nbDiscs); // reserve
																					// memory
																					// only
		addJuvenileWoodLimit (piece, points, false);
		addPithLimit (piece, points, true);
		fillPolygon (g2, points);
	}

	private void fillBark (Graphics2D g2, GPiece piece) {
		g2.setColor (colorBark);
		int nbDiscs = piece.getNumberOfDiscs();
		Vector<Point2D.Double> points = new Vector<Point2D.Double> (2 * nbDiscs); // reserve
																					// memory
																					// only
		addRingLimit (piece, points, 0, false);
		addRingLimit (piece, points, 1, true);
		fillPolygon (g2, points);
	}

	private void drawOneRing (Graphics2D g2, GPiece piece, int cambialAge) {
		int nbDiscs = piece.getNumberOfDiscs();
		Vector<Point2D.Double> points = new Vector<Point2D.Double> (nbDiscs); // reserve
																				// memory
																				// only
		addRingLimit (piece, points, cambialAge, false);
		drawPolyLine (g2, points);
	}

	private void drawHeartWoodLimit (Graphics2D g2, GPiece piece) {
		g2.setColor (colorHeartWood);
		int nbDiscs = piece.getNumberOfDiscs();
		Vector<Point2D.Double> points = new Vector<Point2D.Double> (nbDiscs); // reserve
																				// memory
																				// only
		addHeartWoodLimit (piece, points, false);
		drawPolyLine (g2, points);
	}

	private void drawKnottyCoreLimit (Graphics2D g2, GPiece piece) {
		g2.setColor (colorKnottyCore);
		int nbDiscs = piece.getNumberOfDiscs();
		Vector<Point2D.Double> points = new Vector<Point2D.Double> (nbDiscs); // reserve
																				// memory
																				// only
		addKnottyCoreLimit (piece, points, false);
		drawPolyLine (g2, points);
	}

	private void drawFirstDeadBranchLimit (Graphics2D g2, GPiece piece) {
		g2.setColor (colorFirstDeadBranch);
		int nbDiscs = piece.getNumberOfDiscs();
		Vector<Point2D.Double> points = new Vector<Point2D.Double> (nbDiscs); // reserve
																				// memory
																				// only
		addFirstDeadBranchLimit (piece, points, false);
		drawPolyLine (g2, points);
	}

	private void drawJuvenileWoodLimit (Graphics2D g2, GPiece piece) {
		g2.setColor (colorJuvenileWood);
		int nbDiscs = piece.getNumberOfDiscs();
		Vector<Point2D.Double> points = new Vector<Point2D.Double> (nbDiscs); // reserve
																				// memory
																				// only
		addJuvenileWoodLimit (piece, points, false);
		drawPolyLine (g2, points);
	}

	private void drawBark (Graphics2D g2, GPiece piece) {
		g2.setColor (colorBark);
		drawOneRing (g2, piece, 0);
	}

	private void drawWoodRings (Graphics2D g2, GPiece piece) {
		GPieceDisc disc0 = piece.getBottomDisc();
		double rw0 = disc0.getRingWidth_mm();
		// System.out.println ("rw0=" + rw0 + " pw=" + pixelWidth);
		if (rw0 > 2.5 * pixelWidth) {
			g2.setColor (colorWoodRings);
			int CambialAgeMax = disc0.getNbWoodRings();
			for (int cambialAge = 1; cambialAge <= CambialAgeMax; cambialAge++) {
				drawOneRing (g2, piece, cambialAge);
			}
		}

		// ~ System.out.println ("cerne" + ", " + "h" + ", " + "rInt" + ", " +
		// "rExt" + ", " + "lgc" + ", " + "dur" + ", " + "id");
		// ~ int CambialAgeMax = PieceUtil.getNbWoodRings (disc0);
		// ~ int nbDiscs = PieceUtil.getNbDiscs (piece);
		// ~ for (int cambialAge = 1; cambialAge<= CambialAgeMax; cambialAge +=
		// 25) {
		// ~ for (GPieceDisc disc : PieceUtil.getDiscs (piece)) {
		// ~ int pithAge = PieceUtil.getAgeFromPith (disc, cambialAge);
		// ~ boolean duramen = PieceUtil.isHeartWood (disc, cambialAge);
		// ~ boolean end = cambialAge > PieceUtil.getNbWoodRings (disc);
		// ~ double rInt = end ? 0.0 : PieceUtil.getIntRadius_mm (disc,
		// cambialAge);
		// ~ double rExt = end ? 0.0 : PieceUtil.getExtRadius_mm (disc,
		// cambialAge);
		// ~ double lgc = rExt - rInt;
		// ~ double id = fagacees.util.FgOakAnnex.getWoodBasicDensity_kgpm3 (
		// ~ pithAge, lgc, duramen);
		// ~ double h = PieceUtil.getHeight_m (disc);
		// ~ int dur = duramen ? 1 : 0;
		// ~ System.out.println (cambialAge + ", " + h + ", " + rInt + ", " +
		// rExt + ", " + lgc + ", " + dur + ", " + id);
		// ~ if (end) {
		// ~ break;
		// ~ }
		// ~ }
		// ~ }
		// ~ System.exit (0);
	}

	private void drawTopBottomDiscs (Graphics2D g2, GPiece piece) {
		Stroke oriStroke = g2.getStroke ();
		// g2.setStroke (new BasicStroke ((float) pixelHeight * 3));

		g2.setColor (colorDiscs);
		GPieceDisc disc0 = piece.getBottomDisc();
		GPieceDisc disc1 = piece.getTopDisc();
		double x1 = disc0.getRadius_mm(true); // overbark
		if (Double.isNaN (x1)) {
			x1 = 0;
		} // Tempo for Pp3 base
//		double y1 = PieceUtil.getHeight_m (disc0) - yBase;
		double y1 = disc0.getHeight_m() - yBase;
		double x2 = disc1.getRadius_mm(true); // overbark
		if (Double.isNaN (x2)) {
			x2 = 0;
		} // Tempo for Pp3 base
//		double y2 = PieceUtil.getHeight_m (disc1) - yBase;
		double y2 = disc1.getHeight_m() - yBase;
		g2.draw (new Line2D.Double (0, y1, x1, y1));
		g2.draw (new Line2D.Double (0, y2, x2, y2));
		g2.setStroke (oriStroke);

		if (withPieceName.isSelected ()) {
			// name = "pieceProduct" (= tree name for a fake piece)
			// full name = "rankInTree" + "pieceProduct" | "[pieceOrigin]"
			String name = isFakePiece ? piece.getLogCategory().getName() : piece.getFullName();
			// System.out.println ("nom=\"" + name + "\"");
			double y = (y1 + y2) / 2; // - pixelHeight * 5;
			double h = y + yBase;
			double r = piece.getRadius_mm (h, true); // overbark
			double x = r + pixelWidth * 3;
			g2.drawString (name, (float) x, (float) y);
		}
	}

	private void drawAxis (Graphics2D g2) {
		// To draw the axis
		g2.setColor (Color.black);

		// 1 - Axis
		g2.draw (new Line2D.Double (0, 0, userWidth, 0));
		g2.draw (new Line2D.Double (0, 0, 0, userHeight));

		// 2 - Dash and Labels
		int yAxisStep = (int) Math.ceil (userHeight / 10);
		// int xAxisStep = (int) Math.ceil (userWidth/7);

		int xDashesNumber = 8; // approximative
		double userWidth_cm = userWidth / 10.0;
		int xAxisStep = 10 * (int) Math.ceil (userWidth_cm / xDashesNumber); // mm

		assert (xAxisStep > 0 && yAxisStep > 0);

		float yDashLength = (float) pixelWidth * 5; // yDashLength is the length
													// of the dash on the y axis
													// (so in the x direction)
		float xDashLength = (float) pixelHeight * 5; // xDashLength is the
														// length of the dash on
														// the x axis (so in the
														// y direction)

		int yDashHeight = 0;
		int xDashWidth = 0;

		// Main Dash
		while (yDashHeight < userHeight) {
			g2.draw (new Line2D.Double (0, yDashHeight, -yDashLength, yDashHeight));
			g2.drawString ("" + yDashHeight, -yDashLength * 4, yDashHeight - xDashLength);
			yDashHeight += yAxisStep;
		}

		while (xDashWidth < userWidth) {
			g2.draw (new Line2D.Double (xDashWidth, 0, xDashWidth, -xDashLength));
			int radius_cm = xDashWidth / 10;
			g2.drawString ("" + radius_cm, xDashWidth - yDashLength, -xDashLength * 4);
			xDashWidth += xAxisStep;
		}

		// Small dash
		yDashHeight = yAxisStep / 2;
		xDashWidth = xAxisStep / 2;
		while (yDashHeight < userHeight) {
			g2.draw (new Line2D.Double (0, yDashHeight, -yDashLength / 2, yDashHeight));
			yDashHeight += yAxisStep;
		}
		while (xDashWidth < userWidth) {
			g2.draw (new Line2D.Double (xDashWidth, 0, xDashWidth, -xDashLength / 2));
			xDashWidth += xAxisStep;
		}

		// units Labels
		g2.drawString (Translator.swap ("PieceViewer2D.height"), 0, (float) userHeight + xDashLength);
		g2
				.drawString (Translator.swap ("PieceViewer2D.radius"), (float) userWidth - yDashLength * 10,
						-xDashLength * 7);

	}

	// Draw a box (or line) of "color" followed by "text" at top left
	// coordinates x,y
	private void draw1Legend (Graphics2D g2, String text, double x, double y, Color color, double lineHeight,
			boolean drawBox) {
		g2.setColor (color);
		double yMiddle = y - lineHeight / 2; // middle line
		double boxWidth = pixelWidth * 10; // box or line length
		if (drawBox) {
			double boxHeight = lineHeight - pixelHeight * 2; // box height
			g2.fill (new Rectangle2D.Double (x, yMiddle - boxHeight / 2, boxWidth, boxHeight));
		} else {
			g2.draw (new Line2D.Double (x, yMiddle, x + boxWidth, yMiddle));
		}

		double xText = x + boxWidth + pixelWidth * 5;
		g2.setColor (Color.black);
		g2.drawString (Translator.swap (text), (float) xText, (float) (y - lineHeight));
	}

	private void drawLegend (Graphics2D g2) {
		// legend

		// Stroke oriStroke = g2.getStroke ();
		// g2.setStroke (new BasicStroke ((float) pixelHeight * 3));

		// Size of a legend line (should depend on font...)
		double lineHeight = pixelHeight * 10;
		double lineWidth = pixelWidth * 100;

		// Coordinates of the top left corner (approximative ?) :
		double xLeft = userWidth + pixelWidth * X_MARGIN_IN_PIXELS;
		double yTop = userHeight + pixelHeight * Y_MARGIN_IN_PIXELS;

		double x = xLeft - lineWidth;
		double y = yTop - lineHeight;
		double dy = lineHeight; // interline

		int nbLines = (hasSapWood ? 1 : 0) + (hasHeartWood ? 1 : 0) + (hasBark ? 1 : 0) + (hasKnottyCore ? 1 : 0)
				+ (hasFirstDeadBranch ? 1 : 0) + (hasJuvenileWood ? 1 : 0);

		// Framing rectangle (semi transparent) :
		{
			double mX = pixelWidth * 5;
			double mY = pixelHeight * 5;
			double h = nbLines * lineHeight;
			Rectangle2D.Double rect = new Rectangle2D.Double (x - mX, y - mY - h, lineWidth + 2 * mX, h + 2 * mY);
			g2.setColor (new Color (1.0f, 1.0f, 1.0f, 0.5f));
			g2.fill (rect);
			// g2.setColor (Color.black);
			// g2.draw (rect);
		}

		if (hasSapWood) {
			String name = hasHeartWood ? "sapwood" : "wood";
			draw1Legend (g2, "PieceViewer2D." + name, x, y, colorSapWood, lineHeight, true);
			y -= dy;
		}

		if (hasHeartWood) {
			draw1Legend (g2, "PieceViewer2D.heartWood", x, y, colorHeartWood, lineHeight, true);
			y -= dy;
		}

		if (hasBark) {
			draw1Legend (g2, "PieceViewer2D.bark", x, y, colorBark, lineHeight, true);
			y -= dy;
		}

		if (hasKnottyCore) {
			draw1Legend (g2, "PieceViewer2D.crownBase", x, y, colorKnottyCore, lineHeight, false);
			y -= dy;
		}

		if (hasFirstDeadBranch) {
			draw1Legend (g2, "PieceViewer2D.firstDeadBranch", x, y, colorFirstDeadBranch, lineHeight, false);
			y -= dy;
		}

		if (hasJuvenileWood) {
			draw1Legend (g2, "PieceViewer2D.juvenileWood", x, y, colorJuvenileWood, lineHeight, false);
			// y -= dy;
		}
	}

	/**
	 * From Drawer interface. We may receive (from Panel2D) a selection
	 * rectangle (in user space i.e. meters) and return a JPanel containing
	 * information about the objects (trees) inside the rectangle. If no objects
	 * are found in the rectangle, return null.
	 */
	public JPanel select (Rectangle.Double r, boolean ctrlIsDown) {
		return null;
	}

	// fc - 12.9.2008 - new OVSelector framework
	public Collection show (Collection candidateSelection) {
		try {
			pieces = extractPieces (candidateSelection);
			realSelection = pieces;
			initialiseScale ();
			calculatePanel2D (pieces);
			resetPanel2D ();
			return realSelection;
		} catch (Exception e) {
			Log.println (Log.ERROR, "PieceViewer2D.show ()",
					"An exception disturbed OVSelector show () method (aborted)", e);
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * User interface definition.
	 */
	private void createUI () {

		this.setLayout (new BorderLayout ());

		// 1. Drawing
		JPanel part1 = new JPanel (new BorderLayout ());
		scroll = new JScrollPane ();

		// Do not set sizes explicitly inside object viewers
		// ~ scroll.setPreferredSize (new Dimension (300, 500)); // fc -
		// 15.2.2008 - no preferred size is better
		part1.add (scroll, BorderLayout.CENTER);

		// 2. Control panel
		JPanel pControl = new JPanel (new FlowLayout (FlowLayout.RIGHT));

		withPieceName = new JCheckBox (Translator.swap ("PieceViewer2D.withPieceName"));
		withPieceName.setSelected (withPieceNameDefault);
		withPieceName.addActionListener (this);
		pControl.add (withPieceName);

		withLegend = new JCheckBox (Translator.swap ("PieceViewer2D.withLegend"));
		withLegend.setSelected (withLegendDefault);
		withLegend.addActionListener (this);
		pControl.add (withLegend);

		help = new JButton (Translator.swap ("Shared.help"));
		help.addActionListener (this);
		pControl.add (help);

		// Set close as default (see AmapDialog)
		// ~ close.setDefaultCapable (true);
		// ~ getRootPane ().setDefaultButton (close);

		// Layout parts
		this.add (part1, BorderLayout.CENTER);
		this.add (pControl, BorderLayout.SOUTH);

	}

}
