package capsis.lib.fire;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import jeeb.lib.util.Vertex2d;
import capsis.kernel.GScene;
import capsis.kernel.Plot;
import capsis.lib.fire.fuelitem.FiLayerSet;
import capsis.lib.fire.fuelitem.FiPlant;

/**
 * FiDendromStandProperties : static method to compute cover fraction load and phytovolume of tree collections, and layerSet collection
 * These properties can be computed above a polygon or above the all stand
 *
 * @author Ph. Dreyfus - September 2008
 */
public class FiComputeStateProperties {

	/*
calcCanCov
complete
	 */
	/**
	 * FP 4-05-2009: Total, Shrub and Tree cover in a given Gplot, for a tree
	 * collection
	 */
	// --------------------------------------------------------------------------------->
	// Class create by fp for cover computation
	public static class ValuePoint extends Point.Double {

		public double value;

		/**
		 * Constructor 1.
		 */
		public ValuePoint() {
			super();
			this.value = 0.0;
		}
		public ValuePoint(double x, double y, double value) {
			super(x, y);
			this.value = value;
		}
	}

//	/**
//	 * get trees in polygon
//	 * 
//	 * @param trees
//	 * @param poly
//	 * @return
//	 */
//	public static Collection getTreesInPoly(Collection trees, Polygon poly) {
//		if (poly == null) {
//			return trees;
//		} else {
//			Collection trees2 = new ArrayList();
//			for (Iterator i = trees.iterator(); i.hasNext();) {
//				FiPlant t = (FiPlant) i.next();
//				if (poly.contains(t.getX(), t.getY())) {
//					trees2.add(t);
//				}
//			}
//			return trees2;
//		}
//	}

	// update the points and their value for a give Collection of points, depending on the cover the min and max heigth, the meanLoad
	// also return cover, load and phytovolume
	public static double[] updatePoints(FiLayerSet ls, Collection gp, double cover, double hmin,double hmax,double meanLoad,int nbpg){
		double[] result = new double[3];//emptiness,load,phytovolume
		for (Iterator k = gp.iterator(); k.hasNext();) {
			ValuePoint pt = (ValuePoint) k.next();
			if (ls.contains(pt.x, pt.y)) {
				double v=pt.value-cover;
				if (v <= 0) { // completely full
					k.remove();
					result[0] += pt.value;
					result[1]+=meanLoad/nbpg*(pt.value/cover); // ponderation of the load because not all the load can go in this point (already full)
					result[2]+=pt.value*(hmax-hmin)/nbpg*10000;
				} else { // not completely full
					pt.value = v;
					result[0] += cover;
					result[1]+=meanLoad/nbpg;
					result[2]+=cover*(hmax-hmin)/nbpg*10000;
				}
				
			}
		}
		return result;
	}


