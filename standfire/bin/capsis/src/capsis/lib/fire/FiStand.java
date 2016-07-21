package capsis.lib.fire;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import jeeb.lib.maps.geom.Point2;
import jeeb.lib.maps.geom.Polygon2;
import jeeb.lib.util.Log;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeList;
import capsis.kernel.Plot;
import capsis.lib.fire.fuelitem.FiLayerSet;
import capsis.lib.fire.fuelitem.FiPlant;
import capsis.lib.fire.fuelitem.FuelItem;

/**
 * A FiStand contains FiPlants and FiLayerSets.
 * 
 * @author F. Pimont, F. de Coligny - September 2013
 */
public class FiStand extends TreeList {
	public int maxId = 0;
	// protected static Set <String> particleNames; // list of particles
	// available in a given Stand
	protected Map<Integer, FiLayerSet> layerSets; // key = layerSet id
	public Random rnd;

	public Collection<FuelItem> getFuelItems() {
		Collection<FuelItem> fi = new ArrayList<FuelItem>();
		fi.addAll(getLayerSets());
		for (Tree t : getTrees()) {
			fi.add((FuelItem) t);
		}
		return fi;

	}

	/**
	 * Add a layerSet.
	 */
	public void addLayerSet(FiLayerSet layerSet) {
		if (layerSet == null) {
			return;
		}
		if (layerSet.getId() <= 0) {
			Log.println(Log.ERROR, "FiStand.addLayerSet ()", "Can not add a LayerSet without id, passed");
			return;
		}
		if (layerSets == null) {
			layerSets = new HashMap<Integer, FiLayerSet>();
		}
		layerSets.put(layerSet.getId(), layerSet);
	}

	/**
	 * Remove a layerSet.
	 */
	public void removeLayer(FiLayerSet layerSet) {
		if (layerSet == null) {
			return;
		}
		if (layerSets == null || layerSets.isEmpty()) {
			return;
		}
		if (layerSet.getId() <= 0) {
			Log.println(Log.ERROR, "FiStand.removeLayer ()", "Can not remove a LayerSet without id, passed");
			return;
		}
		layerSets.remove(layerSet.getId());
	}

	/**
	 * Get the layerSet with the given id.
	 */
	public FiLayerSet getLayerSet(int id) {
		return layerSets.get(id);
	}

	/**
	 * Get the layerSet collection.
	 */
	public Collection<FiLayerSet> getLayerSets() {
		// public Collection<FmLayerSet> getLayerSets () { // bug? fc-7.11.2014
		// changed 'FmLayerSet' to 'FiLayerSet'
		return layerSets != null ? layerSets.values() : Collections.EMPTY_LIST;
	}

