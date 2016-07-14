package capsis.lib.fire.fuelitem;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import jeeb.lib.maps.geom.Point2;
import jeeb.lib.maps.geom.Polygon2;
import jeeb.lib.maps.geom.Segment2;
import jeeb.lib.sketch.scene.item.Polygon;
import jeeb.lib.util.Log;
import jeeb.lib.util.StatusDispatcher;
import jeeb.lib.util.Translator;
import jeeb.lib.util.Vertex3d;
import capsis.lib.fire.exporter.PhysExporter;
import capsis.lib.fire.fuelitem.FuelMatrix.FuelMatrixOptions;
import capsis.lib.fire.fuelitem.FuelMatrix.HorizontalDistribution;

/**
 * FiLayerSet is a Collection of layers that generally represent the fuel
 * complex of an understorey the different layers are the different fuel type in
 * the understorey (kermes oak, rosemary, etc. in a french garrigue) This class
 * extends "polygon", that defines the horizontal area of the layerSet Each
 * layer has a Species and a height (can come from the database) Example: a
 * given FiLayerSet can be composed of 2 layers : Quercus suber 5m height 0.25
 * coverfraction and Buxus sempervirens 0.75m height and 0.12 coverFraction. The
 * sum of the coverFraction is not mandatory 1.
 * 
 * @author F. de Coligny, F. Pimont - march 2009
 */

// NOTE: FiLayerSet should not extend Polygon Item directly. Should implement
// some interface in order to be encapsulated in a sketch Polygon (or subclass)
// This will be reviewed - fc - 18.9.2009

public class FiLayerSet extends jeeb.lib.sketch.scene.item.Polygon implements FuelItem, Cloneable {

	protected int id;
	private Collection<FiLayer> layers;
	protected double height; // height of the highest layer (m)
	protected double baseHeight; // min base heights of the layers (m)
	protected double internalCover; // internal cover layer (m), added by FP
	protected double internalLoad; // internal load (kg/m2), added by FP
	protected double internalWaterLoad; // internal water load (kg/m2), added by FP
	protected boolean surfaceFuel = false; // tell if a fuel is a surface fuel
											// or not, when a surfaceFuel, the
											// FuelMatrix has just one cell in
											// height

	protected Color color; // fc+fp-7.11.2014 added a color per layerSet
	protected Boolean computeColor = true; // computed from waterLoad by default if not provided

	/**
	 * Constructor 1. Basic constructor, used in standfire and fireparadox
	 */
	public FiLayerSet(int id) {
		this.id = id;
		layers = new ArrayList<FiLayer>();
		// type = new fireparadox.sketch.FmLayerSetType ();

		// Temporary (FP will change this, will come from the input file)
		color = new Color(0, 0, 0);

	}

	/**
	 * Constructor 2.
	 */
	public FiLayerSet(int id, Collection<FiLayer> layers) {
		this(id);
		this.setType();
		setLayers(layers);
		update();
	}

	/**
	 * method to set the type (not used in standfire, because no sketch)
	 */
	public void setType() {
		type = new fireparadox.sketch.FmLayerSetType();
	}

	public double getFuelMass(PhysExporter exporter) throws Exception {
		return this.computeTotalBiomass(exporter.fmo.particleNames);
	}
	
