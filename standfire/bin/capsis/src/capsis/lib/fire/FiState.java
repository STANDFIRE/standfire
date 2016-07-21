package capsis.lib.fire;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import jeeb.lib.maps.geom.Polygon2;
import jeeb.lib.sketch.scene.item.Polygon;
import capsis.kernel.Plot;
import capsis.lib.fire.fuelitem.FiLayerSet;
import capsis.lib.fire.fuelitem.FiPlant;

/**
 * FiState summarizes the fuel properties of a vegetal scene.
 * 
 * @author O. Vigy, E. Rigaud - November 2006, F. de Coligny - January 2015
 */
public class FiState implements Serializable {

	private double heightThreshold; // FP 4-05-2009 separation tree / shrub

	private int totalNumber;
	private double totalCover; // %
	private double maxHeight;

	private int treeNumberAboveThreshold;
	private double treeCover; // %
	private double treeLoad; // %
	private double treeLAI; // %

	private double shrubCover; // %
	private double shrubLoad;
	private double shrubPhytovolume;

	private String domTreeSpecies;
	private String domShrubSpecies;
	private double herbsCover; // %

	private FiMethodProvider mp;

	/**
	 * Constructor.
	 */
	public FiState(FiMethodProvider mp) {
		super();
		this.mp = mp;
	}

	/**
	 * Updates the state on the given FiStand / polygon. the polygon may be
	 * null.
	 */
	public void update(FiStand fiStand, FiModel model, Polygon selectedPolygon) {
		try {
		if (fiStand == null)
			return;

		Collection trees = fiStand.getTrees();
		Collection layerSets = fiStand.getLayerSets();
		Plot currentplot = fiStand.getPlot();

		Polygon2 polygon = null;
		if (selectedPolygon != null) {
			if (selectedPolygon.isClosed()) {
				polygon = selectedPolygon.getPolygon2();
			}
		}

		double[] properties;
		
			properties = FiStand.calcMultiCov(trees, layerSets, currentplot, heightThreshold, polygon, 
					model.particleNames);
		
		totalCover = properties[0];
		shrubCover = properties[1];
		treeCover = properties[2];
		shrubLoad = properties[3];
		treeLoad = properties[4];
		shrubPhytovolume = properties[5];
		treeLAI = properties[6];

		Collection treesInPoly = getTreesInPoly(trees, selectedPolygon);
		if (treesInPoly == null)
			return;

		double layerSetHmax = 0d;
		for (Object ls : layerSets) {
			layerSetHmax = Math.max(layerSetHmax, ((FiLayerSet) ls).getHeight());
		}

		maxHeight = Math.max(mp.getHmax(treesInPoly), layerSetHmax);
		totalNumber = (int) mp.getN(null, treesInPoly);
		treeNumberAboveThreshold = (int) mp.getNAboveThreshold(null, treesInPoly, heightThreshold);
		domTreeSpecies = "";
		domShrubSpecies = "";
		} catch (Exception e) {
			// TODO FP Auto-generated catch block
			e.printStackTrace();
		} // fc-2.2.2015 particleNames

	}

	/**
	 * Returns the list of trees in the given polygon.
	 */
	public Collection getTreesInPoly(Collection trees, Polygon polygon) {
		if (polygon == null) {
			return trees;
		} else {
			Collection trees2 = new ArrayList();
			for (Iterator i = trees.iterator(); i.hasNext();) {
				FiPlant t = (FiPlant) i.next();
				if (polygon.contains(t.getX(), t.getY())) {
					trees2.add(t);
				}
			}
			return trees2;
		}
	}

	public double getTotalCover() {
		return totalCover;
	}

	public double getTotalLoad() {
		return treeLoad + shrubLoad;
	}

	public double getMaxHeight() {
		return maxHeight;
	}

	public double getHeightThreshold() {
		return heightThreshold;
	}

	public double getTreeCover() {
		return treeCover;
	}

	public double getTreeLAI() {
		return treeLAI;
	}

	public double getTreeLoad() {
		return treeLoad;
	}

	public int getTotalNumber() {
		return totalNumber;
	}

	public int getTreeNumberAboveThreshold(double threshold) {
		return treeNumberAboveThreshold;
	}

	public String getDomTreeSpecies() {
		return domTreeSpecies;
	}

	public double getShrubCover() {
		return shrubCover;
	};

	public double getShrubLoad() {
		return shrubLoad;
	};

	public double getShrubPhytovolume() {
		return shrubPhytovolume;
	}

	public String getDomShrubSpecies() {
		return domShrubSpecies;
	}

	public double getHerbsCover() {
		return herbsCover;
	}

	public void setHeightThreshold(double var) {
		heightThreshold = var;
	}

}