	/**
	 * Canopy Cover fraction at stand level
	 */
	// --------------------------------------------------------------------------------->
	public static double calcCanCov (GScene stand, Collection trees, double widthOfAnExtensionBuffer) { // stand
																										// is
																										// only
																										// for
																										// the
																										// polygon
																										// (plot)
																										// ->
																										// point
																										// grid
		// trees are for cover estimation : every tree in the stand, or only a group of trees
		// (see FiMethodProvider and DETimeCanopyCover)
		// widthOfAnExtensionBuffer : =0 when computing real cover (for a case where
		// widthOfAnExtensionBuffer!=0 : see FireDVO2Loader)
		if (!stand.hasPlot ()) return -1;
		Plot plot = stand.getPlot ();
		// Computing CANOPY COVER (%) par sondage :
		// grille de points
		int nbpg = 0; // nombre de points de la grille
		Collection pg = new ArrayList (); // Points-Grilles

		double minX = plot.getOrigin ().x;
		double maxX = minX + plot.getXSize ();
		double minY = plot.getOrigin ().y;
		double maxY = minY + plot.getYSize ();
		// Log.println(" minX: "+minX+" maxX: "+maxX+" minY: "+minY+" maxY: "+maxY);
		// double maille = 1.5; // maille de la grille en m
		// Changement pour une alternative consistant à fixer le nombre de
		// points (de sondage du couvert) // PhD 2008-03-18
		// du coup, le temps de calcul n'augmente plus avec la surface du peuplement // PhD
		// 2008-03-18
		double nbpoints = 10000; // arbitraire ! // PhD 2008-03-18
		double maille = Math.sqrt (plot.getArea () / nbpoints); // PhD 2008-03-18
		// System.out.println ( " Maille : "+maille);

		// On compte les points de la grille tout en les mettant dans une collection
		double x = minX + maille * 0.5;
		while (x <= maxX) {
			x += maille;
			double y = minY + maille * 0.5;
			while (y <= maxY) {
				y += maille;
				Point.Double item = new Point.Double (x, y);
				pg.add (item);
				nbpg++;
			}
		}

		// On passe en revue les arbres du pplt
		// à chaque arbre, on supprime de la collection les points couverts par
		// son houppier
		FiPlant tt;
		Point.Double pt;
		for (Iterator i = trees.iterator (); i.hasNext ();) {
			tt = (FiPlant) i.next ();
			if (tt.getDbh () > 0) { // ici, on ne prend pas en compte les arbres < 1,30 m
				// PhD 2009-02-06 : the ExtensionBuffer - NOT its half - is added to the crown
				// radius
				double pseudoCrownDiameter = 2.0 * (tt.getCrownRadius () + widthOfAnExtensionBuffer);

				// Rappel : Ellipse2D.Double (double x, double y, double w, double h)
				// ... avec : w - the width of the rectangle (Bounding box)
				// h - the height of the rectangle
				// x : X coordinate of the upper left corner
				// y : Y coordinate of the upper left corner du rectangle
				Shape sh = new Ellipse2D.Double (tt.getX () - pseudoCrownDiameter * 0.5, tt.getY ()
						- pseudoCrownDiameter * 0.5, pseudoCrownDiameter, pseudoCrownDiameter); // circle

				for (Iterator j = pg.iterator (); j.hasNext ();) {
					pt = (Point.Double) j.next ();
					if (sh.contains (pt)) {
						j.remove (); // point de la grille "couvert" par un
						// houppier => enlevé de la collection => on
						// ne le re-teste pas pour l'arbre suivant
						// => gain de temps
					}
				}
			}
		}

		// Le couvert (en %) est déduit de la proportion de points qui
		// subsistent dans la collection (= points qui ne sont couverts par
		// aucun houppier)
		return 0.1 * (int) (1000.0 * (1d - (double) pg.size () / (double) nbpg));
	}


	// ////////
	/*
	 * public static double calcCanCov(GStand stand, int xydim, double[] xi,
	 * double[] yi, double crownDiameter) { if (!stand.hasPlot ()) return -1;
	 * GPlot plot = (GPlot) stand.getPlot (); // Computing CANOPY COVER (%) : //
	 * grille de points int nbpg = 0; // nombre de points de la grille
	 * Collection pg = new ArrayList(); // Points-Grilles
	 * 
	 * //double minX = 0; // car on génére les arbres sur un carré d'1 ha 100
	 * m x 100m //TBC ? //double maxX = 100; //double minY = 0; //double maxY =
	 * 100; double minX = plot.getOrigin ().x; double maxX = minX +
	 * plot.getWidth (); double minY = plot.getOrigin ().y; double maxY = minY +
	 * plot.getHeight ();
	 * //Log.println(" minX: "+minX+" maxX: "+maxX+" minY: "+minY
	 * +" maxY: "+maxY); //double maille = 1.5; // maille de la grille en m //
	 * Changement pour une alternative consistant à fixer le nombre de points
	 * (de sondage du couvert) // PhD 2008-03-18 // du coup, le temps de calcul
	 * n'augmente plus avec la surface du peuplement // PhD 2008-03-18 double
	 * nbpoints = 10000; // arbitraire ! // PhD 2008-03-18 double maille =
	 * Math.sqrt (plot.getArea() / nbpoints); // PhD 2008-03-18
	 * //System.out.println ( " Maille : "+maille); // On compte les points de
	 * la grille tout en les mettant dans une collection double x = minX +
	 * maille*0.5; while (x <= maxX) { x += maille; double y = minY +
	 * maille*0.5; while (y <= maxY) { y += maille; Point.Double item = new
	 * Point.Double (x, y); pg.add (item); nbpg++; } } // On passe en revue les
	 * arbres de CETTE LISTE (de coordonnées xi, yi) // à chaque arbre, on
	 * supprime de la collection les points couverts par son houppier
	 * Point.Double pt; for (int e = 0; e < xydim; e++) { double cd =
	 * crownDiameter; Shape sh = new Ellipse2D.Double (xi[e] - cd*0.5, yi[e] -
	 * cd*0.5, cd, cd); // circle for (Iterator j = pg.iterator (); j.hasNext
	 * ();) { pt = (Point.Double) j.next (); if (sh.contains (pt)) { j.remove
	 * (); // point de la grille "couvert" par un houppier => enlevé de la
	 * collection => on ne le re-teste pas pour l'arbre suivant => gain de temps
	 * } } } // Le couvert (en %) est déduit de la proportion de points qui
	 * subsistent dans la collection (= points qui ne sont couverts par aucun
	 * houppier) return 0.1 * (int) (1000.0 * (1d - (double) pg.size () /
	 * (double) nbpg)) ; }
	 */