	/**
	 * Method addFuelTo (implements FuelIem) that adds the FiLayerSet to the
	 * PhysData, based on a grid. This method builds a horizontalDistribution
	 * based on a 2D grid of "resolution",
	 */
	public double addFuelTo(PhysExporter exporter) throws Exception {
		if (this.internalLoad <= 0d)
			return 0d;
		// BUILD THE HORIZONTAL DISTRIBUTION
		boolean verbose = exporter.fmo.verbose;
		double resolution = exporter.fmo.fiLayerSetHorizontalDistributionDx;
		if (exporter.fmo.verbose) {
			this.printSyntheticData(exporter.fmo.particleNames);
		}
		String sourceName = "" + getClass().getSimpleName() + "_" + getId(); // unique
																				// key

		// Computation of horizontal distribution for the whole layerSet
		
		HorizontalDistribution hd = buildHorizontalDistribution(resolution, exporter.stand.getTrees(), exporter.model.rnd, verbose);

		if (verbose) {
			this.printPhysdata(hd, exporter.fmo.particleNames, resolution);
		}

		// If the Polygon is too big according to the resolution requirements,
		// the layerSet is splited in different pieces (slee polygonList2)

		double maxArea = exporter.fmo.horizontalDistribVoxelNumberMaximum * resolution * resolution;
		List<Polygon2> polygonList = dividePolygon(maxArea, verbose);

		FiLayerSet layerSet = copy(); // To avoid modification of original
										// layerSet....
		int partNumber = 0;
		double totalBiomass = 0d;
		double totalCrownVolume = 0d;
		double xmin = Double.MAX_VALUE;
		double xmax = -Double.MAX_VALUE;
		double ymin = Double.MAX_VALUE;
		double ymax = -Double.MAX_VALUE;

		double exportedBiomass = 0d;
		// BUILD AND ADD THE FUELMATRIX
		for (Polygon2 polytemp2 : polygonList) {
			partNumber++;
			// if (polygonList.size () == 1) {
			layerSet.updateToMatch(buildPolygon(polytemp2));
			// }
			if (verbose) {
				StatusDispatcher.print(Translator.swap("FiLayerSet.addFuelTo") + " layerSet: " + sourceName
						+ " triangle " + partNumber + "/" + polygonList.size() + ": area="
						+ layerSet.getPolygon2().getPositiveArea());
			}

			// NB: the horizontalDistribution hd is defined on the initialLayer
			// (not on the copy
			// modified)
			FuelMatrix fm = layerSet.buildFuelMatrix(sourceName, exporter.fmo, hd, exporter.model.rnd);
			if (verbose) {
				fm.printData();
			}

			fm.computeTotalBiomassAndVolume();
			totalBiomass += fm.totalBiomass;
			// System.out.println ("	" + totalBiomass);
			totalCrownVolume += fm.totalCrownVolume;
			xmin = Math.min(fm.x0, xmin);
			xmax = Math.max(fm.x1, xmax);
			ymin = Math.min(fm.y0, ymin);
			ymax = Math.max(fm.y1, ymax);
			exportedBiomass += exporter.physData.addFuelMatrix(fm, exporter.grids, exporter.pdo);
			System.gc();
		}
		if (verbose) {
			System.out.println("Sum of the FuelMatrix for " + sourceName);
			// System.out.println ("	nx=" + nx + ", ny=" + ny + ", nz=" + nz +
			// ",np=" + particles.size
			// ());
			System.out.println("	biomass=" + totalBiomass + " kg; plantVolume(m3)=" + totalCrownVolume);
			System.out.println("	 xmin=" + xmin + ",xmax=" + xmax + ",ymin=" + ymin + ", ymax=" + ymax);
			// System.out.println ("	 zmin=" + getZ (0, 0, 0) + "	 zmax=" + getZ
			// (nz, nx - 1, ny - 1));
		}
		return exportedBiomass;

	}

	/**
	 * Builds a FuelMatrix. Requires an HorizontalDistribution.
	 */
	public FuelMatrix buildFuelMatrix(String sourceName, FuelMatrixOptions fmo, HorizontalDistribution hd, Random rnd)
			throws Exception {
		// System.out.println ("FiLayerSet in buildFuelMatrix, name " + getName
		// ());
		// origin of the horizontal distribution
		double x0hd = hd.x0;
		double y0hd = hd.y0;

		// quantities to build FuelMatrix
		Vertex3d southWest = this.getMin();
		Vertex3d northEast = this.getMax();
		double dx = hd.dx;
		double dy = hd.dy;

		double dz;
		if (surfaceFuel) {
			// set dz = height for surface fuel (2d grid)
			dz = height;
		} else {
			dz = Math.max(fmo.fiLayerSetVerticalDiscretization * (height - baseHeight), fmo.fiLayerSetMinDz);
		}

		// Computation accounting for x0,x1,y0, y1 for the fuelMatrix
		double x0 = x0hd + dx * Math.floor((southWest.x - x0hd) / dx);
		double y0 = y0hd + dy * Math.floor((southWest.y - y0hd) / dy);
		double x1 = northEast.x;
		double y1 = northEast.y;
		double z0 = baseHeight;
		double z1 = height;
		FuelMatrix fm = new FuelMatrix(sourceName, dx, dy, dz, x0, y0, z0, x1, y1, z1, fmo, color,rnd);
		fm.surfaceFuel = surfaceFuel;
		fm.horizontalDistribution = hd;

		for (FiLayer layer : getLayers()) {
			layer.addToFuelMatrix(fm, fmo, this);
		}
		return fm;

	}