	/**
	 * This method compute the Total, Shrub and Tree stand is only for the
	 * polygon (plot) -> point grid adapted by FP from PhD calcCanCov result[0]
	 * total cover result[1] shrub cover result[2] tree cover result[3]
	 * shrubLoad (kg/m2) result[4] =treeLoad; result[5] = shrubPhyto
	 * (m3/ha);result[6]= tree LAI
	 * @throws Exception 
	 */
	public static double[] calcMultiCov(Collection trees, Collection layerSets, Plot plot, double heightThreshold,
			Polygon2 poly, Set<String> particleNames) throws Exception { // fc-2.2.2015 added particleNames
		// fc-19.1.2015 MOVED from FmStand (moving state panel to
		// capsis.lib.fire)

		// System.out.println("heightThreshold"+ heightThreshold);
		double[] result = new double[7]; // { -1, -1, -1 };
		if (trees == null & layerSets == null) {
			result[0] = 0;
			result[1] = 0;
			result[2] = 0;
			result[3] = 0;
			result[4] = 0;
			result[5] = 0;
			result[6] = 0;
		} else {
			// Computing COVER (%) par sondage :
			// grille de points
			int nbpg = 0; // nombre de points de la grille
			Collection totalGridPoints = new ArrayList(); // grid points (all)
			Collection treeGridPoints = new ArrayList(); // grid points (below
															// heightThreshold)
			Collection shrubGridPoints = new ArrayList(); // grid points (above
															// heightThreshold)

//			Collection treeGridPoints2 = new ArrayList(); // grid points (below
			// heightThreshold)
//			Collection shrubGridPoints2 = new ArrayList(); // grid points (above
			// heightThreshold)
			Collection<Point2> gridPoints = new ArrayList<Point2>(); // used for layerSet/polygon intersection
			double totalEmptiness = 0.; // sum of value in a given pg (replace
			// pg.size())
			double shrubEmptiness = 0.; // sum of value in a given pg (replace
			// pg.size())
			double treeEmptiness = 0.; // sum of value in a given pg (replace
			// pg.size())
			double shrubLoad = 0.; // mean load under heightThreshold
			double shrubPhyto = 0.; // mean phyto under heightThreshold
			double treeLoad = 0.; // mean load above heightThreshold
			double treeLAI = 0.; // mean LAI above heightThreshold

//			double shrubLoad2 = 0.; // mean load below heightThreshold
//			double treeLoad2 = 0.; // mean load above heightThreshold
//			double treeLAI2 = 0.; // mean LAI above heightThreshold

			// coodinates of the zone for sampling
			double minX = plot.getOrigin().x;
			double maxX = minX + plot.getXSize();
			double minY = plot.getOrigin().y;
			double maxY = minY + plot.getYSize();

			if (poly != null) {
				minX = Math.max(poly.getXmin(), minX); // poly.getMin().x;
				maxX = Math.min(poly.getXmax(), maxX); // .x;;
				minY = Math.max(poly.getYmin(), minY); // .y;
				maxY = Math.min(poly.getYmax(), maxY); // .y;
			}
			double plotSurface = (maxX - minX) * (maxY - minY);
			double nbpoints = 500; // low for tests;
			// System.out.println("x1=" + minX + " x2=" + maxX + " Y1=" + minY
			// + " Y2=" + maxY);
			if (poly != null) {
				nbpoints = 500;
			}
			// it should be 40000; // FP 2009-05-04
			// La loi normale a un intervalle de confiance à 99 % de
			// 2.32*sqrt(p*(1-p)/nbpoints) donc si on veut les pourcentages à 1
			// %
			// pres
			// il faut nbpoints>(2.32/0.01)**2 soit environ 40000 points.
			double maille = Math.sqrt(plotSurface / nbpoints); // PhD 2008-03-18

			// We count the sum of "value" in putting them in 3
			// collections : pgtot, pgup (plus haut que heightThreshold) et
			// pglow (plus
			// bas)
			// nbpoints = 0;
			double x = minX + maille * 0.5;
			while (x <= maxX) {

				double y = minY + maille * 0.5;
				while (y <= maxY) {

					FiComputeStateProperties.ValuePoint totalItem = new FiComputeStateProperties.ValuePoint(x, y, 1.0); // initialisation
					FiComputeStateProperties.ValuePoint shrubItem = new FiComputeStateProperties.ValuePoint(x, y, 1.0); // initialisation
					FiComputeStateProperties.ValuePoint treeItem = new FiComputeStateProperties.ValuePoint(x, y, 1.0); // initialisation
					// to 1. cell is empty
					if ((poly == null) || (!(poly == null) && poly.contains(new Point2(x, y)))) { // contains(x,
																									// y)))
																									// {
						totalGridPoints.add(totalItem);
						shrubGridPoints.add(shrubItem);
						treeGridPoints.add(treeItem);
						gridPoints.add(new Point2(x, y));
						nbpg++;
					}
					y += maille;
				}
				x += maille;
			}
			//System.out.println("nbpg=" + nbpg);
			totalEmptiness = nbpg;// completement vide=pgtot.size()
			shrubEmptiness = nbpg;// completement vide=pglow.size()
			treeEmptiness = nbpg;
			// On passe en revue les arbres du pplt
			// à chaque arbre, on supprime de la collection les points couverts
			// par
			// son houppier
			FiPlant tt;
			FiComputeStateProperties.ValuePoint pt;
			for (Iterator i = trees.iterator(); i.hasNext();) {
				tt = (FiPlant) i.next();
				// if (tt.getDbh() > 0) {
				double pseudoCrownDiameter = 2.0 * tt.getCrownRadius();
				// Rappel : Ellipse2D.Double (double x, double y, double w,
				// double h)
				// ... avec : w - the width of the rectangle (Bounding box)
				// h - the height of the rectangle
				// x : X coordinate of the upper left corner
				// y : Y coordinate of the upper left corner du rectangle
				Shape sh = new Ellipse2D.Double(tt.getX() - pseudoCrownDiameter * 0.5, tt.getY() - pseudoCrownDiameter
						* 0.5, pseudoCrownDiameter, pseudoCrownDiameter); // circle
				for (Iterator j = totalGridPoints.iterator(); j.hasNext();) {
					pt = (FiComputeStateProperties.ValuePoint) j.next();
					if (sh.contains(pt)) {
						j.remove(); // point de la grille "couvert" par un
						// houppier => enlevé de la collection => on
						// ne le re-teste pas pour l'arbre suivant
						// => gain de temps
						totalEmptiness += -1.0;
					}
				}
				// shrubs (below heightThreshold)
				if (tt.getCrownBaseHeight() <= heightThreshold) {
//					for (Iterator j = shrubGridPoints2.iterator(); j.hasNext();) {
//						pt = (FiComputeStateProperties.ValuePoint) j.next();
//						if (sh.contains(pt)) {
//							// here we consider vertical distribution of fuel to
//							// uniform...
//							shrubLoad2 += tt.computeLoad()
//									* (Math.min(heightThreshold, tt.getHeight()) - tt.getCrownBaseHeight())
//									/ (tt.getHeight() - tt.getCrownBaseHeight()) / nbpg;
//						}
//					}
					shrubLoad += tt.computeThinMass(particleNames)
							* (Math.min(heightThreshold, tt.getHeight()) - tt.getCrownBaseHeight())
							/ (tt.getHeight() - tt.getCrownBaseHeight()) /plotSurface;
					for (Iterator j = shrubGridPoints.iterator(); j.hasNext();) {
						pt = (FiComputeStateProperties.ValuePoint) j.next();
						if (sh.contains(pt)) {
//							shrubGridPoints2.add(pt);
							j.remove(); // point de la grille "couvert" par un
							// houppier => enlevé de la collection
							// => on ne le re-teste pas pour l'arbre
							// suivant => gain de temps
							shrubEmptiness += -1.0;
//							shrubLoad += tt.computeLoad()
//									* (Math.min(heightThreshold, tt.getHeight()) - tt.getCrownBaseHeight())
//									/ (tt.getHeight() - tt.getCrownBaseHeight()) / nbpg;
//
							shrubPhyto += (-tt.getCrownBaseHeight() + Math.min(heightThreshold, tt.getHeight())) / nbpg
									* 10000;
						}
					}
				}

				// trees (above heightThreshold)
				if (tt.getHeight() >= heightThreshold) {
//					for (Iterator j = treeGridPoints2.iterator(); j.hasNext();) {
//						pt = (FiComputeStateProperties.ValuePoint) j.next();
//						if (sh.contains(pt)) {
//							treeLoad2 += tt.computeLoad()
//									* (tt.getHeight() - Math.max(heightThreshold, tt.getCrownBaseHeight()))
//									/ (tt.getHeight() - tt.getCrownBaseHeight()) / nbpg;
//
//							treeLAI2 += tt.getLai() / nbpg;
//						}
//					}
					treeLoad += tt.computeThinMass(particleNames)
							* (tt.getHeight() - Math.max(heightThreshold, tt.getCrownBaseHeight()))
							/ (tt.getHeight() - tt.getCrownBaseHeight())/plotSurface;

					treeLAI += tt.getLai() * Math.PI * tt.getCrownRadius() *tt.getCrownRadius()/plotSurface;
					for (Iterator j = treeGridPoints.iterator(); j.hasNext();) {
						pt = (FiComputeStateProperties.ValuePoint) j.next();
						if (sh.contains(pt)) {
							//treeGridPoints2.add(pt);
							j.remove(); // point de la grille "couvert" par un
							// houppier => enlevé de la collection
							// => on ne le re-teste pas pour l'arbre
							// suivant => gain de temps
							treeEmptiness += -1.0;
						}
//							treeLoad += tt.computeLoad()
//									* (tt.getHeight() - Math.max(heightThreshold, tt.getCrownBaseHeight()))
//									/ (tt.getHeight() - tt.getCrownBaseHeight()) / nbpg;
//							treeLAI += tt.getLai() / nbpg;
						
					}

				}

				// } // teste Dbh
			} // fin iterator arbre
				// Layerset
			if (layerSets != null) {
				
				FiLayerSet ls;
				for (Iterator i = layerSets.iterator(); i.hasNext();) {
					ls = (FiLayerSet) i.next();
					double intersection = 0d; // to compute loads in polygon
					for (Point2 point: gridPoints) {
						if (ls.contains(point.getX(), point.getY())) {
							intersection += 1d/nbpg;
						}
					}
					
					// heightThreshold=2;// for test
					double[] cover = ls.getInternalCoverThreshold(heightThreshold);
					double fiTotalCover = cover[0]; // of ls
					double fiShrubCover = cover[1];// of ls
					// System.out.println("TOTALCOVER2=" + fiTotalCover);
					// System.out.println("SHRUBCOVER2=" + fiShrubCover);
					double fiTreeCover = cover[2];// of ls
					
					double[] load = ls.getInternalLoadThreshold(heightThreshold, particleNames); // fc-2.2.2015
//					double[] load = ls.getInternalLoadThreshold(heightThreshold, FiModel.particleNames); // fc-29.1.2015
					
					// double[] load =
					// ls.getInternalLoadThreshold(heightThreshold,
					// FmModel.particleNames); // fc-29.1.2015
					double fiTotalLoad = load[0]; // of ls
					double fiShrubLoad = load[1];// of ls
					double fiTreeLoad = load[2];// of ls
					//System.out.println("fiTotalLoad=" + load[0]+",fiShrubLoad=" + load[1]+",fiTreeLoad=" + load[2]);
					double[] temp;
					if (fiTotalCover > 0.) {
						temp = FiComputeStateProperties.updatePoints(ls, totalGridPoints, fiTotalCover, 0,
								ls.getHeight(), fiTotalLoad, nbpg);
						totalEmptiness -= temp[0];
					}
					if (fiShrubCover > 0.) {
						temp = FiComputeStateProperties.updatePoints(ls, shrubGridPoints, fiShrubCover, 0,
								Math.min(heightThreshold, ls.getHeight()), fiShrubLoad, nbpg);
						shrubEmptiness -= temp[0];
						//shrubLoad += temp[1];
						shrubLoad += fiShrubLoad * intersection ;
						shrubPhyto += temp[2];
						// System.out.println("SHRUBCOVEREMPTINESS="
						// + shrubEmptiness);
					}
					if (fiTreeCover > 0.) {// above
						temp = FiComputeStateProperties.updatePoints(ls, treeGridPoints, fiTreeCover,
								Math.max(heightThreshold, ls.getBaseHeight()), ls.getHeight(), fiTreeLoad, nbpg);
						treeEmptiness -= temp[0];
						//treeLoad += temp[1];
						treeLoad += fiTreeLoad * intersection ;
					}
					//System.out.println("shrubLoad=" + treeLoad+",treeLoad=" + treeLoad);
					
					
					
					
				} // end iterator i layersets
			} // end teste i layersets
			result[0] = (int) (100.0 * (1d - totalEmptiness / nbpg));
			// shrub cover
			result[1] = (int) (100.0 * (1d - shrubEmptiness / nbpg));
			// System.out.println("SHRUBCOVER=" + result[1]);
			// tree cover
			result[2] = (int) (100.0 * (1d - treeEmptiness / nbpg));
			result[3] = shrubLoad;// + shrubLoad2;
			result[4] = treeLoad;// + treeLoad2;
			result[5] = (int) shrubPhyto;
			result[6] = treeLAI;// + treeLAI2;

		} // fin teste pas d'arbre et pas de layer
		return result;
	}

}
