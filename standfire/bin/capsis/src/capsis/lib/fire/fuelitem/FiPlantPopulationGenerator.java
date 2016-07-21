package capsis.lib.fire.fuelitem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jeeb.lib.sketch.scene.item.Polygon;
import jeeb.lib.util.Vertex3d;
import capsis.lib.spatial.CrownAvoidancePattern;
import capsis.lib.spatial.GibbsPattern;

/**
 * This class is used to generate a tree distribution - property distribution
 * (height, dbh,...) when empirical laws for generation are known - spatial
 * distribution : position of trees in a given polygon to satisfy some
 * properties of non overlapping, gibbs distributions, etc.
 * 
 * 
 * @author pimont
 * 
 */
public class FiPlantPopulationGenerator {

	protected Polygon polygon; // polygon where the population is
	protected List<FiPlant> plants;
	protected List<Vertex3d> positions; // index is tree number
	protected double altitude = 0d;
	public static String RANDOM = "Random";
	public static String GIBBS = "Gibbs";
	public static String HARDCORE = "HardCore";
    public static Random rnd;
	/**
	 * constructor for sub classes
	 */
	protected FiPlantPopulationGenerator(Polygon polygon, Random rnd) {
		this.plants = new ArrayList<FiPlant>();
		this.polygon = polygon;
		this.positions = new ArrayList<Vertex3d>();
		this.rnd = rnd;
	}

	/**
	 * basic constructor of populationGenerator : assume a List of FiPlant and a
	 * polygon where they should be
	 * 
	 * @param plants
	 * @param polygon
	 */
	public FiPlantPopulationGenerator(List<FiPlant> plants, Polygon polygon, Random rnd) {
		this.plants = plants;
		this.polygon = polygon;
		this.positions = new ArrayList<Vertex3d>();
		this.rnd = rnd;
	}

	/**
	 * method to generate the distribution of stems based on 3 methods (random,
	 * gibbs or hardcore)
	 * 
	 * @param stemNumber
	 * @param method
	 * @param gibbsValue
	 * @throws Exception
	 */
	public void setSpatialDistribution(String method, double gibbsValue) throws Exception {
		int stemNumber = plants.size();
		if (method.equals(RANDOM)) {
			this.generateRandomPositions(stemNumber);
		} else if (method.equals(GIBBS)) {
			this.generateGibbsPositions(stemNumber, gibbsValue);
		} else if (method.equals(HARDCORE)) {
			this.generateHardCorePositions(stemNumber);
		} else {
			throw new Exception("FiPlantPopulationGenerator : spatial method unknown in setPositionDistribution");
		}
		if (stemNumber != positions.size()) {
			throw new Exception("FiPlantPopulationGenerator : wrong number of position:" + positions.size()
					+ "; FiPlant number is +" + stemNumber);
		}
		for (int ipt = 0; ipt < stemNumber; ipt++) {
			Vertex3d v = positions.get(ipt);
			plants.get(ipt).setXYZ(v.x, v.y, v.z);
		}
	}