	/**
	 * Builds an horizontal distribution for the layerSet, needed for the
	 * FuelMatrix construction. This horizontal distribution contains the
	 * distributions xy of each fuel layer
	 * 
	 * @param verbose
	 */
	protected HorizontalDistribution buildHorizontalDistribution(double polygonResolution, Collection trees, Random rnd,
			boolean verbose) throws Exception {

		Vertex3d southWest = getMin();
		Vertex3d northEast = getMax();

		double dx = polygonResolution;
		double dy = polygonResolution;
		double x0 = southWest.x;
		double y0 = southWest.y;

		double x1 = northEast.x;
		double y1 = northEast.y;

		HorizontalDistribution hd = new HorizontalDistribution(x0, y0, x1, y1, dx, dy,rnd, verbose);

		// tell if a given position is in the polygone
		boolean inPoly[][] = new boolean[hd.nx][hd.ny];

		// computation of tree overstory horizontal distribution biomass

		// the following map contains the spatialGroup and the cover of the
		// group
		Map<Integer, Double> spatialGroupList = new HashMap<Integer, Double>();

		boolean lanlGrassOrLitter = false;

		// update of spatialGroupList : add the cover of the current layer to
		// spatialGroupCover
		for (FiLayer layer : getLayers()) {
			hd.put(layer);
			int spatialGroup = layer.getSpatialGroup();
			double spatialGroupCoverFraction = layer.getCoverFraction();
			if (spatialGroupList.containsKey(spatialGroup)) {
				spatialGroupCoverFraction += spatialGroupList.get(spatialGroup);
			}
			spatialGroupList.put(spatialGroup, spatialGroupCoverFraction);
		}

		for (int i = 0; i < hd.nx; i++) {
			for (int j = 0; j < hd.ny; j++) {
				double cx = x0 + (i + 0.5) * dx;
				double cy = y0 + (j + 0.5) * dy;
				if (contains(cx, cy)) {// within the polygon
					inPoly[i][j] = true;
					hd.cellsInPoly++;

					// initialization of tree biomass in TREES horizontal
					// distribution
					// if (lanlGrassOrLitter) { throw new Exception (
					// "FuelMatrix : impossible to do lanlGrassOrLitter");
					/*
					 * double rhomax = 0d; double avgcanht = 0d; double treeLoad
					 * = 0d;
					 * 
					 * for (Iterator it = trees.iterator(); it.hasNext();) {
					 * FiPlant p = (FiPlant) it.next(); rhomax +=
					 * p.getMeanThinBulkDensity() * 1.5 / trees.size(); avgcanht
					 * += p.getHeight() / trees.size();
					 * 
					 * if (Math.pow(cx - p.getX(), 2d) + Math.pow(cy - p.getY(),
					 * 2d) <= p .getCrownRadius()) { treeLoad +=
					 * p.computeLoad(); } } horizontalDistribution.set(TREES, i,
					 * j, treeLoad / Math.max(rhomax * avgcanht, treeLoad));
					 */
					// }
				} else {
					inPoly[i][j] = false;
				}
			}
		}
		if (hd.cellsInPoly == 0) {
			throw new Exception("FuelMatrix : no cells in the polygon of layerSet " + getId());
		}

		// for each spatialGroup
		for (int spatialGroup : spatialGroupList.keySet()) {
			if (spatialGroupList.get(spatialGroup) > 1d) {
				throw new Exception("FuelMatrix : impossible for layerSet " + getId()
						+ "due to cover fraction higher than 1");
			}
			hd.resetCellOccupation(); // set cellOccupied array to false for the
										// current spatial group
			if (verbose) {
				System.out.println("	SPATIAL GROUP =" + spatialGroup + " emptycellsInPoly=" + hd.emptyCellsInPoly);
			}
			int lnumb = 0;
			double heterogeneousCoverAlreadyAdded = 0d;

			for (FiLayer layer : getLayersWithSpatialGroup(spatialGroup)) {

				lnumb++;
				// if (!(layer.getSpeciesName ().equals (FuelMatrix.LANL_GRASS)
				// ||
				// layer.getSpeciesName ()
				// .equals (FuelMatrix.LANL_LITTER))) {
				// NORMAL SUFF

				if (layer.getCoverFraction() == 1 || layer.getCharacteristicSize() <= 0d) {// homogeneous
					// distribution
					// in
					// the layerSet
					if (verbose) {
						System.out.println("   	horizontalDistribution is homogeneous for " + layer.layerType);
					}
					for (int i = 0; i < hd.nx; i++) {
						for (int j = 0; j < hd.ny; j++) {
							if (inPoly[i][j]) {
								hd.set(layer, i, j, layer.getCoverFraction());
							}
						}
					}
				} else { // complex internal distribution when sum over cover<=
							// 1d-0.001
					if (heterogeneousCoverAlreadyAdded <= 1d - layer.getCoverFraction() - 0.001) {
						hd.addPatches(layer, inPoly);
						if (verbose) {
							System.out.println("		After layer " + layer.layerType + ":" + hd.emptyCellsInPoly
									+ " empty cells over " + hd.cellsInPoly);
						}
					} else { // complete internal distribution when sum over
								// cover > 1d-0.001 to entail the algorithm to
								// end...
						// hd.complete homogeneously with local cover of
						// heterogeneousCoverAlreadyAdded +
						// layer.getCoverFraction ()
						for (int i = 0; i < hd.nx; i++) {
							for (int j = 0; j < hd.ny; j++) {
								if (inPoly[i][j] && !hd.isCellOccupied(i, j)) {
									hd.set(layer, i, j, layer.getCoverFraction()
											/ (1d - heterogeneousCoverAlreadyAdded));
									hd.emptyCellsInPoly -= 1;
								}
							}
						}
						if (verbose) {
							System.out.println("		Layer " + layer.layerType
									+ " complete previous layers for a full occupation (total cover=1)");
							System.out.println("		After layer " + layer.layerType + ":" + hd.emptyCellsInPoly
									+ " empty cells over " + hd.cellsInPoly);
						}
					}

					if (layer.getCharacteristicSize() > 0) {
						heterogeneousCoverAlreadyAdded += layer.getCoverFraction();
					}
				}
				// } else {
				// // LANL GRASS AND LITTER
				// for (int i = 0; i < nx; i++) {
				// for (int j = 0; j < ny; j++) {
				// if (inPoly[i][j] && !cellIsOccupied[i][j]) {
				// double shadeFactor = Math.exp (-5d *
				// horizontalDistribution.get (fm.TREES, i,
				// j));
				//
				// if (layer.getSpeciesName ().equals (FuelMatrix.LANL_GRASS)) {
				//
				// fm.horizontalDistribution.set (layer, i, j, shadeFactor);
				// } else { // LANL_LITTER
				// double coverFactor = 1d - shadeFactor;
				// fm.horizontalDistribution.set (layer, i, j, coverFactor);
				// }
				//
				// }
				//
				// }
				// }
				// }
			}
		}
		// System.out.println ("end building horizontal distribution");
		return hd;

	}