	// ////////
	public static double calcCanCov (GScene stand, int xydim, Collection vxy, double crownDiameter) {
		if (!stand.hasPlot ()) return -1;
		Plot plot = stand.getPlot ();
		// Computing CANOPY COVER (%) par sondage :
		// grille de points
		int nbpg = 0; // nombre de points de la grille
		Collection pg = new ArrayList (); // Points-Grilles

		double minX = plot.getOrigin ().x;
		double maxX = minX + plot.getXSize ();
		double minY = plot.getOrigin ().y;
		double maxY = minY + plot.getYSize ();
		// Log.println(" minX: "+minX+" maxX: "+maxX+" minY: "+minY+" maxY: "+maxY);
		// double maille = 1.5; // maille de la grille en m
		// Changement pour une alternative consistant à fixer le nombre de
		// points (de sondage du couvert) // PhD 2008-03-18
		// du coup, le temps de calcul n'augmente plus avec la surface du peuplement // PhD
		// 2008-03-18
		double nbpoints = 10000; // arbitraire ! // PhD 2008-03-18
		double maille = Math.sqrt (plot.getArea () / nbpoints); // PhD 2008-03-18
		// System.out.println ( " Maille : "+maille);
		// On compte les points de la grille tout en les mettant dans une collection
		double x = minX + maille * 0.5;
		while (x <= maxX) {
			x += maille;
			double y = minY + maille * 0.5;
			while (y <= maxY) {
				y += maille;
				Point.Double item = new Point.Double (x, y);
				pg.add (item);
				nbpg++;
			}
		}
		// On passe en revue les Vertex2d de la liste de positions vxy (x et y)
		// à chaque position, on supprime de la collection les points couverts
		// par le houppier correspondant (diamètre fixe, mais on pourrait avoir
		// une liste de diamètres)
		double xi, yi, cd;
		Point.Double pt;
		for (Iterator i = vxy.iterator (); i.hasNext ();) {
			Vertex2d v = (Vertex2d) i.next ();
			cd = crownDiameter;
			xi = v.x;
			yi = v.y;
			Shape sh = new Ellipse2D.Double (xi - cd * 0.5, yi - cd * 0.5, cd, cd); // circle
			for (Iterator j = pg.iterator (); j.hasNext ();) {
				pt = (Point.Double) j.next ();
				if (sh.contains (pt)) {
					j.remove (); // point de la grille "couvert" par un houppier
					// => enlevé de la collection => on ne le
					// re-teste pas pour l'arbre suivant => gain de
					// temps
				}
			}
		}
		// Le couvert (en %) est déduit de la proportion de points qui
		// subsistent dans la collection (= points qui ne sont couverts par
		// aucun houppier)
		return 0.1 * (int) (1000.0 * (1d - (double) pg.size () / (double) nbpg));
	}



	// ----------------------------------------------------------------------------------------------------------------------------------->
	// >
	// ----------------------------------------------------------------------------------------------------------------------------------->
	/**
	 * complete crown info
	 */
	// --------------------------------------------------------------------------------->
	/*
	 * public static void complete(double height, double crownBaseHeight, double crownDiameter,
	 * double crownDiameterHeight) {
	 * 
	 * if (crownBaseHeight < 0) crownBaseHeight = 0.67 * height; if (crownDiameter < 0)
	 * crownDiameter = 0.67 * (height - crownBaseHeight); if (crownDiameterHeight < 0)
	 * crownDiameterHeight = 0.5 * (height + crownBaseHeight); return; }
	 */

} // end of FiDendromStandProperties