	/**
	 * add exactly stemNumber position in the vertex3d List returned, based on
	 * gibbs
	 * 
	 * @param stemNumber
	 * @param gibbs
	 *            : gibbs parameter (used between r= 0 and r=intervalRadius[1],
	 *            see below)
	 */
	private void generateGibbsPositions(int stemNumber, double gibbs) {
		double minX = polygon.getMin().x;
		double minY = polygon.getMin().y;
		double maxX = polygon.getMax().x;
		double maxY = polygon.getMax().y;
		double polygonEnvelopArea = (maxX - minX) * (maxY - minY);
		double polygonArea = polygon.getPolygon2().getPositiveArea();
		// theoretical tree number in the envelop of the polygon
		int nStemGibbs = (int) Math.floor((polygonEnvelopArea / (polygonArea + Double.MIN_VALUE) * stemNumber));
		// definition of Gibbs params
		double[] xi = new double[nStemGibbs + 1];
		double[] yi = new double[nStemGibbs + 1];
		double[] intervalRadius = new double[2]; // reject around each tree
		// 2, because we use the 1 but not the 0
		double[] intervalCost = new double[2];

		intervalRadius[1] = Math.sqrt((maxX - minX) * (maxY - minY) / (nStemGibbs * Math.PI)) * 1.3;
		// 1.3 factor :empirical after a few tests
		int intervalNumber = 1;
		intervalCost[1] = gibbs;
		int iterationNumber = (int) Math.min(10000, stemNumber);
		if (gibbs == 0d) {
			iterationNumber = 1;
		}
		GibbsPattern.simulateXY(nStemGibbs, xi, yi, minX + 0.05, maxX - 0.05, minY + 0.05, maxY - 0.05, 0.01,
				intervalNumber, intervalRadius, intervalCost, iterationNumber);

		for (int nt = 0; nt < nStemGibbs; nt++) {
			Vertex3d v = new Vertex3d(xi[nt + 1], yi[nt + 1], this.altitude);
			if (polygon.contains(v) && (this.positions.size() < stemNumber)) {
				this.positions.add(v);
			}
		}
		if (this.positions.size() < stemNumber) {
			this.generateRandomPositions(stemNumber - positions.size());
		}

	}

	/**
	 * add exactly stemNumber position in the vertex3dCollection returned
	 * 
	 * @param stemNumber
	 * @throws Exception
	 */
	private void generateHardCorePositions(int stemNumber) throws Exception {
		// generation of the spatial pattern in the polygon envelop
		double minX = polygon.getMin().x;
		double minY = polygon.getMin().y;
		double maxX = polygon.getMax().x;
		double maxY = polygon.getMax().y;
		double polygonEnvelopArea = (maxX - minX) * (maxY - minY);
		double polygonArea = polygon.getPolygon2().getPositiveArea();
		// theoretical tree number in the envelop of the polygon
		int nStemHardCore = stemNumber; // Math.max ((int) Math.floor
										// ((polygonEnvelopArea / (polygonArea +
										// Double.MIN_VALUE) * stemNumber)),
										// stemNumber);

		List<double[][]> treeRadius = new ArrayList<double[][]>(stemNumber);
		for (int it = 0; it < stemNumber; it++) {
			FiPlant pt = plants.get(it);
			double[][] crownProfile = pt.getCrownGeometry();
			double maxRadius = pt.getCrownRadius();
			double height = pt.getHeight();
			double cbh = pt.getCrownBaseHeight();
			double[][] radius = new double[crownProfile.length][2];
			for (int i = 0; i < crownProfile.length; i++) {
				radius[i][0] = crownProfile[i][1] * maxRadius * 0.01;
				radius[i][1] = crownProfile[i][0] * (height - cbh) * 0.01 + cbh;
			}
			treeRadius.add(it, radius);
		}
		// System.out.println("	Hardcore:"+stemNumber+";"+nStemHardCore+";"+treeRadius.size());
		double[] xi = new double[nStemHardCore];
		double[] yi = new double[nStemHardCore];
		// TODO FP: crownavoidancepattern should work directly on the polygon...
		CrownAvoidancePattern.simulateXY(nStemHardCore, xi, yi, this.polygon, 1d, treeRadius);

		for (int nt = 0; nt < nStemHardCore; nt++) {
			Vertex3d v = new Vertex3d(xi[nt], yi[nt], this.altitude);
			if (polygon.contains(v) && (this.positions.size() < stemNumber)) {
				this.positions.add(v);
			}
		}
		if (this.positions.size() < stemNumber) {
			System.out.println("Afer CrownAvoidancePattern: positions.size=" + positions.size() + ",stemNumber:"
					+ stemNumber);
			this.generateRandomPositions(stemNumber - positions.size());
		}
	}