	/**
	 * method to print some synthetic data
	 */
	public void printSyntheticData(Set<String> particleNames) {
		// controls:
		System.out.println("LayerSet synthetic data" + getId() + ":");
		System.out.println("	Total biomass=" + this.internalLoad * this.getPolygonArea() + " kg; Volume(m3)=" + computeTotalVolume());
		System.out.println("	Exported biomass=" + computeTotalBiomass(particleNames) + " kg; Volume(m3)=" + computeTotalVolume());
		System.out.println("	 xmin=" + getPolygon2().getXmin() + ",xmax=" + getPolygon2().getXmax() + ",ymin="
				+ getPolygon2().getYmin() + ",ymax=" + getPolygon2().getYmax());
		System.out.println("	 cbh=" + getBaseHeight() + ",h=" + getHeight() + ",polygonArea="
				+ getPolygon2().getPositiveArea());

	}

	/**
	 * private method to print synthetic data from horizontalDistribution
	 * 
	 * @param hd
	 * @param particles
	 * @param resolution
	 */
	private void printPhysdata(HorizontalDistribution hd, Set<String> particles, double resolution) {
		// IO to check horizontal distribution:
		int lnumb = 0;
		for (FiLayer layer : layers) {
			lnumb++;
			double biomass = 0d;
			for (int i = 0; i < hd.nx; i++) {
				for (int j = 0; j < hd.ny; j++) {
					biomass += hd.get(layer, i, j);
				}
			}
			// System.out.println("	cover="+biomass);
			System.out.println("	sumbd=" + layer.getSumBulkDensity(particles));
			biomass *= layer.getSumBulkDensity(particles) * (layer.getHeight() - layer.getBaseHeight()) * resolution
					* resolution;
			// cover fraction is already in "biomass"
			System.out.println("	layer data after horizontal distribution " + lnumb + ": " + layer.getLayerType()
					+ " h=" + layer.getHeight() + " cover=" + layer.getCoverFraction() + ", size="
					+ layer.getCharacteristicSize() + " biomass=" + biomass + ",biomasstheorique="
					+ layer.getLoad(particles) * this.getPolygonArea());
			// for (String fp:particles) {
			// System.out.println ("	layer moisture " + lnumb + " for particle "
			// + fp + " is "+layer.getMoisture (fp) +
			// " and bd is "+layer.getBulkDensity (particle));
			// }
		}
	}

	/**
	 * divide the polygon associated to the FiLayerSet until the size of each
	 * polygon in the list is smaller than maxArea. Used for the construction of
	 * the FuelMatrix
	 * 
	 * @return list of "small" polygons
	 * @throws Exception
	 */
	private List<Polygon2> dividePolygon(double maxArea, boolean verbose) throws Exception {
		Polygon2 initialPoly = getPolygon2();
		List<Polygon2> polygonList = new ArrayList<Polygon2>();
		double polyMaxArea = (initialPoly.getXmax() - initialPoly.getXmin())
				* (initialPoly.getYmax() - initialPoly.getYmin());
		if (polyMaxArea <= maxArea) { // No triangulation
			polygonList.add(initialPoly); // single polygon in list
		} else { // Triangulation
			if (!initialPoly.isSimple()) {
				throw new Exception("FiLayerSet.dividePolygon: polygon of layer " + this.getId() + " is not simple");
			}
			// triangulate
			boolean counter_clockwise = true;
			polygonList = initialPoly.triangulate(counter_clockwise);// divided
																		// triangle
																		// in 3
																		// when
			// big

			// check if all polygon in polygonList are smaller than area
			// otherwise divide in 2
			ArrayList<Polygon2> newPoly = new ArrayList<Polygon2>();
			boolean sizeNotOK = true;
			while (sizeNotOK) {
				sizeNotOK = false;
				for (Iterator i = polygonList.iterator(); i.hasNext();) {
					Polygon2 polytemp2 = (Polygon2) i.next();
					polyMaxArea = (polytemp2.getXmax() - polytemp2.getXmin())
							* (polytemp2.getYmax() - polytemp2.getYmin());
					if (polyMaxArea > maxArea) {
						i.remove(); // remove current poly
						newPoly.addAll(divideTriangleIn2(polytemp2)); // add the
																		// division
																		// in 2
						// newPoly.addAll (divideTriangleIn3 (polytemp2)); //
						// add the division in 3
						sizeNotOK = true;
					}
				}
				polygonList.addAll(newPoly);
				newPoly.clear();
			}
			double areaSum = 0d;
			for (Polygon2 poly : polygonList) {
				areaSum += poly.getPositiveArea();
			}
			if (verbose) {
				System.out.println("This layer is splitted in " + polygonList.size() + " triangles for memory");
				System.out.println("Initial area was " + initialPoly.getPositiveArea() + ". sum of Small triangle is "
						+ areaSum);
			}
		}
		return polygonList;
	}

	/**
	 * build a Polygon from a Polygon2
	 * 
	 * @param polygon2
	 * @return
	 */
	private Polygon buildPolygon(Polygon2 polygon2) {
		ArrayList<Vertex3d> vertices = new ArrayList<Vertex3d>();
		int n = polygon2.size();
		for (int i = 0; i < n; i++) {
			double x = polygon2.getX(i);
			double y = polygon2.getY(i);
			Vertex3d v = new Vertex3d(x, y, 0d);
			vertices.add(v);
		}
		Polygon polygon = new Polygon(vertices);
		return polygon;
	}

	/**
	 * This method discretises a triangle in tree triangles.
	 */
	private ArrayList<Polygon2> divideTriangleIn3(Polygon2 polygon2) throws Exception {
		int n = polygon2.size();
		if (!(n == 3)) {
			throw new Exception("FiretecFeeder.divideTriangleIn3: triangle has " + n + " sommits");
		}
		ArrayList<Polygon2> result = new ArrayList<Polygon2>();
		// barycenter
		double bx = 0d;
		double by = 0d;
		double bz = 0d;
		for (int i = 0; i < 3; i++) {
			bx += polygon2.getX(i) / 3d;
			by += polygon2.getY(i) / 3d;
			bz += 0d / 3d;
		}
		ArrayList xposT1 = new ArrayList();
		ArrayList yposT1 = new ArrayList();
		xposT1.add(bx);
		xposT1.add(polygon2.getX(0));
		xposT1.add(polygon2.getX(1));
		yposT1.add(by);
		yposT1.add(polygon2.getY(0));
		yposT1.add(polygon2.getY(1));
		Polygon2 t1 = new Polygon2(xposT1, yposT1);
		result.add(t1);
		ArrayList xposT2 = new ArrayList();
		ArrayList yposT2 = new ArrayList();
		xposT2.add(bx);
		xposT2.add(polygon2.getX(1));
		xposT2.add(polygon2.getX(2));
		yposT2.add(by);
		yposT2.add(polygon2.getY(1));
		yposT2.add(polygon2.getY(2));
		Polygon2 t2 = new Polygon2(xposT2, yposT2);
		result.add(t2);
		ArrayList xposT3 = new ArrayList();
		ArrayList yposT3 = new ArrayList();
		xposT3.add(bx);
		xposT3.add(polygon2.getX(2));
		xposT3.add(polygon2.getX(0));
		yposT3.add(by);
		yposT3.add(polygon2.getY(2));
		yposT3.add(polygon2.getY(0));
		Polygon2 t3 = new Polygon2(xposT3, yposT3);
		result.add(t3);

		return result;
	}