	/**
	 * add a certain number of random position in the Polygon
	 * 
	 * @param stemNumber
	 */
	private void generateRandomPositions(int stemNumber) {
		for (int nt = 0; nt < stemNumber; nt++) {
			boolean inPoly = false;
			while (!inPoly) {
				double xPos = polygon.getMin().x + rnd.nextDouble() * (polygon.getMax().x - polygon.getMin().x);
				double yPos = polygon.getMin().y + rnd.nextDouble() * (polygon.getMax().y - polygon.getMin().y);
				//double xPos = polygon.getMin().x + Math.random() * (polygon.getMax().x - polygon.getMin().x);
				//double yPos = polygon.getMin().y + Math.random() * (polygon.getMax().y - polygon.getMin().y);
				Vertex3d current = new Vertex3d(xPos, yPos, this.altitude);
				if (polygon.contains(current)) {
					positions.add(current);
					inPoly = true;
				}
			}
		}
	}

	public List<FiPlant> getPlants() {
		return plants;
	}

	/**
	 * permute tree positions in order to limit competition between trees to
	 * have a more realistic stand when the number of available positions is
	 * higher than the treeSet size
	 */
	// private List<Integer> selectPositions (double[][] treeSet, List<Vertex3d>
	// treePositions,
	// String speciesName,
	// boolean random) throws Exception {
	// private List<Integer> permutePositions (boolean random) throws Exception
	// {
	//
	// List<Vertex3d> newPositions = new ArrayList<Vertex3d> ();
	//
	// int stemNumber = plants.size ();
	// if (stemNumber > positions.size ()) { throw new Exception (
	// "Not enought positions available in treePositionSet (" + positions.size
	// ()
	// + ") to allocate positions for trees in treeSet (" + stemNumber + ")"); }
	//
	// // creation of a temporary map of available position for trees
	// Map<Integer,Vertex3d> treePositionMap = new HashMap<Integer,Vertex3d> ();
	// for (int i = 0; i < positions.size (); i++) {
	// treePositionMap.put (i, positions.get (i));
	// }
	// List<Double> keptTreeRadius = new ArrayList<Double> ();
	// // System.out.println("stemNumber1=" + stemNumber);
	// // System.out.println("treeposition=" + treePositions.size());
	// for (int i = 0; i < stemNumber; i++) {
	// double crownRadius = plants.get (i).getCrownRadius ();
	// keptTreeRadius.add (crownRadius);
	//
	// // We now look for an acceptable position for tree i
	// // (given the j(<i) that already have their position
	// // keptTree.get(j) in treePositions
	// int bestIMap = 0;
	// double bestIMapCriteria = Double.MAX_VALUE;
	// for (int iMap : treePositionMap.keySet ()) {
	// Vertex3d tPos = positions.get (iMap);
	// double criteria = 0d;
	// // check that it is ok with previous trees:
	// for (int j = 0; j < i; j++) {
	// Vertex3d tPos2 = treePositions.get (keptTree.get (j));
	// double crownRadius2 = keptTreeRadius.get (j);
	// // the criterion for treePosition acceptation is:
	// // radius+radius2-dist<=(radius+radius2)/3
	// double d = Math.sqrt (Math.pow (tPos.x - tPos2.x, 2d) + Math.pow (tPos.y
	// - tPos2.y, 2d)
	// + Math.pow (tPos.z - tPos2.z, 2d));
	// // if ((crownRadius+crownRadius2)>d) {
	// // double tempCriteria =
	// // (1d-1d/3d)*(crownRadius+crownRadius2) - d;
	// double tempCriteria = crownRadius + crownRadius2 - d;
	// criteria = Math.max (tempCriteria, criteria);
	// //
	// System.out.println("	t"+i+":criteria="+criteria+";cr1"+crownRadius+"; cr2 "+crownRadius2+
	// // "; d="+d);
	// }
	// if (criteria == 0d) {
	// // A correct tree position was found:
	// bestIMapCriteria = criteria;
	// bestIMap = iMap;
	// break;
	// } else {
	// if (criteria < bestIMapCriteria) {
	// // no correct position was found
	// // the best of them is
	// bestIMap = iMap;
	// bestIMapCriteria = criteria;
	// }
	// }
	// }
	// System.out.println ("tree " + i + ": position is " + bestIMap +
	// "criterion : " +
	// bestIMapCriteria);
	// keptTree.add (bestIMap);
	// treePositionMap.remove (bestIMap);
	// }
	// }
	// return keptTree;
	// }

}