	/**
	 * This method discretises a triangle in two triangles.
	 */
	private ArrayList<Polygon2> divideTriangleIn2(Polygon2 poly) throws Exception {
		int n = poly.size();
		if (!(n == 3)) {
			throw new Exception("FiretecFeeder.divideTriangleIn3: triangle has " + n + " sommits");
		}
		ArrayList<Polygon2> result = new ArrayList<Polygon2>();
		// computation of maximal extension
		int ilow = 0;
		int iup = 0;
		int imed = 0;
		// new point
		double newx = 0d;
		double newy = 0d;

		if (poly.getXmax() - poly.getXmin() > poly.getYmax() - poly.getYmin()) {
			// it is a triangle so ilow and iup are different
			for (int i = 0; i < 3; i++) {
				if (poly.getX(i) < poly.getX(ilow)) {
					ilow = i;
				}
				if (poly.getX(i) > poly.getX(iup)) {
					iup = i;
				}
			}
			imed = 3 - ilow - iup;

			if (poly.getX(imed) == poly.getX(iup) || poly.getX(imed) == poly.getX(ilow)) {
				newx = 0.5 * (poly.getX(ilow) + poly.getX(iup));
			} else {
				newx = poly.getX(imed);
			}
			newy = poly.getY(ilow) + (poly.getY(iup) - poly.getY(ilow)) * (newx - poly.getX(ilow))
					/ (poly.getX(iup) - poly.getX(ilow));
		} else {
			// it is a triangle so ilow and iup are different
			for (int i = 0; i < 3; i++) {
				if (poly.getY(i) < poly.getY(ilow)) {
					ilow = i;
				}
				if (poly.getY(i) > poly.getY(iup)) {
					iup = i;
				}
			}
			imed = 3 - ilow - iup;

			if (poly.getY(imed) == poly.getY(iup) || poly.getY(imed) == poly.getY(ilow)) {
				newy = 0.5 * (poly.getY(ilow) + poly.getY(iup));
			} else {
				newy = poly.getY(imed);
			}
			newx = poly.getX(ilow) + (poly.getX(iup) - poly.getX(ilow)) * (newy - poly.getY(ilow))
					/ (poly.getY(iup) - poly.getY(ilow));
		}

		ArrayList xposT1 = new ArrayList();
		ArrayList yposT1 = new ArrayList();
		xposT1.add(newx);
		xposT1.add(poly.getX(ilow));
		xposT1.add(poly.getX(imed));
		yposT1.add(newy);
		yposT1.add(poly.getY(ilow));
		yposT1.add(poly.getY(imed));
		Polygon2 t1 = new Polygon2(xposT1, yposT1);
		result.add(t1);
		ArrayList xposT2 = new ArrayList();
		ArrayList yposT2 = new ArrayList();
		xposT2.add(newx);
		xposT2.add(poly.getX(iup));
		xposT2.add(poly.getX(imed));
		yposT2.add(newy);
		yposT2.add(poly.getY(iup));
		yposT2.add(poly.getY(imed));
		Polygon2 t2 = new Polygon2(xposT2, yposT2);
		result.add(t2);
		return result;
	}

	/**
	 * Clone method, does not clone the layers. Can be used for layerSet
	 * evolution.
	 */
	public FiLayerSet clone() {
		try {
			FiLayerSet c = (FiLayerSet) super.clone();
			c.layers = new ArrayList<FiLayer>();
			return c;

		} catch (Exception e) {
			Log.println(Log.ERROR, "FiLayerSet.clone ()", "Exception", e);
			return null;
		}
	}

	/**
	 * Copy of FiLayerSet
	 * 
	 * @throws Exception
	 */
	public FiLayerSet copy() throws Exception {
		// Clone does not clone the layers
		FiLayerSet copy = clone();
		// Copy the layers
		for (FiLayer layer : layers) {
			copy.addLayer(layer.copy());
		}
		return copy;
	}

	/**
	 * Adds a layer in this layer set.
	 */
	public void addLayer(FiLayer layer) {
		layers.add(layer);
		// this.layerFromDB;
		// fromDBAlreadySet = false;
		update();
	}

	/**
	 * Replace all the layers.
	 */
	public void setLayers(Collection<? extends FiLayer> layers) {
		this.layers = new ArrayList<FiLayer>();
		this.layers.addAll(layers);
		// fromDBAlreadySet = false;
		update();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Return the layers.
	 */
	public Collection<FiLayer> getLayers() {
		return layers;
	}

	/**
	 * Return the layers with a given spatialGroup
	 */
	public Collection<FiLayer> getLayersWithSpatialGroup(int spatialGroup) {
		Collection<FiLayer> res = new ArrayList<FiLayer>();
		for (FiLayer l : layers) {
			if (l.getSpatialGroup() == spatialGroup) {
				res.add(l);
			}
		}
		return res;
	}

	public boolean isEmpty() {
		return layers == null || layers.isEmpty();
	}

	/**
	 * Get the layers maximum height.
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * Get the lines minimum height.
	 */
	public double getBaseHeight() {
		return baseHeight;
	}

	/**
	 * Get the internal cover of all lines.
	 */
	public double getInternalCover() {
		return internalCover;
	}

	public double getInternalLoad() {
		return internalLoad;
	}

	public double getInternalWaterLoad() {
		return internalWaterLoad;
	}

	
	/**
	 * Get the internal cover of all line : total, below threshold, above
	 * threshold.
	 */
	public double[] getInternalCoverThreshold(double threshold) {
		double shrubCover = 0d;
		double treeCover = 0d;
		Map<Integer, Double> shrubCovPerSpatialGroup = new HashMap<Integer, Double>();
		Map<Integer, Double> treeCovPerSpatialGroup = new HashMap<Integer, Double>();
		for (FiLayer layer : layers) {
			if (layer.getBaseHeight() <= threshold) {
				if (shrubCovPerSpatialGroup.containsKey(layer.getSpatialGroup())) {
					shrubCovPerSpatialGroup.put(layer.getSpatialGroup(),
							shrubCovPerSpatialGroup.get(layer.getSpatialGroup()) + layer.getCoverFraction());
				} else {
					shrubCovPerSpatialGroup.put(layer.getSpatialGroup(), layer.getCoverFraction());
				}
			}
			if (layer.getHeight() > threshold) {
				if (treeCovPerSpatialGroup.containsKey(layer.getSpatialGroup())) {
					treeCovPerSpatialGroup.put(layer.getSpatialGroup(),
							treeCovPerSpatialGroup.get(layer.getSpatialGroup()) + layer.getCoverFraction());
				} else {
					treeCovPerSpatialGroup.put(layer.getSpatialGroup(), layer.getCoverFraction());
				}
			}
		}
		double emptiness = 1d;
		for (int i : shrubCovPerSpatialGroup.keySet()) {
			emptiness *= 1d - shrubCovPerSpatialGroup.get(i);
		}
		shrubCover = (1d - emptiness);
		emptiness = 1d;
		for (int i : treeCovPerSpatialGroup.keySet()) {
			emptiness *= 1d - treeCovPerSpatialGroup.get(i);
		}
		treeCover = (1d - emptiness);

		// System.out.println("SHRUBCOVER=" + shrubCover);
		// System.out.println("TREECOVER=" + treeCover);
		return new double[] { internalCover, shrubCover, treeCover };
	}

	/**
	 * Get the internal load of all line : total, blow threshold, above
	 * threshold.
	 */
	public double[] getInternalLoadThreshold(double threshold, Set<String> particles) {
		double shrubLoad = 0;
		double treeLoad = 0;
		Collection _layers = this.getLayers();
		for (Iterator j = _layers.iterator(); j.hasNext();) {
			FiLayer l = (FiLayer) j.next();
			shrubLoad += l.getLoadLoadBelow(threshold, particles);
			treeLoad += l.getLoad(particles) - l.getLoadLoadBelow(threshold, particles);
		}
		return new double[] { shrubLoad + treeLoad, shrubLoad, treeLoad };
	}

	public double getPolygonArea() {
		return this.getPolygon2().getPositiveArea();
	}

	public double computeTotalBiomass(Set<String> particleNames) {
		double[] inLoad = getInternalLoadThreshold(2d, particleNames);
		return inLoad[0] * this.getPolygonArea();
	}

	public double computeTotalVolume() {
		return (height - baseHeight) * this.getPolygonArea();
	}

	/**
	 * Updates height, baseHeight, internal cover and load.
	 */
	public void update() {
		height = -Double.MAX_VALUE;
		baseHeight = Double.MAX_VALUE;
		internalLoad = 0.0;
		internalWaterLoad = 0.0;
		// computation of cover fraction group after group
		Map<Integer, Double> coverMap = new HashMap<Integer, Double>();

		for (FiLayer layer : layers) {
			System.out.println("layer " + layer.layerType + " of height=" + layer.getHeight() + ", intload="
					+ layer.getLoad(FiParticle.ALL) + " icov=" + layer.getCoverFraction());
			height = Math.max(height, layer.getHeight());
			baseHeight = Math.min(baseHeight, layer.getBaseHeight());
			internalLoad += layer.getLoad(FiParticle.ALL);
			internalWaterLoad += layer.getWaterLoad(FiParticle.ALL);
			int group = layer.getSpatialGroup();
			if (coverMap.containsKey(group)) {
				coverMap.put(group, coverMap.get(group) + layer.getCoverFraction());
			} else {
				coverMap.put(group, layer.getCoverFraction());
			}
		}

		double emptiness = 1d;
		for (int group : coverMap.keySet()) {
			emptiness *= (1d - coverMap.get(group));
		}
		internalCover = 1d - emptiness;
		System.out.println("layerSet cover " + internalCover);
		if (computeColor) this.computeColor();
	}
	
	/**
	 * compute a new rgb between yellow and green proportionally to internalWaterLoad (increases with biomass and MC)
	 */
	public void computeColor() {
		float f = Math.min(1f,(float) internalWaterLoad/1f);
		//(G,Y)
		float r = Math.min(Math.max((0 * f + 255 *(1-f))/255f,0f),1f); 
		float g = Math.min(Math.max((255 * f + 255 *(1-f))/255f,0f),1f);
		float b = 0f; //(0 * f + 0 *(1-f))/255f;
		//System.out.println("color="+r+","+g+","+b);
		color = new Color(r,g,b);
		
	}

	@Override
	public String toString() {
		return "FiLayerSet_with_" + layers.size() + "lines_maxHeight=" + height;
	}

	public String getName() {
		return name = "hmax=" + this.getHeight() + "m; xmin,ymin=" + this.getMin().x + "," + this.getMin().y;
	}

	/**
	 * implements FuelItem
	 */
	public boolean isInRectangle(double xMin, double xMax, double yMin, double yMax) {
		if (getMax().x < xMin)
			return false;
		if (getMin().x > xMax)
			return false;
		if (getMax().y < yMin)
			return false;
		if (getMin().y > yMax)
			return false;
		return true;
	}

	// TODO FP : check isPointOnEdge
	public boolean isPointOnEdge(double cx, double cy) {
		if (!isClosed()) {
			return false;
		} // TO BE IMPLEMENTED for polylines
		Point2 _p = new Point2(cx, cy);
		// Rely on Polygon2
		List<Point2> ps = new ArrayList<Point2>();
		for (Vertex3d v : getVertices()) {
			ps.add(new Point2(v.x, v.y));
		}
		Polygon2 p = new Polygon2(ps);
		Point2 p0, p1, p2;
		int n, N;

		// the test point _p is a vertex of the polygon
		p0 = p.getPoint(0);
		if (p0.isEqualTo(_p)) {
			return true;
		}

		// angle evaluation
		p2 = p0;
		N = p.size() - 1;
		for (n = 0; n < N; n++) {
			p1 = p2;
			p2 = p.getPoint(n + 1);
			if (p2.isEqualTo(_p)) { // the test point _p is a vertex of the
				// polygon
				return true;
			}
			if (_p.isOn(new Segment2(p1, p2))) { // the test point _p is on an
				// edge of the polygon
				return true;
			}
		}
		return false;
	}

	public void setSurfaceFuel(boolean value) { // throws Exception {
	// int npart=0;
	// for(FiLayer layer : this.layers){
	// npart += layer.getBulkDensityMap ().keySet ().size ();
	// }
	// if (npart > 1) {
	// throw new Exception (
	// "FiLayerSet : FiLayerSet "
	// +this.name+" can not be a surface fuel because has "+npart+" which is greater than 1");
	// }
		this.surfaceFuel = value;
	}

	public Color getColor() { // fc+fp-7.11.2014
		//System.out.println (color);
		return color;
	}
	public void setColor(Color color){
		this.color = color;
		this.computeColor = false;
	}
	

}
